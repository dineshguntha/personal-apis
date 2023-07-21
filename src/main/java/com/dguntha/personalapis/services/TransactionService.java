package com.dguntha.personalapis.services;

import com.dguntha.personalapis.exception.DocumentAlreadyExistsException;
import com.dguntha.personalapis.exception.DocumentIdNotFoundException;
import com.dguntha.personalapis.exception.DocumentNotPresentException;
import com.dguntha.personalapis.client.OCRApiCall;
import com.dguntha.personalapis.model.dto.DocumentTypeDto;
import com.dguntha.personalapis.model.dto.MetaDataDto;
import com.dguntha.personalapis.utils.Constants;
import com.dguntha.personalapis.utils.DateHelper;
import com.dguntha.personalapis.utils.DocumentCompare;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final MongoTemplate mongoTemplate;
    private final OCRApiCall ocrApiCall;
    private final DocumentTypeService documentTypeService;
    @Value("${ocr.code}")
    private String ocrCode;


    /**
     * Method used for searching based on the criteria.
     *
     */
    public List<Document> findByEmailProcessor(
            Map<String, Object> searchParams) {

        Criteria criteria = new Criteria();
        searchParams.forEach((key, value) -> {
            if (value instanceof String)
                 criteria.and(key).regex(String.valueOf(value), "i");
            else
                 criteria.and(key).is(value);
        }        );


        Query query = new Query(criteria);
        List<Document> documents = mongoTemplate.find(query, Document.class, Constants.COLL_TRANSACTION);
        return documents.stream().map(this::convertIDToHex).collect(Collectors.toList());
    }

    private Document convertIDToHex(Document document) {
        if (document.get("_id") instanceof  ObjectId) {
            String existingID = ((ObjectId) document.get("_id")).toHexString();
            document.put("_id", existingID);
        }
        return document;
    }

    public Document findByTransactionId(String id) {

        if (id == null || id.isBlank())
            throw new DocumentIdNotFoundException("Transaction id is mandatory");

      Document document = mongoTemplate.findById(id,Document.class, Constants.COLL_TRANSACTION );
        assert document != null;
        if (document.get("_id") instanceof  ObjectId) {
         String existingID = ((ObjectId) document.get("_id")).toHexString();
         document.put("_id", existingID);
     }
      return document;
    }

    public Document findByTransactionIdObject(Object id) {

        log.info("findByTransactionIdObject ... inside id : {} : into arr : {}", id.toString(), "hwllo");
        if (id == null )
            throw new DocumentIdNotFoundException("Transaction id is mandatory");

        return mongoTemplate.findById(id,Document.class, Constants.COLL_TRANSACTION );
    }

    public Document updateTransaction(Document document) {

      Document existingTransaction =  findByTransactionId(document.getString("_id"));

      if (existingTransaction == null || existingTransaction.isEmpty())
          throw  new DocumentNotPresentException("Transaction does not exists");

        document.put("_id", new ObjectId(document.getString("_id")));
        DocumentCompare.analyticsOCRDetails(document);
      return mongoTemplate.save(document,  Constants.COLL_TRANSACTION);
    }

    public Document createTransaction(Document document) {

        if (document.getString("_id") != null)
            throw new DocumentAlreadyExistsException("Already document is crated");
        DocumentCompare.analyticsOCRDetails(document);
        return mongoTemplate.save(document, Constants.COLL_TRANSACTION);
    }


    /**
     * Method used for uploading the document and add into transaction and then call
     * the OCR end point then return the result.
     * <p>
     * If the OCR endpoint is failed then it will add into the transaction.
     *
     * @param document
     * @param docTypeName
     * @return
     */
    public Document uploadDocumentTransaction(Document document, String docTypeName) {

     Document trasaction =  mongoTemplate.save(document, Constants.COLL_TRANSACTION);
        convertIDToHex(trasaction);
        String id = (String) trasaction.get("_id");
      ResponseEntity<String> entity = ocrApiCall.fetchTaskId(ocrCode, id);
      log.info("Message .. {} ", entity.getStatusCode().value());
      if (entity.getStatusCode().is2xxSuccessful()) {
         trasaction =   findByTransactionId(id);
      } else {
          // Need to get the details of metadata of the based on the document typ
          DocumentTypeDto documentTypeDto = documentTypeService.findDocumentTypeNameAndStatus(docTypeName,"Active");
            // - Documentype service findDocumentTypeNameAndStatus (docTypeName, "Active");
          List<MetaDataDto> metaDataDetailsList= documentTypeDto.getMetaDataList();
          // need to append into the transaction
            Map<String, Object> attachments = (Map<String, Object>)  trasaction.get("attachments");
            attachments.put("ocr_metadata", metaDataDetailsList);
            trasaction.put("attachments", attachments);
            // From DocumentTypeDto get the metadeta details.
            trasaction = updateTransaction(trasaction);

      }
     //https://docuflow-ocr-func.azurewebsites.net/api/docuflow-ocr-function?code=xvsmwyvSi-pxccLQHK1pdXkqFMQ2My4aOfyHlWxQVipUAzFuBmDuVA==&transactionId=
        return  trasaction;
    }

    /**
     * Method used for the creating the transaction template.
     *
     * @param docName
     * @return
     */
    public ByteArrayResource constructTransactionTemplate(String docName) {

        DocumentTypeDto documentTypeDto = documentTypeService.findDocumentTypeNameAndStatus(docName,"Active");
        StringBuilder csvContent = new StringBuilder();

        // Header names
        appendMetadataToCSV(csvContent, "Header Name", documentTypeDto.getMetaDataList(), MetaDataDto::getName, "Upload Document Name");
        // Header type
        appendMetadataToCSV(csvContent, "Header Type", documentTypeDto.getMetaDataList(), MetaDataDto::getType, "String");
        // IsRequired
        appendMetadataToCSV(csvContent, "Is Required", documentTypeDto.getMetaDataList(), e -> String.valueOf(e.getRequired()), "Y");

        // Create a ByteArrayResource from the CSV content byte[]
        return new ByteArrayResource(csvContent.toString().getBytes());

    }

    public void appendMetadataToCSV(StringBuilder csvContent, String headerName, List<MetaDataDto> metadataList,
                                           Function<MetaDataDto, String> propertyExtractor, String docHeader) {
        // Header name
        csvContent.append(headerName).append(",");
        csvContent.append(metadataList.stream().map(propertyExtractor).collect(Collectors.joining(",")));
        csvContent.append("," + docHeader);
        csvContent.append("\n");
    }

    public Document constructNewTransactionDocument(String docTypeId, String docTypeName, String emailConfigId,
                                                    Map<String, Object> attachment) {
        Map<String, Object> emailConfiguration = new HashMap<>();
        emailConfiguration.put("docTypeId", docTypeId);
        emailConfiguration.put("docTypeName", docTypeName);
        emailConfiguration.put("emailConfigId", emailConfigId);


        Document transaction = new Document().append("emailProcessor", emailConfiguration)
                .append("attachments", attachment);
        transaction.put("createdAt", DateHelper.getCurrentDateTimeAsString());
        transaction.put("updatedAt", DateHelper.getCurrentDateTimeAsString());

        return transaction;
    }

}
