package com.dguntha.personalapis.services;

import com.dguntha.personalapis.exception.DocumentNotPresentException;
import com.dguntha.personalapis.model.dto.*;
import com.dguntha.personalapis.repository.DocumentTypeRepository;
import com.itconvergence.docflowadminapi.model.dto.*;
import com.dguntha.personalapis.model.entity.DocumentTypeEntity;
import com.dguntha.personalapis.model.entity.MetaDataDetails;
import com.dguntha.personalapis.model.entity.WorkflowEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocumentTypeService {

    @Autowired
    DocumentTypeRepository documentTypeRepository;


    public DocumentTypeDto createDocumentType(CreateDocumentTypeDto documentTypeDto) {
        DocumentTypeEntity documentTypeEntity = new DocumentTypeEntity();
        BeanUtils.copyProperties(documentTypeDto, documentTypeEntity);
        documentTypeEntity.setUniqueName(documentTypeDto.getName().toLowerCase().replaceAll(" ", "_"));
        log.info("Document created with name : {}", documentTypeDto.getName());
        documentTypeRepository.save(documentTypeEntity);
        DocumentTypeDto documentTypeDto1 = new DocumentTypeDto();
        BeanUtils.copyProperties(documentTypeEntity, documentTypeDto1);
        return documentTypeDto1;
    }

    public DocumentTypeDto updateDocumentType(DocumentTypeDto documentTypeDto) {
           DocumentTypeEntity documentTypeEntity = fetchDocumentTypeEntity(documentTypeDto);
           documentTypeEntity.setName(documentTypeDto.getName());
           documentTypeEntity.setDescription(documentTypeDto.getDescription());
           documentTypeEntity.setOcrThreshold(documentTypeDto.getOcrThreshold());
            copyWorkflowEntity(documentTypeDto, documentTypeEntity);
            log.info("Document updated with name : {}", documentTypeDto.getId());
            documentTypeRepository.save(documentTypeEntity);

            DocumentTypeDto documentTypeDto1 = new DocumentTypeDto();
            BeanUtils.copyProperties(documentTypeEntity, documentTypeDto1);
            copyWorkflowDto(documentTypeDto1, documentTypeEntity);

            return  documentTypeDto1;

    }

    private void copyWorkflowEntity(DocumentTypeDto documentTypeDto, DocumentTypeEntity documentTypeEntity) {
        if (documentTypeDto.getWorkflow() != null && !StringUtils.isEmpty(documentTypeDto.getWorkflow().getDefinitionId())) {
            WorkflowEntity workflowEntity = new WorkflowEntity();
            BeanUtils.copyProperties( documentTypeDto.getWorkflow(), workflowEntity);
            documentTypeEntity.setWorkflow(workflowEntity);
        }
    }

    private void copyWorkflowDto(DocumentTypeDto documentTypeDto, DocumentTypeEntity documentTypeEntity) {
        if (documentTypeEntity.getWorkflow() != null && !StringUtils.isEmpty(documentTypeEntity.getWorkflow().getDefinitionId())) {
            WorkflowDto workflowDto = new WorkflowDto();
            BeanUtils.copyProperties( documentTypeEntity.getWorkflow(), workflowDto);
            documentTypeDto.setWorkflow(workflowDto);
        }
    }

    public String deleteDocumentType(String id) {
        DocumentTypeDto documentTypeDto = new DocumentTypeDto();
        documentTypeDto.setId(id);
        DocumentTypeEntity documentTypeEntity = fetchDocumentTypeEntity(documentTypeDto);
        documentTypeRepository.delete(documentTypeEntity);
        log.info("Deleted document type : {} ", id);
        return "Successfully deleted";
    }

    public boolean deleteMetaData(String docId, String metadataId) {
        DocumentTypeDto documentTypeDto = new DocumentTypeDto();
        documentTypeDto.setId(docId);
        DocumentTypeEntity documentTypeEntity = fetchDocumentTypeEntity(documentTypeDto);
        List<MetaDataDetails> metaDataDetails  = documentTypeEntity.getMetaDataDetails();
        boolean isDelete = false;
        List<MetaDataDetails> newMetaDataDetails = new ArrayList<>();
        for (MetaDataDetails e : metaDataDetails){
            if (e.getUuid().equalsIgnoreCase(metadataId)){
                isDelete = true;
            } else {
                newMetaDataDetails.add(e);
            }
        }
        documentTypeEntity.setMetaDataDetails(newMetaDataDetails);
        documentTypeRepository.save(documentTypeEntity);
        return isDelete;
    }

    public DocumentTypeDto getDocumentTypeId(String id) {
        DocumentTypeDto documentTypeDto = new DocumentTypeDto();
        documentTypeDto.setId(id);
        DocumentTypeEntity entity = fetchDocumentTypeEntity(documentTypeDto);
        BeanUtils.copyProperties(entity, documentTypeDto);
        copyWorkflowDto(documentTypeDto, entity);
        return  documentTypeDto;
    }

    public List<DocumentType> findByConfigTypeOfDocumentTypes(String configName) {
        List<DocumentTypeEntity> documentTypeEntities = documentTypeRepository.findByConfigType(configName);
        return parseDocumentType(documentTypeEntities);
    }

    public List<DocumentType> getListOfDocumentTypes() {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        List<DocumentTypeEntity> documentTypeEntities = documentTypeRepository.getDocumentTypeExcludeMetaData(sort);
        return parseDocumentType(documentTypeEntities);
    }

    private List<DocumentType> parseDocumentType(List<DocumentTypeEntity> documentTypeEntities) {
        List<DocumentType> documentTypes = new ArrayList<>();
        for (DocumentTypeEntity entity : documentTypeEntities) {
            DocumentType documentType = new DocumentType();
            BeanUtils.copyProperties(entity, documentType);
            documentTypes.add(documentType);
        }
        return documentTypes;
    }

    /**** meta data ****/
    public List<DocumentTypeDto> findByConfigurationType(String configType) {
        List<DocumentTypeEntity> documentTypeEntities = documentTypeRepository.findByConfigType(configType);
        return parseToDocumentTypeDto(documentTypeEntities);
    }

    public List<DocumentTypeDto> getListOfMetaData() {
        List<DocumentTypeEntity> documentTypeEntities = documentTypeRepository.findAll();
        return parseToDocumentTypeDto(documentTypeEntities);
    }

    private  List<DocumentTypeDto> parseToDocumentTypeDto(List<DocumentTypeEntity> documentTypeEntities) {
        List<DocumentTypeDto> documentTypes = new ArrayList<>();
        for (DocumentTypeEntity entity : documentTypeEntities) {
            DocumentTypeDto documentType = new DocumentTypeDto();
            BeanUtils.copyProperties(entity, documentType);
            copyWorkflowDto(documentType, entity);
            if (entity.getMetaDataDetails() != null) {
                List<MetaDataDto> metaDataDtos = entity.getMetaDataDetails().stream().map(this::convertMetaDataDto).collect(Collectors.toList());
                documentType.setMetaDataList(metaDataDtos);
            }
            documentTypes.add(documentType);
        }
        return documentTypes;
    }

    private void validateDocumentId(DocumentTypeDto documentTypeDto) {
        if (documentTypeDto.getId() == null)
            throw new RuntimeException("Document type id is mandatory");
    }

    private DocumentTypeEntity fetchDocumentTypeEntity(DocumentTypeDto documentTypeDto) {
        Optional<DocumentTypeEntity> optionalDocumentTypeEntity = documentTypeRepository.findById(documentTypeDto.getId());

        if (optionalDocumentTypeEntity.isEmpty())
            throw new RuntimeException("Document type id is not valid : " + documentTypeDto.getId());

        return optionalDocumentTypeEntity.get();

    }

    public DocumentTypeDto createMetaData(DocumentTypeDto documentTypeDto) {

        validateDocumentId(documentTypeDto);
        DocumentTypeEntity documentTypeEntity = fetchDocumentTypeEntity(documentTypeDto);

        if (documentTypeEntity.getMetaDataDetails() != null) {
            List<String> metaDataNames = documentTypeEntity.getMetaDataDetails().stream().map(e -> e.getName().toLowerCase()).toList();
            for (MetaDataDto metaDataDto : documentTypeDto.getMetaDataList()) {
                if (metaDataNames.contains(metaDataDto.getName().toLowerCase())) {
                    throw new RuntimeException("Already meta data exists for this document type");
                }
            }
        }

        List<MetaDataDetails> metaDataDetails = convertMetaDataEntity(documentTypeDto);
        if (documentTypeEntity.getMetaDataDetails() == null)
            documentTypeEntity.setMetaDataDetails(new ArrayList<>());

        documentTypeEntity.getMetaDataDetails().addAll(metaDataDetails);
        documentTypeRepository.save(documentTypeEntity);

        documentTypeDto.setMetaDataList(metaDataDetails.stream().map(this::convertMetaDataDto).collect(Collectors.toList()));

        return documentTypeDto;
    }

    private void validateUpdateMetaDataList(DocumentTypeDto documentTypeDto) {
        for (MetaDataDto metaDataDto : documentTypeDto.getMetaDataList()) {
            if (Objects.isNull(metaDataDto.getUuid()) || metaDataDto.getUuid().isEmpty())
                throw new RuntimeException("Metadata UUID is mandatory for update");
        }
    }

    public DocumentTypeDto updateMetaData(DocumentTypeDto documentTypeDto) {

        validateDocumentId(documentTypeDto);
        if (documentTypeDto.getMetaDataList() == null || documentTypeDto.getMetaDataList().size() == 0)
            throw new RuntimeException("At-least one metadata should be present");
        validateUpdateMetaDataList(documentTypeDto);
        DocumentTypeEntity documentTypeEntity = fetchDocumentTypeEntity(documentTypeDto);

        List<MetaDataDetails> metaDataDetails = convertMetaDataEntity(documentTypeDto);
        List<MetaDataDetails> metaDataEntities = documentTypeEntity.getMetaDataDetails();

        for (MetaDataDetails metaDataDetail : metaDataDetails) {
            Optional<MetaDataDetails> metaDataDetailsOptional = metaDataEntities.stream().filter(e -> e.getUuid().equals(metaDataDetail.getUuid())).findFirst();
            if (metaDataDetailsOptional.isEmpty()) {
                throw new RuntimeException("Invalid UUID present : " + metaDataDetail.getUuid());
            }
        }
        List<MetaDataDetails> updatedMetaData = new ArrayList<>();
        for (MetaDataDetails metaDataDetail : metaDataEntities) {
            Optional<MetaDataDetails> metaDataDetailsOptional = metaDataDetails.stream().filter(e -> e.getUuid().equals(metaDataDetail.getUuid())).findFirst();
            if (metaDataDetailsOptional.isPresent()) {
                updatedMetaData.add(metaDataDetailsOptional.get());
            } else {
                updatedMetaData.add(metaDataDetail);
            }
        }

        documentTypeEntity.setMetaDataDetails(updatedMetaData);
        documentTypeRepository.save(documentTypeEntity);
        return documentTypeDto;
    }

    public DocumentTypeDto findDocumentTypeNameAndStatus(String name, String status) {
        DocumentTypeEntity documentTypeEntity =  documentTypeRepository.findByName(name).orElse(null);
        if (documentTypeEntity != null && documentTypeEntity.getMetaDataDetails() != null) {
          documentTypeEntity.setMetaDataDetails( documentTypeEntity.getMetaDataDetails().stream().filter(e -> status.equalsIgnoreCase(e.getStatus())).collect(Collectors.toList()));
        }
        return parseDocumentTypeEntity(documentTypeEntity, status);
    }
    public DocumentTypeDto findDocumentTypeName(String name) {

        DocumentTypeEntity documentTypeEntity =  documentTypeRepository.findByName(name).orElse(null);
       return parseDocumentTypeEntity(documentTypeEntity, name);
    }

    private DocumentTypeDto parseDocumentTypeEntity(DocumentTypeEntity documentTypeEntity, String name) {

        if (documentTypeEntity == null)
            throw new DocumentNotPresentException("Document Type not present with "+name);

        DocumentTypeDto documentTypeDto = new DocumentTypeDto();
        BeanUtils.copyProperties(documentTypeEntity, documentTypeDto);

        if (documentTypeEntity.getMetaDataDetails() != null)
            documentTypeDto.setMetaDataList(documentTypeEntity.getMetaDataDetails().stream().map(this::convertMetaDataDto).collect(Collectors.toList()));

        return  documentTypeDto;

    }

    public DocumentTypeDto getMetaDataByDocumentTypeId(String id) {

        DocumentTypeDto documentTypeDto = new DocumentTypeDto();
        documentTypeDto.setId(id);
        DocumentTypeEntity documentTypeEntity = fetchDocumentTypeEntity(documentTypeDto);

        BeanUtils.copyProperties(documentTypeEntity, documentTypeDto);
        copyWorkflowDto(documentTypeDto, documentTypeEntity);

        if (documentTypeEntity.getMetaDataDetails() != null)
            documentTypeDto.setMetaDataList(documentTypeEntity.getMetaDataDetails().stream().map(this::convertMetaDataDto).collect(Collectors.toList()));

       return  documentTypeDto;

    }

    private MetaDataDto convertMetaDataDto(MetaDataDetails metaDataDetails) {
        MetaDataDto metaDataDto = new MetaDataDto();
        BeanUtils.copyProperties(metaDataDetails, metaDataDto);
        return metaDataDto;
    }

   /* private void copyMetaData(DocumentTypeDto documentTypeDto, DocumentTypeEntity documentTypeEntity) {
        List<MetaDataDetails> metaDataDetails = convertMetaDataEntity(documentTypeDto);
        documentTypeEntity.setMetaDataDetails(metaDataDetails);

    } */

    private List<MetaDataDetails> convertMetaDataEntity(DocumentTypeDto documentTypeDto) {
        List<MetaDataDetails> metaDataDetails = new ArrayList<>();
        documentTypeDto.getMetaDataList().forEach(e -> {
            MetaDataDetails metaData = new MetaDataDetails();
            BeanUtils.copyProperties(e, metaData);
            if (e.getUuid() == null)
                metaData.setUuid(UUID.randomUUID().toString());
            metaDataDetails.add(metaData);
        });
        return metaDataDetails;
    }
}
