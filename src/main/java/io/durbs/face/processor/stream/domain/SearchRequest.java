package io.durbs.face.processor.stream.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchRequest {

    private String faceId;
    private IndexRequest indexRequest;
}
