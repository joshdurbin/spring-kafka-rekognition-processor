package io.durbs.face.processor.repository;

import io.durbs.face.processor.repository.entity.IndexResponse;
import io.durbs.face.processor.repository.entity.ReducedIndexResponse;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface IndexResponseRepository extends CrudRepository<IndexResponse, Long> {

    Boolean existsByS3Location(String s3Location);

    Optional<ReducedIndexResponse> findByLargestFaceId(String largestFaceId);
 }
