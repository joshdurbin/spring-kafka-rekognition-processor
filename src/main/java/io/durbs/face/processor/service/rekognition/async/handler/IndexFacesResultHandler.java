package io.durbs.face.processor.service.rekognition.async.handler;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.rekognition.model.IndexFacesRequest;
import com.amazonaws.services.rekognition.model.IndexFacesResult;
import com.amazonaws.services.rekognition.model.InvalidImageFormatException;
import com.amazonaws.services.rekognition.model.InvalidParameterException;
import io.durbs.face.processor.service.persistence.ResultPersistenceService;
import io.durbs.face.processor.stream.domain.IndexRequest;
import io.durbs.face.processor.stream.MessageChannels;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;

@Slf4j
@AllArgsConstructor
public class IndexFacesResultHandler implements AsyncHandler<IndexFacesRequest, IndexFacesResult> {

    private final IndexRequest indexRequest;
    private final ResultPersistenceService resultPersistenceService;
    private final MessageChannels messageChannels;

    @Override
    public void onError(Exception exception) {

        if (exception instanceof InvalidImageFormatException) {

            log.warn("image is not in a png or jpeg format for s3 location {}", indexRequest.getS3Location());
        } else if (exception instanceof InvalidParameterException) {

            log.warn("rekognition is unable to process the image for s3 location {}", indexRequest.getS3Location());
        } else {

            log.error("an aws index processing error occurred for s3 location {}. adding to dead letter queue",
                    indexRequest.getS3Location(),
                    exception);

            messageChannels.indexAsyncDLQ().send(MessageBuilder
                    .withPayload(indexRequest)
                    .build());
        }
    }

    @Override
    public void onSuccess(IndexFacesRequest request, IndexFacesResult indexFacesResult) {

        log.debug("handling index success response for s3 location {}", indexRequest.getS3Location());

        resultPersistenceService.processIndexResponse(
                indexRequest,
                indexFacesResult);
    }
}
