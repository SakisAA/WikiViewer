package Exceptions;

/**
 * Γίνεται throw όταν δεν έχουν επιστραφεί έγκυρα αποτελέσματα
 * για εμφάνιση
 */
public class NoResultsException extends Exception
{
    /**
     * Καθορίζει το μήνυμα που εμφανίζεται στην κονσόλα
     * @param message 
     */
    public NoResultsException(String message)
    {
        super(message);
    }
}
