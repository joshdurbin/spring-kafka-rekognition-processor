package io.durbs.face.processor.service.rekognition.async.handler;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.rekognition.model.SearchFacesRequest;
import com.amazonaws.services.rekognition.model.SearchFacesResult;
import io.durbs.face.processor.service.persistence.ResultPersistenceService;
import io.durbs.face.processor.stream.MessageChannels;
import io.durbs.face.processor.stream.domain.SearchRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;

@Slf4j
@AllArgsConstructor
public class SearchFacesResultHandler implements AsyncHandler<SearchFacesRequest, SearchFacesResult> {

    private final SearchRequest searchRequest;
    private final ResultPersistenceService resultPersistenceService;
    private final MessageChannels messageChannels;

    @Override
    public void onError(Exception exception) {

        log.error("an aws search processing error occurred for face id {}", searchRequest.getFaceId(), exception);

        messageChannels.searchAsyncDLQ().send(MessageBuilder
                .withPayload(searchRequest)
                .build());
    }

    @Override
    public void onSuccess(SearchFacesRequest request, SearchFacesResult searchFacesResult) {

        log.debug("handling search success response for face id {}", request.getFaceId());

        resultPersistenceService.processSearchResponse(
                searchRequest,
                searchFacesResult);
    }
}
