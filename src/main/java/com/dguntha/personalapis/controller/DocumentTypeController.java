package com.dguntha.personalapis.controller;

import com.dguntha.personalapis.model.dto.CreateDocumentTypeDto;
import com.dguntha.personalapis.model.dto.DocumentType;
import com.dguntha.personalapis.model.dto.DocumentTypeDto;
import com.dguntha.personalapis.services.DocumentTypeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/document-type")
public class DocumentTypeController {

    @Autowired
    private DocumentTypeService documentTypeService;

    @PostMapping("/create")
    public ResponseEntity<DocumentTypeDto> createDocumentType(@Valid @RequestBody CreateDocumentTypeDto documentTypeDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentTypeService.createDocumentType(documentTypeDto));
    }

    @GetMapping("/list")
    public List<DocumentType> listOfDocumentTypes() {
        return  documentTypeService.getListOfDocumentTypes();
    }

    @PostMapping("/update")
    public ResponseEntity<DocumentTypeDto> updateDocumentType(@Valid @RequestBody DocumentTypeDto documentTypeDto) {
        return ResponseEntity.status(HttpStatus.OK).body(documentTypeService.updateDocumentType(documentTypeDto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteDocumentType(@PathVariable("id") @NotBlank String id) {
        return ResponseEntity.status(HttpStatus.OK).body(documentTypeService.deleteDocumentType(id));
    }

    @GetMapping("/fetch/{id}")
    public DocumentTypeDto getDocumentTypeId(@PathVariable("id") @NotBlank String id) {
        return documentTypeService.getDocumentTypeId(id);
    }

    @GetMapping("/fetch/config/{configType}")
    public List<DocumentType> fetchByConfigurationType(@PathVariable("configType") @NotBlank String configType) {
        return documentTypeService.findByConfigTypeOfDocumentTypes(configType);
    }

    @GetMapping("/fetch/meta-data/config/{type}")
    public List<DocumentTypeDto> fetchByAllConfigurationType(@PathVariable("type") @NotBlank String configType) {
        return documentTypeService.findByConfigurationType(configType);
    }
}

