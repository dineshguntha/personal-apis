package com.dguntha.personalapis.controller;

import com.dguntha.personalapis.model.entity.SearchConfiguration;
import com.dguntha.personalapis.services.SearchConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/search-configurations")
public class SearchConfigurationController {
    private final SearchConfigurationService searchConfigurationService;

    @Autowired
    public SearchConfigurationController(SearchConfigurationService searchConfigurationService) {
        this.searchConfigurationService = searchConfigurationService;
    }

    @GetMapping
    public List<SearchConfiguration> getAllSearchConfigurations() {
        return searchConfigurationService.getAllSearchConfigurations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SearchConfiguration> getSearchConfigurationById(@PathVariable("id") String id) {
        Optional<SearchConfiguration> searchConfiguration = searchConfigurationService.getSearchConfigurationById(id);
        return searchConfiguration.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SearchConfiguration> createSearchConfiguration(@RequestBody SearchConfiguration searchConfiguration) {
        SearchConfiguration createdSearchConfiguration = searchConfigurationService.createSearchConfiguration(searchConfiguration);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSearchConfiguration);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateSearchConfiguration(
            @PathVariable("id") String id,
            @RequestBody SearchConfiguration updatedSearchConfiguration) {
        searchConfigurationService.updateSearchConfiguration(id, updatedSearchConfiguration);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSearchConfiguration(@PathVariable("id") String id) {
        searchConfigurationService.deleteSearchConfiguration(id);
        return ResponseEntity.noContent().build();
    }
}
