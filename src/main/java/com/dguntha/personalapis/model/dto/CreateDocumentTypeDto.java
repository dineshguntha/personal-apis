package com.dguntha.personalapis.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class CreateDocumentTypeDto {
    @NotBlank(message = "Document type name is mandatory")
    private String name;
    private String description;
    @NotBlank(message = "Configuration type is mandatory")
    private String configType;
}
