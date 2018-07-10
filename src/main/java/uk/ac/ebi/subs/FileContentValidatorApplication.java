package uk.ac.ebi.subs;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.context.ConfigurableApplicationContext;
import uk.ac.ebi.subs.filecontentvalidator.service.FileContentValidationHandler;

/**
 * This will be a command line Spring Boot application for validating the content of a given file.
 */
@SpringBootApplication
@RequiredArgsConstructor
public class FileContentValidatorApplication implements ApplicationRunner {

	@NonNull
	private FileContentValidationHandler fileContentValidationHandler;

	private static final String FILE_PATH_OPTION = "fileContentValidator.filePath";

	private static final Logger LOGGER = LoggerFactory.getLogger(FileContentValidatorApplication.class);

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(FileContentValidatorApplication.class);
		springApplication.setWebEnvironment(false);
		ApplicationPidFileWriter applicationPidFileWriter = new ApplicationPidFileWriter();
		springApplication.addListeners( applicationPidFileWriter );
		ConfigurableApplicationContext ctx = springApplication.run(args);

		SpringApplication.exit(ctx, () -> 0);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		LOGGER.info("FileContentValidatorApplication started executing.");

		if (!args.getOptionNames().isEmpty()) {
			LOGGER.info("File content validation started for file: {}", args.getOptionValues(FILE_PATH_OPTION));

			fileContentValidationHandler.handleFileContentValidation();
		}
	}
}
