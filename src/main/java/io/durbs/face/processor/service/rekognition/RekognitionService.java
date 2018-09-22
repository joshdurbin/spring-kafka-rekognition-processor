package io.durbs.face.processor.service.rekognition;

import io.durbs.face.processor.stream.domain.IndexRequest;
import io.durbs.face.processor.stream.domain.SearchRequest;

public interface RekognitionService {

    void processIndexRequest(IndexRequest indexRequest);

    void processSearchRequest(SearchRequest searchRequest);
}
