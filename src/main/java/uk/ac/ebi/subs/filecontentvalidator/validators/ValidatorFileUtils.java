package uk.ac.ebi.subs.filecontentvalidator.validators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;

class ValidatorFileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidatorFileUtils.class);

    static Path createOutputDir(String folderPrefic) throws IOException {
        Path tempDirectory = Files.createTempDirectory(folderPrefic);
        PosixFileAttributeView attributes = Files.getFileAttributeView(tempDirectory, PosixFileAttributeView.class);
        LOGGER.info("created dir: {} with attributes: {}",tempDirectory,attributes.readAttributes().permissions());
        tempDirectory.toFile().deleteOnExit();
        return tempDirectory;
    }

    static File findOutputFile(Path outputDirectory) {
        File[] outputFiles = outputDirectory.toFile().listFiles();
        LOGGER.info("contents of output dir: {}", outputFiles);
        if (outputFiles == null || outputFiles.length != 1) {
            throw new IllegalStateException();
        }

        return outputFiles[0];
    }
}
