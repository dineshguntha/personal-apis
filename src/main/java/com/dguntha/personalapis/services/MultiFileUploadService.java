package com.dguntha.personalapis.services;

import com.dguntha.personalapis.exception.FileUploadException;
import com.dguntha.personalapis.model.dto.DocumentTypeDto;
import com.dguntha.personalapis.model.dto.MetaDataDto;
import com.dguntha.personalapis.model.entity.JobLogEntity;
import com.dguntha.personalapis.model.entity.LogEvents;
import com.dguntha.personalapis.utils.FileUnzipper;
import com.dguntha.personalapis.utils.RarFileExtractor;
import com.dguntha.personalapis.utils.SevenZFileExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static com.dguntha.personalapis.utils.Constants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MultiFileUploadService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".zip", ".7z", ".rar", ".png", ".jpeg", ".jpg", ".pdf", ".csv", ".tiff", ".doc", ".docx", ".txt");
    private final JobLogService jobLogService;
    private final TransactionService transactionService;
    private final DocumentTypeService documentTypeService;
    private final AzureDocumentUploadService azureDocumentUploadService;

    public void validateFiles(MultipartFile[] files, List<String> filetList) {

        for (MultipartFile file : files) {
            if (file.isEmpty())
                throw new RuntimeException(file.getName() + " empty file is not allowed");

            String fileName = file.getOriginalFilename();
            // Check if the file has an allowed extension
            boolean isValidExtension = ALLOWED_EXTENSIONS.stream()
                    .anyMatch(extension -> {
                        assert fileName != null;
                        return fileName.toLowerCase().endsWith(extension);
                    });

            if (!isValidExtension)
                throw new FileUploadException(file.getOriginalFilename() + " file should have valid extension");

            List<MultipartFile> multipartFiles = Arrays.stream(files)
                    .filter(multipartFile -> multipartFile.getOriginalFilename().endsWith(".zip"))
                    .toList();

            if (multipartFiles.size() > 1)
                throw new FileUploadException("Multiple zip files are not allowed");

            List<MultipartFile> csvFilePart = Arrays.stream(files)
                    .filter(multipartFile -> multipartFile.getOriginalFilename().endsWith(".csv"))
                    .toList();

            if (csvFilePart.size() != 1)
                throw new RuntimeException("Csv files should be present");

            filetList.add(fileName);

        }
    }


    public JobLogEntity createJobLogEvent(List<String> fileNames) {

        LocalDateTime currentTime = LocalDateTime.now();
        JobLogEntity jobLogEntity = new JobLogEntity();
        jobLogEntity.setCreatedAt(currentTime);
        jobLogEntity.setStatus(IN_PROGRESS);
        jobLogEntity.setUpdatedAt(currentTime);
        jobLogEntity.setCreatedBy("System");

        List<LogEvents> logEvents = new LinkedList<>();
        logEvents.add(LogEvents.builder().createdAt(currentTime).order(1).message("Validating Uploaded files").status(SUCCESS).build());
        logEvents.add(LogEvents.builder().createdAt(LocalDateTime.now()).order(2).message("Initiated  this files " + String.join(",", fileNames) + " for uploaded").status(SUCCESS).build());
        jobLogEntity.setLogEvents(logEvents);

        return jobLogService.saveJobLog(jobLogEntity);
    }

    //@Async
    public void processUploadedFiles(List<MultipartFile> multipartFiles,
                                     JobLogEntity jobLogEntity, List<String> fileNames,
                                     String docTypeId, String docTypeName, String emailConfigId) {

        String jobFolder = jobLogEntity.getId();
        List<String> csvRecords = new ArrayList<>();

        File destinationFolder = new File(jobFolder);
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }
        try {
            downloadFilesToLocal(multipartFiles, jobFolder, fileNames, jobLogEntity);
        } catch (Exception ex) {
            addJobLogEntity(jobLogEntity, "Unable download the files ", FAILURE);
            log.error("Unable to download file ", ex);
            throw new FileUploadException("Unable to download the files");
        }
        readCSVRecordsFromMultipart(multipartFiles, csvRecords, jobLogEntity);
        processMultipleTransactions(jobFolder, csvRecords, jobLogEntity, docTypeId, docTypeName, emailConfigId);

    }


    private void processMultipleTransactions(String jobFolder, List<String> csvRecords,
                                             JobLogEntity jobLogEntity, String docTypeId,
                                             String docTypeName, String emailConfigId) {
        CompletableFuture.runAsync(() -> {
            try {
                List<List<String>> csvRecordsList = csvRecords.stream()
                        .map(record -> Arrays.asList(record.split(",")))
                        .toList();

                DocumentTypeDto documentTypeDto = documentTypeService.findDocumentTypeNameAndStatus(docTypeName, "Active");
                List<String> metaDataNames = documentTypeDto.getMetaDataList().stream().map(MetaDataDto::getName).toList();
                List<String> csvHeader = csvRecordsList.stream()
                        .filter(record -> record.stream().anyMatch(metaDataNames::contains))
                        .findFirst().orElse(null);

                if (csvHeader != null) {
                    File folder = new File(jobFolder);
                    File[] listOfFiles = folder.listFiles();

                    for (File file : listOfFiles) {
                        String fileName = file.getName();
                        if (file.isFile()) {
                            try {
                                Map<String, Object> uploadedDetails = azureDocumentUploadService.uploadDocument(file.getName(), file, file.length(), Files.probeContentType(file.toPath()));
                                addToTransaction(csvRecordsList, csvHeader, documentTypeDto.getMetaDataList(),
                                        fileName, uploadedDetails, docTypeId, docTypeName, emailConfigId, jobLogEntity.getId());
                                addJobLogEntity(jobLogEntity, "File " + fileName + " uploaded document successfully", SUCCESS);
                            } catch (Exception ex) {
                                log.error("Unable to upload document for file: {}", file.getName(), ex);
                                addJobLogEntity(jobLogEntity, "File " + fileName + " was unable to upload", FAILURE);
                            }
                        }
                    }
                } else {
                    addJobLogEntity(jobLogEntity, "Files was unable to upload due invalid CSV file ", FAILURE);
                }
            } catch (Exception ex) {
                log.error("Error occurred inside the create transaction", ex);
                addJobLogEntity(jobLogEntity, "Files was unable to upload ", FAILURE);
            } finally {
                addJobLogEntity(jobLogEntity, "Process successfully completed ", SUCCESS);
                deleteFolder(new File(jobFolder));
            }

        });

    }

    private void addJobLogEntity(JobLogEntity jobLogEntity, String message, String status) {
        List<LogEvents> logEvents = jobLogEntity.getLogEvents();
        jobLogEntity.getLogEvents().add(LogEvents.builder().order(logEvents.size() + 1).message(message)
                .status(status).createdAt(LocalDateTime.now()).build());

        jobLogService.updateJobLog(jobLogEntity);
    }

    private void deleteFolder(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    log.info("Deleted file: {}", file.getName());
                    deleteFolder(f);
                }
            }
        }
        file.delete();
    }


    private void addToTransaction(List<List<String>> csvRecordsList, List<String> csvHeaders,
                                  List<MetaDataDto> metaDataDtos, String fileName,
                                  Map<String, Object> uploadedDetails, String docTypeId,
                                  String docTypeName, String emailConfigId, String jobId) {

        List<String> metadataValues = csvRecordsList.stream()
                .filter(record -> record.contains(fileName))
                .findFirst().orElse(null);

        for (MetaDataDto metaDataDto : metaDataDtos) {
            int index = IntStream.range(0, csvHeaders.size())
                    .filter(i -> csvHeaders.get(i).equalsIgnoreCase(metaDataDto.getName()))
                    .findFirst()
                    .orElse(-1);
            Map<String, Object> map = new LinkedHashMap<>();
            if (index != -1 && metadataValues != null) {
                Object value = metadataValues.get(index);
                map.put("text", value);
                map.put("original_ocr_text", value);
            } else {
                map.put("text", "");
                map.put("original_ocr_text", "");
            }
            metaDataDto.setOcrFields(map);

        }

        uploadedDetails.put("ocr_metadata", metaDataDtos);
        Document transaction = transactionService.constructNewTransactionDocument(docTypeId, docTypeName,
                emailConfigId, uploadedDetails);
        transaction.put("multiDocUploadId", jobId);
        transactionService.createTransaction(transaction);
    }

    private void downloadFilesToLocal(List<MultipartFile> multipartFiles, String jobFolder,
                                      List<String> fileNames, JobLogEntity jobLogEntity) {

        if (fileNames.stream().anyMatch(e -> e.endsWith(".zip"))) {
            extractFilesByExtension(multipartFiles, ".zip", jobFolder);
        } else if (fileNames.stream().anyMatch(e -> e.endsWith(".rar"))) {
            extractFilesByExtension(multipartFiles, ".rar", jobFolder);
        } else if (fileNames.stream().anyMatch(e -> e.endsWith(".7z"))) {
            extractFilesByExtension(multipartFiles, ".7z", jobFolder);
        } else {
            downloadUnzipFiles(multipartFiles, jobFolder, jobLogEntity);
        }
    }

    private void downloadUnzipFiles(List<MultipartFile> multipartFiles, String jobFolder, JobLogEntity jobLogEntity) {

        List<String> skipFiles = List.of(".rar", ".zip", ".7z", ".csv");
        List<MultipartFile> files = multipartFiles.stream().filter(e ->
                        skipFiles.stream().noneMatch(t -> e.getOriginalFilename().endsWith(t)))
                .toList();

        for (MultipartFile file : files) {
            try {
                String fileName = file.getOriginalFilename();
                Path targetPath = Path.of(jobFolder, fileName);
                Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            } catch (Exception ex) {
                addJobLogEntity(jobLogEntity, "Unable to download the file " + file.getOriginalFilename(), FAILURE);
                log.error("Unable to download pdf files ", ex);
                throw new FileUploadException("Unable to download the file " + file.getOriginalFilename());
            }
        }
    }

    private void readCSVRecordsFromMultipart(List<MultipartFile> multipartFiles, List<String> csvRecords, JobLogEntity jobLogEntity) {

        MultipartFile csvFile = multipartFiles.stream()
                .filter(e -> e.getOriginalFilename().endsWith(".csv"))
                .findFirst()
                .orElseThrow(() -> {
                    addJobLogEntity(jobLogEntity, "CSV file not exist in bulk upload", FAILURE);
                    return new RuntimeException("CSV file not exist in bulk upload");
                });

        try {
            parseCSVFromByteArray(csvFile.getBytes(), csvRecords);
        } catch (IOException ex) {
            addJobLogEntity(jobLogEntity, "Unable to parse uploaded CSV file " + csvFile.getName(), FAILURE);
            log.error("Unable ot parse the CSV file ", ex);
            throw new RuntimeException("Unable to parse csv file");
        }
    }

    private void extractFilesByExtension(List<MultipartFile> multipartFiles,
                                         String extension, String jobFolder) {
        List<MultipartFile> files = multipartFiles.stream()
                .filter(multipartFile -> multipartFile.getOriginalFilename().endsWith(extension))
                .toList();


        MultipartFile multipartFile = files.get(0);

        try {
            if (extension.equals(".zip")) {
                FileUnzipper.unzip(multipartFile.getBytes(), jobFolder);
                //RarFileExtractor.extract(multipartFile.getBytes(), jobFolder);
            } else if (extension.equals(".rar")) {
                RarFileExtractor.extract(multipartFile.getBytes(), jobFolder);
            } else if (extension.endsWith(".7z")) {

                File tempFile = File.createTempFile("temp", ".7z");

                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(multipartFile.getBytes());
                }
                SevenZFileExtractor.extract(tempFile, jobFolder);

                tempFile.delete();
            } else {
                // Handle unsupported extensions if necessary
                throw new FileUploadException("Unsupported file extension: " + extension);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> parseCSVFromByteArray(byte[] csvData, List<String> records) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(csvData)))) {
            String line;
            while ((line = br.readLine()) != null) {
                records.add(line);
            }
        } catch (IOException e) {
            log.error("Error occurred while parsing the exception ", e);
            throw new FileUploadException("Unable to parse uploaded CSV file");

        }

        return records;
    }

}
