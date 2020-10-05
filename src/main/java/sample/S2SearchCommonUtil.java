package sample;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ca.uhn.fhir.util.BundleUtil;
import ca.uhn.fhir.validation.FhirValidator;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.*;

import java.util.*;

public class S2SearchCommonUtil {
    private IGenericClient client = null;

    public S2SearchCommonUtil(IGenericClient client) {
        this.client = client;
    }

    public Patient findPatientByNameDOB( String givenName, String familyName, DateType dob, String mrn) {
        Patient patientFound = null;
        List<Patient> listPatients = new ArrayList<>();
        Bundle response = client.search()
                .forResource(Patient.class)
                //.where(Patient.NAME.matches().value(name))
                .where(Patient.FAMILY.matchesExactly().value(familyName))
                .and(Patient.GIVEN.matchesExactly().value(givenName))
                .and(Patient.BIRTHDATE.exactly().day(dob.getValueAsString()))
                .and(Patient.IDENTIFIER.exactly().systemAndIdentifier(ConstantsClz.IDENTTIFIER_SYSTEM_MRN, mrn))
                .returnBundle(Bundle.class)
                .execute();
        // Bundle return with pagination; So get all pages
        listPatients.addAll(BundleUtil.toListOfResourcesOfType(client.getFhirContext(),response,Patient.class));
        while(response.getLink(IBaseBundle.LINK_NEXT) != null){
            response = client.loadPage().next(response).execute();
            listPatients.addAll(BundleUtil.toListOfResourcesOfType(client.getFhirContext(),response,Patient.class)) ;
        }
        if (listPatients.size() == 1) {
            patientFound = listPatients.get(0);
        } else if (listPatients.size() == 0) {
            System.out.printf("Could not find any patient with familyName:%s; givenName:%s; DOB:%s ", familyName, givenName, dob.getValueAsString());
        } else {
            patientFound = listPatients.get(0);
            System.out.printf("Found multiple patients with familyName:%s; givenName:%s; DOB:%s ", familyName, givenName, dob.getValueAsString());
        }
        return  patientFound;
    }

    public Observation readObservationById(String id) {
        Observation observation = client.read()
                .resource(Observation.class)
                .withId(id)
                .execute();
        if (observation==null) {
            System.out.printf("Could not find the observation with id: %s \n", id);
        } else {
            System.out.printf("Found observation with id: %s \n", id);
        }
        return observation;
    }

    public Patient readPatientById(String id) {
        Patient patient = client.read()
                .resource(Patient.class)
                .withId(id)
                .execute();
        if (patient==null) {
            System.out.printf("Could not find the patient with id: %s \n", id);
        } else {
            System.out.printf("Found patient with id: %s \n", id);
        }
        return patient;

    }

    /*
    Search patient by the given primary identifier system (such as MRN)
     */
    public Patient searchPatientWithIdentifier(Patient patient,String patientPrimaryIdentifierSystem) {
        Identifier patientPrimaryIdentifier = null;
        for(Identifier identifier: patient.getIdentifier()) {
            if (identifier.getSystem().equals(patientPrimaryIdentifierSystem)) {
                patientPrimaryIdentifier = identifier;
                break;
            }
        }
        if (patientPrimaryIdentifier==null) {
            throw new IllegalArgumentException("Did not provide an existing primary Identifier system for the patient");
        }
        return this.searchPatientWithIdentifier(patientPrimaryIdentifier);
    }

    public Patient searchPatientWithIdentifier(Identifier patientPrimaryIdentifier) {
        Patient patientFound = null;
        Bundle bundle = client
                .search()
                .forResource(Patient.class)
                .where(new TokenClientParam("identifier").exactly().systemAndCode(patientPrimaryIdentifier.getSystem(), patientPrimaryIdentifier.getValue()))
                .returnBundle(Bundle.class)
                .execute();
        List<Patient> patients = new ArrayList<Patient>();
        patients.addAll(BundleUtil.toListOfResourcesOfType(client.getFhirContext(), bundle, Patient.class));
        if ((patients != null) && (patients.size() > 0)) {
            patientFound = patients.get(0);
            System.out.println("Patient already existing with Id:" + patients.get(0).getIdElement().getIdPart());
        }
        return patientFound;
    }


    /*
       Search observation for the given patient with given LOINC code and effective date
        */
    public Observation searchObservationForPatientByLOINCnEffectiveDate(String patientId, String loincCode, DateTimeType when) {
        Observation observationFound = null;
        Set<String> uniquePatientSet = new LinkedHashSet<>();
        Bundle response = client.search()
                .forResource(Observation.class)
                .where(Observation.CODE.exactly().code(loincCode))
                .and(Observation.SUBJECT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        List<Observation> listObeservations = new ArrayList<>();

        // Search results are paginated; so page thru all
        listObeservations.addAll(BundleUtil.toListOfResourcesOfType(client.getFhirContext(), response, Observation.class));
        while (response.getLink(IBaseBundle.LINK_NEXT) != null) {
            response = client.loadPage().next(response).execute();
            listObeservations.addAll(BundleUtil.toListOfResourcesOfType(client.getFhirContext(), response, Observation.class));
        }
        for(Observation observation: listObeservations) {
            DateTimeType datetime= observation.getEffectiveDateTimeType();
            if (!datetime.isEmpty()) {
                Date date = datetime.getValue();
                if (date.compareTo(when.getValue()) == 0) {
                    observationFound = observation;
                    break;
                }
            }
        }
        return observationFound;
    }
    public Observation.ObservationComponentComponent createObservationComponent(String loincCode, String loincDisplayName,
                                                                                double value, String valueUnit, String valueUOMCode) {
        Observation.ObservationComponentComponent obsCompComponent = new Observation.ObservationComponentComponent();
        obsCompComponent
                .getCode()
                .addCoding()
                .setSystem(ConstantsClz.SYSTEM_LOINC)
                .setCode(loincCode)
                .setDisplay(loincDisplayName);
        obsCompComponent.setValue(
                new Quantity()
                        .setValue(value)
                        .setUnit(valueUnit)
                        .setSystem(ConstantsClz.SYSTEM_UOM)
                        .setCode(valueUOMCode));
        return  obsCompComponent;
    }



    public List<Observation.ObservationComponentComponent> createObservationComponentsBP(double sys, double dia) {
        List<Observation.ObservationComponentComponent> list = new ArrayList<Observation.ObservationComponentComponent>();
        list.add(createObservationComponent(ConstantsClz.LOIC_CODE_BLOOD_PRESSURE_PANEL_SYS,"", 106, ConstantsClz.LOIC_UOM_UNIT_BLOOD_PRESSURE_PANEL, ConstantsClz.LOIC_UOM_CODE_BLOOD_PRESSURE_PANEL));
        list.add(createObservationComponent(ConstantsClz.LOIC_CODE_BLOOD_PRESSURE_PANEL_DIA,"", 76, ConstantsClz.LOIC_UOM_UNIT_BLOOD_PRESSURE_PANEL, ConstantsClz.LOIC_UOM_CODE_BLOOD_PRESSURE_PANEL));
        return  list;
    }

}
