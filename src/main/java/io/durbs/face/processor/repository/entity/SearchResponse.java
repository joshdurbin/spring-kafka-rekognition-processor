package io.durbs.face.processor.repository.entity;

import com.amazonaws.services.rekognition.model.FaceMatch;
import io.durbs.face.processor.repository.converter.FaceMatchConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "search_response")
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchResponse extends BaseResponseEntity {

    private String queryFaceId;

    @Convert(converter = FaceMatchConverter.class)
    private List<FaceMatch> faceMatches;

}
