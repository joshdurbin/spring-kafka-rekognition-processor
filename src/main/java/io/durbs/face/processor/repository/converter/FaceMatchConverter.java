package io.durbs.face.processor.repository.converter;

import com.amazonaws.services.rekognition.model.FaceMatch;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.persistence.Converter;
import java.util.List;

@Converter
public class FaceMatchConverter extends GenericConverter<FaceMatch> {

    public FaceMatchConverter() {
        super(new TypeReference<List<FaceMatch>>() { } , FaceMatch.class);
    }
}
