package uk.ac.ebi.subs.filecontentvalidator.validators;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.filecontentvalidator.config.CommandLineParams;
import uk.ac.ebi.subs.filecontentvalidator.service.SingleValidationResultBuilder;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Data
public class BamCramFileValidator implements FileValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BamCramFileValidator.class);
    static final String FOLDER_PREFIX_FOR_USI_BAM_CRAM_VALIDATION = "usi-bam-cram-validation";
    public static final String VERBOSITY_MESSAGE = "verbosity";
    private static final String REPORT_FILE_NAME = "bamcram_report.txt";

    @Value("${fileContentValidator.bamcram.validatorPath}")
    private String validatorPath;

    @NonNull
    private CommandLineParams commandLineParams;

    @NonNull
    private SingleValidationResultBuilder singleValidationResultBuilder;

    @Override
    public List<SingleValidationResult> validateFileContent() throws IOException, InterruptedException {
        Path outputDirectory = ValidatorFileUtils.createOutputDir(FOLDER_PREFIX_FOR_USI_BAM_CRAM_VALIDATION);
        LOGGER.info("output will be written to {}", outputDirectory);

        String reportFileName = String.join("/",
                outputDirectory.toString(),
                String.join("_", UUID.randomUUID().toString(), REPORT_FILE_NAME)
        );

        List<SingleValidationResult> results = doQuickCheckValidation(outputDirectory, reportFileName);

        if (results.isEmpty()) {
            results.addAll(doCountValidation(outputDirectory, reportFileName));
        }

        if (results.isEmpty()) {
            results.add(singleValidationResultBuilder.buildSingleValidationResultWithPassStatus());
        }

        return results;
    }

    void executeValidation(String command) throws IOException, InterruptedException {
        LOGGER.info("command: {}", command);
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
    }

    List<SingleValidationResult> parseOutputFile(File reportFile) throws FileNotFoundException {
        List<SingleValidationResult> results = new ArrayList<>();

        Scanner scanner = new Scanner(reportFile);
        scanner.useDelimiter("\n");

        while (scanner.hasNext()) {
            String message = scanner.nextLine();

            if (message.startsWith(VERBOSITY_MESSAGE)) {
                continue;
            }

            results.add(singleValidationResultBuilder.buildSingleValidationResultWithErrorStatus(message));
        }

        LOGGER.info("results: {}", results);

        deleteTemporaryFile(reportFile);

        return results;
    }

    File getReportFile(Path outputDirectory) {
        return ValidatorFileUtils.findOutputFile(outputDirectory);
    }

    void deleteTemporaryFile(File reportFile) {
        reportFile.delete();
    }

    List<SingleValidationResult> doQuickCheckValidation(Path outputDirectory, String reportFileName) throws IOException, InterruptedException {
        return doValidation(quickCheckValidationCommand(reportFileName), outputDirectory);
    }

    List<SingleValidationResult> doCountValidation(Path outputDirectory, String reportFileName) throws IOException, InterruptedException {
        return doValidation(countValidationCommand(reportFileName), outputDirectory);
    }

    private List<SingleValidationResult> doValidation(String validationCommand, Path outputDirectory)
            throws IOException, InterruptedException {
        executeValidation(validationCommand);

        File reportFile = getReportFile(outputDirectory);

        return parseOutputFile(reportFile);
    }

    String quickCheckValidationCommand(String reportFileName) {
        return String.join(" ",
                validatorPath,
                "quickcheck -vv", getCommandLineParams().getFilePath(),
                "2> ", reportFileName
        );
    }

    String countValidationCommand(String reportFileName) {
        return String.join(" ",
                validatorPath,
                "view -c", getCommandLineParams().getFilePath(),
                "2> ", reportFileName
        );
    }
}
