package com.dguntha.personalapis.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class DocumentTypeDto {

    @NotBlank(message = "Document type name is mandatory")
    private String name;
    private String description;
    @NotBlank(message = "Document type id is mandatory")
    private String id;
    private String uniqueName;
    private String configType;
    private String ocrThreshold;
    private List<MetaDataDto> metaDataList;
    private WorkflowDto workflow;
}
