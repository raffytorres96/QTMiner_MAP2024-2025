package database;

/**
 * Eccezione lanciata quando una query restituisce un ResultSet vuoto.
 * <p>
 * Questa eccezione viene sollevata quando un'operazione si aspetta di trovare
 * almeno un record nel database, ma la query eseguita non restituisce alcun risultato.
 * Tipicamente utilizzata quando si interroga una tabella che risulta essere vuota.
 * </p>
 * 
 * @see Table_Data#getDistinctTransazioni(String)
 */
public class EmptySetException extends Exception {

    /**
     * Costruisce una nuova eccezione senza messaggio descrittivo.
     */
    public EmptySetException(){
        super();
    }

    /**
     * Costruisce una nuova eccezione con il messaggio descrittivo specificato.
     * 
     * @param message il messaggio descrittivo dell'eccezione
     */
    public EmptySetException(String message){
        super(message);
    }
}