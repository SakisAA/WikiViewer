package DBmanager;

import Exceptions.ExistingArticleException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import database.Article;
import database.SearchHistory;

/**
 * Η κλάση DBmanager λειτουργεί ως συνοριακή(boundary class) 
 * για την επικοινωνία της εφαρμογής με την ενσωματωμένη βάση δεδομένων.
 */
public class DBmanager {

    // static για να υπάρχει μόνο ένα.
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("WikiViewerPU");
    private EntityManager em;

    // Constructor για DBManager()
    public DBmanager() {
        em = emf.createEntityManager();
    }

    /**
     * Αποθηκεύει στη βάση (Article, Category, SearchHistory).
     * @param object είναι το αντικείμενο για αποθήκευση
     */
    public void saveObject(Object object) {
        try {
            em.getTransaction().begin();
            em.persist(object);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback(); // Ακύρωση αν γίνει λάθος
            }
            e.printStackTrace(); // Εμφάνιση του λάθους στην κονσόλα
        }
    }

    /**
     * Επιστρέφει όλα τα αποθηκευμένα άρθρα.
     * @return Λίστα άρθρων
     */
    public List<Article> getAllArticles() {
        // Επιλέγουμε τα πάντα από την κλάση Article με JPQL query
        Query q = em.createQuery("SELECT a FROM Article a", Article.class);
        return q.getResultList();
    }

    /**
     * Αναζητά άρθρα στη Βάση Δεδομένων που περιέχουν το keyword
     * @param keyword Η λέξη προς αναζήτηση
     * @return Λίστα με τα άρθρα που βρέθηκαν
     */
    public List<database.Article> searchStoredArticles(String keyword) {
        // Έλεγχος αν το keyword είναι κενό
        if (keyword == null || keyword.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }

        try {
            // το lower τα κανει όλα μικρα για να μην ειναι case sensitive
            String jpql = "SELECT a FROM Article a WHERE LOWER(a.title) LIKE :kw OR LOWER(a.snippet) LIKE :kw OR LOWER(a.userComments) LIKE :kw OR LOWER(a.categoryId.name) LIKE :kw";
            
            Query q = em.createQuery(jpql, database.Article.class);
            
            // το % για να βρει τη λέξη οπουδήποτε μέσα στο κείμενο
            String searcheverywhere = "%" + keyword.toLowerCase() + "%";
            
            q.setParameter("kw", searcheverywhere);
            
            return q.getResultList();
            
        } catch (Exception e) {
            e.printStackTrace();
            // Σε περίπτωση σφάλματος επιστρέφουμε άδεια λίστα
            return new java.util.ArrayList<>(); 
        }
    }
    /**
     * Καταγράφει μια αναζήτηση στο Ιστορικό.
     * @param keyword Η λέξη που αναζητήθηκε
     */
    public void addSearchToHistory(String keyword) {
        SearchHistory sh = new SearchHistory();
        sh.setKeyword(keyword);
        saveObject(sh);
    }
    
    /**
     * Αποθηκεύει ένα άρθρο μαζί με Κατηγορία, Σχόλια και Βαθμολογία.
     * @param apiArt Το άρθρο που ήρθε από το API
     * @param categoryName Το όνομα της κατηγορίας
     * @param comments Τα σχόλια του χρήστη
     * @param rating Η βαθμολογία του χρήστη
     */
    public void saveApiArticle(API.Article apiArt, String categoryName, String comments, Integer rating){
        try { 
            em.getTransaction().begin();
            database.Category category;           
            // Αναζήτηση αν υπάρχει ήδη η κατηγορία
            try {
                Query q = em.createQuery("SELECT c FROM Category c WHERE c.name = :name", database.Category.class);
                q.setParameter("name", categoryName);
                category = (database.Category) q.getSingleResult();
            } catch (Exception e) {
                // Αν δεν υπάρχει τη δημιουργούμε
                category = new database.Category();
                category.setName(categoryName);
                em.persist(category);
            }

            // Δημιουργία αντικειμένου άρθρου
            database.Article dbArt = new database.Article();
            dbArt.setTitle(apiArt.getTitle());
            dbArt.setSnippet(apiArt.getSnippet());
            dbArt.setText(apiArt.getText());
            dbArt.setArticleTimestamp(apiArt.getTimeStamp());
            
            // Προσθήκη των έξτρα στοιχείων
            dbArt.setUserComments(comments);
            dbArt.setRating(rating);
            dbArt.setCategoryId(category);

            em.persist(dbArt);
            // Ολοκλήρωση των αλλαγών
            em.getTransaction().commit();           
            System.out.println("Επιτυχής αποθήκευση άρθρου και κατηγορίας!");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback(); //ακύρωση αλλαγών αν εμφανιστεί σφάλμα
            }
            e.printStackTrace();// Εμφάνιση του λάθους στην κονσόλα
        }
    }
    
    /**Ελέγχει την ύπαρξη άρθρου στη database
     * @throws ExistingArticleException αν το άρθρο υπάρχει ήδη στη database
     */
    public boolean articleExists(API.Article apiArt) throws ExistingArticleException{
        // Έλεγχος αν υπάρχει ήδη το άρθρο στη βάση
        Query checkQuery = em.createQuery("SELECT a FROM Article a WHERE a.title = :title", database.Article.class);
        checkQuery.setParameter("title", apiArt.getTitle());

        List<database.Article> existingArticles = checkQuery.getResultList();

        if (!existingArticles.isEmpty()) {
            System.out.println("Το άρθρο '" + apiArt.getTitle() + "' υπάρχει ήδη στη βάση!");
            //έξοδος απο μεθοδο με exception ώστε να γίνει catch από τη MainSearchScreen
            throw new ExistingArticleException("Existing Article");
        }
        return false;
    }
    /**
     * Επιστρέφει στατιστικά αναζήτησης.
     * @return λίστα πινάκων [0]=λέξη, [1]=πλήθος, [2]=ποσοστό
     */
    public List<Object[]> getSearchStatistics() {
        Long totalSearches = (Long) em.createQuery("SELECT COUNT(s) FROM SearchHistory s").getSingleResult();
        
        if (totalSearches == null || totalSearches == 0) {
            return new java.util.ArrayList<>();
        }

        // Ομαδοποίηση ανά λέξη και καταμέτρηση με δημοφιλέστερα πρώτα (count DESC)
        String jpql = "SELECT s.keyword, COUNT(s) FROM SearchHistory s GROUP BY s.keyword ORDER BY COUNT(s) DESC";
        List<Object[]> results = em.createQuery(jpql).getResultList();
        
        // Υπολογισμος ποσοστού
        List<Object[]> finalResults = new java.util.ArrayList<>();
        for (Object[] row : results) {
            String keyword = (String) row[0];
            Long count = (Long) row[1];
            double percentage = (count.doubleValue() / totalSearches) * 100.0;
            String percentStr = String.format("%.1f%%", percentage);
            
            // πρόσθεση στοιχείων στη λίστα
            finalResults.add(new Object[]{keyword, count, percentStr});
        }
        
        return finalResults;
    }

    /**
     * Επιστρέφει το πλήθος των άρθρων ανά κατηγορία.
     * @return λίστα πινάκων με στοιχείο[0] = Κατηγορία [1] = πλήθος άρθρων και [2]=ποσοστό
     */
    public List<Object[]> getCategoryStatistics() {
        // 1. Βρες το ΣΥΝΟΛΟ των άρθρων
        Long totalArticles = (Long) em.createQuery("SELECT COUNT(a) FROM Article a").getSingleResult();
        
        if (totalArticles == null || totalArticles == 0) {
            return new java.util.ArrayList<>();
        }

        // Ομαδοποίηση ανά κατηγορία και καταμέτρηση με περισσότερα πρώτα (DESC)
        String jpql = "SELECT c.name, COUNT(a) FROM Article a JOIN a.categoryId c GROUP BY c.name ORDER BY COUNT(a) DESC";
        List<Object[]> results = em.createQuery(jpql).getResultList();
        
        // υπολογισμός ποσοστού
        List<Object[]> finalResults = new java.util.ArrayList<>();
        for (Object[] row : results) {
            String catName = (String) row[0];
            Long count = (Long) row[1];        
            double percentage = (count.doubleValue() / totalArticles) * 100.0;
            String percentStr = String.format("%.1f%%", percentage);
            // προσθήκη στοιχείνω στην λίστα
            finalResults.add(new Object[]{catName, count, percentStr});
        }
        
        return finalResults;
    }
    
    public List<Object[]> getArticleWithCategory(){
        try {
            String txt = "SELECT  a.ID, " +
                         "a.ARTICLE_TIMESTAMP, " +
                         "a.RATING, " +
                         "a.SNIPPET, " +
                         "a.TEXT, " +
                         "a.TITLE, " +
                         "c.NAME AS CATEGORY_NAME, " +
                         "a.USER_COMMENTS " +
                         "FROM ARTICLE a " +
                         "JOIN CATEGORY c ON a.CATEGORY_ID = c.ID";
            Query q = em.createNativeQuery(txt);
            
            List<Object[]> results = q.getResultList();
            return results;
        }
        catch (Exception e) {
            e.printStackTrace();
            // Σε περίπτωση σφάλματος επιστρέφουμε άδεια λίστα
            return new java.util.ArrayList<>();
        }
    }
    
    /**
    * Ενημερώνει ένα υπάρχον άρθρο στη βάση δεδομένων.
    * @param id Το μοναδικό ID του άρθρου
    * @param comments Τα νέα σχόλια
    * @param rating Η νέα βαθμολογία
    */
   public void updateArticle(int id, String comments, Integer rating) {
       try {
           em.getTransaction().begin();

           // Εύρεση του άρθρου από τη βάση μέσω του ID
           database.Article article = em.find(database.Article.class, id);

           if (article != null) {
               article.setUserComments(comments);
               article.setRating(rating);
               //article.setSnippet(txtContent);
               em.getTransaction().commit();
               System.out.println("Επιτυχής ενημέρωση άρθρου ID: " + id);
           } else {
               em.getTransaction().rollback();
           }
       } catch (Exception e) {
           if (em.getTransaction().isActive()) em.getTransaction().rollback();
           e.printStackTrace();
       }
   }
    
    // Κλείσιμο σύνδεσης με τον τερματισμό
    public void close() {
        em.close();
    }
}
