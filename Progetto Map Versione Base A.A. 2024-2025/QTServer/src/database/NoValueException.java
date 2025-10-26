package database;

/**
 * Eccezione lanciata quando non è presente alcun valore in un ResultSet.
 * <p>
 * Questa eccezione viene sollevata quando si tenta di accedere a un valore
 * in un ResultSet vuoto o quando un'operazione di aggregazione (come MIN o MAX)
 * restituisce null, indicando l'assenza di dati validi nella colonna interrogata.
 * </p>
 * 
 * @see Table_Data#getAggregateColumnValue(String, database.Table_Schema.Column, QUERY_TYPE)
 */
public class NoValueException extends Exception {
    
    /**
     * Costruisce una nuova eccezione senza messaggio descrittivo.
     */
    public NoValueException() {
        super();
    }
    
    /**
     * Costruisce una nuova eccezione con il messaggio descrittivo specificato.
     * 
     * @param message il messaggio descrittivo dell'eccezione
     */
    public NoValueException(String message) {
        super(message);
    }
}