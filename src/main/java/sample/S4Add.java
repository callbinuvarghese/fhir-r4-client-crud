package sample;


import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.*;

import java.util.*;

/**
 * Adding resources to the FHIR server.
 */
public class S4Add {

    private IGenericClient client = null;
    public static final String PATIENT_1_MRN = "125678";
    public static final String PATIENT_1_GIVEN = "Binu";
    public static final String PATIENT_1_FAMILY = "Doe";
    public static final String PATIENT_1_DOB = "1975-12-24";
    public static final String PATIENT_1_OBS_2_LOINC_CODE = ConstantsClz.LOIC_CODE_BODY_TEMPARATURE;
    public static final String PATIENT_1_OBS_2_LOINC_WHEN = "2011-09-22";


    public S4Add(String baseUrl) {
        FhirContext ctx = FhirContext.forR4();
        client = ctx.newRestfulGenericClient(baseUrl);
    }

    /**
     * Add a new patient with the given first and last name.
     * Return the FHIR ID of the newly created patient.
     */
    public String addPatient(String mrn, String firstName, String lastName, DateType dob) {
        //Place your code here
        String patientId = null;
        Patient patient = new Patient();
        patient.addIdentifier()
                .setSystem(ConstantsClz.IDENTTIFIER_SYSTEM_MRN)
                .setValue(mrn);
        patient.getManagingOrganization().setReference("http://example.com/base/Organization/FOO");
        patient.addName()
                .setFamily(lastName)
                .addGiven(firstName);
        patient.setBirthDate(dob.getValue());
        //Try an extension
        // Create an extension and add it to the String
        // Add an extension (initially with no contents) to the resource
        Extension parent = new Extension(ConstantsClz.IDENTTIFIER_SYSTEM+"#parent");
        patient.addExtension(parent);

        // Add two extensions as children to the parent extension
        Extension child1 = new Extension(ConstantsClz.IDENTTIFIER_SYSTEM+"#childOne", new StringType("value1"));
        parent.addExtension(child1);

        Extension child2 = new Extension(ConstantsClz.IDENTTIFIER_SYSTEM+"#childTwo", new StringType("value1"));
        parent.addExtension(child2);

        S2SearchCommonUtil s2SearchCommonUtil = new S2SearchCommonUtil(this.client);
        Patient patientExisting = s2SearchCommonUtil.searchPatientWithIdentifier(patient,ConstantsClz.IDENTTIFIER_SYSTEM_MRN);
        if (patientExisting!=null) {
            patientId = patientExisting.getId();
            System.out.println("Patient already existing with Id:" + patientId);
        } else {
            MethodOutcome outcome = client.create().resource(patient).execute();
            if (outcome.getId() != null) {
                System.out.println("Patient created with Id:" + outcome.getId());
                patientId = outcome.getId().getValue();
            } else {
                System.out.println("Adding patient failed to return id");
            }
        }

        return patientId;


    }

    private String getObservationId(String loicCode, DateTimeType when) {
        return "obs-"+loicCode+"-"+when.getValueAsString();
    }
    /**
     * Add an observation to the FHIR server for the specified patient by ID.
     * It is assumed that patient with given ID, already exists in the FHIR server.
     * Return the ID of the newly created observation.
     */
    public String addObservationForPatient(String patientId, DateTimeType when, String loincCode, String loincDisplayName,
                                 double value, String valueUnit, String valueUOMCode) {
        String observationId = null;
        Observation observation = new Observation();
        observation.setStatus(Observation.ObservationStatus.FINAL);
        observation.addIdentifier()
                .setSystem(ConstantsClz.IDENTTIFIER_SYSTEM_OBS)
                .setValue(getObservationId(loincCode, when));
        observation
                .getCode()
                .addCoding()
                .setSystem(ConstantsClz.SYSTEM_LOINC)
                .setCode(loincCode)
                .setDisplay(loincDisplayName);

        observation.setValue(
                new Quantity()
                        .setValue(value)
                        .setUnit(valueUnit)
                        .setSystem(ConstantsClz.SYSTEM_UOM)
                        .setCode(valueUOMCode));
        observation.setEffective(when);
        observation.setNote(this.createAnnotation("2015-05-22T10:15:55","Binu","Annotation Text1"));
        observation.setSubject(new Reference(patientId));

        S2SearchCommonUtil s2SearchCommonUtil = new S2SearchCommonUtil(client);
        Observation observationFound = s2SearchCommonUtil.searchObservationForPatientByLOINCnEffectiveDate(patientId, loincCode, when);
        if (observationFound != null) {
            observationId = observationFound.getId();
            System.out.println("Observation found for the patient; observation:"+ observationFound.getId() + "; PatientId:"+ patientId);
        } else {
            MethodOutcome outcome = client.create().resource(observation).execute();
            if (outcome.getId() != null) {
                System.out.println("Observation created with Id:" + outcome.getId());
                observationId = outcome.getId().getValue();
            } else {
                System.out.println("Adding patient failed to return id");
            }
        }
        return observationId;
    }

    /*
    Create Annotation
     */
    private List<Annotation> createAnnotation(String dateTimeString, String authorString, String noteText) {
        List<Annotation> annotationList = new ArrayList<>();
        DateTimeType dateTimeType = new DateTimeType(dateTimeString);
        Annotation annotation = new Annotation();
        annotation.setTimeElement(dateTimeType);
        StringType author = new StringType(authorString);
        annotation.setAuthor(author);
        annotation.setText(noteText);
        annotationList.add(annotation);
        return annotationList;
    }


    public static void main(String[] args) {
        S4Add s3Add = new S4Add(ConstantsClz.FHIR_BASE_URL);
        String patientId = s3Add.addPatient(PATIENT_1_MRN,PATIENT_1_GIVEN, PATIENT_1_FAMILY, new DateType(PATIENT_1_DOB));
        String obeservationId = s3Add.addObservationForPatient(patientId, new DateTimeType("2011-09-03T11:13:00-04:00"),ConstantsClz.LOIC_CODE_BLOOD_REDCELLS, ConstantsClz.LOIC_DESC_BLOOD_REDCELLS, 4.12, "10 trillion/L", "10*12/L");
        if (obeservationId != null) {
            System.out.println("ObservationId:" + obeservationId);
        } else {
            System.out.println("Observation not created successfully");
        }
        obeservationId = s3Add.addObservationForPatient(patientId, new DateTimeType(PATIENT_1_OBS_2_LOINC_WHEN),ConstantsClz.LOIC_CODE_BODY_TEMPARATURE, ConstantsClz.LOIC_DESC_BODY_TEMPARATURE, 36.5, "C", "Cel");
        if (obeservationId != null) {
            System.out.println("ObservationId Body Temperature:" + obeservationId);
        } else {
            System.out.println("Observation Body Temperature not created successfully");
        }
    }
}