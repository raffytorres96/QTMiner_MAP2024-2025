package data;

/**
 * Eccezione controllata lanciata quando un dataset risulta vuoto.
 * 
 * <p>Questa eccezione viene tipicamente sollevata quando:
 * <ul>
 *   <li>Si tenta di caricare un dataset da una tabella vuota del database</li>
 *   <li>Tutte le righe vengono filtrate durante il caricamento</li>
 *   <li>Si verificano errori che portano a un dataset senza esempi</li>
 * </ul>
 * </p>
 * 
 * <p>Essendo un'eccezione controllata (extends {@link Exception}), deve essere
 * dichiarata nel metodo che la può sollevare o gestita con un blocco try-catch.</p>
 * 
 * @see Data
 */
public class EmptyDatasetException extends Exception {
    
    /**
     * Costruttore della classe EmptyDatasetException.
     * Crea una nuova eccezione con il messaggio descrittivo specificato.
     * 
     * @param message il messaggio che descrive la causa dell'eccezione
     *                (es. "Dataset vuoto per la tabella: tableName")
     */
    public EmptyDatasetException(String message) {
        super(message);
    }
}