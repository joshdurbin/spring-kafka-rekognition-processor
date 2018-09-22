package io.durbs.face.processor.stream.domain;

import lombok.Data;

@Data
public class IndexRequest {

    private String accountUuid;
    private String s3Location;
}
