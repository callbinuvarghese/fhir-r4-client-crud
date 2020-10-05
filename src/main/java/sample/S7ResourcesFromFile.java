package sample;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class S7ResourcesFromFile {
    private IGenericClient client = null;

    public S7ResourcesFromFile(String baseUrl) {
        FhirContext ctx = FhirContext.forR4();
        client = ctx.newRestfulGenericClient(baseUrl);
    }

    //"Your String with {{ BP_DIA_VALUE  }}";
    public static String replaceTokenWithValue(String stringInput, String token, String value) {
        String tokenWithPrefixNSuffix = "\\{\\{ " + token + " \\}\\}";
        return stringInput.replaceAll(tokenWithPrefixNSuffix, value);
    }

    public static String getResourceFileAsString(String filePath) throws IOException {
        /*ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }*/
        URL resource = S7ResourcesFromFile.class.getClassLoader().getResource(filePath);
        if (resource==null) {
            throw new IllegalArgumentException("Could not find in classpath:" + filePath);
        } else {
            System.out.printf("Reading resource file from: %s \n", resource.getPath());
            String string = IOUtils.toString(S7ResourcesFromFile.class.getClassLoader().getResourceAsStream(filePath), StandardCharsets.UTF_8);
            return string;
        }
    }
    public static String readResourceFileObservationBP(String patientId, DateTimeType when, double sys_bp, double dia_bp ) {
        try {
            String jsonString = S7ResourcesFromFile.getResourceFileAsString("fhir/Observation-bp.json");
            return jsonString;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Patient findPatientByNameDOB(String givenName, String familyName, DateType dob, String mrn) {
        return new S2SearchCommonUtil(this.client).findPatientByNameDOB(givenName, familyName,  dob, mrn);
    }
    public Observation findObservationByPatientLOINCOnEffectiveDt(Patient patient, DateTimeType when, String loicCode) {
        return new S2SearchCommonUtil(this.client).searchObservationForPatientByLOINCnEffectiveDate(patient.getId(), loicCode, when);
    }
    public Observation parseObservation(String observationJsonString) {
        Observation observation = this.client.getFhirContext().newJsonParser().parseResource(Observation.class, observationJsonString);
        /*FhirValidator validator = this.client.getFhirContext().newValidator();
        validator.setValidateAgainstStandardSchema(true);
        validator.setValidateAgainstStandardSchematron(true);
        ValidationResult result = validator.validateWithResult(observation);
        if (result.isSuccessful() == false) {
            System.out.println("Observation Resource is not valid");
        }*/
        return observation;
    }
    private void addObservationComponentInterpretation(Observation.ObservationComponentComponent obsComp, String code, String disp) {
        Coding coding = new Coding()
                .setSystem(ConstantsClz.TERM_OBSERVATION_INTERPRETATION_SYSTEM)
                .setCode(code)
                .setDisplay(disp);
        CodeableConcept t = new CodeableConcept().addCoding(coding);
        obsComp.addInterpretation(t);
    }
    private Observation.ObservationComponentComponent getObservationComponent(Observation observation, String loincCode) {
        Observation.ObservationComponentComponent compFound = null;
        for ( Observation.ObservationComponentComponent comp : observation.getComponent() ) {
            for (Coding coding :comp.getCode().getCoding()) {
                if (coding.getCode().equals(loincCode)) {
                    compFound = comp;
                    break;
                }
            }
            if (compFound!=null) break;;
        }
        return compFound;
    }
    public  Observation addFillInValues(Observation observation, Patient patient, DateTimeType when, double sys_bp, String sys_bp_inter_code, String sys_bp_inter_disp, double dia_bp, String dia_bp_inter_code, String  dia_bp_inter_disp) {
            observation.setSubject(new Reference(patient.getIdElement().getValue()));
            observation.setEffective(when);
            Observation.ObservationComponentComponent comp = null;
            comp = this.getObservationComponent(observation, ConstantsClz.LOIC_CODE_BLOOD_PRESSURE_PANEL_SYS);
            if (comp==null) {
                System.out.printf("Could not find the observation component in BP Obeservation for %s ", ConstantsClz.LOIC_CODE_BLOOD_PRESSURE_PANEL_SYS);
            } else {
                comp.getValueQuantity().setValue(sys_bp);
                this.addObservationComponentInterpretation(comp, sys_bp_inter_code, sys_bp_inter_disp);
            }
            comp = this.getObservationComponent(observation, ConstantsClz.LOIC_CODE_BLOOD_PRESSURE_PANEL_DIA);
            if (comp==null) {
                System.out.printf("Could not find the observation component in BP Obeservation for %s ", ConstantsClz.LOIC_CODE_BLOOD_PRESSURE_PANEL_DIA);
            } else {
                comp.getValueQuantity().setValue(dia_bp);
                this.addObservationComponentInterpretation(comp, dia_bp_inter_code, dia_bp_inter_disp);
            }
            return  observation;
    }
    public Observation addObservationBP(Patient patient, DateTimeType when, double sys_bp, double dia_bp ) {
        Observation observation = findObservationByPatientLOINCOnEffectiveDt(patient, when, ConstantsClz.LOIC_CODE_BLOOD_PRESSURE_PANEL );
        if (observation != null) {
            System.out.printf("Observation already existing. %s", observation.getId());
            return observation;
        }
        String observationJsonString = S7ResourcesFromFile.readResourceFileObservationBP(patient.getId(), when, sys_bp, dia_bp);
        observation = this.parseObservation(observationJsonString);
        this.addFillInValues(observation,  patient,  when,  sys_bp, ConstantsClz.TERM_OBSERVATION_INTERPRETATION_NORMAL_CODE, ConstantsClz.TERM_OBSERVATION_INTERPRETATION_NORMAL_DESC, dia_bp, ConstantsClz.TERM_OBSERVATION_INTERPRETATION_LO_CODE, ConstantsClz.TERM_OBSERVATION_INTERPRETATION_LO_DESC);
        S4Add s4Add = new S4Add(this.client.getServerBase());
        String observationId = s4Add.createResource(observation);
        System.out.printf("Created Observation Id:%s\n", observationId);
        return new S2SearchCommonUtil(this.client).readObservationById(observationId);
    }

    public static void main(String[] args) {
        S7ResourcesFromFile s7ResourcesFromFile = new S7ResourcesFromFile(ConstantsClz.FHIR_BASE_URL);
        Patient patient = s7ResourcesFromFile.findPatientByNameDOB("Binu", "Doe", new DateType("1975-12-24"), "125678");
        if (patient !=null) {
            s7ResourcesFromFile.addObservationBP(patient, new DateTimeType("2012-12-19"), 107, 60);
        } else {
            System.out.printf("Patient not found");
        }
    }

}
