package com.dguntha.personalapis.repository;

import com.dguntha.personalapis.model.entity.DocumentTypeEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentTypeRepository extends MongoRepository<DocumentTypeEntity, String> {

    @Query(value = "{ }", fields = "{'name': 1, 'description': 1, 'uniqueName': 1}")
    List<DocumentTypeEntity> getDocumentTypeExcludeMetaData(Sort sort);

    List<DocumentTypeEntity> findByConfigType(String configName);

    Optional<DocumentTypeEntity> findByName(String name);

    @Query("{'name': ?0, 'metaDataDetails.status': ?1}")
    Optional<DocumentTypeEntity> findNameAndMetaDetailsStatus(String name, String status);

}
