package uk.ac.ebi.subs.filecontentvalidator.service;

import lombok.Data;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.ac.ebi.subs.filecontentvalidator.config.CommandLineParams;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Service
public class FileContentValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileContentValidator.class);

    @NonNull
    private CommandLineParams commandLineParams;

    private static final String ERROR_RESULT_BEGINNING = "ERROR:";
    private static final String OK_RESULT_BEGINNING = "OK";

    private List<String> validationErrors;

    @Value("${fileContentValidator.fastQ.validatorPath}")
    private String validatorPath;

    CommandLineParams getCommandLineParams() {
        return this.commandLineParams;
    }

    public List<SingleValidationResult> validateFileContent() throws IOException, InterruptedException {
        List<SingleValidationResult> singleValidationResults = new ArrayList<>();

        validateParameters();

        String output = executeValidationAndGetResult();
        String resultMessage;

        int last = output.lastIndexOf("\n");
        if (last >= 0) {
            resultMessage = output.substring(last, output.length());
            if (resultMessage.contains(ERROR_RESULT_BEGINNING)) {
                validationErrors.add(resultMessage.replace(ERROR_RESULT_BEGINNING, "").trim());
            }
        }

        singleValidationResults = validationErrors.stream()
                .map(this::buildSingleValidationResultWithErrorStatus)
                .collect(Collectors.toList());

        if (singleValidationResults.size() == 0) {
            singleValidationResults.add(buildSingleValidationResultWithPassStatus());
        }

        return singleValidationResults;
    }

    String executeValidationAndGetResult() throws IOException, InterruptedException {
        String commandToExecuteValidation = assembleValidatorCommand();

        StringBuilder output = new StringBuilder();

        Runtime rt = Runtime.getRuntime();
        Process process = rt.exec(commandToExecuteValidation);
        process.waitFor();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine())!= null) {
            if (output.length() > 0) {
                output.append("\n");
            }
            output.append(line);
        }

        return output.toString();
    }

    private String assembleValidatorCommand() {
        return String.join(" ", validatorPath, getCommandLineParams().getFilePath());
    }

    boolean validateParameters() {
        initValidationErrors();

        validateFileExistence();

        validateFileType();

        return validationErrors.size() == 0;
    }

    private void initValidationErrors() {
        this.validationErrors = new ArrayList<>();
    }

    private void validateFileExistence() {
        String filePath = getCommandLineParams().getFilePath();
        File file = new File(filePath);
        if(!file.exists() || file.isDirectory()) {
            this.validationErrors.add(String.format(ErrorMessages.FILE_NOT_FOUND_BY_TARGET_PATH, filePath));
        }
    }

    private void validateFileType() {
        String fileType = getCommandLineParams().getFileType();
        if (!FileType.forName(fileType)) {
            this.validationErrors.add(String.format(ErrorMessages.FILE_TYPE_NOT_SUPPORTED, fileType));
        }
    }

    private SingleValidationResult buildSingleValidationResultWithErrorStatus(String errrorMessage) {
        return buildSingleValidationResult(errrorMessage);
    }

    private SingleValidationResult buildSingleValidationResultWithPassStatus() {
        return buildSingleValidationResult("");
    }

    private SingleValidationResult buildSingleValidationResult(String errorMessage) {
        SingleValidationResult singleValidationResult =
                new SingleValidationResult(ValidationAuthor.FileContent, getCommandLineParams().getFileUUID());
        if (!StringUtils.isEmpty(errorMessage)) {
            singleValidationResult.setMessage(errorMessage);
            singleValidationResult.setValidationStatus(SingleValidationResultStatus.Error);
        } else {
            singleValidationResult.setValidationStatus(SingleValidationResultStatus.Pass);
        }

        return singleValidationResult;
    }
}
