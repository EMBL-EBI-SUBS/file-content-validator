package uk.ac.ebi.subs.filecontentvalidator.exception;

public class FileNotFoundException extends RuntimeException {

    public static final String FILE_NOT_FOUND_BY_TARGET_PATH = "File not found with target path: %s";

    public FileNotFoundException(String filePath) {
        super(String.format(FILE_NOT_FOUND_BY_TARGET_PATH, filePath));
    }
}
