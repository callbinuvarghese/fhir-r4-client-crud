package sample;

public class ConstantsClz {
    //Constants
    public static String FHIR_BASE_URL = "http://hapi.fhir.org/baseR4";
    public static final String SYSTEM_LOINC = "http://loinc.org";
    public static final String SYSTEM_UOM = "http://unitsofmeasure.org";
    public static final String IDENTTIFIER_SYSTEM ="http://binuhospital.org";
    public static final String IDENTTIFIER_SYSTEM_MRN = IDENTTIFIER_SYSTEM + "/mrns";
    public static final String IDENTTIFIER_SYSTEM_OBS = IDENTTIFIER_SYSTEM + "/obs";


    //Test Data related
    public static final String LOIC_CODE_BLOOD_REDCELLS = "789-8";
    public static final String LOIC_DESC_BLOOD_REDCELLS = "Erythrocytes [#/volume] in Blood by Automated count";
    public static final String LOIC_UOM_CODE_BODY_REDCELLS  = "10*12/L";
    public static final String LOIC_UOM_UNIT_BODY_REDCELLS  = "10 trillion/L";

    public static final String LOIC_CODE_BODY_TEMPERATURE = "8310-5";
    public static final String LOIC_DESC_BODY_TEMPERATURE = "Body temperature";
    public static final String LOIC_UOM_CODE_BODY_TEMPERATURE  = "Cel";
    public static final String LOIC_UOM_UNIT_BODY_TEMPERATURE  = "C";

    public static final String LOIC_CODE_BLOOD_PRESSURE_PANEL = "85354-9";
    public static final String LOIC_DESC_BLOOD_PRESSURE_PANEL = "Blood pressure panel";
    public static final String LOIC_UOM_CODE_BLOOD_PRESSURE_PANEL  = "mm[Hg]";
    public static final String LOIC_UOM_UNIT_BLOOD_PRESSURE_PANEL  = "mmHg";
    public static final String LOIC_CODE_BLOOD_PRESSURE_PANEL_SYS = "8480-6";
    public static final String LOIC_DESC_BLOOD_PRESSURE_PANEL_SYS = "Systolic blood pressure";
    public static final String LOIC_CODE_BLOOD_PRESSURE_PANEL_DIA = "8462-4";
    public static final String LOIC_DESC_BLOOD_PRESSURE_PANEL_DIA = "Diastolic blood pressure";

    public static final String TERM_OBSERVATION_INTERPRETATION_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation";
    public static final String TERM_OBSERVATION_INTERPRETATION_LO_CODE = "L";
    public static final String TERM_OBSERVATION_INTERPRETATION_LO_DESC = "low";
    public static final String TERM_OBSERVATION_INTERPRETATION_HI_CODE = "H";
    public static final String TERM_OBSERVATION_INTERPRETATION_HI_DESC = "high";
    public static final String TERM_OBSERVATION_INTERPRETATION_NORMAL_CODE = "N";
    public static final String TERM_OBSERVATION_INTERPRETATION_NORMAL_DESC = "NORMAL";

    public static final String PATIENT_ID = "618670";

    public static final String TOKEN_PATIENT_ID="PATIENT_ID";
    public static final String TOKEN_EFF_DATE="EFF_DATE";
    public static final String TOKEN_BP_DIA_VALUE="BP_DIA_VALUE";
    public static final String TOKEN_BP_SYS_VALUE="BP_SYS_VALUE";

}
