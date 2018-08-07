package uk.ac.ebi.subs.filecontentvalidator.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class ValidatorFileUtilsTest {

    @Test
    public void test_file_finding() throws IOException {
        Path outputDirectory = Files.createTempDirectory("file_finder_test");
        outputDirectory.toFile().deleteOnExit();

        Path outputFilePath = outputDirectory.resolve("testFile.vcf.gz.errors_summary.1532535397331.txt");
        Files.createFile(outputFilePath);
        outputFilePath.toFile().deleteOnExit();

        File actualFile = ValidatorFileUtils.findOutputFile(outputDirectory);
        File expectedFile = outputFilePath.toFile();

        assertThat(actualFile, is(equalTo(expectedFile)));

        expectedFile.delete();
        outputDirectory.toFile().delete();
    }

    @Test
    public void test_outputDir_is_a_directory() throws IOException {
        Path outputDirectory = ValidatorFileUtils.createOutputDir(VcfFileValidator.FOLDER_PREFIX_FOR_USI_VCF_VALIDATION);
        File outputDirAsFile = outputDirectory.toFile();

        assertTrue(outputDirAsFile.isDirectory());

    }
}
