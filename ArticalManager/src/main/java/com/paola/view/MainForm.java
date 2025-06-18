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
import com.paola.parser.RssParser;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;

/**
 *
 * @author paola
 */
public class MainForm extends javax.swing.JFrame {

    //private JLabel lblTitle;
    //private JLabel imageLabel;
    //private JTextArea descriptionArea;
    //private JScrollPane scrollPanel;
    //private JButton prevButton;
    //private JButton nextButton;
    //private JLabel dateLabel;
    //private JButton viewFullImageButton;
    //private JComboBox<NewsCategory> categoryComboBox;
    private Image fullSizeImage;

    private JPanel dropPanel;
    //private JLabel dropLabel;

    private NewsController controller;
    private enum ViewMode { TITLE_ONLY, TITLE_DESC, FULL }
    private ViewMode currentViewMode = ViewMode.FULL;

    private JSplitPane splitPane;

    //private JProgressBar loadingProgressBar; // Declared here
    //private JButton refreshButton; // Declare refreshButton at class level to access it in setUIEnabled
    //private JSpinner spinnerLoadCount; // Declare spinnerLoadCount at class level to access it in setUIEnabled

    //JPanel navPanel;
	
    /**
     * Creates new form MainForm
     */
    public MainForm(List<NewsFeed> newsList) {
        
        this.controller= new NewsController(newsList);
        initComponents();
        updateDisplay();
        

        cmbCategory.removeAllItems(); // Clear "Item 1", etc.
        for (NewsCategory category : NewsCategory.values()) {
                cmbCategory.addItem(category.name()); // Or category.toString() if overridden
        }

        // fill combobox
        /*String[] categoryStrings = Arrays.stream(NewsCategory.values())
                                 .map(Enum::name)
                                 .toArray(String[]::new);
        cmbCategory = new JComboBox<>(categoryStrings);
        cmbCategory.insertItemAt(null, 0);
        cmbCategory.setSelectedIndex(0);*/
    
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
        
        /*
        dropPanel = new JPanel();
        dropPanel.setName("dropPanel");
        dropPanel.setPreferredSize(new Dimension(600, 400));
        dropPanel.setBorder(BorderFactory.createTitledBorder("Drop Image Here"));
        dropPanel.setMinimumSize(new Dimension(50, 50));
        //lblBigImg = new JLabel("Drag image here", SwingConstants.CENTER);
        //lblBigImg.setVerticalAlignment(SwingConstants.CENTER);
        dropPanel.setLayout(new BorderLayout());
        dropPanel.add(lblBigImg, BorderLayout.CENTER);
           */
        

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
        //lblBigImg.setTransferHandler(dropPanel.getTransferHandler());

        /*
        dropPanel.setFocusable(true);
        dropPanel.setEnabled(true);
        //dropPanel.setDropTarget(null);
        dropPanel.setVisible(true);
        System.out.println("dropPanel handler set on: " + dropPanel.hashCode());

        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dropPanel, scrollPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(500);
        add(splitPane, BorderLayout.CENTER);
        */
        
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
                            return image;
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

            boolean showDesc = currentViewMode != ViewMode.TITLE_ONLY;
            txtDesc.setText(showDesc ? news.getDescription() : "");
            //scrollPanel.setVisible(showDesc);

            boolean showImage = currentViewMode == ViewMode.FULL;
            lblImageIcon.setVisible(showImage);



            // jsplitter 
            /*
            if (splitPane != null)  {
                remove(splitPane);
            }
            if (currentViewMode == ViewMode.FULL) {
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dropPanel, scrollPanel);
            } else if (currentViewMode == ViewMode.TITLE_DESC) {
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JPanel(), scrollPanel);
            } else {
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JPanel(), new JPanel());
            }
            
            splitPane.setResizeWeight(0.5);
            splitPane.setDividerLocation(500);
            add(splitPane, BorderLayout.CENTER);
            revalidate();
            repaint();
            */
            


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

    /**
     * Starts the loading animation by making the JProgressBar visible and active.
     * Also forces the navigation panel to re-layout itself.
     */
    private void startLoading() {
        loadingProgress.setVisible(true);
        loadingProgress.setIndeterminate(true);
        // Important: Revalidate and repaint the parent container to ensure visibility change is reflected.
        SwingUtilities.invokeLater(() -> {
            jPanelCheck.revalidate();
            jPanelCheck.repaint();
        });
    }

    /**
     * Stops the loading animation by hiding the JProgressBar and deactivating its indeterminate mode.
     * Also forces the navigation panel to re-layout itself.
     */
    private void stopLoading() {
        loadingProgress.setVisible(false);
        loadingProgress.setIndeterminate(false);
        // Important: Revalidate and repaint the parent container to ensure visibility change is reflected.
        SwingUtilities.invokeLater(() -> {
            jPanelCheck.revalidate();
            jPanelCheck.repaint();
        });
    }

    /**
     * Enables or disables key UI elements to prevent user interaction during background operations.
     * @param enabled `true` to enable, `false` to disable.
     */
    private void setUIEnabled(boolean enabled) {
        btnRefresh.setEnabled(enabled);
        btnPrev.setEnabled(enabled);
        btnNext.setEnabled(enabled);
        btnFullimg.setEnabled(enabled);
        cmbCategory.setEnabled(enabled);
        spinnerloadCount.setEnabled(enabled); // Also disable the spinner during loading.
    }
    
    // *****************************************************************
    // CODE END
    // *****************************************************************
  
  
    /**
     * @param args the command line arguments
     */
	
    public static void main(String[] args) {

        NewsRepository repo = new NewsRepository();
        List<NewsFeed> newsList = repo.findAll();

        System.out.println("Loaded " + newsList.size() + " news items from database.");
        System.out.println("Parsed " + newsList.size() + " news items.");

        // Group by pubDate
        Map<String, List<NewsFeed>> grouped = newsList.stream()
                .collect(Collectors.groupingBy(NewsFeed::getPubDate));

        grouped.forEach((date, items) -> {
            System.out.println("Date: " + date + ", Count: " + items.size());
        });

        Set<String> uniqueDates = newsList.stream()
                .map(NewsFeed::getPubDate)
                .collect(Collectors.toSet());

        System.out.println("Unique publication dates: " + uniqueDates.size());

        //javax.swing.SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));

        // old way of starting MaiFrame without dialog before
        //SwingUtilities.invokeLater(() -> new MainFrame(newsList).setVisible(true));

        // Information dialog at startup
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    null,
                    "Welcome to NASA News Viewer!\nUse the Next and Previous buttons to browse the news.\nClick OK to continue.",
                    "Welcome",
                    JOptionPane.INFORMATION_MESSAGE
            );
            new MainForm(newsList).setVisible(true);
        });


    }
          
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
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
        jScrollPane1 = new javax.swing.JScrollPane();
        txtDesc = new javax.swing.JTextArea();
        btnFullimg = new javax.swing.JButton();
        lblDate = new javax.swing.JLabel();
        lblBigImg = new javax.swing.JLabel();
        loadingProgress = new javax.swing.JProgressBar();
        spinnerloadCount = new javax.swing.JSpinner();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblTitle.setText("jLabel1");

        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCategoryActionPerformed(evt);
            }
        });

        lblImageIcon.setText("jLabel1");

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

        txtDesc.setColumns(20);
        txtDesc.setLineWrap(true);
        txtDesc.setRows(5);
        txtDesc.setWrapStyleWord(true);
        jScrollPane1.setViewportView(txtDesc);

        btnFullimg.setText("Full Image");

        lblDate.setText("jLabel1");

        lblBigImg.setText("Big Image");

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

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
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(76, 76, 76)
                                    .addComponent(lblImageIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jPanelCheck, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(126, 126, 126))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 466, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap()))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 462, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblDate, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(153, 153, 153)
                                .addComponent(btnPrev)
                                .addGap(18, 18, 18)
                                .addComponent(btnNext)
                                .addGap(18, 18, 18)
                                .addComponent(btnRefresh)
                                .addGap(18, 18, 18)
                                .addComponent(btnFullimg))
                            .addComponent(lblBigImg, javax.swing.GroupLayout.PREFERRED_SIZE, 441, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(36, 36, 36)
                        .addComponent(loadingProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(spinnerloadCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblDate, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelCheck, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblImageIcon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBigImg, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnRefresh)
                        .addComponent(btnNext)
                        .addComponent(btnPrev)
                        .addComponent(btnFullimg)
                        .addComponent(spinnerloadCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(loadingProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        startLoading(); // Activate the loading indicator.
            setUIEnabled(false); // Disable UI elements during loading.

            int count = (int) spinnerloadCount.getValue();
            System.out.println("COUNT SPINNER: " + count);

            // Perform the refresh operation in a background thread to keep UI responsive.
            new Thread(() -> {
                RssParser parser = new RssParser();
                parser.parse(5);

                NewsRepository repo = new NewsRepository();
                List<NewsFeed> updatedList = repo.findAll();
                controller.setNewsList(updatedList);

                // Update UI on the Event Dispatch Thread (EDT) after background task is done.
                SwingUtilities.invokeLater(() -> {
                    stopLoading(); // Deactivate the loading indicator.
                    setUIEnabled(true); // Re-enable UI elements.
                    updateDisplay(); // Refresh UI with new data.
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
        currentViewMode = ViewMode.TITLE_ONLY;
        updateDisplay();
    }//GEN-LAST:event_rbTitleOnlyActionPerformed

    private void rbTitleDescActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbTitleDescActionPerformed
        // TODO add your handling code here:
        currentViewMode = ViewMode.TITLE_DESC;
        updateDisplay();
    }//GEN-LAST:event_rbTitleDescActionPerformed

    private void rbFullActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbFullActionPerformed
        // TODO add your handling code here:
        currentViewMode = ViewMode.FULL;
        updateDisplay();
    }//GEN-LAST:event_rbFullActionPerformed

 

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFullimg;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
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
    private javax.swing.JTextArea txtDesc;
    // End of variables declaration//GEN-END:variables
}
