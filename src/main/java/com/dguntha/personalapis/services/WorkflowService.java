package com.dguntha.personalapis.services;

import com.dguntha.personalapis.exception.DocumentIdNotFoundException;
import com.dguntha.personalapis.exception.DocumentNotPresentException;
import com.dguntha.personalapis.repository.DocumentTypeRepository;
import com.dguntha.personalapis.client.InvoiceWorkflowClient;
import com.dguntha.personalapis.model.entity.DocumentTypeEntity;
import com.dguntha.personalapis.utils.EmailExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {

    private final InvoiceWorkflowClient invoiceWorkflowClient;
    private final TransactionService transactionService;
    private final DocumentTypeRepository documentTypeRepository;

    public String processInvoiceInitialWorkflow(String transactionId) {
       Document transaction = transactionService.findByTransactionId(transactionId);
        if (transaction == null) {
            log.info("Transaction document not found : {} ", transactionId);
            throw new DocumentIdNotFoundException("Transaction id: "+ transactionId + " not found");
        }

        if (transaction.get("emailProcessor") == null)  {
            log.info("In Transaction document, email processor not found : {} ", transactionId);
            throw new DocumentNotPresentException("Email detail not present for Transaction id: "+ transactionId );
        }
        Document emailProcessor = (Document) transaction.get("emailProcessor");
        String documentTypeName = emailProcessor.getString("docTypeName");
        String definitionId = findWorkflowDefinitionId(transaction, documentTypeName, transactionId);
        validWorkflowDefinitionId(definitionId, transactionId, documentTypeName);
        String emailId = emailProcessor.getString("fromAddress");
       emailId = EmailExtractor.extractEmailId(emailId);
        JSONObject variableObj = new JSONObject();
        JSONObject detail = new JSONObject();
        detail.put("transactionId", getValueJson(transactionId));
        detail.put("email", getValueJson(emailId));
        variableObj.put("variables", detail);
        log.info("response body initial workflow : {} ", variableObj );
      String response =   invoiceWorkflowClient.publishInitialWorkflow(variableObj.toString(), definitionId);
      log.info("Reponse of api :{}", response);
        if (response != null && !StringUtils.isEmpty(response)) {
           if (transaction.get("workflow") != null) {
              Document workflow = (Document) transaction.get("workflow");
              workflow.append("initialStage", Document.parse(response));
              transaction.put("workflow", workflow);
           } else {
              Document workflow = new Document("definitionId", definitionId )
               .append("initialStage", Document.parse(response) );
               transaction.put("workflow", workflow);
           }
           transactionService.updateTransaction(transaction);
        } else {
            log.info("Unable able to send the workflow : {} ", transactionId);
            return "failure";
        }
        return "Success";
    }

    public void processApprovalInvoice(String transactionId, boolean isApproved) {

        Document transaction = transactionService.findByTransactionId(transactionId);
        if (transaction == null) {
            log.info("Transaction document not found : {} ", transactionId);
            throw new DocumentIdNotFoundException("Transaction id: "+ transactionId + " not found");
        }
        if (transaction.get("workflow") == null || ((Document)transaction.get("workflow")).get("initialStage") == null ) {
            log.info("Transaction workflow document not found : {} ", transactionId);
            throw new DocumentIdNotFoundException("Transaction id: "+ transactionId + " not found");
        }

        Document workflow = (Document) transaction.get("workflow");
        Document initialStage = (Document) workflow.get("initialStage");
        String stageId = initialStage.getString("id");
        String taskIdResponse = invoiceWorkflowClient.fetchTaskId(stageId);
        log.info("Details of taskId : {} and response details : {} ",stageId, taskIdResponse);
        JSONArray jsonArray = new JSONArray(taskIdResponse);
        if (jsonArray.length() == 0) {
            log.info("Unable to process this transaction for approval transaction id : {}", transactionId);
            return;
        }

        JSONObject variableObj = new JSONObject();
        JSONObject detail = new JSONObject();
        //TODO
        detail.put("transcationId", getValueJson(transactionId));
        detail.put("approve",getValueJson(isApproved));
        variableObj.put("variables", detail);

        // Iterate over the array elements
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
           String taskId  = item.getString("id");
            log.info("response body of invoice approval workflow : {} ", variableObj );
           ResponseEntity<String> response = invoiceWorkflowClient.publishApprovalWorkflow(variableObj.toString(), taskId);
            Document  approvalRes = new Document("status", response.getStatusCode())
                    .append("approvalFlag", isApproved)
                    .append("taskId", taskId);


            transaction.put("workflow", workflow.append("approvalStage", approvalRes));
            transactionService.updateTransaction(transaction);
           break;
        }


    }

    private JSONObject getValueJson(String value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", value);
        return jsonObject;
    }

    private JSONObject getValueJson(boolean value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", value);
        return jsonObject;
    }

    private String findWorkflowDefinitionId(Document transaction, String documentTypeName, String transactionId) {
        String definitionId;

        if (transaction.get("workflow") == null) {
            log.info("Checking with document type name : {}", documentTypeName);
            DocumentTypeEntity documentTypeEntity = documentTypeRepository.findByName(documentTypeName)
                    .orElse(null);
            if (documentTypeEntity == null || documentTypeEntity.getWorkflow() == null) {
                log.info("Document Type is not found with name : {}  for transaction id : {}", documentTypeName, transactionId);
                throw  new DocumentNotPresentException("Workflow id is not present for this transaction  : "+transactionId);
            }
            definitionId = documentTypeEntity.getWorkflow().getDefinitionId();
        }  else {
            Document workflow = (Document) transaction.get("workflow");
            definitionId = (String) workflow.get("definitionId");
        }
        return definitionId;
    }

    private void validWorkflowDefinitionId(String id, String transactionId, String name) {
        if (StringUtils.isEmpty(id)) {
            log.info("Workflow not found for document type : {} and transaction id : {} ", name, transactionId);
            throw new DocumentNotPresentException("Workflow id is not present for this transaction  : " + id);
        }
    }
}
