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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
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
    private static final String ERROR_MESSAGE = "quickcheck.badheader.bam caused an error whilst reading its header.";
    private static final String INVALID_CODE_LENGTHS_SET_ERROR_MESSAGE = "[E::inflate_gzip_block] Inflate operation failed: invalid code lengths set";
    private static final String SAMVIEW_BAD_HEADER_BAM_ERROR_MESSAGE = "[main_samview] fail to read the header from \"2.quickcheck.badheader.bam\".";

    private static final String ERROR_REPORT_FROM_QUICKCHECK =
            "verbosity set to 2\nquickcheck.badheader.bam caused an error whilst reading its header.";
    private static final String ERROR_REPORT_FROM_QUICKCHECK_WITH_FILEPATH =
            "verbosity set to 2\n/path/to/file/quickcheck.badheader.bam caused an error whilst reading its header.";
    private static final String OK_REPORT_FROM_QUICKCHECK = "verbosity set to 2\n";

    private static final String EXE_PATH = "/path/to/bamcram/validator";
    private static final String OUTPUT_DIR = "/fake/out";
    private Path outputPath = Paths.get(OUTPUT_DIR);
    private static final String REPORT_FILE_NAME = "bamcram_report.txt";

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
                "quickcheck -vv", TEST_FILE_PATH
        );
        String actualCommandLine = bamCramFileValidator.quickCheckValidationCommand();
        assertThat(actualCommandLine, is(equalTo(expectedCommandLine)));
    }

    @Test
    public void test_report_parsing_with_errors() throws IOException {
        bamCramFileValidator = new BamCramFileValidator(commandLineParams, singleValidationResultBuilder);

        List<SingleValidationResult> expectedResults = Collections.singletonList(
                singleValidationResultBuilder.buildSingleValidationResultWithErrorStatus(ERROR_MESSAGE)
        );

        List<SingleValidationResult> actualResults = bamCramFileValidator.parseOutput(ERROR_REPORT_FROM_QUICKCHECK);
        assertThat(actualResults, is(equalTo(expectedResults)));
    }

    @Test
    public void test_report_parsing_with_no_problems() throws IOException {
        bamCramFileValidator = new BamCramFileValidator(commandLineParams, singleValidationResultBuilder);

        List<SingleValidationResult> actualResults = bamCramFileValidator.parseOutput(OK_REPORT_FROM_QUICKCHECK);
        assertThat(actualResults.size(), is(0));
    }

    @Test
    public void whenFileExistsButItsContentsGotErrorByQuickCheckValidation_ThenValidationResultHasError() throws IOException, InterruptedException {
        doReturn(CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, FILE_UUID, TEST_FILE_PATH, FILE_TYPE)).when(this.bamCramFileValidator).getCommandLineParams();
        doReturn(ERROR_REPORT_FROM_QUICKCHECK).when(this.bamCramFileValidator).getValidationOutput(any(String.class));

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
        doReturn(OK_REPORT_FROM_QUICKCHECK).when(this.bamCramFileValidator).getValidationOutput(any(String.class));

        List<SingleValidationResult> validationResults = bamCramFileValidator.validateFileContent();;
        final SingleValidationResult singleValidationResult = validationResults.get(0);

        assertThat(validationResults, hasSize(1));
        assertThat(singleValidationResult.getValidationAuthor(), is(equalTo(ValidationAuthor.FileContent)));
        assertThat(singleValidationResult.getValidationStatus(), is(equalTo(SingleValidationResultStatus.Pass)));
    }
    @Test
    public void whenFileExistsButItsContentsGotErrorByCountValidation_ThenValidationResultHasError() throws IOException, InterruptedException {
        List<String> fileContent = Arrays.asList(
                INVALID_CODE_LENGTHS_SET_ERROR_MESSAGE,
                SAMVIEW_BAD_HEADER_BAM_ERROR_MESSAGE
        );

        doReturn(CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, FILE_UUID, TEST_FILE_PATH, FILE_TYPE)).when(this.bamCramFileValidator).getCommandLineParams();
        doReturn(String.join("\n", fileContent)).when(this.bamCramFileValidator).getValidationOutput(any(String.class));

        doReturn(new ArrayList<>()).when(this.bamCramFileValidator).doQuickCheckValidation();

        List<SingleValidationResult> validationError = bamCramFileValidator.validateFileContent();;

        assertThat(validationError, hasSize(2));

        final SingleValidationResult singleValidationResult1 = validationError.get(0);

        assertThat(singleValidationResult1.getValidationAuthor(), is(equalTo(ValidationAuthor.FileContent)));
        assertThat(singleValidationResult1.getValidationStatus(), is(equalTo(SingleValidationResultStatus.Error)));
        assertThat(singleValidationResult1.getMessage(), is(equalTo(INVALID_CODE_LENGTHS_SET_ERROR_MESSAGE)));

        final SingleValidationResult singleValidationResult2 = validationError.get(1);

        assertThat(singleValidationResult2.getValidationAuthor(), is(equalTo(ValidationAuthor.FileContent)));
        assertThat(singleValidationResult2.getValidationStatus(), is(equalTo(SingleValidationResultStatus.Error)));
        assertThat(singleValidationResult2.getMessage(), is(equalTo(SAMVIEW_BAD_HEADER_BAM_ERROR_MESSAGE)));
    }

    @Test
    public void whenReturningErrorMessage_ThenFilePathGetStrippedFromTheFileName() throws FileNotFoundException {
        bamCramFileValidator = new BamCramFileValidator(commandLineParams, singleValidationResultBuilder);

        List<SingleValidationResult> expectedResults = Collections.singletonList(
                singleValidationResultBuilder.buildSingleValidationResultWithErrorStatus(ERROR_MESSAGE)
        );

        List<SingleValidationResult> actualResults = bamCramFileValidator.parseOutput(ERROR_REPORT_FROM_QUICKCHECK_WITH_FILEPATH);
        assertThat(actualResults, is(equalTo(expectedResults)));
    }
}
