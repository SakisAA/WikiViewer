package API;

import java.util.List;
import java.util.ArrayList;

/**
 * Αποτελείται από επιμέρους αντικείμενα
 * Article (με σχέση συναρμολόγησης - aggregation) σε λίστα.
 * Υλοποιεί την αποθήκευση των περιεχομένων του πεδίου search
 */

public class Articles 
{
    //περιλαμβάνουμε μόνο το πεδίο search
    //που περιέχει τα αποτελέσματα των άρθρων
    //(τα υπόλοιπα θα αγνοηθούν από το gson builder)
    
    //δομή πεδίου search στο json:
    /*"search":[ 
                {<πεδία ανα άρθρο(ns, title, pageid, etc>}, 
                {...}, 
                ... 
                ] 
    -> αποθηκεύεται σε τύπο List με Article objects*/
    
    private List <Article> search = new ArrayList <>();
    
    /**
     * Επιστρέφει τη λίστα articles με πεδία όπως υπάρχουν ακριβώς στο response json
     * @return List Articles με μη διαμορφωμένα πεδία
     */
    public List _getArticles()
    {
        return this.search;
    }
}
