package uk.ac.ebi.subs.filecontentvalidator.validators;

import lombok.Data;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.filecontentvalidator.config.CommandLineParams;
import uk.ac.ebi.subs.filecontentvalidator.service.SingleValidationResultBuilder;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@Service
public class FastqFileValidator implements FileValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FastqFileValidator.class);

    @NonNull
    private CommandLineParams commandLineParams;

    @NonNull
    private SingleValidationResultBuilder singleValidationResultBuilder;

    private static final String ERROR_RESULT_BEGINNING = "ERROR:";
    private static final String OK_RESULT_BEGINNING = "OK";

    @Value("${fileContentValidator.fastQ.validatorPath}")
    private String validatorPath;

    @Override
    public List<SingleValidationResult> validateFileContent() throws IOException, InterruptedException {
        List<SingleValidationResult> singleValidationResults = new ArrayList<>();

        String output = executeValidationAndGetResult().trim();

        LOGGER.warn("File content validator result message: {}", output);

        String resultMessage = "";

        int last = output.lastIndexOf("\n");
        if (last >= 0) {
            resultMessage = output.substring(last, output.length());
            if (resultMessage.contains(ERROR_RESULT_BEGINNING)) {
                singleValidationResults.add(
                        singleValidationResultBuilder.buildSingleValidationResultWithErrorStatus(
                                resultMessage.replace(ERROR_RESULT_BEGINNING, "").trim()));
                LOGGER.info("Error has been added to the SingleValidationResult: {}, list size: {}", resultMessage, singleValidationResults.size());
            }
        }

        LOGGER.warn("File content validator result message after parsing: {}", resultMessage);
        LOGGER.warn("Last position: {}", last);

        if (singleValidationResults.size() == 0) {
            singleValidationResults.add(singleValidationResultBuilder.buildSingleValidationResultWithPassStatus());
        }

        return singleValidationResults;
    }

    String executeValidationAndGetResult() throws IOException, InterruptedException {
        String commandToExecuteValidation = assembleValidatorCommand();

        java.util.Scanner validationResult = new java.util.Scanner(Runtime.getRuntime().exec(commandToExecuteValidation)
                .getInputStream()).useDelimiter("\\A");
        if (!validationResult.hasNext()) {
            validationResult = new java.util.Scanner(Runtime.getRuntime().exec(commandToExecuteValidation)
                    .getErrorStream()).useDelimiter("\\A");
        }

        return validationResult.hasNext() ? validationResult.next() : "";
    }

    private String assembleValidatorCommand() {
        return String.join(" ", validatorPath, getCommandLineParams().getFilePath());
    }





}
