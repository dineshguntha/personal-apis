package com.dguntha.personalapis.services;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.dguntha.personalapis.exception.DocumentIdNotFoundException;
import com.dguntha.personalapis.exception.DocumentNotPresentException;
import org.junit.jupiter.api.*;
import org.mockito.*;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

class EmailConfigurationServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private EmailConfigurationService emailConfigurationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreate() {
        // Prepare data
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test Configuration");

        // Set up mock behavior
        when(mongoTemplate.save(eq(data), anyString())).thenReturn(data);

        // Call the method
        Map<String, Object> result = emailConfigurationService.create(data);

        // Verify the interactions and assertions
        verify(mongoTemplate).save(eq(data), anyString());
        Assertions.assertEquals(data, result);
    }

    @Test
    void testFindOneByFieldValue() {
        // Prepare data
        String fieldName = "name";
        String fieldValue = "Test Configuration";
        Map<String, Object> document = new HashMap<>();
        document.put("_id", "123");
        document.put(fieldName, fieldValue);

        // Set up mock behavior
        when(mongoTemplate.findOne(any(Query.class), eq(Map.class), anyString())).thenReturn(document);

        // Call the method
        Map<String, Object> result = emailConfigurationService.findOneByFieldValue(fieldName, fieldValue);

        // Verify the interactions and assertions
        verify(mongoTemplate).findOne(any(Query.class), eq(Map.class), anyString());
        Assertions.assertEquals(document, result);
    }

    @Test
    void testFindAllDocuments() {
        // Prepare data
        List<Map> documents = new ArrayList<>();
        Map<String, Object> document1 = new HashMap<>();
        document1.put("_id", "123");
        document1.put("name", "Configuration 1");
        Map<String, Object> document2 = new HashMap<>();
        document2.put("_id", "456");
        document2.put("name", "Configuration 2");
        documents.add(document1);
        documents.add(document2);

        // Set up mock behavior
        when(mongoTemplate.findAll(eq(Map.class), anyString())).thenReturn(documents);

        // Call the method
        List<Map> result = emailConfigurationService.findAllDocuments();

        // Verify the interactions and assertions
        verify(mongoTemplate).findAll(eq(Map.class), anyString());
        Assertions.assertEquals(documents, result);
    }

    @Test
    void testDelete() {
        // Prepare data
        String id = "123";

        // Call the method
        emailConfigurationService.delete(id);

        // Verify the interactions
        verify(mongoTemplate).remove(any(Query.class), anyString());
    }

    @Test
    void testUpdate_ValidData() {
        // Prepare data
        String id = "123";
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("_id", id);
        updatedData.put("name", "Updated Configuration");

        // Set up mock behavior
        Query query = Query.query(Criteria.where("_id").is(id));
        Map<String, Object> existingData = new HashMap<>();
        existingData.put("_id", id);
        existingData.put("name", "Old Configuration");
        when(mongoTemplate.findOne(eq(query), eq(Map.class), anyString())).thenReturn(existingData);
        when(mongoTemplate.save(eq(updatedData), anyString())).thenReturn(updatedData);

        // Call the method
        Map<String, Object> result = emailConfigurationService.update(updatedData);

        // Verify the interactions and assertions
        verify(mongoTemplate).findOne(eq(query), eq(Map.class), anyString());
        verify(mongoTemplate).save(eq(updatedData), anyString());
        Assertions.assertEquals(updatedData, result);
    }

    @Test
    void testUpdate_InvalidData() {
        // Prepare data
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", "Updated Configuration");

        // Call the method and verify the exception
        Assertions.assertThrows(DocumentIdNotFoundException.class, () -> {
            emailConfigurationService.update(updatedData);
        });
    }

    @Test
    void testUpdate_DocumentNotFound() {
        // Prepare data
        String id = "123";
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("_id", id);
        updatedData.put("name", "Updated Configuration");

        // Set up mock behavior
        Query query = Query.query(Criteria.where("_id").is(id));
        when(mongoTemplate.findOne(eq(query), eq(Map.class), anyString())).thenReturn(null);

        // Call the method and verify the exception
        Assertions.assertThrows(DocumentNotPresentException.class, () -> {
            emailConfigurationService.update(updatedData);
        });
    }
}
