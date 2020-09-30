package sample;

import java.util.List;
import java.util.ArrayList;

import ca.uhn.fhir.util.BundleUtil;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseBundle;

//import org.hl7.fhir.dstu3.model.Bundle;
//import org.hl7.fhir.dstu3.model.HumanName;
//import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * example methods for reading  from the FHIR server.
 */
public class S1BasicRead {

    private static final Logger logger = LoggerFactory.getLogger(S1BasicRead.class);
    IGenericClient client = null;

    public S1BasicRead(String baseUrl) {
        FhirContext ctx = FhirContext.forR4();
        client = ctx.newRestfulGenericClient(baseUrl);
    }

    /**
     * Read patient with the given ID and return the entire name.
     */
    public String getPatientNameById(String id) {
        // Hint, there is a method that will return the full name including
        // prefix, first, last, and suffix
        // search for patient 123
        Patient patient = client.read()
                .resource(Patient.class)
                .withId(id)
                .execute();
        HumanName name = patient.getName().get(0);
        return name.getNameAsSingleString();
    }

    /**
     * Search all patients and return the IDs.  The search should include matches
     * where any part of the patient name (family, given, prefix, etc.)
     */
    public List<String> getIDListByPatientName(String name) {
        List<String> listPatientIds = new ArrayList<>();
        List<Patient> listPatients = new ArrayList<>();
        Bundle response = client.search()
                .forResource(Patient.class)
                //.where(Patient.NAME.matches().value(name))
                .where(Patient.NAME.matches().value(name) )
                .returnBundle(Bundle.class)
                .execute();
        // Bundle return with pagination; So get all pages
        listPatients.addAll(BundleUtil.toListOfResourcesOfType(client.getFhirContext(),response,Patient.class));
        while(response.getLink(IBaseBundle.LINK_NEXT) != null){
            response = client.loadPage().next(response).execute();
            listPatients.addAll(BundleUtil.toListOfResourcesOfType(client.getFhirContext(),response,Patient.class)) ;
        }

        for(Patient patient : listPatients){
            listPatientIds.add(patient.getIdElement().getIdPart());
        }

        return listPatientIds;
    }


    public static void main(String[] args) {
        // the test server support DSTU2, STU3 and R4
        logger.debug("Hello from Logback");

        // Getting the base URL from the command line argument.

        /*if (args.length == 0) {
            //throw new IllegalStateException("The base URL for the FHIR server must be specified as an argument. " +
            //        "For example: http://hapi.fhir.org/baseR4");
        } else {
            baseUrl = args[0];
        }*/
        logger.debug("Base URL is {}", ConstantsClz.FHIR_BASE_URL);

        //Patient with Id Read
        S1BasicRead s1BasicRead = new S1BasicRead(ConstantsClz.FHIR_BASE_URL);
        String name = s1BasicRead.getPatientNameById(ConstantsClz.PATIENT_ID);
        System.out.println("Name:" + name);

        //Patient Search  with Name
        /*List<String> idList = s1BasicRead.getIDListByPatientName("John");
        System.out.println("Count of list " + idList.size());
        for ( String id: idList) {
            System.out.println("Id:"+ id);
        }*/

    }

}