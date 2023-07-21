package com.dguntha.personalapis.model.entity;

import com.dguntha.personalapis.utils.Constants;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = Constants.COLL_JOB_LOG_TRANSACTION)
@Data
public class JobLogEntity {

    @Id
    private String id;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String status;
    private List<LogEvents> logEvents;
}
