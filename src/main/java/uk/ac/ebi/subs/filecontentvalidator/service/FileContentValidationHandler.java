package uk.ac.ebi.subs.filecontentvalidator.service;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileContentValidationHandler {

    @NonNull
    private FileContentValidator fileContentValidator;

    @NonNull
    private CommandLineParams commandLineParams;

    @NonNull
    private RabbitMessagingTemplate rabbitMessagingTemplate;

    private static final String EVENT_VALIDATION_SUCCESS = "validation.success";
    private static final String EVENT_VALIDATION_ERROR = "validation.error";
    private static final int CONTENT_VALIDATION_RESULT_VERSION = 1;

    public void handleFileContentValidation() throws IOException, InterruptedException {
        List<SingleValidationResult> singleValidationResultList = new ArrayList<>();

        singleValidationResultList.add(fileContentValidator.validateFileContent());

        SingleValidationResultsEnvelope singleValidationResultsEnvelope =
                generateSingleValidationResultsEnvelope(singleValidationResultList);

        sendValidationMessageToAggregator(singleValidationResultsEnvelope);
    }

    private SingleValidationResultsEnvelope generateSingleValidationResultsEnvelope(List<SingleValidationResult> singleValidationResults) {
        return new SingleValidationResultsEnvelope(
                singleValidationResults,
                CONTENT_VALIDATION_RESULT_VERSION,
                commandLineParams.getValidationResultUUID(),
                ValidationAuthor.Core
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
