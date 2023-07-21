package com.dguntha.personalapis.controller;

import com.dguntha.personalapis.model.dto.DocumentTypeDto;
import com.dguntha.personalapis.services.DocumentTypeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/document-type/metadata")
public class DocumentTypeMetaDataController {

    @Autowired
    DocumentTypeService documentTypeService;

    @PostMapping("/list")
    public List<DocumentTypeDto> getAllMetaData() {
        return documentTypeService.getListOfMetaData();
    }

    @PostMapping("/create")
    public DocumentTypeDto createMetaData(@Valid @RequestBody DocumentTypeDto documentTypeDto) {
        if (documentTypeDto.getMetaDataList() == null || documentTypeDto.getMetaDataList().size() == 0) {
            throw new RuntimeException("Alteast one meta data should be present ");
        }
        documentTypeDto.getMetaDataList().forEach(e -> {
            if (ObjectUtils.isNotEmpty(e.getUuid()))
                throw  new RuntimeException("Uuid should be present will creating");
        });
        return documentTypeService.createMetaData(documentTypeDto);
    }

    @PostMapping("/update")
    public DocumentTypeDto updateMetaData(@Valid @RequestBody DocumentTypeDto documentTypeDto) {
        return documentTypeService.updateMetaData(documentTypeDto);
    }

    @GetMapping("/all-metadata/{id}")
    public DocumentTypeDto getDocumentDataAndMetaDataById(@Valid @PathVariable("id") @NotBlank String id) {
        return documentTypeService.getMetaDataByDocumentTypeId(id);
    }

    /**
     *  As per request on 20230620 add path variable for document give all
     * @param name
     * @return
     */
    @GetMapping("/find/{name}")
    public DocumentTypeDto findByName(@Valid @PathVariable("name") @NotBlank String name,
                                      @RequestParam(value = "status", defaultValue = "Active") String status) {
       if (status.equalsIgnoreCase("all"))
            return documentTypeService.findDocumentTypeName(name);
       else
           return documentTypeService.findDocumentTypeNameAndStatus(name, status);
    }

    @DeleteMapping("/delete/{docId}/{metadataId}")
    private ResponseEntity<String> deleteMetaData(@PathVariable("docId") @NotBlank String docId,
                                                  @PathVariable("metadataId") @NotBlank String metadataId) {
        if (documentTypeService.deleteMetaData(docId, metadataId)) {
            return new ResponseEntity<>("MetaData deleted successfully", HttpStatus.OK);
        } else  {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    }

    //private MetaDataDto getMetaDataByUUID(@P)

}
