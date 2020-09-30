package sample;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;

import java.util.List;

public class S7Validate {
    private IGenericClient client = null;

    public S7Validate(String baseUrl) {
        FhirContext ctx = FhirContext.forR4();
        client = ctx.newRestfulGenericClient(baseUrl);
    }

    public void testValidateEmptyElement() {
        String input = "<Patient xmlns=\"http://hl7.org/fhir\">" +
                "  <text>\n" +
                "    <status value=\"generated\"/>\n" +
                "    <div xmlns=\"http://www.w3.org/1999/xhtml\">AAA</div>\n" +
                "  </text>" +
                "  <active value=\"\"/>" +
                "</Patient>";

        /*
        FhirValidator val = this.client.getFhirContext().newValidator();
        val.registerValidatorModule(new FhirInstanceValidator(myValidationSupport));

        ValidationResult result = val.validateWithResult(input);
        List<SingleValidationMessage> all = logResultsAndReturnAll(result);
        assertFalse(result.isSuccessful());
        assertEquals("ele-1: All FHIR elements must have a @value or children [hasValue() or (children().count() > id.count())]", all.get(0).getMessage())
       ;
         */
    }
}
