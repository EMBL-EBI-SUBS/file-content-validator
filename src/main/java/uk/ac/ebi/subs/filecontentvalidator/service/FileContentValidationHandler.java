package uk.ac.ebi.subs.filecontentvalidator.service;

import com.rabbitmq.client.Command;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.filecontentvalidator.config.CommandLineParams;
import uk.ac.ebi.subs.messaging.Exchanges;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Getter
@RequiredArgsConstructor
public class FileContentValidationHandler {

    @NonNull
    private FastqFileValidator fastqFileValidator;

    @NonNull
    private VcfFileValidator vcfFileValidator;

    @NonNull
    private CommandLineParams commandLineParams;

    @NonNull
    private CommandLineParamChecker commandLineParamChecker;

    @NonNull
    private RabbitMessagingTemplate rabbitMessagingTemplate;

    private static final String EVENT_VALIDATION_SUCCESS = "validation.success";
    private static final String EVENT_VALIDATION_ERROR = "validation.error";

    public void handleFileContentValidation() throws IOException, InterruptedException {
        List<SingleValidationResult> singleValidationResultList = commandLineParamChecker.validateParameters(commandLineParams);

        if (singleValidationResultList.isEmpty()){
            singleValidationResultList = doValidation();
        }


        SingleValidationResultsEnvelope singleValidationResultsEnvelope =
                generateSingleValidationResultsEnvelope(singleValidationResultList);

        sendValidationMessageToAggregator(singleValidationResultsEnvelope);
    }

    private List<SingleValidationResult> doValidation() throws IOException, InterruptedException {
        FileType fileType = commandLineParams.getFileTypeEnum();

        if (fileType == FileType.FASTQ){
            return fastqFileValidator.validateFileContent();
        }
        if (fileType == FileType.VCF){
            return vcfFileValidator.validateFileContent();
        }

        throw new IllegalArgumentException("we have not implemented a handler for "+fileType);
    }

    private SingleValidationResultsEnvelope generateSingleValidationResultsEnvelope(List<SingleValidationResult> singleValidationResults) {
        return new SingleValidationResultsEnvelope(
                singleValidationResults,
                Integer.parseInt(commandLineParams.getValidationResultVersion()),
                commandLineParams.getValidationResultUUID(),
                ValidationAuthor.FileContent
        );
    }

    private void sendValidationMessageToAggregator(SingleValidationResultsEnvelope envelope) {
        List<SingleValidationResult> errorResults = envelope.getSingleValidationResults().stream().filter(svr -> svr.getValidationStatus().equals(SingleValidationResultStatus.Error)).collect(Collectors.toList());
        if (errorResults.size() > 0) {
            rabbitMessagingTemplate.convertAndSend(Exchanges.SUBMISSIONS, EVENT_VALIDATION_ERROR, envelope);
        } else {
            rabbitMessagingTemplate.convertAndSend(Exchanges.SUBMISSIONS, EVENT_VALIDATION_SUCCESS, envelope);
        }
    }



}
