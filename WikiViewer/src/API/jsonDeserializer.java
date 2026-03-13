package API;

import Exceptions.NetworkException;
import Exceptions.NoResultsException;
import java.io.IOException;
import com.google.gson.JsonParseException;
import java.util.List;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Μεταφέρει τα δεδομένα από το response json στα πεδία αντικειμένων Article
 * και τα διαμορφώνει. Ανακτά το πλήρες κείμενο κάποιου άρθρου
 */
public class jsonDeserializer
{
    //λίστα που θα φιλοξενήσει τα τελικά διαμορφωμένα article objects
    private List <Article>  results;
    
    /**
     * Constructor class
     * @param json η απάντηση του Wikipedia API σε string
     * @throws NoResultsException όταν δεν υπάρχουν αποτελέσματα αναζήτησης
     * @throws JsonParseException σε περίπτωση malformed Json
     */
    public void parseData(String json) throws NoResultsException, JsonParseException, IOException, NetworkException
    {
        //χρήση JsonParser για απομόνωση του πεδίου "query" από το json
        JsonElement jsonTree = JsonParser.parseString(json);
        JsonObject jsonObject = jsonTree.getAsJsonObject();
        JsonElement queryResults = jsonObject.get("query");

       /*
        * αρχικοποίηση gson builder και παράμετροι:
        * method disableHtmlEscaping επιστρέφει το string χωρίς
        * unicode χαρακτήρες στα HTML tags->έτοιμο για επεξεργασία
        * από Jsoup
        */
        GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
        Gson gson = builder.create();
        
        //δημιουργία Java object Articles(ArrayList από Article objects) από json
        Articles articles = gson.fromJson(queryResults, Articles.class);
        
        //κλήση της λίστας μη διαμορφωμένων Articles
        this.results = articles._getArticles();
        //έλεγχος αν δεν υπάρχουν αποτελέσματα ( η λίστα είναι άδεια)
        if (this.results == null | this.results.isEmpty())
        {
            throw new NoResultsException("No search results");
        }
        //loop διαμόρφωσης των πεδίων ανα άρθρο
        for (Article a: this.results)
        {
            //διαμόρφωση πεδίων σε φιλική προς ανάγνωση μορφή*/
            a.removeSnippetHTML();
            a.parseDateTime();
        }
    }
    
    /**
     * Στέλνει αίτημα για το πλήρες κείμενο του άρθρου article από τη λίστα με τίτλο title
     * όταν ο χρήστης επιλέξει τον τίτλο από το MainSearchScreen
     * @param articles Λίστα με τα άρθρα που επέστρεψε η αναζήτηση
     * @param title 
     * @throws NoResultsException
     * @throws JsonParseException
     * @throws IOException
     * @throws NetworkException 
     */
    public void extractFullText(List<Article> articles, String title) throws NoResultsException, JsonParseException, IOException, NetworkException
    {
        WikiViewerAPI textApi = new WikiViewerAPI(title, false);
        //χρήση JsonParser για απομόνωση του πεδίου "text" από το json
        JsonElement textJsonTree = JsonParser.parseString(textApi.getWikiResponse());
        JsonObject textJsonObject = textJsonTree.getAsJsonObject();
        JsonElement textResults = textJsonObject.get("parse");
        GsonBuilder textBuilder = new GsonBuilder().disableHtmlEscaping();
        Gson textGson = textBuilder.create();
        //ανάθεση του κειμένου μόνο σε νέο Article object (textArticle)
        // και πίσω στο αρχικό Article object
        Article textArticle = textGson.fromJson(textResults, Article.class);
        for (Article a: articles)
        {
            if (a.getTitle() == title)
            {
                a.setText(textArticle.getText());
                a.removeTextHTML();
            }
        }
    }
    /**
     * 
     * @return List Articles με διαμορφωμένα πεδία
     */
    public List getArticles()
    {
        return this.results;
    }
}
