package uk.ac.ebi.subs.filecontentvalidator.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Configuration
public class CommandLineParams {

    @Value("${fileContentValidator.fileUUID:}")
    private String fileUuid;
    @Value("${fileContentValidator.filePath:}")
    private String filePath;
    @Value("${fileContentValidator.fileType:}")
    private String fileType;
}
