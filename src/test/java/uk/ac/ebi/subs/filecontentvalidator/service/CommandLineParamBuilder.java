package uk.ac.ebi.subs.filecontentvalidator.service;

import uk.ac.ebi.subs.filecontentvalidator.config.CommandLineParams;

public class CommandLineParamBuilder {

    public static CommandLineParams build(String fileUUID, String filePath, String fileType) {
        CommandLineParams commandLineParams = new CommandLineParams();
        commandLineParams.setFilePath(filePath);
        commandLineParams.setFileType(fileType);
        commandLineParams.setFileUuid(fileUUID);

        return commandLineParams;
    }
}
