package uk.ac.ebi.subs.filecontentvalidator.service;

import uk.ac.ebi.subs.data.fileupload.FileStatus;
import uk.ac.ebi.subs.filecontentvalidator.exception.ErrorMessages;
import uk.ac.ebi.subs.filecontentvalidator.exception.FileNotFoundException;
import uk.ac.ebi.subs.repository.model.fileupload.File;
import uk.ac.ebi.subs.repository.repos.fileupload.FileRepository;

import java.util.Arrays;
import java.util.List;

public class FileContentValidator {

    private FileRepository fileRepository;

    private String filePath;

    private File fileToValidate;

    public FileContentValidator(FileRepository fileRepository, String filePath) {
        this.fileRepository = fileRepository;
        this.filePath = filePath;
    }

    public boolean isFileExists() {
        if (getFileToValidate() == null) {
            throw new FileNotFoundException(filePath);
        }

        return true;
    }

    private File getFileToValidate() {
        if (fileToValidate == null) {
            if (filePath == null) {
                throw new IllegalStateException(ErrorMessages.FILE_PATH_IS_EMPTY_OR_NULL);
            }
            this.fileToValidate = findFileByTargetPath();
        }

        return fileToValidate;
    }

    private File findFileByTargetPath() {
        return fileRepository.findByTargetPath(filePath);
    }

    public boolean isInValidStatusForContentValidation() {
        if (!validStatusesForContentValidation().contains(getFileToValidate().getStatus())) {
            throw new IllegalStateException(
                    String.format(ErrorMessages.FILE_IN_ILLEGAL_STATE_MESSAGE, fileToValidate.getFilename()));
        }

        return true;
    }

    private List<FileStatus> validStatusesForContentValidation() {
        return Arrays.asList(FileStatus.READY_FOR_CHECKSUM, FileStatus.READY_FOR_ARCHIVE);
    }
}
