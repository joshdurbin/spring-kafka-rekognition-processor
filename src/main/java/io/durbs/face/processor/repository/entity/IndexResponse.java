package io.durbs.face.processor.repository.entity;

import com.amazonaws.services.rekognition.model.FaceRecord;
import io.durbs.face.processor.repository.converter.FaceRecordConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "index_response")
@Data
@EqualsAndHashCode(callSuper = true)
public class IndexResponse extends BaseResponseEntity {

    private String s3Location;
    private String accountUuid;
    private String largestFaceId;
    private String imageId;
    private Integer numberOfDetectedFaces;

    @Convert(converter = FaceRecordConverter.class)
    private List<FaceRecord> faceRecords;

}
