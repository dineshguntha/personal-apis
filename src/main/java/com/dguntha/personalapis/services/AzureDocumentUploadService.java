package com.dguntha.personalapis.services;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AzureDocumentUploadService {

    private final BlobContainerClient blobContainerClient;
    public Map<String, Object> uploadDocument(String fileName, byte[] document, long fileSize, String mimeType) throws IOException {

        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        // Upload the document to the blob
        blobClient.upload(BinaryData.fromBytes(document), true);
        Map<String, Object> attachments = mapDocumentValues(blobClient, fileSize, mimeType);
        log.debug("url" + blobClient.getBlobUrl());
        log.debug("name" + blobClient.getBlobName());
        log.debug(blobContainerClient.getBlobContainerUrl() );
        log.debug(blobClient.getVersionId());

        return  attachments;
    }

    public Map<String, Object> uploadDocument(String fileName, File file, long fileSize, String mimeType) {

        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        // Upload the document to the blob
        blobClient.upload(BinaryData.fromFile(file.toPath()), true);
        Map<String, Object> attachments = mapDocumentValues(blobClient, fileSize, mimeType);
        log.debug("url" + blobClient.getBlobUrl());
        log.debug("name" + blobClient.getBlobName());
        log.debug(blobContainerClient.getBlobContainerUrl() );
        log.debug(blobClient.getVersionId());

        return  attachments;
    }


    private Map<String, Object> mapDocumentValues(BlobClient blobClient,long fileSize, String mimeType ) {
        Map<String, Object> attachments = new LinkedHashMap<>();
        attachments.put("documentName", blobClient.getBlobName());
        attachments.put("documentUrl", blobClient.getBlobUrl());
        attachments.put("mimeType", mimeType);
        attachments.put("size", fileSize);
        attachments.put("isReviewed", "Not Reviewed");
        return  attachments;
    }
}
