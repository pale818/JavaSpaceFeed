/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paola;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;


public class Log {
    
    
    private Log() {
    }

  
    public static void logFailedDownloads(Map<String, String> failedDownloads) {
        if (failedDownloads == null || failedDownloads.isEmpty()) return;

        Path logDir = Paths.get("logs"); 
        Path logFile = logDir.resolve("failed_downloads.txt"); 
        try {
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

           
            try (BufferedWriter writer = Files.newBufferedWriter(logFile,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

                for (Map.Entry<String, String> entry : failedDownloads.entrySet()) {
                    writer.write("GUID: " + entry.getKey() + " - " + entry.getValue());
                    writer.newLine();
                }

                System.out.println("Logged failed downloads to " + logFile.toAbsolutePath());
            }

        } catch (IOException e) {
            System.err.println("Failed to write log file: " + e.getMessage());
        }
    }
}
