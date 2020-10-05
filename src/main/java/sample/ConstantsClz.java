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
    public static final String LOIC_CODE_BODY_TEMPARATURE = "8310-5";
    public static final String LOIC_DESC_BODY_TEMPARATURE = "Body temperature";

    public static final String PATIENT_ID = "618670";
}
