package uk.ac.ebi.subs.filecontentvalidator.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.filecontentvalidator.config.CommandLineParams;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class VcfFileValidatorTest {

    static final String FILE_PATH = "foo/bar/testFile.vcf.gz";
    static final String FILE_TYPE = "VCF";
    static final String OUTPUT_DIR = "/fake/out";
    static final String EXE_PATH = "vcf_validator";
    CommandLineParams commandLineParams;
    VcfFileValidator vcfFileValidator;
    SingleValidationResultBuilder singleValidationResultBuilder;
    Path outputPath = Paths.get(OUTPUT_DIR);

    @Before
    public void buildUp() {
        commandLineParams = CommandLineParamBuilder.build("vr1234", "f5678", FILE_PATH, FILE_TYPE);
        singleValidationResultBuilder = new SingleValidationResultBuilder(commandLineParams);
        vcfFileValidator = new VcfFileValidator(commandLineParams, singleValidationResultBuilder);
        vcfFileValidator.setValidatorPath(EXE_PATH);
    }

    @Test
    public void test_command_line() {
        String expectedCommandLine = String.join(" ",
                EXE_PATH,
                "-i " + FILE_PATH,
                "-o " + OUTPUT_DIR,
                "-r summary"
        );
        String actualCommandLine = vcfFileValidator.vcfValidatorCommandLine(outputPath);
        assertThat(actualCommandLine, is(equalTo(expectedCommandLine)));
    }

    @Test
    public void test_file_finding() throws IOException {
        Path outputDirectory = Files.createTempDirectory("file_finder_test");
        outputDirectory.toFile().deleteOnExit();

        Path outputFilePath = outputDirectory.resolve("testFile.vcf.gz.errors_summary.1532535397331.txt");
        Files.createFile(outputFilePath);
        outputFilePath.toFile().deleteOnExit();

        File actualFile = vcfFileValidator.findOutputFile(outputDirectory);
        File expectedFile = outputFilePath.toFile();

        assertThat(actualFile, is(equalTo(expectedFile)));

        expectedFile.delete();
        outputDirectory.toFile().delete();
    }

    @Test
    public void test_outputDir_is_a_directory() throws IOException {
        Path outputDirectory = vcfFileValidator.createOutputDir();
        File outputDirAsFile = outputDirectory.toFile();

        assertTrue(outputDirAsFile.isDirectory());

    }

    @Test
    public void test_report_parsing_with_errors_and_warnings() throws FileNotFoundException {
        File summaryFile = locateResource("vcf_summary_with_errors_and_warnings.txt");

        List<SingleValidationResult> expectedResults = Arrays.asList(
                singleValidationResultBuilder.buildSingleValidationResultWithErrorStatus("Duplicated variant 8:9419327:>CT found. This occurs 2 time(s), first time in line 10000042."),
                singleValidationResultBuilder.buildSingleValidationResultWithWarningStatus("Reference and alternate alleles do not share the first nucleotide. This occurs 4 time(s), first time in line 10104086."),
                singleValidationResultBuilder.buildSingleValidationResultWithErrorStatus("Duplicated variant 9:55657600:>GC found. This occurs 2 time(s), first time in line 11743106.")

        );

        List<SingleValidationResult> actualResults = vcfFileValidator.parseOutputFile(summaryFile);
        assertThat(actualResults, is(equalTo(expectedResults)));

    }

    @Test
    public void test_report_parsing_with_no_problems() throws FileNotFoundException {
        File summaryFile = locateResource("vcf_summary_with_no_problems.txt");

        List<SingleValidationResult> expectedResults = Arrays.asList(
                singleValidationResultBuilder.buildSingleValidationResultWithPassStatus()
        );

        List<SingleValidationResult> actualResults = vcfFileValidator.parseOutputFile(summaryFile);
        assertThat(actualResults, is(equalTo(expectedResults)));
    }

    private File locateResource(String fileName) {
        String file = Thread.currentThread().getContextClassLoader().getResource(fileName).getFile();
        return new File(file);
    }
}
