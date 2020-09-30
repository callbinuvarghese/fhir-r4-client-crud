package sample;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.*;

public class S8AddBundle {

    public static final String PATIENT1_IDENTIFIER_VALUE = "20074001";
    public static final Enumerations.AdministrativeGender MALE = Enumerations.AdministrativeGender.MALE;
    public static final Enumerations.AdministrativeGender FEMALE = Enumerations.AdministrativeGender.FEMALE;

    private IGenericClient client = null;

    public S8AddBundle(String baseUrl) {
        FhirContext ctx = FhirContext.forR4();
        client = ctx.newRestfulGenericClient(baseUrl);
    }

    public boolean addBundle() {
        Patient patient1 = newPatient(PATIENT1_IDENTIFIER_VALUE, "Philip", "", "Winnie", MALE, "1975-03-25");
        Observation observation1 = newObservation(patient1, ConstantsClz.LOIC_CODE_BLOOD_REDCELLS, ConstantsClz.LOIC_DESC_BLOOD_REDCELLS, new DateTimeType("2007-06-22"), newQuantityBCRed(4.11));
        Observation observation2 = newObservation(patient1, ConstantsClz.LOIC_CODE_BLOOD_REDCELLS, ConstantsClz.LOIC_DESC_BLOOD_REDCELLS, new DateTimeType("2010-08-16"), newQuantityBCRed(4.12));
        Observation observation3 = newObservation(patient1, ConstantsClz.LOIC_CODE_BLOOD_REDCELLS, ConstantsClz.LOIC_DESC_BLOOD_REDCELLS, DateTimeType.today(), newQuantityBCRed(4.15));
        // Create a bundle that will be used as a transaction
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);
        bundle.addEntry()
                .setFullUrl(patient1.getIdElement().getValue())
                .setResource(patient1)
                .getRequest()
                .setUrl("Patient")
                .setIfNoneExist("identifier="+ ConstantsClz.IDENTTIFIER_SYSTEM_MRN+"|"+PATIENT1_IDENTIFIER_VALUE)
                .setMethod(Bundle.HTTPVerb.POST);
        // Add the observation. This entry is a POST with no header
        // (normal create) meaning that it will be created even if
    // a similar resource already exists.
        String observationId = getObservationId(observation1.getCode().getCodingFirstRep().getCode(), observation1.getEffectiveDateTimeType());
        bundle.addEntry()
                .setResource(observation1)
                .getRequest()
                .setUrl("Observation")
                .setIfNoneExist("identifier="+ ConstantsClz.IDENTTIFIER_SYSTEM_OBS+"|"+observationId)
                .setMethod(Bundle.HTTPVerb.POST);
        return true;

    }

    private Quantity newQuantityBCRed(double value) {
        return new Quantity().setValue(value).setUnit("10 trillion/L").setSystem(ConstantsClz.SYSTEM_UOM).setCode("10*12/L");
    }

    private String getObservationId(String loicCode, DateTimeType when) {
        return "obs-"+loicCode+"-"+when.getValueAsString();
    }
    private Observation newObservation(Patient patient, String loicCode, String codeDesc, DateTimeType when, Quantity quantity) {
        // Create an observation object
        Observation observation = new Observation();
        observation.setId(getObservationId(loicCode,when));
        Identifier identifier = observation.addIdentifier();
        identifier.setSystem(ConstantsClz.IDENTTIFIER_SYSTEM_OBS).setValue(getObservationId(loicCode,when));
        observation.setStatus(Observation.ObservationStatus.FINAL);
        observation
                .getCode()
                .addCoding()
                .setSystem(ConstantsClz.SYSTEM_LOINC)
                .setCode(loicCode)
                .setDisplay(codeDesc);
        observation.setEffective(when);
        observation.setValue(quantity);
        // The observation refers to the patient using the ID, which is already
// set to a temporary UUID
        observation.setSubject(new Reference(patient.getIdElement().getValue()));
        observation.setStatus(Observation.ObservationStatus.FINAL);
        return  observation;
    }
    private Patient newPatient(String identifierValue,String familyName, String midInitial, String firstName, Enumerations.AdministrativeGender gender, String dobString ) {
        Patient patient = new Patient();
        patient.addIdentifier()
                .setSystem(ConstantsClz.IDENTTIFIER_SYSTEM_MRN)
                .setValue(identifierValue);
        patient.addName()
                .setFamily(familyName)
                .addGiven(midInitial)
                .addGiven(firstName);
        patient.setGender(gender);
        DateType dateTimeType = new DateType("2015-05-22");
        patient.setBirthDate(dateTimeType.getValue());

        // Give the patient a temporary UUID so that other resources in
        // the transaction can refer to it
        patient.setId(IdType.newRandomUuid());
        return patient;
    }
    public static void main(String[] args) {
        S8AddBundle s6AddBundle = new S8AddBundle(ConstantsClz.FHIR_BASE_URL);
        s6AddBundle.addBundle();
    }

}
