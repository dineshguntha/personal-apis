package com.dguntha.personalapis.services;

import static com.dguntha.personalapis.utils.Constants.COLL_EMAIL_PROCESSOR_CONFIG;

import com.dguntha.personalapis.exception.DocumentIdNotFoundException;
import com.dguntha.personalapis.exception.DocumentNotPresentException;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EmailConfigurationService {

    private final MongoTemplate mongoTemplate;

    public EmailConfigurationService(MongoTemplate mongoTemplate) {
        this.mongoTemplate =  mongoTemplate;
    }

    public Map<String, Object>  create(Map<String, Object> data) {
        if (!data.containsKey("_id")) {
            String id = UUID.randomUUID().toString();
            data.put("_id", id);
        }
       Map<String, Object> response = mongoTemplate.save(data, COLL_EMAIL_PROCESSOR_CONFIG);
        return response;
    }

    public Map<String, Object> findOneByFieldValue( String fieldName, String fieldValue) {
        Query query = Query.query(Criteria.where(fieldName).is(fieldValue));
        Map<String, Object> document = mongoTemplate.findOne(query, Map.class, COLL_EMAIL_PROCESSOR_CONFIG);
        return document;
    }

    public List<Map> findAllDocuments() {
        return mongoTemplate.findAll(Map.class, COLL_EMAIL_PROCESSOR_CONFIG);
    }

    public boolean delete(String id) {
        Query query = Query.query(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, COLL_EMAIL_PROCESSOR_CONFIG);
        return true;
    }

    public Map<String, Object> update(@NotNull  Map<String, Object> updatedData) {

        if (!updatedData.containsKey("_id") || !(updatedData.get("_id") instanceof String))
            throw new DocumentIdNotFoundException("_id is not present for updating the document");

        String id = (String)updatedData.get("_id");
        Query query = Query.query(Criteria.where("_id").is(id));
        Map<String, Object> existingData = mongoTemplate.findOne(query, Map.class, COLL_EMAIL_PROCESSOR_CONFIG);
        if (existingData != null) {
            updatedData.put("_id", id);
            mongoTemplate.save(updatedData, COLL_EMAIL_PROCESSOR_CONFIG);
            return updatedData;
        } else {
            throw  new DocumentNotPresentException("Email configuration is not preset for this " + (String)updatedData.get("_id"));
        }
    }

}
