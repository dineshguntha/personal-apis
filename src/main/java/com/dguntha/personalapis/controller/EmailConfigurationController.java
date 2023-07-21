package com.dguntha.personalapis.controller;

import com.dguntha.personalapis.services.EmailConfigurationService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/email-config")
public class EmailConfigurationController {

    @Autowired
    private EmailConfigurationService emailConfigurationService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody @NotNull Map<String, Object> data) {

            return ResponseEntity.status(HttpStatus.CREATED).body(emailConfigurationService.create(data));
    }

    @GetMapping("/list")
    public ResponseEntity<List<Map>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(emailConfigurationService.findAllDocuments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable("id") @NotBlank String id) {
        return ResponseEntity.status(HttpStatus.OK).body(emailConfigurationService.findOneByFieldValue("_id", id));
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> update( @RequestBody @NotNull Map<String, Object> data) {
        Map<String, Object> updatedConfig = emailConfigurationService.update( data);
        if (updatedConfig != null) {
            return ResponseEntity.status(HttpStatus.OK).body(updatedConfig);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") @NotBlank String id) {
        boolean deleted = emailConfigurationService.delete(id);
        if (deleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Successfully deleted");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unable to delete");
        }
    }
}
