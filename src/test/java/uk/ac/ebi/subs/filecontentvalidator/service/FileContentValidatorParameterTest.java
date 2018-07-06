package uk.ac.ebi.subs.filecontentvalidator.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.filecontentvalidator.exception.FileNotFoundException;
import uk.ac.ebi.subs.filecontentvalidator.exception.NotSupportedFileTypeException;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static uk.ac.ebi.subs.filecontentvalidator.exception.FileNotFoundException.FILE_NOT_FOUND_BY_TARGET_PATH;
import static uk.ac.ebi.subs.filecontentvalidator.exception.NotSupportedFileTypeException.FILE_TYPE_NOT_SUPPORTED;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FileContentValidatorParameterTest {

    @SpyBean
    private FileContentValidator fileContentValidator;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String TEST_FILE_INVALID_PATH = "/invalid/path";
    private static final String TEST_FILE_PATH = "src/test/resources/test_file_for_file_content_validation.txt";
    private static final String FILE_UUID = "112233-aabbcc-223344";
    private static final String FILE_TYPE = "fastQ";
    private static final String NOT_SUPPORTED_FILE_TYPE = "text file";

    @Test
    public void whenFileIsNotExistsInTheProvidedPath_ThenThrowsFileNotFoundException() {

        doReturn(CommandLineParamBuilder.build(FILE_UUID, TEST_FILE_INVALID_PATH, FILE_TYPE))
                .when(this.fileContentValidator).getCommandLineParams();

        this.thrown.expect(FileNotFoundException.class);
        this.thrown.expectMessage(String.format(FILE_NOT_FOUND_BY_TARGET_PATH, TEST_FILE_INVALID_PATH));

        fileContentValidator.validateParameters();
    }

    @Test
    public void whenUnsupportedFileTypeProvided_ThenThrowsNotSupportedFileTypeException() {
        doReturn(CommandLineParamBuilder.build(FILE_UUID, TEST_FILE_PATH, NOT_SUPPORTED_FILE_TYPE))
                .when(this.fileContentValidator).getCommandLineParams();

        this.thrown.expect(NotSupportedFileTypeException.class);
        this.thrown.expectMessage(String.format(FILE_TYPE_NOT_SUPPORTED, NOT_SUPPORTED_FILE_TYPE));

        fileContentValidator.validateParameters();
    }

    @Test
    public void whenParametersValid_ThenNoErrors() {
        doReturn(CommandLineParamBuilder.build(FILE_UUID, TEST_FILE_PATH, FILE_TYPE))
                .when(this.fileContentValidator).getCommandLineParams();

        assertThat(fileContentValidator.validateParameters(), is(equalTo(true)));
    }
}
