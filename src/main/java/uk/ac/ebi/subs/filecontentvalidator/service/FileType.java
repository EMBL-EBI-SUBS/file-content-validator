package uk.ac.ebi.subs.filecontentvalidator.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum FileType {
    FASTQ, BAM, CRAM, VCF;

    private static List<String> SUPPORTED_TYPES =
            Collections.singletonList(FileType.FASTQ.name());

    private static final Map<String, FileType> nameToValueMap = new HashMap<>();

    static {
        for (FileType value : EnumSet.allOf(FileType.class)) {
            nameToValueMap.put(value.name(), value);
        }
    }

    public static boolean forName(String name) {
        return SUPPORTED_TYPES.contains(name.toUpperCase());
    }
}
