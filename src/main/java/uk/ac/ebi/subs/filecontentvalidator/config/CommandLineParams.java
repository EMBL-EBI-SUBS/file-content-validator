package uk.ac.ebi.subs.filecontentvalidator.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class CommandLineParams {

    @Value("${fileContentValidator.validationResultUUID}")
    private String validationResultUUID;
    @Value("${fileContentValidator.filePath}")
    private String filePath;
    @Value("${fileContentValidator.fileType}")
    private String fileType;
}
