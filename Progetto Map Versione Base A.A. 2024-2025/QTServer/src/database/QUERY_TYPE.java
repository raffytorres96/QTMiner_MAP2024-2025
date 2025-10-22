package database;

/**
 * Enumera i tipi di query di aggregazione supportate.
 * Specifica se l'utente è interessato al valore minimo (MIN)
 * o al valore massimo (MAX) di un attributo.
 */
public enum QUERY_TYPE {
    /**
     * Identifica una query per la ricerca del valore minimo.
     */
    MIN,

    /**
     * Identifica una query per la ricerca del valore massimo.
     */
    MAX
}
