package com.dguntha.personalapis.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDto {
    private String definitionId;
    private String definitionKey;
    private String definitionName;
    private String deploymentId;
    private Integer version;
    private String tenantId;
    private boolean isRequired;
}
