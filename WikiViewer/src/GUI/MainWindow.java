package GUI;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;


/**
 * Η κύρια οθόνη (Frame) της εφαρμογής WikiViewer.
 * θα κάνει τη διαχείριση στην πλοήγηση μεταξύ των οθονών (Login, Search), 
 * το κεντρικό μενού και την εναλλαγή θεμάτων (Dark/Light mode).
 */
public class MainWindow extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainWindow.class.getName());
    private Timer statusTimer;  // Timer για την αυτόματη εκκαθάριση μηνυμάτων στο statusBar
    
    private static final String CARD_LOGIN = "login";
    private static final String CARD_MAIN  = "main";
    private MainSearchScreen mainSearchScreen;
    
    /**
     * Ο κατασκευαστής του κεντρικού παραθύρου.
     */
    public MainWindow() {
        // Απενεργοποίηση της ενσωμάτωσης του Menu Bar στον τίτλο
        javax.swing.UIManager.put("TitlePane.menuBarEmbedded", false);
        
        initComponents();
        this.setLocationRelativeTo(null);   //Κεντράρισμα στην οθόνη
        
        initCards();
        showLogin();
        
        // Χειρισμός του κλεισίματος του παραθύρου για ασφαλή τερματισμό της βάσης
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                handleExit();
            }
        });
    }
    
    /**
     * Αρχικοποιεί το CardLayout και προσθέτει τις οθόνες (Panels).
     */
    private void initCards() {
        // Βάζουμε την LoginScreen ως πρώτη κάρτα
        cardsPanel.removeAll();
        cardsPanel.add(new LoginScreen(this), CARD_LOGIN);        
        
        // Προσθήκη κύριας οθόνης αναζήτησης
        this.mainSearchScreen = new MainSearchScreen(this);
        cardsPanel.add(this.mainSearchScreen, CARD_MAIN);
       
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }
    
    /**
     * Εμφανίζει την οθόνη σύνδεσης και αποκρύπτει το menubar και statusbar.
     */
    public void showLogin() {
        // Κρύψε menu + status
        menuBar.setVisible(false);
        statusBar.setVisible(false);
        
        // Δείξε την κάρτα login
        CardLayout cl = (CardLayout) cardsPanel.getLayout();
        cl.show(cardsPanel, CARD_LOGIN);
        
        revalidate();
        repaint();
    }
    
    /**
     * Εμφανίζει την κύρια οθόνη αναζήτησης μετά από επιτυχή είσοδο.
     * 
     * @param username Το όνομα του χρήστη που συνδέθηκε.
     */
    public void showMain(String username) {
        // Εμφάνισε menu + status
        setJMenuBar(menuBar);
        menuBar.setVisible(true);
        statusBar.setVisible(true);
        StatusLblUsername.setText(username);
        
        // Προεπιλεγμένη ρύθμιση για το Theme
        ViewThemeDark.setSelected(true);
        ViewThemeLight.setSelected(false);
        
        // Δείξε την κύρια κάρτα
        CardLayout cl = (CardLayout) cardsPanel.getLayout();
        cl.show(cardsPanel, CARD_MAIN);
        
        revalidate();
        repaint();
    }
    
    /**
     * Αλλάζει το θέμα της εφαρμογής (Dark ή Light).
     * 
     * @param isDark true για Dark Mode, false για Light Mode.
     */
    public void changeTheme(boolean isDark){
        try {
            if (isDark) {
                FlatDarkLaf.setup();
                ViewThemeDark.setSelected(true);
                ViewThemeLight.setSelected(false);
            }
            else {
                FlatLightLaf.setup();
                ViewThemeDark.setSelected(false);
                ViewThemeLight.setSelected(true);
            }
            
            com.formdev.flatlaf.FlatLaf.updateUI();            
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to change theme", ex);
        }
    }
    
    /**
     * Εμφανίζει ένα μήνυμα στο Status Bar το οποίο εξαφανίζεται μετά από 5 δευτερόλεπτα.
     * 
     * @param message Το μήνυμα προς εμφάνιση.
     */
    public void setStatusMessage(String message){
        statusGeneralMsg.setText(message);
        
        // Αν υπάρχει ήδη timer που μετράει, τον σταματάμε
        if (statusTimer != null && statusTimer.isRunning()) {
            statusTimer.stop();
        }

        // Ξεκινάμε νέο χρονόμετρο
        statusTimer = new Timer(5000, e -> statusGeneralMsg.setText(""));    
        statusTimer.setRepeats(false); // Θέλουμε να τρέξει μόνο μία φορά
        statusTimer.start();
    }
    
    /**
     * Διαχειρίζεται την έξοδο από την εφαρμογή, κλείνοντας τη βάση δεδομένων.
     */
    private void handleExit() {
        if (mainSearchScreen != null) {
            mainSearchScreen.closeDB();
        }
        System.exit(0);
    }
    
    /**
     * Κύρια μέθοδος εκκίνησης της εφαρμογής.
     * 
     * @param args command line arguments
     */
    public static void main(String args[]) {
        try {
            // Αρχικό Setup του Dark θέματος
            FlatDarkLaf.setup();
        } catch (Exception ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, "FlatLaf Init Failed", ex);
        }

        java.awt.EventQueue.invokeLater(() -> new MainWindow().setVisible(true));
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        contentPanel = new javax.swing.JPanel();
        cardsPanel = new javax.swing.JPanel();
        statusBar = new javax.swing.JPanel();
        lblUser = new javax.swing.JLabel();
        StatusLblUsername = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        statusGeneralMsg = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        MenuSaveDB = new javax.swing.JMenuItem();
        MenuRestoreDB = new javax.swing.JMenuItem();
        MenuStatistics = new javax.swing.JMenuItem();
        MenuExportPdf = new javax.swing.JMenuItem();
        MenuExit = new javax.swing.JMenuItem();
        jMenuEdit = new javax.swing.JMenu();
        EditCopy = new javax.swing.JMenuItem();
        EditClearSearch = new javax.swing.JMenuItem();
        jMenuView = new javax.swing.JMenu();
        ViewTheme = new javax.swing.JMenu();
        ViewThemeDark = new javax.swing.JCheckBoxMenuItem();
        ViewThemeLight = new javax.swing.JCheckBoxMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        HelpAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("WikiViewer Ver1.0");
        setMinimumSize(new java.awt.Dimension(800, 600));
        setName("mainFrame"); // NOI18N
        setPreferredSize(new java.awt.Dimension(1024, 768));

        contentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 15, 5, 15));
        contentPanel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        contentPanel.setLayout(new java.awt.BorderLayout());

        cardsPanel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        cardsPanel.setLayout(new java.awt.CardLayout());
        contentPanel.add(cardsPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(contentPanel, java.awt.BorderLayout.CENTER);

        statusBar.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(1, 15, 5, 15), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED)));
        statusBar.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        statusBar.setName("statusBar"); // NOI18N
        statusBar.setPreferredSize(new java.awt.Dimension(10, 30));
        statusBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                statusBarMouseClicked(evt);
            }
        });
        java.awt.GridBagLayout statusBarLayout = new java.awt.GridBagLayout();
        statusBarLayout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        statusBarLayout.rowHeights = new int[] {0};
        statusBar.setLayout(statusBarLayout);

        lblUser.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        lblUser.setText("User: ");
        lblUser.setMaximumSize(new java.awt.Dimension(32, 30));
        lblUser.setMinimumSize(new java.awt.Dimension(32, 30));
        lblUser.setPreferredSize(new java.awt.Dimension(32, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 1, 5, 1);
        statusBar.add(lblUser, gridBagConstraints);

        StatusLblUsername.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        StatusLblUsername.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        StatusLblUsername.setBorder(null);
        StatusLblUsername.setFocusable(false);
        StatusLblUsername.setMargin(new java.awt.Insets(2, 2, 2, 2));
        StatusLblUsername.setMinimumSize(new java.awt.Dimension(60, 30));
        StatusLblUsername.setName(""); // NOI18N
        StatusLblUsername.setPreferredSize(new java.awt.Dimension(60, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 1, 2, 1);
        statusBar.add(StatusLblUsername, gridBagConstraints);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setMinimumSize(new java.awt.Dimension(2, 20));
        jSeparator1.setPreferredSize(new java.awt.Dimension(2, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        statusBar.add(jSeparator1, gridBagConstraints);

        statusGeneralMsg.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        statusGeneralMsg.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 1, 2, 1);
        statusBar.add(statusGeneralMsg, gridBagConstraints);

        getContentPane().add(statusBar, java.awt.BorderLayout.SOUTH);

        menuBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 15, 5, 15));
        menuBar.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        menuBar.setMinimumSize(new java.awt.Dimension(111, 30));
        menuBar.setName("menuBar"); // NOI18N
        menuBar.setPreferredSize(new java.awt.Dimension(0, 30));

        jMenuFile.setText("File");
        jMenuFile.setToolTipText("");
        jMenuFile.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        MenuSaveDB.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        MenuSaveDB.setText("Save to DB");
        MenuSaveDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuSaveDBActionPerformed(evt);
            }
        });
        jMenuFile.add(MenuSaveDB);

        MenuRestoreDB.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        MenuRestoreDB.setText("Restore From DB");
        MenuRestoreDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuRestoreDBActionPerformed(evt);
            }
        });
        jMenuFile.add(MenuRestoreDB);

        MenuStatistics.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        MenuStatistics.setText("Statistics");
        MenuStatistics.setToolTipText("");
        MenuStatistics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuStatisticsActionPerformed(evt);
            }
        });
        jMenuFile.add(MenuStatistics);

        MenuExportPdf.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        MenuExportPdf.setText("Export to PDF");
        MenuExportPdf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuExportPdfActionPerformed(evt);
            }
        });
        jMenuFile.add(MenuExportPdf);

        MenuExit.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        MenuExit.setText("Exit");
        MenuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuExitActionPerformed(evt);
            }
        });
        jMenuFile.add(MenuExit);

        menuBar.add(jMenuFile);

        jMenuEdit.setText("Edit");
        jMenuEdit.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        EditCopy.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        EditCopy.setText("Copy");
        EditCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EditCopyActionPerformed(evt);
            }
        });
        jMenuEdit.add(EditCopy);

        EditClearSearch.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        EditClearSearch.setText("Clear All");
        EditClearSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EditClearSearchActionPerformed(evt);
            }
        });
        jMenuEdit.add(EditClearSearch);

        menuBar.add(jMenuEdit);

        jMenuView.setText("View");
        jMenuView.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        ViewTheme.setText("Select Theme");
        ViewTheme.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        ViewThemeDark.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        ViewThemeDark.setSelected(true);
        ViewThemeDark.setText("Dark Mode");
        ViewThemeDark.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ViewThemeDarkActionPerformed(evt);
            }
        });
        ViewTheme.add(ViewThemeDark);

        ViewThemeLight.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        ViewThemeLight.setSelected(true);
        ViewThemeLight.setText("Light Mode");
        ViewThemeLight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ViewThemeLightActionPerformed(evt);
            }
        });
        ViewTheme.add(ViewThemeLight);

        jMenuView.add(ViewTheme);

        menuBar.add(jMenuView);

        jMenuHelp.setText("Help");
        jMenuHelp.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        HelpAbout.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        HelpAbout.setText("About");
        HelpAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HelpAboutActionPerformed(evt);
            }
        });
        jMenuHelp.add(HelpAbout);

        menuBar.add(jMenuHelp);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void MenuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuExitActionPerformed
       handleExit();
    }//GEN-LAST:event_MenuExitActionPerformed

    private void ViewThemeDarkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ViewThemeDarkActionPerformed
       changeTheme(true);
    }//GEN-LAST:event_ViewThemeDarkActionPerformed

    private void ViewThemeLightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ViewThemeLightActionPerformed
        changeTheme(false);
    }//GEN-LAST:event_ViewThemeLightActionPerformed

    private void EditClearSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EditClearSearchActionPerformed
        mainSearchScreen.clearAllFields();
    }//GEN-LAST:event_EditClearSearchActionPerformed

    private void statusBarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_statusBarMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_statusBarMouseClicked

    private void HelpAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HelpAboutActionPerformed
        //δημιουργία παραθύρου επέκτασης πληροφοριών σε modal λειτουργία
        AboutScreen about = new AboutScreen(this, true);
        about.setLocationRelativeTo(this);
        about.setVisible(true);
    }//GEN-LAST:event_HelpAboutActionPerformed

    private void MenuSaveDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuSaveDBActionPerformed
        mainSearchScreen.menuSaveToDB();
    }//GEN-LAST:event_MenuSaveDBActionPerformed

    private void MenuRestoreDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuRestoreDBActionPerformed
        mainSearchScreen.menuRestoreFromDB();
    }//GEN-LAST:event_MenuRestoreDBActionPerformed

    private void MenuStatisticsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuStatisticsActionPerformed
        mainSearchScreen.menuStatistics();
    }//GEN-LAST:event_MenuStatisticsActionPerformed

    private void MenuExportPdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuExportPdfActionPerformed
        mainSearchScreen.menuExportToPDF();
    }//GEN-LAST:event_MenuExportPdfActionPerformed

    private void EditCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EditCopyActionPerformed
        mainSearchScreen.copyContentToClipboard();
    }//GEN-LAST:event_EditCopyActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem EditClearSearch;
    private javax.swing.JMenuItem EditCopy;
    private javax.swing.JMenuItem HelpAbout;
    private javax.swing.JMenuItem MenuExit;
    private javax.swing.JMenuItem MenuExportPdf;
    private javax.swing.JMenuItem MenuRestoreDB;
    private javax.swing.JMenuItem MenuSaveDB;
    private javax.swing.JMenuItem MenuStatistics;
    private javax.swing.JTextField StatusLblUsername;
    private javax.swing.JMenu ViewTheme;
    private javax.swing.JCheckBoxMenuItem ViewThemeDark;
    private javax.swing.JCheckBoxMenuItem ViewThemeLight;
    private javax.swing.JPanel cardsPanel;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JMenu jMenuEdit;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenu jMenuView;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblUser;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JPanel statusBar;
    private javax.swing.JLabel statusGeneralMsg;
    // End of variables declaration//GEN-END:variables
}