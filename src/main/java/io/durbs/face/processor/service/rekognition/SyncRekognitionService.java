package io.durbs.face.processor.service.rekognition;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.model.Attribute;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.IndexFacesRequest;
import com.amazonaws.services.rekognition.model.InvalidImageFormatException;
import com.amazonaws.services.rekognition.model.InvalidParameterException;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.SearchFacesRequest;
import com.amazonaws.services.s3.AmazonS3URI;
import io.durbs.face.processor.repository.IndexResponseRepository;
import io.durbs.face.processor.repository.SearchResponseRepository;
import io.durbs.face.processor.service.persistence.ResultPersistenceService;
import io.durbs.face.processor.stream.domain.IndexRequest;
import io.durbs.face.processor.stream.domain.SearchRequest;
import io.durbs.face.processor.stream.StreamConstants;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "aws.async", havingValue = "false", matchIfMissing = true)
public class SyncRekognitionService implements RekognitionService {

    @Autowired
    private AmazonRekognition amazonRekognition;

    @Value("${aws.rekognition.collectionId}")
    private String rekognitionCollectionId;

    @Value("${aws.rekognition.searchMaxFaces}")
    private Integer rekognitionSearchMaxFaces;

    @Value("${aws.rekognition.confidenceThreshold}")
    private Float rekognitionConfidenceThreshold;

    @Autowired
    private IndexResponseRepository indexResponseRepository;

    @Autowired
    private SearchResponseRepository searchResponseRepository;

    @Autowired
    private ResultPersistenceService resultPersistenceService;

    @StreamListener(StreamConstants.INDEX_INPUT)
    public void processIndexRequest(final IndexRequest indexRequest) {

        log.debug("processing index domain for s3 location {}", indexRequest.getS3Location());

        if (!indexResponseRepository.existsByS3Location(indexRequest.getS3Location())) {

            val s3Uri = new AmazonS3URI(indexRequest.getS3Location());

            val indexFacesRequest = new IndexFacesRequest()
                    .withCollectionId(rekognitionCollectionId)
                    .withDetectionAttributes(Attribute.ALL)
                    .withImage(new Image().withS3Object(new S3Object()
                            .withBucket(s3Uri.getBucket())
                            .withName(s3Uri.getKey())));

            try {

                resultPersistenceService.processIndexResponse(
                        indexRequest,
                        amazonRekognition.indexFaces(indexFacesRequest));

            } catch (InvalidParameterException invalidParameterException) {

                log.warn("rekognition is unable to process the image for s3 location {}", indexRequest.getS3Location());
            } catch (InvalidImageFormatException invalidImageFormatException) {

                log.warn("image is not in a png or jpeg format for s3 location {}", indexRequest.getS3Location());
            }
        } else {

            log.debug("skipping index domain for s3 location {}", indexRequest.getS3Location());
        }
    }

    @StreamListener(StreamConstants.SEARCH_INPUT)
    public void processSearchRequest(final SearchRequest searchRequest) {

        log.debug("processing search domain for face id {}", searchRequest.getFaceId());

        if (!searchResponseRepository.existsByQueryFaceId(searchRequest.getFaceId())) {

            val searchFacesRequest = new SearchFacesRequest()
                    .withCollectionId(rekognitionCollectionId)
                    .withFaceId(searchRequest.getFaceId())
                    .withFaceMatchThreshold(rekognitionConfidenceThreshold)
                    .withMaxFaces(rekognitionSearchMaxFaces);

            resultPersistenceService.processSearchResponse(
                    searchRequest,
                    amazonRekognition.searchFaces(searchFacesRequest));
        } else {

            log.debug("skipping search domain for face id {} to due pre-existing results", searchRequest.getFaceId());
        }
    }

}
