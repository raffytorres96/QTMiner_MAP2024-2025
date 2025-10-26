package database;

/**
 * Eccezione lanciata quando si verifica un errore nella connessione al database.
 * <p>
 * Questa eccezione viene sollevata quando il tentativo di stabilire una connessione
 * al database fallisce, ad esempio per driver mancanti, credenziali errate,
 * server non raggiungibile o altri problemi di connettività.
 * </p>
 * 
 * @see DbAccess#initConnection()
 */
public class DatabaseConnectionException extends Exception {
    
    /**
     * Costruisce una nuova eccezione senza messaggio descrittivo.
     */
    public DatabaseConnectionException() {
        super();
    }
    
    /**
     * Costruisce una nuova eccezione con il messaggio descrittivo specificato.
     * 
     * @param message il messaggio descrittivo dell'eccezione
     */
    public DatabaseConnectionException(String message) {
        super(message);
    }
}