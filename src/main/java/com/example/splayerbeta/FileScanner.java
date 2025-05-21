package com.example.splayerbeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileScanner {

    private static final String[] SUPPORTED_FORMATS = {
            ".mp3", ".mp4", ".avi", ".mov", ".wav", ".m4a"
    };



    private static void scanDirectory(File directory, List<File> mediaFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, mediaFiles); // Recursive scan for subdirectories
                } else if (isSupportedMediaFile(file)) {
                    mediaFiles.add(file);
                }
            }
        }
    }

    private static boolean isSupportedMediaFile(File file) {
        if (!file.isFile()) return false;

        String fileName = file.getName().toLowerCase();
        for (String format : SUPPORTED_FORMATS) {
            if (fileName.endsWith(format)) {
                return true;
            }
        }
        return false;
    }
}