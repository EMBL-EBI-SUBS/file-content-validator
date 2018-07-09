package uk.ac.ebi.subs.filecontentvalidator.service;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum FileType {
    FASTQ, BAM, CRAM, VCF;

    private static List<String> NOT_YET_SUPPORTED_TYPES =
            Arrays.asList(FileType.BAM.name(), FileType.CRAM.name(), FileType.VCF.name());

    private static final Map<String, FileType> nameToValueMap = new HashMap<>();

    static {
        for (FileType value : EnumSet.allOf(FileType.class)) {
            nameToValueMap.put(value.name(), value);
        }
    }

    public static boolean forName(String name) {
        return !NOT_YET_SUPPORTED_TYPES.contains(name) && nameToValueMap.get(name.toUpperCase()) != null;
    }
}
