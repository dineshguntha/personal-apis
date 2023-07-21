package com.dguntha.personalapis.utils;
import com.github.junrar.*;
import com.github.junrar.exception.RarException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class RarFileExtractor {

    public static void extract(byte[] rarFilePath, String destinationFolderPath) throws IOException, RarException {
        File destinationFolder = new File(destinationFolderPath);
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }
        Junrar.extract(new ByteArrayInputStream(rarFilePath) , destinationFolder);

    }
}
