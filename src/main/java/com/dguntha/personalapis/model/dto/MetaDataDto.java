package com.dguntha.personalapis.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetaDataDto {
    private String uuid;
    @NotBlank(message = "MetaData name is mandatory")
    private String name;
    @NotBlank(message = "MetaData type is mandatory")
    @Pattern(regexp = "text|password|email|date|number|tel|url", message = "Required is allowed  'Number' or 'String' character")
    private String type;
    private Integer min;
    private Integer max;
    @NotBlank(message = "Required is mandatory")
    @Pattern(regexp = "Y|N", message = "Required is allowed only 'Y' or 'N' character")
    private Character required;
    private String status;
    private String defaultValue;
    private Integer decimals;
    private String createdBy;
    private Instant createdDate;
    private String ocrLabels;
    private Object ocrFields;
}
