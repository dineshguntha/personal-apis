package com.dguntha.personalapis.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DocumentType {
    private String id;
    private String name;
    private String description;
    private String uniqueName;
}
