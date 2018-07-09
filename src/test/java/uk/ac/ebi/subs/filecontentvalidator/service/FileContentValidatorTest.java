package uk.ac.ebi.subs.filecontentvalidator.service;

import org.junit.Before;
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isEmptyString;
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
    private static final String FILE_TYPE = "fastQ";
    private static final String ERROR_MESSAGE = "This is an error";
    private static final String FULL_ERROR_MESSAGE = "This is some text \nThis is another text \nERROR: %s";
    private static final String OK_MESSAGE = "OK";

    @Before
    public void setup() {
        fileContentValidator.setValidationError(null);
    }

    @Test
    public void whenFileExistsButItsContentsGotError_ThenValidationResultHasError() throws IOException, InterruptedException {
        doReturn(CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, TEST_FILE_PATH, FILE_TYPE)).when(this.fileContentValidator).getCommandLineParams();
        doReturn(String.format(FULL_ERROR_MESSAGE, ERROR_MESSAGE)).when(this.fileContentValidator).executeValidationAndGetResult();

        fileContentValidator.validateFileContent();

        String validationError = fileContentValidator.getValidationError();

        assertThat(validationError, not(isEmptyString()));
    }

    @Test
    public void whenFileExistsAndItsContentsCorrect_ThenValidationResultHasOK() throws IOException, InterruptedException {
        doReturn(CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, TEST_FILE_PATH, FILE_TYPE)).when(this.fileContentValidator).getCommandLineParams();
        doReturn(OK_MESSAGE).when(this.fileContentValidator).executeValidationAndGetResult();

        fileContentValidator.validateFileContent();

        String validationError = fileContentValidator.getValidationError();

        assertThat(validationError, isEmptyOrNullString());
    }

    @Test
    public void whenFileExistsButItsContentsGotError_ThenSingleValidationResultContainsTheError() throws IOException, InterruptedException {
        doReturn(CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, TEST_FILE_PATH, FILE_TYPE)).when(this.fileContentValidator).getCommandLineParams();
        doReturn(String.format(FULL_ERROR_MESSAGE, ERROR_MESSAGE)).when(this.fileContentValidator).executeValidationAndGetResult();

        fileContentValidator.validateFileContent();
        SingleValidationResult singleValidationResult = fileContentValidator.buildSingleValidationResult();

        assertThat(singleValidationResult.getValidationStatus(), is(equalTo(SingleValidationResultStatus.Error)));
        assertThat(singleValidationResult.getValidationAuthor(), is(equalTo(ValidationAuthor.FileContent)));
        assertThat(singleValidationResult.getMessage(), is(equalTo(ERROR_MESSAGE)));
    }

    @Test
    public void whenFileExistsAndItsContentsCorrect_ThenSingleValidationResultContainsPassedStatus() throws IOException, InterruptedException {
        doReturn(CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, TEST_FILE_PATH, FILE_TYPE)).when(this.fileContentValidator).getCommandLineParams();
        doReturn(OK_MESSAGE).when(this.fileContentValidator).executeValidationAndGetResult();

        fileContentValidator.validateFileContent();
        SingleValidationResult singleValidationResult = fileContentValidator.buildSingleValidationResult();

        assertThat(singleValidationResult.getValidationStatus(), is(equalTo(SingleValidationResultStatus.Pass)));
        assertThat(singleValidationResult.getValidationAuthor(), is(equalTo(ValidationAuthor.FileContent)));
    }
}
