package com.dguntha.personalapis.model.entity;

import lombok.Data;

import java.time.Instant;

@Data
public class MetaDataDetails {

    private String uuid;
    private String name;
    private String type;
    private Integer min;
    private Integer max;
    private Character required;
    private String status;
    private String defaultValue;
    private Integer decimals;
    private String createdBy;
    private Instant createdDate;
    private String ocrLabels;
    private Object ocrFields;
}
