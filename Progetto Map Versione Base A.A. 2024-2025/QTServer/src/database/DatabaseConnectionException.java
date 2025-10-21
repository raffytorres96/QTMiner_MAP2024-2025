package database;

/**
 * La classe 'DatabaseConnectionException' modella il 
 * fallimento nella connessione al database.
 */
public class DatabaseConnectionException extends Exception {
    
    /**
     * Costruttore di default.
     */
    public DatabaseConnectionException() {
        super();
    }
    
    /**
     * Costruttore con messaggio personalizzato.
     * @param message il messaggio descrittivo dell'eccezione.
     */
    public DatabaseConnectionException(String message) {
        super(message);
    }
    
    // /**
    //  * Costruttore con messaggio e causa.
    //  * @param message il messaggio descrittivo dell'eccezione.
    //  * @param cause l'eccezione che ha causato questo errore.
    //  */
    // public DatabaseConnectionException(String message, Throwable cause) {
    //     super(message, cause);
    // }
    
    // /**
    //  * Costruttore con causa.
    //  * @param cause l'eccezione che ha causato questo errore.
    //  */
    // public DatabaseConnectionException(Throwable cause) {
    //     super(cause);
    // }
}