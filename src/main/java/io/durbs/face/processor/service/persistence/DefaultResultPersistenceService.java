package io.durbs.face.processor.service.persistence;

import com.amazonaws.services.rekognition.model.IndexFacesResult;
import com.amazonaws.services.rekognition.model.SearchFacesResult;
import com.google.common.collect.Iterables;
import io.durbs.face.processor.repository.IndexResponseRepository;
import io.durbs.face.processor.repository.SearchResponseRepository;
import io.durbs.face.processor.repository.entity.IndexResponse;
import io.durbs.face.processor.repository.entity.SearchResponse;
import io.durbs.face.processor.stream.domain.CrossReferencedSearchResponse;
import io.durbs.face.processor.stream.domain.IndexRequest;
import io.durbs.face.processor.stream.MessageChannels;
import io.durbs.face.processor.stream.domain.SearchRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DefaultResultPersistenceService implements ResultPersistenceService {

    @Autowired
    private MessageChannels messageChannels;

    @Autowired
    private IndexResponseRepository indexResponseRepository;

    @Autowired
    private SearchResponseRepository searchResponseRepository;

    public void processIndexResponse(final IndexRequest indexRequest,
                                     final IndexFacesResult indexFacesResult) {

        if (indexFacesResult.getFaceRecords().isEmpty()) {

            log.debug("index faces domain for s3 location {} returned no faces", indexRequest.getS3Location());

        } else if (indexFacesResult.getFaceRecords().size() == 1) {

            log.info("index faces domain for s3 location {} returned a single face with confidence {}",
                    indexRequest.getS3Location(),
                    Iterables.getFirst(indexFacesResult.getFaceRecords(), null).getFaceDetail().getConfidence());
        } else {

            val doubleStream = indexFacesResult.getFaceRecords()
                    .stream()
                    .mapToDouble(record -> record.getFaceDetail().getConfidence().doubleValue());

            log.info("index faces domain for s3 location {} returned {} faces confidences min {}, avg {}, max {}",
                    indexRequest.getS3Location(),
                    indexFacesResult.getFaceRecords().size(),
                    doubleStream.min(),
                    doubleStream.average(),
                    doubleStream.max());
        }

        if (!indexFacesResult.getFaceRecords().isEmpty()) {

            if (!indexResponseRepository.existsByS3Location(indexRequest.getS3Location())) {

                val largestFace = indexFacesResult
                        .getFaceRecords()
                        .stream()
                        .max((faceA, faceB) -> {

                            val faceASize = faceA.getFace().getBoundingBox().getWidth() * faceA.getFace().getBoundingBox().getHeight();
                            val faceBSize = faceB.getFace().getBoundingBox().getWidth() * faceB.getFace().getBoundingBox().getHeight();

                            return Float.compare(faceASize, faceBSize);
                        }).get();

                val faceIndexResult = new IndexResponse();
                faceIndexResult.setS3Location(indexRequest.getS3Location());
                faceIndexResult.setLargestFaceId(largestFace.getFace().getFaceId());
                faceIndexResult.setFaceRecords(indexFacesResult.getFaceRecords());
                faceIndexResult.setImageId(largestFace.getFace().getImageId());
                faceIndexResult.setNumberOfDetectedFaces(indexFacesResult.getFaceRecords().size());

                indexResponseRepository.save(faceIndexResult);

                val searchRequest = new SearchRequest(largestFace.getFace().getFaceId(), indexRequest);

                messageChannels.searchInput().send(MessageBuilder.withPayload(searchRequest).build());

            } else {

                log.debug("skipping persistence for s3 location {}", indexRequest.getS3Location());
            }
        }
    }

    public void processSearchResponse(final SearchRequest searchRequest,
                                      final SearchFacesResult searchFacesResult) {

        if (searchFacesResult.getFaceMatches().isEmpty()) {

            log.debug("search faces found no faces for face id {}", searchRequest.getFaceId());

        } else if (searchFacesResult.getFaceMatches().size() == 1) {

            log.debug("search faces found a single face for face id {}, with similarity {}",
                    searchRequest.getFaceId(),
                    Iterables.getFirst(searchFacesResult.getFaceMatches(), null).getSimilarity());

        } else {

            val doubleStream = searchFacesResult.getFaceMatches()
                    .stream()
                    .mapToDouble(record -> record.getSimilarity().doubleValue());

            log.info("search faces found {} faces for face id {} with similarities min {}, avg {}, max {}",
                    searchFacesResult.getFaceMatches().size(),
                    searchRequest.getFaceId(),
                    doubleStream.min(),
                    doubleStream.average(),
                    doubleStream.max());
        }

        if (!searchFacesResult.getFaceMatches().isEmpty()) {

            if (!searchResponseRepository.existsByQueryFaceId(searchRequest.getFaceId())) {

                val faceSearchResult = new SearchResponse();
                faceSearchResult.setFaceMatches(searchFacesResult.getFaceMatches());
                faceSearchResult.setQueryFaceId(searchRequest.getFaceId());

                searchResponseRepository.save(faceSearchResult);

                val responses = searchFacesResult
                        .getFaceMatches()
                        .stream()
                        .map(awsFaceMatch -> {

                            val matchingImageId = awsFaceMatch.getFace().getImageId();
                            val faceIndexResultOptional = indexResponseRepository.findByLargestFaceId(matchingImageId);

                            final CrossReferencedSearchResponse crossReferencedSearchResponse;

                            if (faceIndexResultOptional.isPresent()) {

                                val faceIndexResult = faceIndexResultOptional.get();

                                crossReferencedSearchResponse = new CrossReferencedSearchResponse();
                                crossReferencedSearchResponse.setSearchAccountUuid(searchRequest.getIndexRequest().getAccountUuid());
                                crossReferencedSearchResponse.setSearchFaceId(searchRequest.getFaceId());
                                crossReferencedSearchResponse.setMatchAccountUuid(faceIndexResult.getAccountUuid());
                                crossReferencedSearchResponse.setMatchFaceId(awsFaceMatch.getFace().getFaceId());
                                crossReferencedSearchResponse.setMatchConfidence(awsFaceMatch.getSimilarity());

                            } else {

                                crossReferencedSearchResponse = null;

                                log.warn("face match for query face id {} not found in db", searchRequest.getFaceId());
                            }

                            return crossReferencedSearchResponse;

                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                messageChannels.searchOutput().send(MessageBuilder.withPayload(responses).build());

            } else {

                log.debug("skipping persistence for face id {}", searchRequest.getFaceId());
            }
        }
    }
}
