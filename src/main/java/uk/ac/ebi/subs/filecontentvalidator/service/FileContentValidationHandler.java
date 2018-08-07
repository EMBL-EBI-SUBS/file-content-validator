package uk.ac.ebi.subs.filecontentvalidator.service;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.filecontentvalidator.config.CommandLineParams;
import uk.ac.ebi.subs.filecontentvalidator.validators.FastqFileValidator;
import uk.ac.ebi.subs.filecontentvalidator.validators.FileValidator;
import uk.ac.ebi.subs.filecontentvalidator.validators.ValidatorSupplier;
import uk.ac.ebi.subs.filecontentvalidator.validators.VcfFileValidator;
import uk.ac.ebi.subs.messaging.Exchanges;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
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

    @NonNull
    private SingleValidationResultBuilder singleValidationResultBuilder;

    @NonNull
    private ValidatorSupplier validatorSupplier;

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

        FileValidator fileValidator = validatorSupplier.supplyFileValidator(fileType);

        if (fileValidator != null) {
            return fileValidator.validateFileContent();
        }

        //default approach for unimplemented validation
        return Collections.singletonList(singleValidationResultBuilder.buildSingleValidationResultWithPassStatus());
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
