package com.dguntha.personalapis.repository;

import com.dguntha.personalapis.model.entity.JobLogEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobLogRepository extends MongoRepository<JobLogEntity, String> {
}
