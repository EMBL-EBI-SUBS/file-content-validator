package uk.ac.ebi.subs.filecontentvalidator.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.filecontentvalidator.config.CommandLineParams;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommandLineParamChecker {

    @NonNull
    private SingleValidationResultBuilder singleValidationResultBuilder;

    public List<SingleValidationResult> validateParameters(CommandLineParams commandLineParams) {

        List<SingleValidationResult> validationErrors = new ArrayList<>();

        validateFileExistence(commandLineParams).ifPresent(validationErrors::add);

        validateFileType(commandLineParams).ifPresent(validationErrors::add);

        return validationErrors;
    }

    private Optional<SingleValidationResult> validateFileExistence(CommandLineParams commandLineParams) {
        String filePath = commandLineParams.getFilePath();
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            return Optional.of(singleValidationResultBuilder.buildSingleValidationResultWithErrorStatus(String.format(ErrorMessages.FILE_NOT_FOUND_BY_TARGET_PATH, filePath)));
        }

        return Optional.empty();
    }

    private Optional<SingleValidationResult> validateFileType(CommandLineParams commandLineParams) {
        String fileType = commandLineParams.getFileType();
        if (!FileType.forName(fileType)) {
            return Optional.of(singleValidationResultBuilder.buildSingleValidationResultWithErrorStatus(String.format(ErrorMessages.FILE_TYPE_NOT_SUPPORTED, fileType)));
        }

        return Optional.empty();
    }
}
