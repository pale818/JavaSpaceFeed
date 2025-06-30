/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paola;

import java.io.File;
import java.util.Arrays;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author paola
 */
public class FileUtils {
     private FileUtils() {}
    
    private static final String UPLOAD = "Upload";

    public static File uploadFile(String description, String...extensions) {
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        chooser.setFileFilter(new FileNameExtensionFilter(description, extensions));
        chooser.setDialogTitle(UPLOAD);
        chooser.setApproveButtonText(UPLOAD);
        chooser.setApproveButtonToolTipText(UPLOAD);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf(".") + 1);
            return selectedFile.exists() && Arrays.asList(extensions).contains(extension.toLowerCase()) ? selectedFile : null;            
        }
        return null;
    }
}
