package sample;


import ca.uhn.fhir.rest.api.MethodOutcome;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.*;

/**
 * deleting resoruces from the FHIR server.
 */
public class S6Delete {

    private IGenericClient client = null;

    public S6Delete(String baseUrl) {
        FhirContext ctx = FhirContext.forR4();
        client = ctx.newRestfulGenericClient(baseUrl);
    }

    /**
     * Delete the patient with the given ID from the FHIR server.
     */
    public void deletePatient(String patientId) {
        MethodOutcome response = client
                .delete()
                .resourceById(new IdType("Patient", patientId))
                .execute();
    }

    /**
     * Delete the observation with the given ID
     */
    public void deleteObservation(String observationId) {
        MethodOutcome response = client
                .delete()
                .resourceById(new IdType("Observation", observationId))
                .execute();
        if (response != null) {
            System.out.printf("Deleted Observation id:%s\n", response.getId());
        }
    }
    public Patient findPatientByNameDOB(String givenName, String familyName, DateType dob, String mrn) {
        S2SearchCommonUtil s2SearchCommonUtil = new S2SearchCommonUtil(this.client);
        return s2SearchCommonUtil.findPatientByNameDOB(givenName, familyName,  dob, mrn);
    }

    public Observation findObservationByPatientLOINCOnEffectiveDt(Patient patient,  DateTimeType when, String loicCode) {
        S2SearchCommonUtil s2SearchCommonUtil = new S2SearchCommonUtil(this.client);
        return s2SearchCommonUtil.searchObservationForPatientByLOINCnEffectiveDate(patient.getId(), loicCode, when);
    }
    public Observation readObservationById(String id) {
        S2SearchCommonUtil s2SearchCommonUtil = new S2SearchCommonUtil(this.client);
        return s2SearchCommonUtil.readObservationById(id);
    }

    public static void main(String[] args) {
        S6Delete s5Delete = new S6Delete(ConstantsClz.FHIR_BASE_URL);
        Patient patient = s5Delete.findPatientByNameDOB("Binu", "Doe", new DateType("1975-12-24"), "125678");
        Observation observation = s5Delete.findObservationByPatientLOINCOnEffectiveDt(patient, new DateTimeType("2011-09-22"),ConstantsClz.LOIC_CODE_BODY_TEMPARATURE);
        String observationId = observation.getId();
        observation = s5Delete.readObservationById(observationId);
        if (observation ==null) {
            System.out.printf("Could not find observation with id:%s\n",observationId);
        }
        s5Delete.deleteObservation(observation.getId());
        System.out.println("Reading observation by id:%s after deleted it\n"+ observationId);
        observation = s5Delete.readObservationById(observationId);
        if (observation ==null) {
            System.out.printf("Could not find observation with id:%s\n",observationId);
        }

        //s5Delete.deletePatient(patient.getId);
    }


    }