package io.durbs.face.processor.service.persistence;

import com.amazonaws.services.rekognition.model.IndexFacesResult;
import com.amazonaws.services.rekognition.model.SearchFacesResult;
import io.durbs.face.processor.stream.domain.IndexRequest;
import io.durbs.face.processor.stream.domain.SearchRequest;

public interface ResultPersistenceService {

    void processIndexResponse(IndexRequest indexRequest, IndexFacesResult indexFacesResult);

    void processSearchResponse(SearchRequest searchRequest, SearchFacesResult searchFacesResult);
}
