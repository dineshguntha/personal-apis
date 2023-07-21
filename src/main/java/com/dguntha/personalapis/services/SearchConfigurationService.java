package com.dguntha.personalapis.services;

import com.dguntha.personalapis.repository.SearchConfigurationRepository;
import com.dguntha.personalapis.model.entity.SearchConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SearchConfigurationService {
    private final SearchConfigurationRepository repository;

    @Autowired
    public SearchConfigurationService(SearchConfigurationRepository repository) {
        this.repository = repository;
    }

    public List<SearchConfiguration> getAllSearchConfigurations() {
        return repository.findAll();
    }

    public Optional<SearchConfiguration> getSearchConfigurationById(String id) {
        return repository.findById(id);
    }

    public SearchConfiguration createSearchConfiguration(SearchConfiguration searchConfiguration) {
        // Generate a unique ID if id is not provided
        if (searchConfiguration.getId() == null || searchConfiguration.getId().isEmpty()) {
            searchConfiguration.setId(UUID.randomUUID().toString());
        }
        return repository.save(searchConfiguration);
    }

    public void updateSearchConfiguration(String id, SearchConfiguration updatedSearchConfiguration) {
        Optional<SearchConfiguration> existingSearchConfiguration = repository.findById(id);
        existingSearchConfiguration.ifPresent(searchConfiguration -> {
            searchConfiguration.setSearchName(updatedSearchConfiguration.getSearchName());
            searchConfiguration.setCreateBy(updatedSearchConfiguration.getCreateBy());
            searchConfiguration.setCreateDate(updatedSearchConfiguration.getCreateDate());
            searchConfiguration.setSearchCriteria(updatedSearchConfiguration.getSearchCriteria());
            repository.save(searchConfiguration);
        });
    }

    public void deleteSearchConfiguration(String id) {
        repository.deleteById(id);
    }
}
