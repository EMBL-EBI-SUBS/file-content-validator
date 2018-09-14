package uk.ac.ebi.subs.filecontentvalidator.validators;

import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.io.IOException;
import java.util.List;

public interface FileValidator {

    List<SingleValidationResult> validateFileContent() throws IOException, InterruptedException;
}
