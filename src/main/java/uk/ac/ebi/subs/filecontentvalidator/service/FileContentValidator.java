package uk.ac.ebi.subs.filecontentvalidator.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.filecontentvalidator.exception.FileNotFoundException;
import uk.ac.ebi.subs.filecontentvalidator.exception.NotSupportedFileTypeException;

import java.io.File;

@RequiredArgsConstructor
public class FileContentValidator {

    @NonNull
    private String fileUuid;
    @NonNull
    private String filePath;
    @NonNull
    private String fileType;


    public boolean validateParameters() {
        validateFileExistence();

        validateFileType();

        return true;
    }

    private void validateFileExistence() {
        File file = new File(filePath);
        if(!file.exists() || file.isDirectory()) {
            throw new FileNotFoundException(filePath);
        }
    }

    private void validateFileType() {
        if (!FileType.forName(fileType)) {
            throw new NotSupportedFileTypeException(fileType);
        }
    }
}
