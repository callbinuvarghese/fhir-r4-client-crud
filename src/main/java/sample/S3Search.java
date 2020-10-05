package sample;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.BundleUtil;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;

/**
 * Bit more complex reads that the Basic Read Class
 */
public class S3Search {

    private IGenericClient client = null;

    public S3Search(String baseUrl) {
        FhirContext ctx = FhirContext.forR4();
        client = ctx.newRestfulGenericClient(baseUrl);
    }

    public Patient readPatientByID(String id) {
        S2SearchCommonUtil s2SearchCommonUtil = new S2SearchCommonUtil(this.client);
        return s2SearchCommonUtil.readPatientById(id);
    }

    public Patient searchPatientBySystemIdentifier(String mrn){
        S2SearchCommonUtil s2SearchCommonUtil = new S2SearchCommonUtil(this.client);
        Identifier mrnIdentifier = new Identifier().setSystem(ConstantsClz.IDENTTIFIER_SYSTEM_MRN).setValue(mrn);
        return s2SearchCommonUtil.searchPatientWithIdentifier(mrnIdentifier);
    }
    /**
     * Search for all observations with a given loinc code and return the total
     * number of patients referenced.
     */
    public int getNumberOfPatientsByObservationForLOINCCOde(String loincCode) {
        int numPatients =0;
        Set<String> uniquePatientSet = new LinkedHashSet<>();
        Bundle response = client.search()
                .forResource(Observation.class)
                .where(Observation.CODE.exactly().code(loincCode))
                .returnBundle(Bundle.class)
                .execute();

        List<Observation> listObeservations =  new ArrayList<>();

        // Search results are paginated; so page thru all
        listObeservations.addAll( BundleUtil.toListOfResourcesOfType(client.getFhirContext(),response,Observation.class));
        while(response.getLink(IBaseBundle.LINK_NEXT) != null){
            response = client.loadPage().next(response).execute();
            listObeservations.addAll(BundleUtil.toListOfResourcesOfType(client.getFhirContext(),response,Observation.class)) ;
        }


        for (Observation observation : listObeservations){
            //Referece is supposed to have patient; but the test server may have bad data
            if ((observation.getSubject().getReference() != null) && (!observation.getSubject().getReference().isEmpty())) {
                uniquePatientSet.add(observation.getSubject().getReference().replaceAll("Patient/", ""));
            }
        }

        numPatients =uniquePatientSet.size();

        return numPatients;
    }


    public static void main(String[] args) {
        S3Search s2Search = new S3Search(ConstantsClz.FHIR_BASE_URL);
        Patient patient = s2Search.searchPatientBySystemIdentifier(S4Add.PATIENT_1_MRN);
        if (patient==null) {
            System.out.printf("Could not find patient with MRN: %s \n", S4Add.PATIENT_1_MRN);
        } else {
            System.out.printf("Found patient with MRN: %s \n", patient.getId());
        }
        System.out.println("Stated searching for Observation.. it may take coupld of mins");
        int patientCount  = s2Search.getNumberOfPatientsByObservationForLOINCCOde(ConstantsClz.LOIC_CODE_BODY_TEMPARATURE);
        System.out.println("PatientCount:" + patientCount);

    }

}