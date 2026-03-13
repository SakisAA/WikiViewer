package PdfExporter;

import java.io.IOException;
import java.util.List;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.UnitValue;

/**
 * Εξάγει σε pdf τα στατιστικά από τη ΒΔ
 */
public class pdfExporter 
{
    //μεταβλητές που χρησιμοποιούνται από όλα τα methods
    private String filename;
    private Document document;
    private PdfFont greekFontBold;
    private PdfFont greekFont;
    
    /**
     * Constructor που αρχικοποιεί τα fonts που θα χρησιμοποιηθούν
     * (truetype Arial για υποστήριξη ελληνικών) - Απαιτεί το φάκελο Fonts
     * @throws IOException σε περίπτωση αδυναμίας πρόσβασης στα ttf αρχεία
     */
    public pdfExporter() throws IOException 
    {
        // λήψη του path για κάθε font ως input stream
        java.io.InputStream boldStream = getClass().getResourceAsStream("/fonts/arialbd.ttf");
        java.io.InputStream regStream = getClass().getResourceAsStream("/fonts/arial.ttf");

        if (boldStream == null || regStream == null) {
            throw new IOException("Fonts not found in JAR! Ensure they are in src/fonts/");
        }

        // ανάγνωση ως byte arrays
        byte[] boldBytes = boldStream.readAllBytes();
        byte[] regBytes = regStream.readAllBytes();

        // δημιουργία των κατάλληλων fonts
        this.greekFontBold = com.itextpdf.kernel.font.PdfFontFactory.createFont(
            boldBytes, 
            com.itextpdf.io.font.PdfEncodings.IDENTITY_H, 
            com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED
        );

        this.greekFont = com.itextpdf.kernel.font.PdfFontFactory.createFont(
            regBytes, 
            com.itextpdf.io.font.PdfEncodings.IDENTITY_H, 
            com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED
        );

        boldStream.close();
        regStream.close();
    }  
    /**
     * Αρχικοποιεί το pdf document με filename και τίτλο
     * @param filename όνομα αρχείου όπως δόθηκε από το χρήστη
     * @throws IOException σε περίπτωση αποτυχίας δημιουργίας του αρχείου
     */
    public void init(String filename) throws IOException
    {
        this.filename = filename;
        //αρχικοποίηση pdfWriter&Document από iText
        //όρισμα του writer το path για το αρχείο
        PdfWriter writer = new PdfWriter(filename + ".pdf");
        PdfDocument pdf = new PdfDocument(writer);
       this.document = new Document(pdf);
    }
    
    /**
     * Δημιουργεί τους πίνακες στατιστικών και τους τοποθετεί στο pdf
     * @param dbList λίστα Object[] στατιστικών όπως επιστρέφεται από τη ΒΔ
     * @param columnTitles array με τους τίτλους των στηλών
     * @param title ο τίτλος του πίνακα
     * @throws ArrayIndexOutOfBoundsException αν το πλήθος τίτλων στηλών δεν
     * αντιστοιχεί στο ίδιο πλήθος στατιστικών στοιχείων κάθε object της dbList
     */
    public void addTable(List <Object[]> dbList, String title, String[] columnTitles) throws ArrayIndexOutOfBoundsException
    {
        //Τίτλος του πίνακα
        this.document.add(new Paragraph (title)
                .setFont(greekFont)
                .setFontSize(18));
        //το πλήθος των στηλών και των δεδομένων κάθε στοιχείου πρέπει να ταιριάζουν
        if (dbList.get(0).length != columnTitles.length)
        {
            throw new ArrayIndexOutOfBoundsException("Non matching lengths of dblist and column titles");
        }
        //δημιουργία columnTitles στηλών ίσου μήκους
        int columnCount = columnTitles.length;
        float[] columnWidths = new float[columnTitles.length];
        for (int i = 0; i < columnCount; i++)
        {
            columnWidths[i] = 1;
        }
        Table table = new Table(UnitValue.createPointArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));
        
        //προσθήκη τίτλων στηλών
        for (int i = 0; i < columnCount; i++)
        {
            table.addHeaderCell(new Cell().add(new Paragraph(columnTitles[i]).setFont(this.greekFontBold)));
        }
        //για κάθε index του κάθε Object[] της λίστας δημιουργούνται κελιά
        dbList.stream()
                .limit(10)
                .forEach(o ->
        {
            for (int i = 0; i < columnCount; i++)
            {
                String data = o[i].toString();
               table.addCell(new Cell().add(new Paragraph(data).setFont(greekFont))); 
            }
        });
        //προσθήκη του πίνακα στο pdf document
        this.document.add(table);
        //μικρό κενό μεταξύ των πινάκων
        for (int i = 0; i < 5; i++)
        {
            this.document.add(new Paragraph(" "));
        }
    }
    
    /**
     * Ολκληρώνει τη δημιουργία του pdf και εμφανίζει σχετικό μήνυμα στο console
     */
    public void export()
    {
        this.document.close();
        System.out.println("Pdf succesfuly created: " + filename + ".pdf");
    }
}
