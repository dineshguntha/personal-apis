package com.dguntha.personalapis.model.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LogEvents {
    private String name;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private int order;
}
