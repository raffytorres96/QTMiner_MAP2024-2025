/**
 * Classe che modella l'eccezione controllata che viene 
 * sollevata dal sistema server e trasmessa al client dallo stream di connessione.
 */
public class ServerException extends Exception {
    /**
     * Costruttore della classe `ServerException`.
     * Crea una nuova eccezione con il messaggio di dettaglio specificato. 
     * @param message
     */
    public ServerException(String message){
        super(message);
    }
}
