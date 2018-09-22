package io.durbs.face.processor.repository;

import io.durbs.face.processor.repository.entity.SearchResponse;
import org.springframework.data.repository.CrudRepository;

public interface SearchResponseRepository extends CrudRepository<SearchResponse, Long> {

    Boolean existsByQueryFaceId(String queryFaceId);
}
