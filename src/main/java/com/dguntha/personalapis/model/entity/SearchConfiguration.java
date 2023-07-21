package com.dguntha.personalapis.model.entity;

import com.dguntha.personalapis.utils.Constants;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = Constants.COLL_SEARCH_CONFIG_TYPES)
@Data
public class SearchConfiguration {
    @Id
    private String id;
    @Indexed(unique = true)
    private String searchName;
    private String createBy;
    private Instant createDate;
    private String docTypeName;
    private List<Object> searchCriteria;
}
