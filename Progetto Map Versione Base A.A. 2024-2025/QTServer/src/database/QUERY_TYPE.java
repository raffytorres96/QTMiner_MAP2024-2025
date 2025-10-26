package database;

/**
 * Enumerazione dei tipi di query di aggregazione supportate.
 * <p>
 * Specifica se l'utente è interessato al valore minimo o massimo
 * di un attributo in una query di aggregazione SQL.
 * Utilizzata per costruire query dinamiche con funzioni MIN o MAX.
 * </p>
 * 
 * @see Table_Data#getAggregateColumnValue(String, database.Table_Schema.Column, QUERY_TYPE)
 */
public enum QUERY_TYPE {
    /**
     * Identifica una query per la ricerca del valore minimo.
     * Corrisponde alla funzione SQL {@code MIN()}.
     */
    MIN,

    /**
     * Identifica una query per la ricerca del valore massimo.
     * Corrisponde alla funzione SQL {@code MAX()}.
     */
    MAX
}