package com.dguntha.personalapis.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUnzipper {
    public static void unzip(byte[] zipData, String destinationFolderPath) throws IOException {


        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null) {
                String entryName = entry.getName();
                String entryPath = destinationFolderPath + File.separator + entryName;
                if (!entry.isDirectory()) {
                    extractFile(zipInputStream, entryPath);
                } else {
                    File dir = new File(entryPath);
                    dir.mkdirs();
                }
                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }
        }
    }

    private static void extractFile(ZipInputStream zipInputStream, String entryPath) throws IOException {
        byte[] buffer = new byte[1024];
        File outputFile = new File(entryPath);
        outputFile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(outputFile);
        int length;
        while ((length = zipInputStream.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }
        fos.close();
    }


}
