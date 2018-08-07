package uk.ac.ebi.subs.filecontentvalidator.validators;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.subs.filecontentvalidator.service.FileType;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ValidatorSupplier {

    @NonNull
    private FastqFileValidator fastqFileValidator;
    @NonNull
    private VcfFileValidator vcfFileValidator;
    @NonNull
    private BamCramFileValidator bamCramFileValidator;

    private Map<FileType, FileValidator> fileValidators() {
        Map<FileType, FileValidator> fileValidators = new HashMap<>();
        fileValidators.put(FileType.FASTQ, fastqFileValidator);
        fileValidators.put(FileType.VCF, vcfFileValidator);
        fileValidators.put(FileType.BAM, bamCramFileValidator);
        fileValidators.put(FileType.CRAM, bamCramFileValidator);

        return fileValidators;
    }

    public FileValidator supplyFileValidator(FileType fileType) {

        return fileValidators().get(fileType);
    }
}
