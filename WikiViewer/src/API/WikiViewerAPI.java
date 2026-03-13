package API;

import java.io.IOException;
import Exceptions.NetworkException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Διαμορφώνει το request url για το Wikipedia API
 * και λαμβάνει την απάντηση json σε μορφή String
 */
public class WikiViewerAPI 
{
    private String urlToGet;
    //μεταβλητή που καθορίζει τη λειτουργία του class, true για αναζήτηση άρθρου,
    //false για ανάκτηση πλήρους κειμένου συγκεκριμένου άρθρου
    private boolean search;
    /**
     * Constructor class με ορίσματα
     * @param term ο όρος αναζήτησης του χρήστη ή τίτλος άρθρου για πλήρες κείμενο
     * @param mode καθορίζει αν ζητείται json αποτελεσμάτων αναζήτησης ή πλήρους κειμένου
     * @throws IOException εάν ληφθεί κενός (null) όρος
     */
    public WikiViewerAPI(String term, boolean mode) throws IOException
    {
        this.search = mode;
        if (term.isEmpty())
        {
            throw new IOException("Empty input string");
        }
        if (this.search)
        {
            this.urlToGet = 
                "https://el.wikipedia.org/w/api.php?action=query&list=search&srsearch=" 
                + term + 
                "&format=json";
        }
        else
        {
            this.urlToGet =
                    "https://el.wikipedia.org/w/api.php?action=parse&formatversion=2&page="
                    + term
                    +"&prop=text&format=json";
        }
        
    }
    
    /**
     * Στέλνει αίτημα στο Wikipedia API και λαμβάνει response
     * @return response body ως String
     * @throws NetworkException εάν το αίτημα δεν είναι επιτυχές
     */
    public String getWikiResponse() throws NetworkException
    {
        OkHttpClient client = new OkHttpClient();

        // build request με user agent header ώστε να γίνει αποδεκτό από το API
        Request request = new Request.Builder()
            .url(this.urlToGet)
            .header("User-Agent", "MyJavaWikiApp/1.0 (nickzoumakis@gmail.com)") 
            .build();
        // επιστρορφή της απάντησης με έλεγχο σφάλματος σε κάθε επίπεδο
        try(Response response = client.newCall(request).execute())
        {
            if (!response.isSuccessful())
            {
                throw new NetworkException("No response from API");
            }
            return response.body().string();
        }
        catch (IOException e)
        {
            throw new NetworkException("Network Error: " + e.getMessage());
        }
    }
}
