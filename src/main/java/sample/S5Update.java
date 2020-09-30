package sample;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.r4.model.*;

/**
 * Updating resources in the FHIR server.
 */
public class S5Update {

    private IGenericClient client = null;

    public S5Update(String baseUrl) {
        FhirContext ctx = FhirContext.forR4();
        client = ctx.newRestfulGenericClient(baseUrl);
    }

    /**
     * Read the patient with the given ID and update the home phone number.
     * If the patient does not already have a home phone number, add it.
     * Return the ID of the updated resource.
     */
    public String updatePatientPhoneNumber(String patientId, String homePhoneNumber) {

        Patient patient = client.read()
                .resource(Patient.class)
                .withId(patientId)
                .execute();
        if (patient != null) {
            if (patient.getTelecom() == null) {
                System.out.println("Home phone number is adding..");
                patient.addTelecom().setUse(ContactPoint.ContactPointUse.HOME).setSystem(ContactPoint.ContactPointSystem.PHONE).setValue(homePhoneNumber);
            } else {
                System.out.println("Home phone number is already existing.. Updating..");
                boolean homefound = false;
                for (ContactPoint tele:  patient.getTelecom()) {
                    if (tele.getUse().equals(ContactPoint.ContactPointUse.HOME) && tele.getSystem().equals(ContactPoint.ContactPointSystem.PHONE)) {
                        tele.setValue(homePhoneNumber);
                        homefound=true;
                    }
                }
                if (!homefound) {
                    patient.addTelecom().setUse(ContactPoint.ContactPointUse.HOME).setSystem(ContactPoint.ContactPointSystem.PHONE).setValue(homePhoneNumber);
                }
            }
        }
        MethodOutcome outcome = client.update()
                .resource(patient)
                .execute();
        if (outcome.getId() != null ) {
            System.out.println("Updated Patient with id:"+outcome.getId().getValue());
        }
        return outcome.getId().getValue();
    }

    /**
     * Read the observation with the given ID and update the value.
     * Return the ID of the updated resource.
     */
    public String updateObservationQuantityValue(String observationId, double value) {
        Observation observation = client.read()
                .resource(Observation.class)
                .withId(observationId)
                .execute();
        if (observation != null) {
            Quantity quantity = (Quantity) observation.getValue();
            quantity.setValue(value);
        } else {
            System.out.println("Could not find observation..");
        }
        MethodOutcome outcome = client.update()
                .resource(observation)
                .execute();
        if (outcome.getId() != null ) {
            System.out.println("Updated Observation with id:"+outcome.getId().getValue());
        }
        return outcome.getId().getValue();
    }
    public Patient findPatientByNameDOB( String givenName,String familyName, DateType dob, String mrn) {
        S2SearchCommonUtil s2SearchCommonUtil = new S2SearchCommonUtil(this.client);
        return s2SearchCommonUtil.findPatientByNameDOB(givenName, familyName,  dob, mrn);
    }
    public Observation findObservationByPatientLOINCOnEffectiveDt(Patient patient,  DateTimeType when, String loicCode) {
        S2SearchCommonUtil s2SearchCommonUtil = new S2SearchCommonUtil(this.client);
        return s2SearchCommonUtil.searchObservationForPatientByLOINCnEffectiveDate(patient.getId(), loicCode, when);
    }


    public static void main(String[] args) {
        S5Update s4Update = new S5Update(ConstantsClz.FHIR_BASE_URL);
        Patient patient = s4Update.findPatientByNameDOB(S4Add.PATIENT_1_GIVEN, S4Add.PATIENT_1_FAMILY, new DateType(S4Add.PATIENT_1_DOB), S4Add.PATIENT_1_MRN);
        s4Update.updatePatientPhoneNumber(patient.getId(),"1 (678) 621-6709");
        Observation observation = s4Update.findObservationByPatientLOINCOnEffectiveDt(patient, new DateTimeType("2011-09-03T11:13:00-04:00"),ConstantsClz.LOIC_CODE_BLOOD_REDCELLS);
        s4Update.updateObservationQuantityValue(observation.getId(), 6.12);
    }
}