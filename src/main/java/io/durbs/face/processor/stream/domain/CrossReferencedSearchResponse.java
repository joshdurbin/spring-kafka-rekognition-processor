package io.durbs.face.processor.stream.domain;

import lombok.Data;

@Data
public class CrossReferencedSearchResponse {

    String searchAccountUuid;
    String searchFaceId;
    String matchAccountUuid;
    String matchFaceId;
    Float matchConfidence;
}
