package uk.ac.ebi.subs.filecontentvalidator.validators;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

@RequiredArgsConstructor
@Component
public class VcfFileValidator implements FileValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(VcfFileValidator.class);
    static final String FOLDER_PREFIX_FOR_USI_VCF_VALIDATION = "usi-vcf-validation";

    @NonNull
    private CommandLineParams commandLineParams;

    @NonNull
    private SingleValidationResultBuilder singleValidationResultBuilder;

    @Value("${fileContentValidator.vcf.validatorPath}")
    @Setter
    private String validatorPath;

    private static final String ERROR_PREFIX = "Error: ";
    private static final String WARNING_PREFIX = "Warning: ";

    @Override
    public List<SingleValidationResult> validateFileContent() throws IOException, InterruptedException {

        Path outputDirectory = ValidatorFileUtils.createOutputDir(FOLDER_PREFIX_FOR_USI_VCF_VALIDATION);
        LOGGER.info("output will be written to {}", outputDirectory);

        Scanner processOutputScanner = executeVcfValidator(outputDirectory);

        while (processOutputScanner.hasNext()){
            LOGGER.debug("VCF validator output: {}",processOutputScanner.next());
        }

        File summaryFile = ValidatorFileUtils.findOutputFile(outputDirectory);

        List<SingleValidationResult> results = parseOutputFile(summaryFile);

        summaryFile.delete();

        return results;
    }

    List<SingleValidationResult> parseOutputFile(File summaryFile) throws FileNotFoundException {
        List<SingleValidationResult> results = new ArrayList<>();

        Scanner scanner = new Scanner(summaryFile);
        scanner.useDelimiter("\n");

        while (scanner.hasNext()) {
            String line = scanner.nextLine();

            if (line.startsWith(ERROR_PREFIX)) {
                String trimmedMessage = line.replace(ERROR_PREFIX, "");
                results.add(
                        singleValidationResultBuilder.buildSingleValidationResultWithErrorStatus(trimmedMessage)
                );
            }
            if (line.startsWith(WARNING_PREFIX)) {
                String trimmedMessage = line.replace(WARNING_PREFIX, "");
                results.add(
                        singleValidationResultBuilder.buildSingleValidationResultWithWarningStatus(trimmedMessage)
                );
            }
        }

        if (results.isEmpty()) {
            results.add(singleValidationResultBuilder.buildSingleValidationResultWithPassStatus());
        }
        LOGGER.debug("results: {}", results);

        return results;
    }

    Scanner executeVcfValidator(Path outputDirectory) throws IOException, InterruptedException {
        String command = vcfValidatorCommandLine(outputDirectory);
        LOGGER.debug("command: {}", command);
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
        Scanner scanner = new Scanner(process.getInputStream()).useDelimiter("\n");
        return scanner;
    }

    String vcfValidatorCommandLine(Path outputDirectory) {
        return String.join(" ",
                validatorPath,
                "-i", commandLineParams.getFilePath(),
                "-o", outputDirectory.toString(),
                "-r", "summary"
        );
    }
}
