package uk.ac.ebi.subs.filecontentvalidator.validators;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.filecontentvalidator.config.CommandLineParams;
import uk.ac.ebi.subs.filecontentvalidator.service.CommandLineParamBuilder;
import uk.ac.ebi.subs.filecontentvalidator.service.SingleValidationResultBuilder;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {FastqFileValidator.class, SingleValidationResultBuilder.class, CommandLineParams.class })
public class BamCramFileValidatorTest {

    @SpyBean
    private BamCramFileValidator bamCramFileValidator;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private SingleValidationResultBuilder singleValidationResultBuilder;
    private CommandLineParams commandLineParams;

    private static final String TEST_FILE_PATH = "src/test/resources/test_file_for_file_content_validation.txt";
    private static final String VALIDATION_RESULT_UUID = "112233-aabbcc-223344";
    private static final String FILE_UUID = "9999-aabbcc-223344";
    private static final String FILE_TYPE = "bam";
    private static final String ERROR_MESSAGE = "2.quickcheck.badheader.bam caused an error whilst reading its header.";

    private static final String EXE_PATH = "/path/to/bamcram/validator";
    private static final String OUTPUT_DIR = "/fake/out";
    private Path outputPath = Paths.get(OUTPUT_DIR);

    @Before
    public void buildUp() {
        commandLineParams = CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, FILE_UUID, TEST_FILE_PATH, FILE_TYPE);
        singleValidationResultBuilder = new SingleValidationResultBuilder(commandLineParams);
    }

    @Test
    public void test_command_line() {
        doReturn(CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, FILE_UUID, TEST_FILE_PATH, FILE_TYPE)).when(this.bamCramFileValidator).getCommandLineParams();
        String expectedCommandLine = String.join(" ",
                EXE_PATH,
                "quickcheck -vv", TEST_FILE_PATH,
                "2> ", OUTPUT_DIR
        );
        String actualCommandLine = bamCramFileValidator.validatorCommandLine(outputPath);
        assertThat(actualCommandLine, is(equalTo(expectedCommandLine)));
    }

    @Test
    public void test_report_parsing_with_errors() throws FileNotFoundException, IOException {
        bamCramFileValidator = new BamCramFileValidator(commandLineParams, singleValidationResultBuilder);

        List<String> fileContent = Arrays.asList(
                "verbosity set to 2",
                "2.quickcheck.badheader.bam caused an error whilst reading its header."
        );

        File reportFile = createTestResources(fileContent, "bam_report_with_error.txt");

        List<SingleValidationResult> expectedResults = Collections.singletonList(
                singleValidationResultBuilder.buildSingleValidationResultWithErrorStatus(
                        "2.quickcheck.badheader.bam caused an error whilst reading its header.")
        );

        List<SingleValidationResult> actualResults = bamCramFileValidator.parseOutputFile(reportFile);
        assertThat(actualResults, is(equalTo(expectedResults)));
    }

    @Test
    public void test_report_parsing_with_no_problems() throws FileNotFoundException, IOException {
        bamCramFileValidator = new BamCramFileValidator(commandLineParams, singleValidationResultBuilder);

        List<String> fileContent = Collections.singletonList(
                "verbosity set to 2"
        );

        File reportFile = createTestResources(fileContent, "bam_report_with_no_problems.txt");

        List<SingleValidationResult> expectedResults = Collections.singletonList(
                singleValidationResultBuilder.buildSingleValidationResultWithPassStatus()
        );

        List<SingleValidationResult> actualResults = bamCramFileValidator.parseOutputFile(reportFile);
        assertThat(actualResults, is(equalTo(expectedResults)));
    }

    @Test
    public void whenFileExistsButItsContentsGotError_ThenValidationResultHasError() throws IOException, InterruptedException {
        doReturn(CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, FILE_UUID, TEST_FILE_PATH, FILE_TYPE)).when(this.bamCramFileValidator).getCommandLineParams();
        doNothing().when(this.bamCramFileValidator).executeValidation(any(Path.class));
        doNothing().when(this.bamCramFileValidator).deleteTemporaryFile(any(File.class));

        List<String> fileContent = Arrays.asList(
                "verbosity set to 2",
                "2.quickcheck.badheader.bam caused an error whilst reading its header."
        );

        File reportFile = createTestResources(fileContent, "bam_report_with_no_errors.txt");

        doReturn(reportFile).when(this.bamCramFileValidator).getReportFile(any(Path.class));

        List<SingleValidationResult> validationError = bamCramFileValidator.validateFileContent();;
        final SingleValidationResult singleValidationResult = validationError.get(0);

        assertThat(validationError, hasSize(1));

        assertThat(singleValidationResult.getValidationAuthor(), is(equalTo(ValidationAuthor.FileContent)));
        assertThat(singleValidationResult.getValidationStatus(), is(equalTo(SingleValidationResultStatus.Error)));
        assertThat(singleValidationResult.getMessage(), is(equalTo(ERROR_MESSAGE)));
    }

    @Test
    public void whenFileExistsAndItsContentsCorrect_ThenValidationResultHasOK() throws IOException, InterruptedException {
        doReturn(CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, FILE_UUID, TEST_FILE_PATH, FILE_TYPE)).when(this.bamCramFileValidator).getCommandLineParams();
        doNothing().when(this.bamCramFileValidator).executeValidation(any(Path.class));
        doNothing().when(this.bamCramFileValidator).deleteTemporaryFile(any(File.class));

        List<String> fileContent = Collections.singletonList(
                "verbosity set to 2"
        );

        File reportFile = createTestResources(fileContent, "bam_report_with_no_problems.txt");

        doReturn(reportFile).when(this.bamCramFileValidator).getReportFile(any(Path.class));

        List<SingleValidationResult> validationResults = bamCramFileValidator.validateFileContent();;
        final SingleValidationResult singleValidationResult = validationResults.get(0);

        assertThat(validationResults, hasSize(1));
        assertThat(singleValidationResult.getValidationAuthor(), is(equalTo(ValidationAuthor.FileContent)));
        assertThat(singleValidationResult.getValidationStatus(), is(equalTo(SingleValidationResultStatus.Pass)));
    }

    private File locateResource(String fileName) {
        String file = Thread.currentThread().getContextClassLoader().getResource(fileName).getFile();
        return new File(file);
    }

    private File createTestResources(List<String> fileContent, String fileName) throws IOException {
        Path tempDirectory = Files.createTempDirectory("bamcram_temp_");
        tempDirectory.toFile().deleteOnExit();
        Path file = Paths.get(String.join("/", tempDirectory.toString(), fileName));
        return Files.write(file, fileContent, Charset.forName("UTF-8")).toFile();
    }
}
