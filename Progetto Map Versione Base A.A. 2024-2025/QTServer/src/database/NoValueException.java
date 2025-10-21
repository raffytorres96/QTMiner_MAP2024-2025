package database;

/**
 * Classe che modella l’assenza di un valore all’interno di un resultset
 */
public class NoValueException extends Exception {
    
    /**
     * Costruttore di default.
     */
    public NoValueException() {
        super();
    }
    
    /**
     * Costruttore con messaggio personalizzato.
     * @param message il messaggio descrittivo dell'eccezione.
     */
    public NoValueException(String message) {
        super(message);
    }
}