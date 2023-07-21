package com.dguntha.personalapis.controller;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
@RestController
public class DocumentController {
    @Autowired
    private BlobContainerClient blobContainerClient;

    @GetMapping("/document/{documentName}")
    public byte[] getDocument(@PathVariable String documentName) throws BlobStorageException {
        return getFileFromStorage(documentName);
    }

    @GetMapping("/document/encoded/{documentName}")
    public byte[] getEncodedDocument(@PathVariable String documentName) throws BlobStorageException {
        return Base64.getEncoder().encode(getFileFromStorage(documentName));
    }

    @GetMapping("/document/encoded/string/{documentName}")
    public String getEncodedStringDocument(@PathVariable String documentName) throws BlobStorageException {
        return new String(Base64.getEncoder().encode(getFileFromStorage(documentName)));
    }

    // /document/download/template get request
    // logic need to write the create /onfly csv file and with header altest 5 columns
    // then return that csv file.



    private byte[] getFileFromStorage(String documentName) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blobContainerClient.getBlobClient(documentName).download(outputStream);
        return outputStream.toByteArray();
    }


    @GetMapping(path = "/download")
    public ResponseEntity<ByteArrayResource> downloadFile(@RequestParam(value = "file") String file) throws IOException {
        byte[] data = getFileFromStorage(file);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + file + "\"")
                .body(resource);

    }

    @GetMapping(path = "/download/encoded")
    public ResponseEntity<Resource> downloadFileEncode(@RequestParam(value = "file") String file) throws IOException {
        byte[] data = getFileFromStorage(file);
       // ByteArrayResource resource = new ByteArrayResource(data);
        byte[] base64Bytes = Base64.getEncoder().encode(data);
        String base64String = new String(base64Bytes);

        // Create a ByteArrayResource from the Base64 content
        ByteArrayResource resource = new ByteArrayResource(base64Bytes);

        // Set the response headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=converted.pdf");

        // Return the Base64 content as a downloadable PDF file
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}



