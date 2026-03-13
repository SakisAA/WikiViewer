//custom exceptions πακέτο
package Exceptions;

/**
 * Γίνεται throw όταν δεν υπάρχει δίκτυο
 * ή δε λαμβάνεται απάντηση από το Wikipedia API
 */
public class NetworkException extends Exception
{
    /**
     * Καθορίζει το μήνυμα που εμφανίζεται στην κονσόλα
     * @param message 
     */
    public NetworkException(String message)
    {
        super(message);
    }
}
