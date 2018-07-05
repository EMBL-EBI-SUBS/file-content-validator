package uk.ac.ebi.subs.filecontentvalidator.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.filecontentvalidator.exception.FileNotFoundException;
import uk.ac.ebi.subs.filecontentvalidator.exception.NotSupportedFileTypeException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.ac.ebi.subs.filecontentvalidator.exception.FileNotFoundException.FILE_NOT_FOUND_BY_TARGET_PATH;
import static uk.ac.ebi.subs.filecontentvalidator.exception.NotSupportedFileTypeException.FILE_TYPE_NOT_SUPPORTED;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FileContentValidatorTest {

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
        fileContentValidator = new FileContentValidator(FILE_UUID, TEST_FILE_INVALID_PATH, FILE_TYPE);

        this.thrown.expect(FileNotFoundException.class);
        this.thrown.expectMessage(String.format(FILE_NOT_FOUND_BY_TARGET_PATH, TEST_FILE_INVALID_PATH));

        fileContentValidator.validateParameters();
    }

    @Test
    public void whenUnsupportedFileTypeProvided_ThenThrowsNotSupportedFileTypeException() {
        fileContentValidator = new FileContentValidator(FILE_UUID, TEST_FILE_PATH, NOT_SUPPORTED_FILE_TYPE);

        this.thrown.expect(NotSupportedFileTypeException.class);
        this.thrown.expectMessage(String.format(FILE_TYPE_NOT_SUPPORTED, NOT_SUPPORTED_FILE_TYPE));

        fileContentValidator.validateParameters();
    }

    @Test
    public void whenParametersValid_ThenNoErrors() {
        fileContentValidator = new FileContentValidator(FILE_UUID, TEST_FILE_PATH, FILE_TYPE);

        assertThat(fileContentValidator.validateParameters(), is(equalTo(true)));
    }


}
