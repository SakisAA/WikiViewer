package GUI;

import API.*;
import Exceptions.*;
import PdfExporter.pdfExporter;
import java.io.IOException;
import java.io.File;
import com.google.gson.JsonParseException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.DefaultListModel;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.Toolkit;
import DBmanager.*;

/**
 * Η κλάση MainSearchScreen αποτελεί την κύρια οθόνη για την αναζήτηση
 * και τη διαχείριση άρθρων.
 * Λειτουργίες:
 *     α) Αναζήτηση άρθρων στη Wikipedia μέσω API
 *     β) Προβολή αποτελεσμάτων
 *     γ) Αποθήκευση άρθρων στη βάση δεδομένων 
 *     δ) Εξαγωγή στατιστικών σε αρχείο PDF.
 */
public class MainSearchScreen extends javax.swing.JPanel {
    
    private final MainWindow mainWindow;
    private List <Article> results;     // Αποτελέσματα από το API
    private List <Object[]> dbResults;  // Αποτελέσματα από τη βάση δεδομένων
    private DBmanager db;               // Ο Διαχειριστής της βάσης δεδομένων
    private Integer selectedArticleId;  // Το ID του άρθρου στην βάση δεδομένων
    
    /**
     * Ο κατασκευαστής της κλάσης. Αρχικοποιεί τον DBmanager και τις ρυθμίσεις
     * για το GUI.
     * 
     * @param mainWindow Το κεντρικό παράθυρο της εφαρμογής.
     */
    public MainSearchScreen(MainWindow mainWindow) {
        //αρχικοποίηση database manager
        this.db = new DBmanager();
        this.mainWindow = mainWindow;
        initComponents();
        setupTextAreaSplitPane();
        setupListListener();
    }
    
    /**
     * Ρυθμίσεις για το TextArea και το SplitPane
     */
    private void setupTextAreaSplitPane(){
        // Ρυθμίσεις του textArea
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtContent.setEditable(true);
        
        // Προσθήκη για το κεντράρισμα του SplitPane
        javax.swing.SwingUtilities.invokeLater(()-> {
            jSplitPaneMain.setDividerLocation(0.5);
        });
    }
    
    /**
     * Ρυθμίσεις για τα συμβάντα επιλογής άρθρου από τη λίστα.
     */
    private void setupListListener(){    
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                if (!evt.getValueIsAdjusting()) {
                    handleListSelection();
                }
            }
        });
    }
    
    /**
     * Επεξεργάζεται την επιλογή του χρήστη και ενημερώνει
     * τα πεδία προβολής του άρθρου.
     */
    private void handleListSelection() {
        int index = jList1.getSelectedIndex();
        if (index ==-1) return;
        
        btnSave.setEnabled(true);   //ενεργοποίηση του πλήκτρου αποθήκευσης
        
        // Α Περίπτωση: Αναζήτηση και προβολή από Search (API)
        if (results != null) {
            if (results.get(index).getText() == null) {
                try {
                    //διαμόρφωση του νέου request για το κείμενο του άρθρου
                    jsonDeserializer deserializer = new jsonDeserializer();
                    deserializer.extractFullText(results, results.get(index).getTitle());
                    }
                catch (Exception e){
                    System.out.println("Error retrieving the article. " + e.toString());
                            }
                        }
            updateArticleView(results.get(index).getSnippet(),
                            results.get(index).getText(),
                            results.get(index).getTitle(),
                            results.get(index).getTimeStamp(), 
                            null, 
                            null,
                            null);
        }
        // Β Περίπτωση: Αναζήτηση και προβολή από DB
        else if (dbResults !=null){
            Object[] row = dbResults.get(index);
            selectedArticleId = (Integer) row[0];
            
            String snippet = (String) row[3];
            String fullText = (String) row[4];
            String title = (String) row[5];
            String timestamp = (String) row[1];
            String category = (String) row[6];
            Integer rating = null;
            String comments = (String) row[7];
            
            if (row[2] != null) {
                rating = (Integer) row[2];
            }
            
            updateArticleView(snippet,
                            fullText,
                            title,
                            timestamp,
                            category,
                            rating, 
                            comments);
        }
        
        // υπολογισμός των λέξεων του άρθρου
        int plithos = wordsCount(txtContent.getText());
        String txtStatusMessage = "Word count: " + plithos;
        mainWindow.setStatusMessage(txtStatusMessage);
    }

    /**
     * Ενημερώνει τα στοιχεία του GUI με τα δεδομένα του επιλεγμένου άρθρου.
     */
    private void updateArticleView(String snippet, String fullText, String title, String timestamp, 
                                   String category, Integer rating, String comments) {
        txtContent.setText("Snippet:\n" + snippet + "\n\nFull text:\n" + fullText);
        txtContent.setCaretPosition(0);
        lblDBTitle.setText(title);
        lblDBDTstamp.setText(timestamp);
        
        if (category != null){
            lblDBCategory.setText(category);
        }
        else {
            lblDBCategory.setText("");
        }
        
        if (rating != null){
            lblDBRating.setText(String.valueOf(rating));
        }
        else {
            lblDBRating.setText("");
        }
        
        if (comments != null){
            lblDBComments.setText(comments);
        }
        else {
            lblDBComments.setText("");
        }
    }
    
    /**
     * Μετράει τις λέξεις ενός κειμένου.
     * 
     * @param text Το κείμενο προς έλεγχο.
     * @return Το πλήθος των λέξεων.
     */
    public int wordsCount(String text){
        if (text == null || text.trim().isEmpty()){
            return 0;            
        }
        
        String[] words = text.trim().split("\\s+");
        return words.length;
    }
    
     /**
     * Πραγματοποιεί κλήση της wikiSearch. Χειρισμός των exceptions
     * με κατάλληλο error message.
     * Ενημέρωση της λίστας αποτελεσμάτων.
     */
    private void executeSearch() {
        String searchTerm = this.txtSearch.getText().trim(); //λήψη του String αναζήτησης που πληκτρολόγησε ο χρήστης
        if (searchTerm.isEmpty()){
            return;
        }
        
        this.clearAllFields();
        this.txtSearch.setText(searchTerm);
        
        //κλήση της μεθόδου αναζήτησης wikiSearch
        try {
            //εκχώρηση των αποτελεσμάτων στην results
            this.results = this.wikiSearch(searchTerm, true);
            //γέμισμα του JList με τους τίτλους των αποτελεσμάτων
            DefaultListModel<String> model = new DefaultListModel<>();
            //επαύξηση του model με τους τίτλου, η Jlist ενημερώνεται αυτόματα
            for (Article a: results)
            {
                model.addElement(a.getTitle());
            }
            //δυναμική ανάθεση του model στην Jlist
            jList1.setModel(model);
            
            // Εμφάνιση του πλήθους των αποτελεσμάτων στo status bar
            String txtStatusMessage = "Found " + results.size() + " results";
            mainWindow.setStatusMessage(txtStatusMessage);
            
            //ενημέρωση της database με τον όρο της αναζήτησης
            if (this.results != null && !this.results.isEmpty()) 
            {
                this.db.addSearchToHistory(searchTerm.toLowerCase());
            }
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(
                    mainWindow, 
                    "Invalid search term",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        catch (NetworkException e)
        {
            JOptionPane.showMessageDialog(
                    mainWindow, 
                    "Network Error",
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
        catch (NoResultsException e)
        {
            JOptionPane.showMessageDialog(
                    mainWindow,  
                    "No results found",
                    "Information window",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        catch (JsonParseException e)
        {
            JOptionPane.showMessageDialog(
                    mainWindow,  
                    "Data Parsing Error",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * απενεργοποιεί το save button και κάνει εκκαθάριση της επιλογής άρθρου
     */
    public void resetSelection() {
        this.btnSave.setEnabled(false);
        this.jList1.clearSelection();
    }
    
    /**
     * Καθαρίζει όλα τα πεδία της οθόνης και επαναφέρει τις αρχικές ρυθμίσεις.
     */
    public void clearAllFields() {
        this.txtSearch.setText("");
        this.jList1.clearSelection();
        this.jList1.setModel(new DefaultListModel<>());
        this.txtContent.setText("");
        this.lblDBTitle.setText("");
        this.lblDBDTstamp.setText("");
        this.lblDBCategory.setText("");
        this.lblDBRating.setText("");
        this.lblDBComments.setText("");
        
        this.btnSave.setEnabled(false);
        txtSearch.requestFocus();
    }
        
    /**
     * Εκτελεί την αναζήτηση μέσω του API
     * @param searchTerm ο όρος αναζήτησης
     * @param mode true για αναζήτηση, false για ανάκτηση από db
     * @throws IOException
     * @throws NetworkException
     * @throws NoResultsException
     * @throws JsonParseException
     * @return Λίστα με τα άρθρα του αποτελέσματος αναζήτησης
     */
    private List <Article> wikiSearch(String searchTerm, boolean mode)throws IOException, NetworkException, NoResultsException, JsonParseException {
        //δημιουργία του request URL σε mode αναζήτησης
        WikiViewerAPI api = new WikiViewerAPI(searchTerm, mode);
        //λήψη του json response από Wikipedia API
        String jsonResponse = api.getWikiResponse();
        /*deserialize των σχετικών στοιχείων του json response
        σε αντικείμενα Article*/
        jsonDeserializer deserializer = new jsonDeserializer();
        try
        {
            deserializer.parseData(jsonResponse);
        }
        catch (JsonParseException e)
        {
            throw new JsonParseException("Malformed Json");
        }
        
        List <Article> articlesResults = deserializer.getArticles();
        return articlesResults;
    }
    
    /**
     * Αποθηκεύει στη db το επιλεγμένο άρθρο από τη jList μαζί με
     * επιπλέον παραμέτρους που έχει ορίσει ο χρήστης (προαιρετικά)
     * στο InformationExtensionDialog
     * @param category
     * @param comments
     * @param rating 
     */
    private void saveArticle(String category, String comments, Integer rating) {
        int index = jList1.getSelectedIndex();
        if (results != null && index >= 0){
            db.saveApiArticle(this.results.get(index), category, comments, rating);
        }
    }
    
    /**
     * Αντιγράφει το κείμενο του txtContent στο πρόχειρο (Clipboard).
     * Αν είναι κενό, εμφανίζει σχετικό μήνυμα στο status bar.
     */
    public void copyContentToClipboard() {
        String textToCopy = txtContent.getText();
        
        // Έλεγχος αν υπάρχει κείμενο
        if (textToCopy == null || textToCopy.trim().isEmpty()) {
            mainWindow.setStatusMessage("Δεν υπάρχει κείμενο για αντιγραφή.");
            return;
        }
        
        // Εστίαση στο txtContent
        txtContent.requestFocusInWindow();
        txtContent.selectAll();
        
        // Διαδικασία αντιγραφής στο Clipboard της Java
        StringSelection stringSelection = new StringSelection(textToCopy);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        
        // Εμφάνιση μηνύματος επιτυχίας
        mainWindow.setStatusMessage("Το περιεχόμενο αντιγράφηκε στο πρόχειρο!");
    }
    
    /**
     * Κλείνει τη σύνδεση με τη βάση δεδομένων.
     */
    public void closeDB(){
        if (this.db != null) {
            this.db.close();
            System.out.println("Η σύνδεση με τη βάση έκλεισε επιτυχώς.");
        }
    }
    
    /**
     * Αποθηκεύει τα δεδομένα στη βάση δεδομένων.
     */
    public void menuSaveToDB(){
        if (this.btnSave.isEnabled()) {
            this.btnSave.doClick(); // Προσομοιώνει το κλικ
        } else {
            // Αν δεν έχει επιλεγεί άρθρο, βγάζει ένα φιλικό μήνυμα
            mainWindow.setStatusMessage("Please choose an article to save.");
        }
    }
    
    public void menuRestoreFromDB(){
        this.btnRestore.doClick(); // Προσομοιώνει το κλικ
    }
    
    public void menuStatistics(){
        this.btnStatistics.doClick(); // Προσομοιώνει το κλικ
        
    }
    
    public void menuExportToPDF(){
        this.btnExportPdf.doClick(); // Προσομοιώνει το κλικ
        
    }
    
    public void clearTxtSearch() {
        txtSearch.setText("");
        txtSearch.requestFocus();
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

        jPanelHeader = new javax.swing.JPanel();
        lblSearch = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jSplitPaneMain = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtContent = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblDTstamp = new javax.swing.JLabel();
        lblCategory = new javax.swing.JLabel();
        lblRating = new javax.swing.JLabel();
        lblComments = new javax.swing.JLabel();
        lblDBTitle = new javax.swing.JTextArea();
        lblDBDTstamp = new javax.swing.JLabel();
        lblDBCategory = new javax.swing.JLabel();
        lblDBRating = new javax.swing.JLabel();
        lblDBComments = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        btnRestore = new javax.swing.JButton();
        btnStatistics = new javax.swing.JButton();
        btnExportPdf = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEtchedBorder());
        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setMinimumSize(new java.awt.Dimension(400, 300));
        setLayout(new java.awt.BorderLayout());

        jPanelHeader.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 15, 5, 5));
        jPanelHeader.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanelHeader.setName(""); // NOI18N
        jPanelHeader.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblSearch.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        lblSearch.setText("Search Wikipedia");
        lblSearch.setPreferredSize(new java.awt.Dimension(100, 25));
        jPanelHeader.add(lblSearch);

        txtSearch.setColumns(30);
        txtSearch.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        txtSearch.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txtSearch.setToolTipText("search field");
        txtSearch.setMargin(new java.awt.Insets(5, 5, 5, 5));
        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });
        jPanelHeader.add(txtSearch);

        btnSearch.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        btnSearch.setText("Search");
        btnSearch.setPreferredSize(new java.awt.Dimension(90, 25));
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });
        jPanelHeader.add(btnSearch);

        add(jPanelHeader, java.awt.BorderLayout.NORTH);

        jSplitPaneMain.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jSplitPaneMain.setDividerLocation(0.5);
        jSplitPaneMain.setResizeWeight(0.5);
        jSplitPaneMain.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jScrollPane1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        jList1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jScrollPane1.setViewportView(jList1);

        jSplitPaneMain.setLeftComponent(jScrollPane1);

        jPanel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanel2.setMinimumSize(new java.awt.Dimension(18, 18));
        jPanel2.setPreferredSize(new java.awt.Dimension(260, 132));
        jPanel2.setLayout(new java.awt.GridLayout(2, 1));

        jScrollPane3.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        txtContent.setColumns(20);
        txtContent.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        txtContent.setLineWrap(true);
        txtContent.setRows(5);
        jScrollPane3.setViewportView(txtContent);

        jPanel2.add(jScrollPane3);

        jPanel3.setEnabled(false);
        jPanel3.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel3.setLayout(new java.awt.GridBagLayout());

        lblTitle.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTitle.setText("Title ");
        lblTitle.setMinimumSize(new java.awt.Dimension(90, 20));
        lblTitle.setPreferredSize(new java.awt.Dimension(90, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(50, 10, 10, 10);
        jPanel3.add(lblTitle, gridBagConstraints);

        lblDTstamp.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        lblDTstamp.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblDTstamp.setText("Date-timestamp ");
        lblDTstamp.setMinimumSize(new java.awt.Dimension(90, 20));
        lblDTstamp.setPreferredSize(new java.awt.Dimension(90, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        jPanel3.add(lblDTstamp, gridBagConstraints);

        lblCategory.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        lblCategory.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCategory.setText("Category ");
        lblCategory.setMinimumSize(new java.awt.Dimension(90, 20));
        lblCategory.setPreferredSize(new java.awt.Dimension(90, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        jPanel3.add(lblCategory, gridBagConstraints);

        lblRating.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        lblRating.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRating.setText("Rating ");
        lblRating.setMinimumSize(new java.awt.Dimension(90, 20));
        lblRating.setPreferredSize(new java.awt.Dimension(90, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        jPanel3.add(lblRating, gridBagConstraints);

        lblComments.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        lblComments.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblComments.setText("Comments ");
        lblComments.setMinimumSize(new java.awt.Dimension(90, 20));
        lblComments.setPreferredSize(new java.awt.Dimension(90, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        jPanel3.add(lblComments, gridBagConstraints);

        lblDBTitle.setEditable(false);
        lblDBTitle.setColumns(1);
        lblDBTitle.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        lblDBTitle.setLineWrap(true);
        lblDBTitle.setRows(2);
        lblDBTitle.setTabSize(1);
        lblDBTitle.setWrapStyleWord(true);
        lblDBTitle.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        lblDBTitle.setOpaque(false);
        lblDBTitle.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(50, 0, 10, 10);
        jPanel3.add(lblDBTitle, gridBagConstraints);

        lblDBDTstamp.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        lblDBDTstamp.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        lblDBDTstamp.setMinimumSize(new java.awt.Dimension(0, 20));
        lblDBDTstamp.setPreferredSize(new java.awt.Dimension(41, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
        jPanel3.add(lblDBDTstamp, gridBagConstraints);

        lblDBCategory.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        lblDBCategory.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDBCategory.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        lblDBCategory.setMinimumSize(new java.awt.Dimension(0, 20));
        lblDBCategory.setPreferredSize(new java.awt.Dimension(41, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
        jPanel3.add(lblDBCategory, gridBagConstraints);

        lblDBRating.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        lblDBRating.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDBRating.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        lblDBRating.setMinimumSize(new java.awt.Dimension(0, 20));
        lblDBRating.setPreferredSize(new java.awt.Dimension(41, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
        jPanel3.add(lblDBRating, gridBagConstraints);

        lblDBComments.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        lblDBComments.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblDBComments.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        lblDBComments.setMinimumSize(new java.awt.Dimension(0, 20));
        lblDBComments.setPreferredSize(new java.awt.Dimension(41, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
        jPanel3.add(lblDBComments, gridBagConstraints);

        jPanel2.add(jPanel3);

        jSplitPaneMain.setRightComponent(jPanel2);

        add(jSplitPaneMain, java.awt.BorderLayout.CENTER);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 0));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        btnSave.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        btnSave.setText("Save to DB");
        btnSave.setEnabled(false);
        btnSave.setMaximumSize(new java.awt.Dimension(130, 30));
        btnSave.setMinimumSize(new java.awt.Dimension(100, 25));
        btnSave.setPreferredSize(new java.awt.Dimension(125, 25));
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 5);
        jPanel1.add(btnSave, gridBagConstraints);

        btnRestore.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        btnRestore.setText("Restore From DB");
        btnRestore.setMaximumSize(new java.awt.Dimension(130, 30));
        btnRestore.setMinimumSize(new java.awt.Dimension(100, 25));
        btnRestore.setPreferredSize(new java.awt.Dimension(125, 25));
        btnRestore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRestoreActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 5);
        jPanel1.add(btnRestore, gridBagConstraints);

        btnStatistics.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        btnStatistics.setText("Statistics");
        btnStatistics.setMaximumSize(new java.awt.Dimension(130, 30));
        btnStatistics.setMinimumSize(new java.awt.Dimension(100, 25));
        btnStatistics.setPreferredSize(new java.awt.Dimension(125, 25));
        btnStatistics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStatisticsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 5);
        jPanel1.add(btnStatistics, gridBagConstraints);

        btnExportPdf.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        btnExportPdf.setText("Export to PDF");
        btnExportPdf.setMaximumSize(new java.awt.Dimension(130, 30));
        btnExportPdf.setMinimumSize(new java.awt.Dimension(100, 25));
        btnExportPdf.setPreferredSize(new java.awt.Dimension(125, 25));
        btnExportPdf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportPdfActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 5);
        jPanel1.add(btnExportPdf, gridBagConstraints);

        add(jPanel1, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Μέθοδοι που ανταποκρίνονται στην εκτέλεση αναζήτησης
     * είτε με πάτημα του κουμπιού Search είτε με Enter
     * απευθείας στο πεδίο αναζήτησης
     * @param evt 
     */
    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        //κλήση της μεθόδου αναζήτησης executeSearch
        this.executeSearch();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        //κλήση της μεθόδου αναζήτησης executeSearch
        this.executeSearch();
    }//GEN-LAST:event_txtSearchActionPerformed

    /**
     * Ανοίγει το παράθυρο στατιστικών.
     */
    private void btnStatisticsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStatisticsActionPerformed
        // Δημιουργούμε το παράθυρο και του περνάμε τον db manager της κλάσης μας
        StatisticsScreen stats = new StatisticsScreen(this.mainWindow, true, this.db);
        stats.setLocationRelativeTo(this); // Κεντράρισμα
        stats.setVisible(true); // Εμφάνιση
    }//GEN-LAST:event_btnStatisticsActionPerformed

    /**
     * Αποθηκεύει ή ενημερώνει ένα άρθρο στη βάση δεδομένων.
     *
     * @param evt 
     */
    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        int index = jList1.getSelectedIndex();
        if (index == -1) return;

        // Α Περίπτωση: Νέα αποθήκευση από API
            //έλεγχος αν το επιλεγμένο άρθρο ανήκει ήδη στη λίστα
        if (this.results!=null){        
            try {
                if (!(db.articleExists(this.results.get(index)))){
                    //δημιουργία παραθύρου επέκτασης πληροφοριών σε modal λειτουργία
                    InformationExtensionDialog extensionDialog = new InformationExtensionDialog(this.mainWindow, true);
                    extensionDialog.setLocationRelativeTo(this);
                    extensionDialog.setVisible(true);
                    
                    //αν ο χρήστης πάτησε το πλήκτρο Save, αποθηκεύονται
                    //το άρθρο και οι πληροφορίες που εισήγαγε στη ΒΔ
                    if (extensionDialog.performedSave()) {
                        this.saveArticle(extensionDialog.getCategory(), 
                                extensionDialog.getComments(),
                                extensionDialog.getRating()
                                );
                        mainWindow.setStatusMessage("Article '" + this.results.get(index).getTitle() + "' saved!");
                    }
                    // αποδέσμευση του παραθύρου από τη μνήμη
                    extensionDialog.dispose();
                }
            }
            catch (ExistingArticleException e) {
                JOptionPane.showMessageDialog(
                        mainWindow,  
                        "Article already exists in database",
                        "Information window",
                        JOptionPane.INFORMATION_MESSAGE);
                }
        }
        // Β Περίπτωση: Ενημέρωση υπάρχοντος άρθρου στη DB
        else if (this.dbResults != null && this.selectedArticleId != null) {
            // Ανοίγουμε τον ίδιο διάλογο για να εισάγει ο χρήστης τις αλλαγές
            InformationExtensionDialog extensionDialog = new InformationExtensionDialog(this.mainWindow, true);
        
            // Γέμισμα του διαλόγου με τα υπάρχοντα δεδομένα
            Object[] row = dbResults.get(index);
            String existingCategory = (String) row[6];
            Integer existingRating = (Integer) row[2];
            String existingComments = (String) row[7];
            //String updContent = this.txtContent.getText();
            
            extensionDialog.setCategory(existingCategory);
            extensionDialog.setRating(existingRating);
            extensionDialog.setComments(existingComments);
        
        
            // Εμφάνιση του παραθύρου του διαλόγου
            extensionDialog.setLocationRelativeTo(this);
            extensionDialog.setVisible(true);

            if (extensionDialog.performedSave()) {
                // Κλήση της νέας μεθόδου Update
                db.updateArticle(this.selectedArticleId, extensionDialog.getComments(), extensionDialog.getRating());
                mainWindow.setStatusMessage("Changes saved to DB!");

                // Ανανέωση των αποτελεσμάτων με τα νέα δεδομένα
                btnRestore.doClick();
            }
            extensionDialog.dispose();
        }
        
        //καθαρίζεται η επιλογή από την JList
        //και απενεργοποιείται και πάλι το btnSave μέχρι να επιλεγεί νέο άρθρο
        this.resetSelection();
    }//GEN-LAST:event_btnSaveActionPerformed

    /**
     * Εξάγει τα στατιστικά της εφαρμογής σε αρχείο PDF.
     * 
     * @param evt
     */
    private void btnExportPdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportPdfActionPerformed
        List<Object[]> searchStats = db.getSearchStatistics();
        List<Object[]> categoryStats = db.getCategoryStatistics();
        // αν η ΒΔ είναι κενή, εμφάνιση σχετικού μηνύματος
        //και επιστροφή στην κανονική ροή προγράμματος
        if (searchStats.isEmpty())
        {
            JOptionPane.showMessageDialog(
                    mainWindow, 
                    "Database is empty, nothing to export",
                    "EmptyDatabase Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        //έλεγχος αν το αρχείο statistics υπάρχει ήδη και απαίτηση επιβεβαίωσης
        //από το χρήστη για την επανεγγραφή του
        File currentPdf = new File("Statistics.pdf");
        if (currentPdf.exists()) {
            int reply = JOptionPane.showConfirmDialog(
                    null,
                    "Statistics.pdf already exists.\nOverwrite?",
                    "Confirm overwrite",
                    JOptionPane.YES_NO_OPTION);
            if (reply != JOptionPane.YES_OPTION) {
                return;
            }
        }
        //διαδικασία εξαγωγής pdf
        try {
            //αρχικοποίηση του pdfexporter
            pdfExporter pdf = new pdfExporter();
            pdf.init("Statistics");
            String[] searchColumnTitles = {"Λέξη-Κλειδί", "Πλήθος Αναζητήσεων", "Ποσοστό"};
            //προσθήκη πίνακα στατιστικών αναζήτησης
            pdf.addTable(searchStats, "Στατιστικά Αναζήτησης Wikipedia", searchColumnTitles);
            //προσθήκη πίνακα στατιστικών κατηγορίας 
            //εφόσον υπάρχουν αποθηκευμένα άρθρα
            if (!categoryStats.isEmpty()) {
                String[] catColumnTitles = {"Κατηγορία", "Πλήθος Άρθρων", "Ποσοστό"};
                pdf.addTable(categoryStats, "Στατιστικά Αποθηκευμένων Άρθρων", catColumnTitles);
            }
            pdf.export();
            //εμφάνιση μηνύματος επιτυχίας
            System.out.println("Successful PDF export.");
            JOptionPane.showMessageDialog(
                    mainWindow,  
                    "Pdf export successful, see \"Statistics.pdf\" for details",
                    "Pdf Export",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(
                    mainWindow, 
                    "Failed to create pdf file",
                    e.toString(),
                    JOptionPane.ERROR_MESSAGE);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.printf("%s%n", e.toString());
        }        
    }//GEN-LAST:event_btnExportPdfActionPerformed

    /**
     * Ανακτά και εμφανίζει όλα τα αποθηκευμένα άρθρα από τη βάση δεδομένων.
     */
    private void btnRestoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRestoreActionPerformed
        // Clear the fields
        this.clearAllFields();
        this.results = null;        
        // Ανάκτηση των δεδομένων από DB
        this.dbResults = this.db.getArticleWithCategory();
        
        if (this.dbResults.isEmpty()){
            mainWindow.setStatusMessage("Database is empty.");
        }
        
        //γέμισμα του JList με τους τίτλους των αποτελεσμάτων
        DefaultListModel<String> model = new DefaultListModel<>();
        
        for (Object[] row : this.dbResults) {
            String title = (String) row[5];            
            model.addElement(title);
        }
        
        //δυναμική ανάθεση του model στην Jlist
        jList1.setModel(model);
        mainWindow.setStatusMessage("Retrieving " + dbResults.size() + " articles from database.");
    }//GEN-LAST:event_btnRestoreActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExportPdf;
    private javax.swing.JButton btnRestore;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnStatistics;
    private javax.swing.JList<String> jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelHeader;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPaneMain;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblComments;
    private javax.swing.JLabel lblDBCategory;
    private javax.swing.JLabel lblDBComments;
    private javax.swing.JLabel lblDBDTstamp;
    private javax.swing.JLabel lblDBRating;
    private javax.swing.JTextArea lblDBTitle;
    private javax.swing.JLabel lblDTstamp;
    private javax.swing.JLabel lblRating;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JTextArea txtContent;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
