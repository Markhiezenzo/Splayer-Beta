package com.example.splayerbeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileScanner {

    private static final String[] SUPPORTED_FORMATS = {
            ".mp3", ".wav", ".m4a", ".flac", ".aac", ".mov" // Added .flac, .aac, and kept .mov
    };

    public static List<File> scanMediaFiles(File directory) {
        List<File> mediaFiles = new ArrayList<>();
        scanDirectory(directory, mediaFiles);
        return mediaFiles;
    }

    private static void scanDirectory(File directory, List<File> mediaFiles) {
        if (!directory.isDirectory()) {
            return;
        }
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

    public static boolean isSupportedMediaFile(File file) {
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