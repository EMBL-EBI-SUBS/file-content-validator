package uk.ac.ebi.subs.filecontentvalidator.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.data.fileupload.FileStatus;
import uk.ac.ebi.subs.filecontentvalidator.exception.ErrorMessages;
import uk.ac.ebi.subs.filecontentvalidator.exception.FileNotFoundException;
import uk.ac.ebi.subs.repository.model.fileupload.File;
import uk.ac.ebi.subs.repository.repos.fileupload.FileRepository;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FileContentValidatorTest {

    private FileContentValidator fileContentValidator;

    @MockBean
    private FileRepository fileRepository;

    private File fileToValidate;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String TEST_FILE_TARGET_PATH = "/target/path/to/the/test/file";
    private static final String TEST_FILE_INVALID_TARGET_PATH = "/invalid/path";
    private static final String TEST_FILENAME = "test_file_for_file_content_validation.txt";
    private static final String SUBMISSION_ID = "112233-aabbcc-223344";

    @Before
    public void setup() {
        fileToValidate = createTestFile();
    }

    @Test
    public void whenFileNotExistInRepository_ThenThrowsFileNotFoundException() {
        when(fileRepository.findByTargetPath(TEST_FILE_INVALID_TARGET_PATH)).thenReturn(null);

        this.thrown.expect(FileNotFoundException.class);
        this.thrown.expectMessage(String.format(FileNotFoundException.FILE_NOT_FOUND_BY_TARGET_PATH, TEST_FILE_INVALID_TARGET_PATH));

        fileContentValidator = new FileContentValidator(fileRepository, TEST_FILE_INVALID_TARGET_PATH);
        fileContentValidator.isFileExists();
    }

    @Test
    public void whenStatusIsNotCorrectForFileContentValidation_ThenThrowsIllagelStateException() {
        fileToValidate.setStatus(FileStatus.UPLOADING);
        when(fileRepository.findByTargetPath(TEST_FILE_TARGET_PATH)).thenReturn(fileToValidate);

        this.thrown.expect(IllegalStateException.class);
        this.thrown.expectMessage(String.format(
                ErrorMessages.FILE_IN_ILLEGAL_STATE_MESSAGE, fileToValidate.getFilename()));

        fileContentValidator = new FileContentValidator(fileRepository, TEST_FILE_TARGET_PATH);
        fileContentValidator.isInValidStatusForContentValidation();
    }

    private File createTestFile() {
        File file = new File();
        file.setFilename(TEST_FILENAME);
        file.setStatus(FileStatus.READY_FOR_CHECKSUM);
        file.setSubmissionId(SUBMISSION_ID);

        ClassLoader classLoader = getClass().getClassLoader();
        String filePath = new java.io.File(classLoader.getResource(TEST_FILENAME).getFile()).getAbsolutePath();


        file.setTargetPath(filePath);

        return file;

    }

}
