package uk.ac.ebi.subs.filecontentvalidator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(value = {"fileContentValidator.validationResultUUID:1111",
						"fileContentValidator.validationResultVersion:11",
						"fileContentValidator.fileUUID:2222",
						"fileContentValidator.filePath:src/test/resources/test_file_for_file_content_validation.txt",
						"fileContentValidator.fileType:fastQ"})
public class FileContentValidatorApplicationTests {

	@Autowired
	ApplicationContext ctx;

	@Test
	public void contextLoads() throws Exception {
	}

}
