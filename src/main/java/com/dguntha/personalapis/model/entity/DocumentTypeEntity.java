package com.dguntha.personalapis.model.entity;

import com.dguntha.personalapis.utils.Constants;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = Constants.COLL_DOCUMENT_TYPES)
@Data
public class DocumentTypeEntity {

    @Id
    private String id;
    @Indexed(unique = true)
    private String name;
    private String description;
    @Indexed(unique = true)
    private String uniqueName;
    private String configType;
    private String ocrThreshold;
    private List<MetaDataDetails> metaDataDetails;
    private WorkflowEntity workflow;

}
