package com.dguntha.personalapis.controller;

import com.dguntha.personalapis.services.EmailConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class EmailConfigurationControllerTest {

    @Mock
    private EmailConfigurationService emailConfigurationService;

    @InjectMocks
    private EmailConfigurationController emailConfigurationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_ShouldReturnCreatedStatusAndResponseBody() {
        // Prepare test data
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("key1", "value1");
        requestData.put("key2", "value2");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Email configuration created successfully");

        when(emailConfigurationService.create(requestData)).thenReturn(response);

        // Perform the test
        ResponseEntity<Map<String, Object>> result = emailConfigurationController.create(requestData);

        // Verify the result
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(emailConfigurationService, times(1)).create(requestData);
    }

    @Test
    void findAll_ShouldReturnOkStatusAndResponseBody() {
        // Prepare test data
        List<Map> emailConfigurations = List.of(
                Map.of("key1", "value1"),
                Map.of("key2", "value2")
        );

        when(emailConfigurationService.findAllDocuments()).thenReturn(emailConfigurations);

        // Perform the test
        ResponseEntity<List<Map>> result = emailConfigurationController.findAll();

        // Verify the result
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(emailConfigurations, result.getBody());
        verify(emailConfigurationService, times(1)).findAllDocuments();
    }

    @Test
    void findById_WithValidId_ShouldReturnOkStatusAndResponseBody() {
        // Prepare test data
        String id = "12345";
        Map<String, Object> emailConfiguration = Map.of("key1", "value1");

        when(emailConfigurationService.findOneByFieldValue("_id", id)).thenReturn(emailConfiguration);

        // Perform the test
        ResponseEntity<Map<String, Object>> result = emailConfigurationController.findById(id);

        // Verify the result
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(emailConfiguration, result.getBody());
        verify(emailConfigurationService, times(1)).findOneByFieldValue("_id", id);
    }

    @Test
    void update_WithValidData_ShouldReturnOkStatusAndResponseBody() {
        // Prepare test data
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("key1", "value1");
        requestData.put("key2", "value2");

        Map<String, Object> updatedConfiguration = new HashMap<>();
        updatedConfiguration.put("key1", "updatedValue1");
        updatedConfiguration.put("key2", "updatedValue2");

        when(emailConfigurationService.update(requestData)).thenReturn(updatedConfiguration);

        // Perform the test
        ResponseEntity<Map<String, Object>> result = emailConfigurationController.update(requestData);

        // Verify the result
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(updatedConfiguration, result.getBody());
        verify(emailConfigurationService, times(1)).update(requestData);
    }

    @Test
    void update_WithInvalidData_ShouldReturnNotFoundStatus() {
        // Prepare test data
        Map<String, Object> requestData = new HashMap<>();

        when(emailConfigurationService.update(requestData)).thenReturn(null);

        // Perform the test
        ResponseEntity<Map<String, Object>> result = emailConfigurationController.update(requestData);

        // Verify the result
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(emailConfigurationService, times(1)).update(requestData);
    }

    @Test
    void delete_WithValidId_ShouldReturnNoContentStatusAndSuccessMessage() {
        // Prepare test data
        String id = "12345";

        when(emailConfigurationService.delete(id)).thenReturn(true);

        // Perform the test
        ResponseEntity<String> result = emailConfigurationController.delete(id);

        // Verify the result
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertEquals("Successfully deleted", result.getBody());
        verify(emailConfigurationService, times(1)).delete(id);
    }

    @Test
    void delete_WithInvalidId_ShouldReturnNotFoundStatusAndErrorMessage() {
        // Prepare test data
        String id = "12345";

        when(emailConfigurationService.delete(id)).thenReturn(false);

        // Perform the test
        ResponseEntity<String> result = emailConfigurationController.delete(id);

        // Verify the result
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Unable to delete", result.getBody());
        verify(emailConfigurationService, times(1)).delete(id);
    }
}
