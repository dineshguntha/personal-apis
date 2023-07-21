package com.dguntha.personalapis.utils;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SevenZFileExtractor {

    private static final int BUFFER_SIZE = 4096;

    public static void extract(File archiveFilePath, String destinationFolderPath) throws IOException {
        File destinationFolder = new File(destinationFolderPath);

        try (SevenZFile sevenZFile = new SevenZFile(archiveFilePath)) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    extractEntry(sevenZFile, entry, destinationFolder);
                }
            }
        }
    }

    private static void extractEntry(SevenZFile sevenZFile, ArchiveEntry entry, File destinationFolder) throws IOException {
        String entryName = entry.getName();
        File extractedFile = new File(destinationFolder, entryName);

        try (FileOutputStream outputStream = new FileOutputStream(extractedFile)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = sevenZFile.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

   /* public static void main(String[] args) {
        String archiveFilePath = "/path/to/your/file.7z";
        String destinationFolderPath = "/path/to/destination/folder";

        try {
            extract(archiveFilePath, destinationFolderPath);
        } catch (IOException | ArchiveException e) {
            e.printStackTrace();
        }
    } */
}


