package Exceptions;

/**
 * Γίνεται τhrow από τον dbmanager όταν ο χρήστης προσπαθήσει να αποθηκεύσει
 * άρθρο που υπάρχει ήδη στη ΒΔ
 */
public class ExistingArticleException extends Exception
{
    /**
     * Καθορίζει το μήνυμα που εμφανίζεται στην κονσόλα
     * @param message 
     */
    public ExistingArticleException(String message)
    {
        super(message);
    }
}
