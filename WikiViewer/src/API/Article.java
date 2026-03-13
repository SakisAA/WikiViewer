package API;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Περιέχει όλα τα πεδία
 * του κάθε array element του search, δηλαδή
 * τα στοιχεία των άρθρων που επέστρεψε η αναζήτηση
 */
public class Article 
{
    //πεδία ανα άρθρο στο json (παράδειγμα):
    /*
       {
        "ns": 0,
        "title": "Ελλάδα",
        "pageid": 412,
        "size": 185432,
        "wordcount": 18210,
        "snippet": "Η <span class=\"searchmatch\">Ελλάδα</span> (επίσημα: Ελληνική Δημοκρατία), είναι χώρα της Νοτιοανατολικής Ευρώπης...",
        "timestamp": "2024-03-15T10:00:00Z"
       }
    */
    /*όπως και στην Αrticles, δηλώνουμε  μόνο τα πεδία
      που μας ενδιαφέρουν για τον gson builder: title, snippet & timestamp*/
    private String title;
    private String snippet;
    private String timestamp; 
    private String text;
    /**
    * Αφαιρεί τα html tags από το κείμενο του snippet
    * και ενημερώνει το πεδίο του class ώστε να επιστρέφεται
    * το καθαρό κείμενο όταν καλειται το getter
    */
    public void removeSnippetHTML()
    {
        Document snip = Jsoup.parse(this.snippet);
        this.snippet = snip.text();
    }
    
    /**
     * Αφαιρεί HTML tags και μη αναγώσιμα τμήματα 
     * από το πλήρες κείμενο συγκεκριμένου άρθρου
     */
    public void removeTextHTML()
    {
        Document htmlText = Jsoup.parse(this.text);
        htmlText.select(".infobox").remove();
        htmlText.select(".reflist").remove();
        htmlText.select(".thumb").remove();
        htmlText.select(".table").remove();
        htmlText.select(".mw-editsection").remove();
        this.text = htmlText.text();
    }
    
    /**
     * Mορφοποιεί το timestamp μέσω της jodatime βιβλιοθήκης
     * από ISO 8601 σε dd/MM/yyyy - HH:mm
     */
    public void parseDateTime()
    {
        DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
        
        DateTime dt = parser.parseDateTime(this.timestamp);
        
        //επιλογή φορματ εμφάνισης του timestamp
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy - HH:mm");
       
        this.timestamp = formatter.print(dt);
    }
    
    /**
     * Setter για τη μεταβλητή text
     * @param text το μη μορφοποιημένο κείμενο με HTML tags
     */
    public void setText(String text)
    {
        this.text = text;
    }
    
    /*Getter methods για τα πεδία της κλάσης*/
    /**
     * Επιστρέφει τον τίτλο του άρθρου ως String
     * @return title
     */
    public String getTitle()
    {
        return this.title;
    }
    /**
     * Επιστρέφει το snippet που εμφανίζεται το λήμμα αναζήτησης ως String
     * @return snippet
     */
    public String getSnippet()
    {
        return this.snippet;
    }
    /**
     * Επιστρέφει το μορφοποιημένο timestamp ως String
     * @return timestamp 
     */
    public String getTimeStamp()
    {
        return this.timestamp;
    }
    /**
     * Επιστρέφει το πλήρες κείμενο ως String
     * @return text
     */
    public String getText()
    {
        return this.text;
    }
}
