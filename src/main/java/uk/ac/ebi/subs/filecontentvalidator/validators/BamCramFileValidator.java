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

@Service
@RequiredArgsConstructor
@Data
public class BamCramFileValidator implements FileValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BamCramFileValidator.class);
    static final String FOLDER_PREFIX_FOR_USI_BAM_CRAM_VALIDATION = "usi-bam-cram-validation";
    public static final String VERBOSITY_MESSAGE = "verbosity";

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

        executeValidation(outputDirectory);

        File reportFile = getReportFile(outputDirectory);

        List<SingleValidationResult> results = parseOutputFile(reportFile);

        deleteTemporaryFile(reportFile);

        return results;
    }

    void deleteTemporaryFile(File reportFile) {
        reportFile.delete();
    }

    File getReportFile(Path outputDirectory) {
        return ValidatorFileUtils.findOutputFile(outputDirectory);
    }

    List<SingleValidationResult> parseOutputFile(File summaryFile) throws FileNotFoundException {
        List<SingleValidationResult> results = new ArrayList<>();

        Scanner scanner = new Scanner(summaryFile);
        scanner.useDelimiter("\n");

        while (scanner.hasNext()) {
            String message = scanner.nextLine();

            if (message.startsWith(VERBOSITY_MESSAGE)) {
                continue;
            }

            results.add(singleValidationResultBuilder.buildSingleValidationResultWithErrorStatus(message));
        }

        if (results.isEmpty()) {
            results.add(singleValidationResultBuilder.buildSingleValidationResultWithPassStatus());
        }
        LOGGER.info("results: {}", results);

        return results;
    }

    void executeValidation(Path outputDirectory) throws IOException, InterruptedException {
        String command = validatorCommandLine(outputDirectory);
        LOGGER.info("command: {}", command);
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
    }

    String validatorCommandLine(Path outputDirectory) {
        return String.join(" ",
                validatorPath,
                "quickcheck -vv", getCommandLineParams().getFilePath(),
                "2> ", outputDirectory.toString()
        );
    }
}
