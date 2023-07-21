package com.dguntha.personalapis.controller;

import com.dguntha.personalapis.model.entity.JobLogEntity;
import com.dguntha.personalapis.services.AzureDocumentUploadService;
import com.dguntha.personalapis.services.MultiFileUploadService;
import com.dguntha.personalapis.services.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final MultiFileUploadService multiFileUploadService;
    private final AzureDocumentUploadService azureDocumentUploadService;

    @GetMapping("/search")
    public List<Document> searchEmailDocuments(@RequestParam Map<String, Object> searchParams) {

        return transactionService.findByEmailProcessor(searchParams);
    }


    @GetMapping("/{id}")
    public Document getTransactionById(@PathVariable("id") String id) {
        return transactionService.findByTransactionId(id);
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateTransaction(@Valid @RequestBody @NotNull Document document) {
        transactionService.updateTransaction(document);
        return ResponseEntity.ok("Updated successfully");
    }

    @PostMapping("/upload")
    //@ApiOperation(value = "Upload a file")
    @Validated
    public ResponseEntity<Document> uploadDocument(@RequestParam(value = "file", required = true) MultipartFile file,
                                               @NotNull(message = "Document type is mandatory")   @RequestParam(value = "docTypeId", required = true) String docTypeId,
                                                 @Valid @NotNull(message = "Document name is mandatory")  @RequestParam(value = "docTypeName", required = true) String docTypeName,
                                                 @Valid @NotNull(message = "Email config id is mandatory") @RequestParam(value = "emailConfigId", required = true) String emailConfigId
                                                 ) {
        try {
            // Upload the document to Azure Blob Storage
            Map<String, Object>  attachment = azureDocumentUploadService.uploadDocument(file.getOriginalFilename(), file.getBytes(),
                    file.getSize(), file.getContentType());


            return ResponseEntity.ok(transactionService
                    .uploadDocumentTransaction(transactionService.constructNewTransactionDocument(docTypeId, docTypeName, emailConfigId, attachment)
                    , docTypeName));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Document("result", "Error uploading document."));
        }
    }
    @PostMapping("/multiple/upload")
    //@ApiOperation(value = "Upload a file")
    @Validated
    public ResponseEntity<JobLogEntity> uploadMultipleDocument(@RequestParam(value = "file", required = true) MultipartFile[] files,
                                                               @NotNull(message = "Document type is mandatory")   @RequestParam(value = "docTypeId", required = true) String docTypeId,
                                                               @Valid @NotNull(message = "Document name is mandatory")  @RequestParam(value = "docTypeName", required = true) String docTypeName,
                                                               @Valid @NotNull(message = "Email config id is mandatory") @RequestParam(value = "emailConfigId", required = true) String emailConfigId) {
        List<String> fileNames = new ArrayList<>();
        multiFileUploadService.validateFiles(files, fileNames);
        JobLogEntity jobLogEntity = multiFileUploadService.createJobLogEvent(fileNames);
        multiFileUploadService.processUploadedFiles(Arrays.asList(files), jobLogEntity,
                fileNames, docTypeId, docTypeName, emailConfigId);
        // validate the uploaded document contains one csv file and .zip or pdf or image files
        // csv file validate with uploaded zip file
        // In side the Job Log Collection
            // 1. Status
            // each object contains
            //
        // Create the job log structure
            //1. Initated zipping logs
            //2. reading csv file
            // 3. each line mapping

      /*  for (MultipartFile file2 : files) {
            try {
                // Read the file bytes
                byte[] document = file.getBytes();
               String fileName=file.getName();
               if(!fileName.endsWith(".csv") || !fileName.endsWith(".zip"))
               {
                   throw new RuntimeException("file should be ends with .csv or .zip");
               }
                if (file.getOriginalFilename().endsWith(".zip")) {
                    File zip = File.createTempFile(UUID.randomUUID().toString(), "temp");
                    FileOutputStream o = new FileOutputStream(zip);
                    IOUtils.copy(file.getInputStream(), o);
                    o.close();
                    String destination = "D:\\destination";
                    try {
                        ZipFile zipFile = new ZipFile(zip);
                        //zipFile.notifyAll(destination);
                    } catch (ZipException e) {
                        e.printStackTrace();
                    } finally {
                        zip.delete();
                    }
                } else if (file.getOriginalFilename().endsWith(".csv")) {
                    // process the CSV here
                } else if (file.getOriginalFilename().endsWith(".war")) {
                    // process the WAR here
                }

            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Document("result", "Error uploading document."));
            }
        } */
         return ResponseEntity.ok().body(jobLogEntity);
    }

        @GetMapping("/download/template")
    public ResponseEntity<Resource> generateCsvFile(@RequestParam(value = "docName", required = true) String docName) {


        // Set the content type and attachment filename
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Transaction_Upload_Template.csv");

        // Return the CSV file as a ResponseEntity
        return ResponseEntity.ok()
                .headers(headers)
                .body(transactionService.constructTransactionTemplate(docName));
    }


}

