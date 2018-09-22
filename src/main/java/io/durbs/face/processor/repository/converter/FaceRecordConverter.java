package io.durbs.face.processor.repository.converter;

import com.amazonaws.services.rekognition.model.FaceRecord;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.persistence.Converter;
import java.util.List;

@Converter
public class FaceRecordConverter extends GenericConverter<List<FaceRecord>> {

    public FaceRecordConverter() {
        super(new TypeReference<List<FaceRecord>>() { } , FaceRecord.class);
    }
}
