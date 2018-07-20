package uk.ac.ebi.subs.filecontentvalidator.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.filecontentvalidator.config.CommandLineParams;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CommandLineParamChecker.class, CommandLineParams.class, SingleValidationResultBuilder.class })
public class CommandLineParameterCheckerTest {

    @Autowired
    private CommandLineParamChecker commandLineParamChecker;

    private static final String TEST_FILE_INVALID_PATH = "/invalid/path";
    private static final String TEST_FILE_PATH = resourceToAbsolutePath("test_file_for_file_content_validation.txt");
    private static final String VALIDATION_RESULT_UUID = "112233-aabbcc-223344";
    private static final String FILE_UUID = "9999-aabbcc-223344";
    private static final String FILE_TYPE = "fastQ";
    private static final String NOT_SUPPORTED_FILE_TYPE = "cram";

    @Test
    public void whenFileIsNotExistsInTheProvidedPath_ThenThrowsFileNotFoundException() {

        CommandLineParams commandLineParams = CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, FILE_UUID, TEST_FILE_INVALID_PATH, FILE_TYPE);
        String expectedValidationError = String.format(ErrorMessages.FILE_NOT_FOUND_BY_TARGET_PATH, TEST_FILE_INVALID_PATH);

        List<SingleValidationResult> parameterErrors = commandLineParamChecker.validateParameters(commandLineParams);
        final SingleValidationResult singleValidationResult = parameterErrors.get(0);

        assertThat(parameterErrors, not(emptyIterable()));
        assertThat(singleValidationResult.getValidationAuthor(), is(equalTo(ValidationAuthor.FileContent)));
        assertThat(singleValidationResult.getValidationStatus(), is(equalTo(SingleValidationResultStatus.Error)));
        assertThat(singleValidationResult.getMessage(), is(equalTo(expectedValidationError)));
    }

    @Test
    public void whenUnsupportedFileTypeProvided_ThenThrowsNotSupportedFileTypeException() {
        CommandLineParams commandLineParams = CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, FILE_UUID, TEST_FILE_PATH, NOT_SUPPORTED_FILE_TYPE);

        String expectedValidationError = String.format(ErrorMessages.FILE_TYPE_NOT_SUPPORTED, NOT_SUPPORTED_FILE_TYPE);

        List<SingleValidationResult> parameterErrors = commandLineParamChecker.validateParameters(commandLineParams);
        final SingleValidationResult singleValidationResult = parameterErrors.get(0);

        assertThat(parameterErrors, not(emptyIterable()));
        assertThat(singleValidationResult.getValidationAuthor(), is(equalTo(ValidationAuthor.FileContent)));
        assertThat(singleValidationResult.getValidationStatus(), is(equalTo(SingleValidationResultStatus.Error)));
        assertThat(singleValidationResult.getMessage(), is(equalTo(expectedValidationError)));
    }

    @Test
    public void whenParametersValid_ThenNoErrors() {
        CommandLineParams commandLineParams = CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, FILE_UUID, TEST_FILE_PATH, FILE_TYPE);

        assertThat(commandLineParamChecker.validateParameters(commandLineParams), emptyIterable());
    }

    private static String resourceToAbsolutePath(String resourcePath){
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL is = classloader.getResource(resourcePath);
        File file = new File(is.getFile());
        return file.getAbsolutePath();
    }
}
