package com.dguntha.personalapis.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="invoiceWorkflow", url = "${workflow.url}")
public interface InvoiceWorkflowClient {

    @PostMapping(value = "/process-definition/{definitionId}/submit-form", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    String publishInitialWorkflow(@RequestBody String body, @PathVariable("definitionId") String workflowDefinitionId);

    @GetMapping(value = "/task", consumes = MediaType.APPLICATION_JSON_VALUE)
    String fetchTaskId(@RequestParam("processInstanceId") String id);

    @PostMapping(value = "/task/{definitionId}/submit-form", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> publishApprovalWorkflow(@RequestBody String body, @PathVariable("definitionId") String workflowDefinitionId);

}
