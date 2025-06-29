/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paola;

import javax.swing.*;
import java.awt.*;
import java.io.File;


public class Dialog {
    
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    
    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

   
    public static void showPlainDialog(Component parent, Component content, String title) {
        JOptionPane.showMessageDialog(parent, content, title, JOptionPane.PLAIN_MESSAGE);
    }

   
    public static File chooseImageFile(Component parent) {
        JFileChooser chooser = new JFileChooser(); 
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY); 
        int result = chooser.showOpenDialog(parent);        
        return (result == JFileChooser.APPROVE_OPTION) ? chooser.getSelectedFile() : null;
    }
}
