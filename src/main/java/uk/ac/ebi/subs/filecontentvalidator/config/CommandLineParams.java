package uk.ac.ebi.subs.filecontentvalidator.config;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.filecontentvalidator.service.ErrorMessages;
import uk.ac.ebi.subs.filecontentvalidator.service.FileType;
import uk.ac.ebi.subs.filecontentvalidator.service.SingleValidationResultBuilder;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Configuration
@Data
public class CommandLineParams {

    @Value("${fileContentValidator.validationResultUUID}")
    private String validationResultUUID;
    @Value("${fileContentValidator.validationResultVersion}")
    private String validationResultVersion;
    @Value("${fileContentValidator.fileUUID}")
    private String fileUUID;
    @Value("${fileContentValidator.filePath}")
    private String filePath;
    @Value("${fileContentValidator.fileType}")
    private String fileType;

    public FileType getFileTypeEnum(){
        return FileType.valueOf(fileType);
    }
}
