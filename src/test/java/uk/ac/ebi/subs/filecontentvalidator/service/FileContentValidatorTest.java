package uk.ac.ebi.subs.filecontentvalidator.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.filecontentvalidator.config.CommandLineParams;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FileContentValidatorTest {

    @SpyBean
    private FileContentValidator fileContentValidator;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String TEST_FILE_PATH = "src/test/resources/test_file_for_file_content_validation.txt";
    private static final String FILE_UUID = "112233-aabbcc-223344";
    private static final String FILE_TYPE = "fastQ";

    @Test
    public void whenFileExistsButItsContentsGotError_ThenValidationResultHasError() throws IOException, InterruptedException {
        CommandLineParams commandLineParams = new CommandLineParams();
        commandLineParams.setFilePath(TEST_FILE_PATH);
        commandLineParams.setFileType(FILE_TYPE);
        commandLineParams.setFileUuid(FILE_UUID);

        doReturn(commandLineParams).when(this.fileContentValidator).getCommandLineParams();
        doReturn(new StringBuilder("ERROR: This is an error")).when(this.fileContentValidator).executeValidationAndGetResult();

        fileContentValidator.validateFileContent();

        String validationError = fileContentValidator.getValidationError();

        assertThat(validationError, not(isEmptyString()));
    }
}
