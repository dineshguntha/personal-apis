package com.dguntha.personalapis.model.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseDto {
    private String message;
    private String code;
}
