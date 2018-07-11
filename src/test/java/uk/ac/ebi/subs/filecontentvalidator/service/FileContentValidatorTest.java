package uk.ac.ebi.subs.filecontentvalidator.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.filecontentvalidator.config.CommandLineParams;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {FileContentValidator.class, CommandLineParams.class })
public class FileContentValidatorTest {

    @SpyBean
    private FileContentValidator fileContentValidator;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String TEST_FILE_PATH = "src/test/resources/test_file_for_file_content_validation.txt";
    private static final String VALIDATION_RESULT_UUID = "112233-aabbcc-223344";
    private static final String FILE_UUID = "9999-aabbcc-223344";
    private static final String FILE_TYPE = "fastQ";
    private static final String ERROR_MESSAGE = "This is an error";
    private static final String FULL_ERROR_MESSAGE = "This is some text \nThis is another text \nERROR: %s";
    private static final String OK_MESSAGE = "OK";

    @Test
    public void whenFileExistsButItsContentsGotError_ThenValidationResultHasError() throws IOException, InterruptedException {
        doReturn(CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, FILE_UUID, TEST_FILE_PATH, FILE_TYPE)).when(this.fileContentValidator).getCommandLineParams();
        doReturn(String.format(FULL_ERROR_MESSAGE, ERROR_MESSAGE)).when(this.fileContentValidator).executeValidationAndGetResult();

        List<SingleValidationResult> validationError = fileContentValidator.validateFileContent();;
        final SingleValidationResult singleValidationResult = validationError.get(0);

        assertThat(validationError, hasSize(1));

        assertThat(singleValidationResult.getValidationAuthor(), is(equalTo(ValidationAuthor.FileContent)));
        assertThat(singleValidationResult.getValidationStatus(), is(equalTo(SingleValidationResultStatus.Error)));
        assertThat(singleValidationResult.getMessage(), is(equalTo(ERROR_MESSAGE)));
    }

    @Test
    public void whenFileExistsAndItsContentsCorrect_ThenValidationResultHasOK() throws IOException, InterruptedException {
        doReturn(CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, FILE_UUID, TEST_FILE_PATH, FILE_TYPE)).when(this.fileContentValidator).getCommandLineParams();
        doReturn(OK_MESSAGE).when(this.fileContentValidator).executeValidationAndGetResult();

        List<SingleValidationResult> validationResults = fileContentValidator.validateFileContent();;
        final SingleValidationResult singleValidationResult = validationResults.get(0);

        assertThat(validationResults, hasSize(1));
        assertThat(singleValidationResult.getValidationAuthor(), is(equalTo(ValidationAuthor.FileContent)));
        assertThat(singleValidationResult.getValidationStatus(), is(equalTo(SingleValidationResultStatus.Pass)));
    }

    @Test
    public void whenFileExistsButItsContentsGotError_ThenSingleValidationResultContainsTheError() throws IOException, InterruptedException {
        doReturn(CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, FILE_UUID, TEST_FILE_PATH, FILE_TYPE)).when(this.fileContentValidator).getCommandLineParams();
        doReturn(String.format(FULL_ERROR_MESSAGE, ERROR_MESSAGE)).when(this.fileContentValidator).executeValidationAndGetResult();

        List<SingleValidationResult> singleValidationResults = fileContentValidator.validateFileContent();

        assertThat(singleValidationResults, hasSize(1));

        SingleValidationResult singleValidationResult = singleValidationResults.get(0);

        assertThat(singleValidationResult.getValidationStatus(), is(equalTo(SingleValidationResultStatus.Error)));
        assertThat(singleValidationResult.getValidationAuthor(), is(equalTo(ValidationAuthor.FileContent)));
        assertThat(singleValidationResult.getMessage(), is(equalTo(ERROR_MESSAGE)));
    }

    @Test
    public void whenFileExistsAndItsContentsCorrect_ThenSingleValidationResultContainsPassedStatus() throws IOException, InterruptedException {
        doReturn(CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, FILE_UUID, TEST_FILE_PATH, FILE_TYPE)).when(this.fileContentValidator).getCommandLineParams();
        doReturn(OK_MESSAGE).when(this.fileContentValidator).executeValidationAndGetResult();

        List<SingleValidationResult> singleValidationResults = fileContentValidator.validateFileContent();

        assertThat(singleValidationResults, hasSize(1));

        SingleValidationResult singleValidationResult = singleValidationResults.get(0);

        assertThat(singleValidationResult.getValidationStatus(), is(equalTo(SingleValidationResultStatus.Pass)));
        assertThat(singleValidationResult.getValidationAuthor(), is(equalTo(ValidationAuthor.FileContent)));
    }
}
