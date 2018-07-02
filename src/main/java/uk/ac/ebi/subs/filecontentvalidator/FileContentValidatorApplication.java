package uk.ac.ebi.subs.filecontentvalidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ConfigurableApplicationContext;
import uk.ac.ebi.subs.repository.repos.fileupload.FileRepository;

/**
 * This will be a command line Spring Boot application for validating the content of a given file.
 */
@SpringBootApplication
public class FileContentValidatorApplication implements CommandLineRunner {

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
	public void run(String... args) throws Exception {
		LOGGER.info("FileContentValidatorApplication started executing.");
		if (args.length > 0) {
			String fileId = args[0];
			LOGGER.info("Checksum calculation started fro file id: {}", fileId);

		}
	}
}
