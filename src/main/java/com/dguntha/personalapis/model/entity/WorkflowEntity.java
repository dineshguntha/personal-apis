package com.dguntha.personalapis.model.entity;

import lombok.Data;

@Data
public class WorkflowEntity {
    private String definitionId;
    private String definitionKey;
    private String definitionName;
    private String deploymentId;
    private Integer version;
    private String tenantId;
    private boolean isRequired;
}
