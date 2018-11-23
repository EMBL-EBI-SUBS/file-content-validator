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

import java.io.FileNotFoundException;
import java.io.IOException;
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
    private static final String REPORT_FILE_NAME = "bamcram_report.txt";
    public static final String REMOVE_STANDARD_OUTPUT = "> /dev/null";

    @Value("${fileContentValidator.bamcram.validatorPath}")
    private String validatorPath;

    @NonNull
    private CommandLineParams commandLineParams;

    @NonNull
    private SingleValidationResultBuilder singleValidationResultBuilder;

    @Override
    public List<SingleValidationResult> validateFileContent() throws IOException, InterruptedException {
        List<SingleValidationResult> results = doQuickCheckValidation();

        if (results.isEmpty()) {
            results.addAll(doCountValidation());
        }

        if (results.isEmpty()) {
            results.add(singleValidationResultBuilder.buildSingleValidationResultWithPassStatus());
        }

        return results;
    }

    String getValidationOutput(String command) throws IOException, InterruptedException {
        LOGGER.info("command: {}", command);
        java.util.Scanner validationOutput =
                new java.util.Scanner(Runtime.getRuntime().exec(command)
                        .getErrorStream()).useDelimiter("\\A");

        return validationOutput.hasNext() ? validationOutput.next() : "";
    }

    List<SingleValidationResult> parseOutput(String validationOutput) throws FileNotFoundException {
        List<SingleValidationResult> results = new ArrayList<>();

        Scanner scanner = new Scanner(validationOutput);
        scanner.useDelimiter("\n");

        while (scanner.hasNext()) {
            String message = scanner.nextLine();

            if (message.startsWith(VERBOSITY_MESSAGE)) {
                continue;
            }

            results.add(singleValidationResultBuilder.buildSingleValidationResultWithErrorStatus(message));
        }

        LOGGER.info("results: {}", results);

        return results;
    }

    List<SingleValidationResult> doQuickCheckValidation() throws IOException, InterruptedException {
        // TODO karoly: temporary switch off the quickcheck validation
        // we should put it back when samtools has been updated to greater then 1.9
        // https://github.com/samtools/samtools/pull/920
        //return doValidation(quickCheckValidationCommand());

        return new ArrayList<>();
    }

    List<SingleValidationResult> doCountValidation() throws IOException, InterruptedException {
        return doValidation(countValidationCommand());
    }

    private List<SingleValidationResult> doValidation(String validationCommand)
            throws IOException, InterruptedException {
        String validationOutput = getValidationOutput(validationCommand);

        return parseOutput(validationOutput);
    }

    String quickCheckValidationCommand() {
        return String.join(" ",
                validatorPath,
                "quickcheck -vv", getCommandLineParams().getFilePath()
        );
    }

    String countValidationCommand() {
        return String.join(" ",
                validatorPath,
                "view -c", getCommandLineParams().getFilePath()
        );
    }
}
