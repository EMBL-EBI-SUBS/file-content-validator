package uk.ac.ebi.subs.filecontentvalidator.service;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.ac.ebi.subs.filecontentvalidator.config.CommandLineParams;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

@Component
@Getter
@RequiredArgsConstructor
public class SingleValidationResultBuilder {

    @NonNull
    private CommandLineParams commandLineParams;

    public SingleValidationResult buildSingleValidationResultWithErrorStatus(String message) {
        SingleValidationResult svr = buildSingleValidationResult(message);
        svr.setValidationStatus(SingleValidationResultStatus.Error);
        return svr;
    }

    public SingleValidationResult buildSingleValidationResultWithWarningStatus(String message){
        SingleValidationResult svr = buildSingleValidationResult(message);
        svr.setValidationStatus(SingleValidationResultStatus.Warning);
        return svr;
    }

    public SingleValidationResult buildSingleValidationResultWithPassStatus() {
        SingleValidationResult svr = buildSingleValidationResult("");
        svr.setValidationStatus(SingleValidationResultStatus.Pass);
        return svr;
    }

    private SingleValidationResult buildSingleValidationResult(String errorMessage) {
        SingleValidationResult singleValidationResult =
                new SingleValidationResult(ValidationAuthor.FileContent, getCommandLineParams().getFileUUID());

        if (!StringUtils.isEmpty(errorMessage)) {
            singleValidationResult.setMessage(errorMessage);
        }

        return singleValidationResult;
    }
}
