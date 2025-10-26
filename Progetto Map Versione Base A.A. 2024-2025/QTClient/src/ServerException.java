/**
 * Eccezione personalizzata che rappresenta errori verificatisi sul lato server
 * durante l'elaborazione delle richieste del client.
 * 
 * <p>Questa eccezione controllata viene sollevata dal sistema server quando si verificano
 * condizioni di errore (come tabelle non trovate, file inesistenti, errori di clustering)
 * e viene trasmessa al client attraverso lo stream di connessione per permettere
 * una gestione appropriata dell'errore lato client.</p>
 * 
 * <p>Esempi di utilizzo:
 * <ul>
 *   <li>Tabella database non esistente o non valida</li>
 *   <li>File cluster non trovato</li>
 *   <li>Raggio di clustering non appropriato</li>
 *   <li>Operazione richiesta prima del caricamento dei dati</li>
 * </ul>
 * </p>
 *
 * @see Exception
 */
public class ServerException extends Exception {
    
    /**
     * Costruttore della classe ServerException.
     * Crea una nuova eccezione con il messaggio di dettaglio specificato.
     * 
     * <p>Il messaggio dovrebbe descrivere chiaramente l'errore verificatosi
     * sul server per permettere al client di informare adeguatamente l'utente.</p>
     * 
     * @param message il messaggio descrittivo dell'errore che verrà visualizzato al client
     */
    public ServerException(String message){
        super(message);
    }
}