package uk.ac.ebi.subs.filecontentvalidator.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.filecontentvalidator.config.CommandLineParams;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {FileContentValidator.class, CommandLineParams.class })
public class FileContentValidatorParameterTest {

    @SpyBean
    private FileContentValidator fileContentValidator;

    private static final String TEST_FILE_INVALID_PATH = "/invalid/path";
    private static final String TEST_FILE_PATH = "src/test/resources/test_file_for_file_content_validation.txt";
    private static final String VALIDATION_RESULT_UUID = "112233-aabbcc-223344";
    private static final String FILE_UUID = "9999-aabbcc-223344";
    private static final String FILE_TYPE = "fastQ";
    private static final String NOT_SUPPORTED_FILE_TYPE = "cram";

    @Test
    public void whenFileIsNotExistsInTheProvidedPath_ThenThrowsFileNotFoundException() {

        doReturn(CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, FILE_UUID, TEST_FILE_INVALID_PATH, FILE_TYPE))
                .when(this.fileContentValidator).getCommandLineParams();
        String expectedValidationError = String.format(ErrorMessages.FILE_NOT_FOUND_BY_TARGET_PATH, TEST_FILE_INVALID_PATH);

        assertThat(fileContentValidator.validateParameters(), is(equalTo(false)));
        assertThat(fileContentValidator.getValidationError(), is(equalTo(expectedValidationError)));
    }

    @Test
    public void whenUnsupportedFileTypeProvided_ThenThrowsNotSupportedFileTypeException() {
        doReturn(CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, FILE_UUID, TEST_FILE_PATH, NOT_SUPPORTED_FILE_TYPE))
                .when(this.fileContentValidator).getCommandLineParams();

        String expectedValidationError = String.format(ErrorMessages.FILE_TYPE_NOT_SUPPORTED, NOT_SUPPORTED_FILE_TYPE);

        assertThat(fileContentValidator.validateParameters(), is(equalTo(false)));
        assertThat(fileContentValidator.getValidationError(), is(equalTo(expectedValidationError)));
    }

    @Test
    public void whenParametersValid_ThenNoErrors() {
        doReturn(CommandLineParamBuilder.build(VALIDATION_RESULT_UUID, FILE_UUID, TEST_FILE_PATH, FILE_TYPE))
                .when(this.fileContentValidator).getCommandLineParams();

        assertThat(fileContentValidator.validateParameters(), is(equalTo(true)));
    }
}
