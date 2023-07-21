package com.dguntha.personalapis.model.entity;

import com.dguntha.personalapis.utils.Constants;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = Constants.COLL_TRANSACTION)
@Data
public class TransactionEntity {
    @Id
    private String id;
    private Object emailProcessor;
}
