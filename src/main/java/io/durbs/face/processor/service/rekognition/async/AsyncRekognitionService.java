package io.durbs.face.processor.service.rekognition.async;

import com.amazonaws.services.rekognition.AmazonRekognitionAsync;
import com.amazonaws.services.rekognition.model.Attribute;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.IndexFacesRequest;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.SearchFacesRequest;
import com.amazonaws.services.s3.AmazonS3URI;
import io.durbs.face.processor.repository.IndexResponseRepository;
import io.durbs.face.processor.repository.SearchResponseRepository;
import io.durbs.face.processor.service.rekognition.RekognitionService;
import io.durbs.face.processor.service.persistence.ResultPersistenceService;
import io.durbs.face.processor.service.rekognition.async.handler.IndexFacesResultHandler;
import io.durbs.face.processor.service.rekognition.async.handler.SearchFacesResultHandler;
import io.durbs.face.processor.stream.domain.IndexRequest;
import io.durbs.face.processor.stream.MessageChannels;
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
@ConditionalOnProperty(name = "aws.async", havingValue = "true")
public class AsyncRekognitionService implements RekognitionService {

    @Autowired
    private AmazonRekognitionAsync amazonRekognition;

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

    @Autowired
    private MessageChannels messageChannels;

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

            val handler = new IndexFacesResultHandler(indexRequest,
                    resultPersistenceService,
                    messageChannels);

            amazonRekognition.indexFacesAsync(indexFacesRequest, handler);

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

            val handler = new SearchFacesResultHandler(searchRequest,
                    resultPersistenceService,
                    messageChannels);

            amazonRekognition.searchFacesAsync(searchFacesRequest, handler);

        } else {

            log.debug("skipping search domain for face id {} to due pre-existing results", searchRequest.getFaceId());
        }
    }
}
