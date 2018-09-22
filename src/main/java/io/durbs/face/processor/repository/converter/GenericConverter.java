package io.durbs.face.processor.repository.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;

@Slf4j
public class GenericConverter<T> implements AttributeConverter<T, String> {

    private ObjectMapper objectMapper = new ObjectMapper();

    private TypeReference typeReference;
    private Class clazz;

    public GenericConverter(TypeReference typeReference, Class clazz) {
        this.typeReference = typeReference;
        this.clazz = clazz;
    }

    @Override
    public String convertToDatabaseColumn(T objectToConvert) {

        final StringBuilder conversionResult = new StringBuilder();

        try {
            conversionResult.append(objectMapper.writeValueAsString(objectToConvert));
        } catch (Exception exception) {
            log.error("Unable to convert type {} to JSON", clazz.getName(), exception);
        }

        return conversionResult.toString();
    }

    @Override
    public T convertToEntityAttribute(String dbData) {

        T result = null;

        try {
            result = objectMapper.readValue(dbData, typeReference);
        } catch (Exception exception) {
            log.error("Unable to convert JSON to type {}", clazz.getName(), exception);
        }

        return result;
    }
}
