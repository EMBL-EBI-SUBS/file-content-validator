package uk.ac.ebi.subs.filecontentvalidator.exception;

public class NotSupportedFileTypeException extends RuntimeException {

    public static final String FILE_TYPE_NOT_SUPPORTED = "File type is not supported: %s";

    public NotSupportedFileTypeException(String message) {
        super(String.format(FILE_TYPE_NOT_SUPPORTED, message));
    }
}
