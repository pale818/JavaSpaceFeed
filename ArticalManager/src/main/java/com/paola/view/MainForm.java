/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.paola.view;


import com.paola.Dialog;
import com.paola.Models.NewsCategory;
import com.paola.Models.NewsFeed;
import com.paola.controller.NewsController;
import com.paola.dal.NewsRepository;
import com.paola.Models.User;
import com.paola.dal.sql.DatabaseSingleton;
import com.paola.parser.RssParser;
import com.paola.ui.LoginDialog;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;

/**
 *
 * @author paola
 */
public class MainForm extends javax.swing.JFrame {

    private Image fullSizeImage;
    
    
    private NewsController controller;
    private enum ViewMode { TITLE_ONLY, TITLE_DESC, FULL }
    private ViewMode currentViewMode = ViewMode.FULL;
    private User loggedInUser;
    
    
    public MainForm(List<NewsFeed> newsList, User loggedInUser) {
        
        this.controller= new NewsController(newsList);
        this.loggedInUser = loggedInUser;

        initComponents();
        updateDisplay();
        
        
        jMenuDelete.setVisible(false);
        // Show only if admin
        if (loggedInUser.isAdmin()) {
            jMenuDelete.setVisible(true);
        }
        
        

        cmbCategory.removeAllItems(); 
        cmbCategory.addItem("");
        for (NewsCategory category : NewsCategory.values()) {
                cmbCategory.addItem(category.name()); // Or category.toString() if overridden
        }

        
    
        spinnerloadCount.setModel(new javax.swing.SpinnerNumberModel(5, 1, 20, 1));

        rbTitleOnly.addActionListener(e -> {
            currentViewMode = ViewMode.TITLE_ONLY;
            updateDisplay();
        });
        rbTitleDesc.addActionListener(e -> {
            currentViewMode = ViewMode.TITLE_DESC;
            updateDisplay();
        });
        rbFull.addActionListener(e -> {
            currentViewMode = ViewMode.FULL;
            updateDisplay();
        });
		
        ButtonGroup viewModeGroup = new ButtonGroup();
        viewModeGroup.add(rbTitleOnly);
        viewModeGroup.add(rbTitleDesc);
        viewModeGroup.add(rbFull);
        
        
        

        lblBigImg.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                System.out.println("Drop target entered: " + support.getComponent().getName());
                return support.isDataFlavorSupported(DataFlavor.imageFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                try {
                    Image img = (Image) support.getTransferable().getTransferData(DataFlavor.imageFlavor);
                    if (img != null) {
                        fullSizeImage = img;
                        resizeDropLabelImage();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        });

        lblBigImg.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                resizeDropLabelImage();
            }
        });
        
        
        // draging icon lblImageIcon
        lblImageIcon.setTransferHandler(new TransferHandler() {
            @Override
            protected Transferable createTransferable(JComponent c) {
                Icon icon = lblImageIcon.getIcon();
                System.out.println("ICON = " + lblImageIcon.getIcon());

                if (icon instanceof ImageIcon) {
                    Image image = ((ImageIcon) icon).getImage();
                    return new Transferable() {
                        @Override
                        public DataFlavor[] getTransferDataFlavors() {
                            return new DataFlavor[]{DataFlavor.imageFlavor};
                        }

                        @Override
                        public boolean isDataFlavorSupported(DataFlavor flavor) {
                            return DataFlavor.imageFlavor.equals(flavor);
                        }

                        @Override
                        public Object getTransferData(DataFlavor flavor) {
                            return fullSizeImage;
                        }
                    };
                }
                return null;
            }

            @Override
            public int getSourceActions(JComponent c) {
                return COPY;
            }
        });
        
        // 🟡 Now listen to mouse movement
        lblImageIcon.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    System.out.println("Dragging...");
                    lblImageIcon.getTransferHandler().exportAsDrag(lblImageIcon, e, TransferHandler.COPY);
                }
           });

        lblImageIcon.setEnabled(true);
        lblImageIcon.setFocusable(true);
        lblImageIcon.setOpaque(true);
    }
      // *****************************************************************
      // CODE BEGIN
      // *****************************************************************

        private void resizeDropLabelImage() {
        if (fullSizeImage == null || lblBigImg.getWidth() == 0 || lblBigImg.getHeight() == 0)
            return;

        Image scaled = fullSizeImage.getScaledInstance(
                lblBigImg.getWidth(),
                lblBigImg.getHeight(),
                Image.SCALE_SMOOTH
        );

        lblBigImg.setIcon(new ImageIcon(scaled));
        lblBigImg.setText("");
    }

    private void navigate(int offset) {
        if (offset > 0 && controller.hasNext()) {
            controller.nextNews();
        } else if (offset < 0 && controller.hasPrevious()) {
            controller.previousNews();
        }
        updateDisplay();
    }

    private void updateDisplay() {
        

        controller.getOptionalCurrentNews().ifPresent(news -> {
            System.out.println("TITLE: " + news.getTitle());

            lblTitle.setText(news.getTitle());
            lblDate.setText("Published: " + news.getPubDate());

           
            // default
            txtDesc.setVisible(true);
            lblBigImg.setVisible(true);

            if (currentViewMode == ViewMode.TITLE_ONLY) {
                lblBigImg.setVisible(false);
                txtDesc.setVisible(false);
                txtDesc.setText("");
            } 

            if (currentViewMode == ViewMode.TITLE_DESC) {
                lblBigImg.setVisible(false);
                txtDesc.setVisible(true);
                txtDesc.setText(news.getDescription());
            } 
            
            if (currentViewMode == ViewMode.FULL) {
               System.out.println("SET VISIBILITY FULL");

                lblBigImg.setVisible(true);
                //resizeDropLabelImage();

                txtDesc.setVisible(true);
                txtDesc.setText(news.getDescription());
                
                SwingUtilities.invokeLater(() -> {
                    splitPane.setResizeWeight(0.5);
                    splitPane.setDividerLocation(splitPane.getWidth() / 2);
                });
            } 
                        
            ImageIcon icon = null;
            try {
                String path = news.getLocalImagePath();
                if (path != null) {
                    BufferedImage img = ImageIO.read(new File(path));
                    fullSizeImage = img;
                    if (img != null) {
                        Image scaled = img.getScaledInstance(150, 100, Image.SCALE_SMOOTH);
                        icon = new ImageIcon(scaled);
                    }
                }
            } catch (Exception ignored) {}

            lblImageIcon.setIcon(icon);
            lblImageIcon.setText(null);

        });
    }

    private void showFullImageDialog() {
        controller.getOptionalCurrentNews().ifPresent(news -> {
            String path = news.getLocalImagePath();
            if (path == null) {
                Dialog.showInfo(this, "No image available.");
                return;
            }

            try {
                BufferedImage img = ImageIO.read(new File(path));
                if (img != null) {
                    ImageIcon fullIcon = new ImageIcon(img);
                    JLabel fullImageLabel = new JLabel(fullIcon);
                    JScrollPane scrollPane = new JScrollPane(fullImageLabel);
                    scrollPane.setPreferredSize(new Dimension(1000, 700));

                    Dialog.showPlainDialog(this, scrollPane, "Full Image");

                } else {
                    throw new Exception("Image load failed.");
                }
            } catch (Exception e) {
                Dialog.showError(this, "Failed to load full image.");
            }
        });
    }

    
    private void startLoading() {
        loadingProgress.setVisible(true);
        loadingProgress.setIndeterminate(true);
        SwingUtilities.invokeLater(() -> {
            jPanelCheck.revalidate();
            jPanelCheck.repaint();
        });
    }

   
    private void stopLoading() {
        loadingProgress.setVisible(false);
        loadingProgress.setIndeterminate(false);
        SwingUtilities.invokeLater(() -> {
            jPanelCheck.revalidate();
            jPanelCheck.repaint();
        });
    }

    
    private void setUIEnabled(boolean enabled) {
        btnRefresh.setEnabled(enabled);
        btnPrev.setEnabled(enabled);
        btnNext.setEnabled(enabled);
        cmbCategory.setEnabled(enabled);
        spinnerloadCount.setEnabled(enabled); // Also disable the spinner during loading.
    }
    
    // **********************************************************
    // CODE END
    // ***********************************************
  
  
    /**
     * @param args the command line arguments
     */
	
    public static void main(String[] args) {

        NewsRepository repo = new NewsRepository();
        List<NewsFeed> newsList = repo.findAll();

        System.out.println("Loaded " + newsList.size() + " news items from database.");
        System.out.println("Parsed " + newsList.size() + " news items.");

        LoginDialog loginDialog = new LoginDialog(null, true); 
        loginDialog.setVisible(true);
        
        User loggedInUser = loginDialog.getAuthenticatedUser();
        
        if (loggedInUser != null) {
            JOptionPane.showMessageDialog(
                null,
                "Welcome to NASA News Viewer, " + loggedInUser.getUsername() + "!\nUse the Next and Previous buttons to browse the news.\nClick OK to continue.",
                "Welcome",
                JOptionPane.INFORMATION_MESSAGE
            );

            MainForm mainForm = new MainForm(newsList, loggedInUser);
            mainForm.setVisible(true);

        } else {
            JOptionPane.showMessageDialog(
                null,
                "Login cancelled or failed. Application will now exit.",
                "Exit",
                JOptionPane.INFORMATION_MESSAGE
            );
            System.exit(0); // Exit the application if login is not successful
        }


    }
          
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        cmbCategory = new javax.swing.JComboBox<>();
        lblImageIcon = new javax.swing.JLabel();
        jPanelCheck = new javax.swing.JPanel();
        rbTitleOnly = new javax.swing.JRadioButton();
        rbTitleDesc = new javax.swing.JRadioButton();
        rbFull = new javax.swing.JRadioButton();
        btnRefresh = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnPrev = new javax.swing.JButton();
        lblDate = new javax.swing.JLabel();
        loadingProgress = new javax.swing.JProgressBar();
        spinnerloadCount = new javax.swing.JSpinner();
        splitPane = new javax.swing.JSplitPane();
        lblBigImg = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtDesc = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuDelete = new javax.swing.JMenuItem();
        Exit = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblTitle.setText("Title");

        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCategoryActionPerformed(evt);
            }
        });

        lblImageIcon.setText("Icon");

        rbTitleOnly.setText("Title Only");
        rbTitleOnly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbTitleOnlyActionPerformed(evt);
            }
        });

        rbTitleDesc.setText("Title + Description");
        rbTitleDesc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbTitleDescActionPerformed(evt);
            }
        });

        rbFull.setSelected(true);
        rbFull.setText("Full");
        rbFull.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbFullActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelCheckLayout = new javax.swing.GroupLayout(jPanelCheck);
        jPanelCheck.setLayout(jPanelCheckLayout);
        jPanelCheckLayout.setHorizontalGroup(
            jPanelCheckLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCheckLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rbTitleOnly, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(rbTitleDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addComponent(rbFull, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelCheckLayout.setVerticalGroup(
            jPanelCheckLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCheckLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanelCheckLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbTitleOnly)
                    .addComponent(rbTitleDesc)
                    .addComponent(rbFull))
                .addContainerGap(41, Short.MAX_VALUE))
        );

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        btnNext.setText("Next");
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });

        btnPrev.setText("Previous");
        btnPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevActionPerformed(evt);
            }
        });

        lblDate.setText("Date");

        splitPane.setDividerLocation(500);

        lblBigImg.setText("Big Image");
        lblBigImg.setMinimumSize(new java.awt.Dimension(10, 10));
        splitPane.setLeftComponent(lblBigImg);

        txtDesc.setColumns(20);
        txtDesc.setLineWrap(true);
        txtDesc.setRows(5);
        txtDesc.setWrapStyleWord(true);
        jScrollPane1.setViewportView(txtDesc);

        splitPane.setRightComponent(jScrollPane1);

        jMenu1.setText("File");

        jMenuDelete.setText("Delete all data");
        jMenuDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuDeleteActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuDelete);

        Exit.setText("Exit");
        Exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExitActionPerformed(evt);
            }
        });
        jMenu1.add(Exit);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 108, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(44, 44, 44)
                                .addComponent(lblImageIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanelCheck, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 462, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblDate, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(126, 126, 126))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(153, 153, 153)
                                .addComponent(btnPrev)
                                .addGap(18, 18, 18)
                                .addComponent(btnNext)
                                .addGap(18, 18, 18)
                                .addComponent(btnRefresh)
                                .addGap(139, 139, 139)
                                .addComponent(loadingProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(spinnerloadCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(splitPane, javax.swing.GroupLayout.PREFERRED_SIZE, 807, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblDate, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelCheck, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblImageIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnRefresh)
                        .addComponent(btnNext)
                        .addComponent(btnPrev)
                        .addComponent(spinnerloadCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(loadingProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        startLoading(); 
            setUIEnabled(false); 
            int count = (int) spinnerloadCount.getValue();
            System.out.println("COUNT SPINNER: " + count);

            new Thread(() -> {
                
                RssParser.parse(count);

                NewsRepository repo = new NewsRepository();
                List<NewsFeed> updatedList = repo.findAll();
                controller.setNewsList(updatedList);

                // Update UI on the Event Dispatch Thread (EDT) after background task is done.
                SwingUtilities.invokeLater(() -> {
                    stopLoading(); 
                    setUIEnabled(true); 
                    updateDisplay();
                });
            }).start();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        // TODO add your handling code here:
        navigate(1); 
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
        // TODO add your handling code here:
        navigate(-1); 
    }//GEN-LAST:event_btnPrevActionPerformed

    private void cmbCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCategoryActionPerformed
        // TODO add your handling code here:
        String selected = (String) cmbCategory.getSelectedItem();
        if (selected != null && !selected.isEmpty()) {
            try {
                NewsCategory selectedCategory = NewsCategory.valueOf(selected);
                controller.filterByCategory(selectedCategory);
            } catch (IllegalArgumentException ex) {
                // Handle unexpected string
                System.err.println("Unknown category: " + selected);
            }
        } else {
            // null or "All" option selected — remove filter
            controller.filterByCategory(null);
        }

        updateDisplay();
    }//GEN-LAST:event_cmbCategoryActionPerformed

    private void rbTitleOnlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbTitleOnlyActionPerformed
        // TODO add your handling code here:
        System.out.println("TITLE_ONLY:");
        currentViewMode = ViewMode.TITLE_ONLY;
        updateDisplay();
    }//GEN-LAST:event_rbTitleOnlyActionPerformed

    private void rbTitleDescActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbTitleDescActionPerformed
        // TODO add your handling code here:
        System.out.println("TITLE_DEXC");
        currentViewMode = ViewMode.TITLE_DESC;
        updateDisplay();
    }//GEN-LAST:event_rbTitleDescActionPerformed

    private void rbFullActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbFullActionPerformed
        // TODO add your handling code here:
        System.out.println("FULL");
        currentViewMode = ViewMode.FULL;
        updateDisplay();
    }//GEN-LAST:event_rbFullActionPerformed

    private void ExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExitActionPerformed
        // TODO add your handling code here:
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit?",
            "Exit Confirmation",
            JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }//GEN-LAST:event_ExitActionPerformed

    private void jMenuDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuDeleteActionPerformed
        // TODO add your handling code here:
        int result = JOptionPane.showConfirmDialog(this, "Delete all news and images?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            DatabaseSingleton.deleteAllNewsFeeds();
            deleteAllAssetImages();
            controller.setNewsList(Collections.emptyList());
            txtDesc.setText("");
            lblImageIcon.setIcon(null);
            lblBigImg.setIcon(null);
            updateDisplay();
            JOptionPane.showMessageDialog(this, "All news and images have been deleted.");
        } 
        
    }//GEN-LAST:event_jMenuDeleteActionPerformed

     private static void deleteAllAssetImages() {
        File assetsDir = new File("assets"); // relative to project root / working directory

        if (!assetsDir.exists() || !assetsDir.isDirectory()) {
            System.out.println("assets folder not found.");
            return;
        }

        File[] files = assetsDir.listFiles();
        if (files == null) {
            System.out.println("Failed to list files in assets folder.");
            return;
        }

        int deletedCount = 0;
        for (File file : files) {
            if (file.isFile()) {
                if (file.delete()) {
                    deletedCount++;
                } else {
                    System.out.println("Failed to delete: " + file.getName());
                }
            }
        }

        System.out.println("Deleted " + deletedCount + " file(s) from assets folder.");
    } 

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem Exit;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuDelete;
    private javax.swing.JPanel jPanelCheck;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblBigImg;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblImageIcon;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JProgressBar loadingProgress;
    private javax.swing.JRadioButton rbFull;
    private javax.swing.JRadioButton rbTitleDesc;
    private javax.swing.JRadioButton rbTitleOnly;
    private javax.swing.JSpinner spinnerloadCount;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JTextArea txtDesc;
    // End of variables declaration//GEN-END:variables
}
