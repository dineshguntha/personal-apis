package com.dguntha.personalapis.controller;

import com.dguntha.personalapis.services.WorkflowService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @GetMapping("/invoice/initial/{transactionId}")
    public ResponseEntity<String> publishInitialWorkFlow(@Valid @NotBlank(message = "Transaction id should present") @PathVariable("transactionId") String transactionId) {
        String status = workflowService.processInvoiceInitialWorkflow(transactionId);

        return ResponseEntity.status(status.equalsIgnoreCase("success")? HttpStatus.OK:  HttpStatus.BAD_GATEWAY).body(status);
    }

    @GetMapping("/invoice/approval/{transactionId}/{isApproved}")
    public ResponseEntity approveWorkFlow(@Valid @NotBlank(message = "Transaction id should present") @PathVariable("transactionId") String transactionId,
                                          @Valid @PathVariable(name = "isApproved") boolean isApproved) {

        workflowService.processApprovalInvoice(transactionId, isApproved);
        return ResponseEntity.noContent().build();
    }
}
