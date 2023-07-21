package com.dguntha.personalapis.repository;

import com.dguntha.personalapis.model.entity.SearchConfiguration;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SearchConfigurationRepository extends MongoRepository<SearchConfiguration, String> {
    // You can add custom query methods if needed
}
