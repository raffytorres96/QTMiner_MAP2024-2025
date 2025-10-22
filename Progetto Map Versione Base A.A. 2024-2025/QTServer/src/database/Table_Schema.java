package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Costruisce e modella lo schema di una tabella del database relazionale.
 * <p>
 * Questa classe si connette al database tramite un oggetto {@link DbAccess},
 * legge i metadati di una tabella specifica e memorizza le informazioni
 * sulle sue colonne (nome e tipo semplificato) in una lista interna.
 */
public class Table_Schema {
    /**
     * L'istanza di DbAccess utilizzata per ottenere la connessione al database.
     */
    DbAccess db;
    /**
     * Elenco delle colonne che compongono lo schema della tabella.
     */
    public class Column{
        /**
         * Il nome della colonna.
         */
        private String name;
        /**
         * Il tipo Java semplificato (es. "number" o "string").
         */
        private String type;
        /**
         * Costruisce un'istanza di Colonna.
         *
         * @param name il nome della colonna nel database.
         * @param type il tipo Java semplificato ("string" o "number").
         */
        Column(String name,String type){
            this.name=name;
            this.type=type;
        }
        /**
         * Restituisce il nome della colonna.
         *
         * @return il nome della colonna.
         */
        public String getColumnName(){
            return name;
        }
        /**
         * Controlla se il tipo della colonna è numerico.
         *
         * @return {@code true} se il tipo è "number", {@code false} altrimenti.
         */
        public boolean isNumber(){
            return type.equals("number");
        }
        /**
         * Restituisce una rappresentazione testuale della colonna.
         *
         * @return una stringa nel formato "nome:tipo".
         */
        public String toString(){
            return name+":"+type;
        }
    }
    /**
     * Costruisce lo schema della tabella leggendo i metadati dal database.
     * <p>
     * Si connette al database, interroga i metadati per la tabella specificata
     * e popola la lista {@code tableSchema} con le colonne trovate,
     * mappando i tipi SQL in tipi Java semplificati ("string" o "number").
     *
     * @param db l'oggetto {@link DbAccess} che fornisce la connessione al database.
     * @param tableName il nome della tabella di cui estrarre lo schema.
     * @throws SQLException se si verifica un errore durante l'accesso ai
     * metadati del database (es. tabella non trovata).
     */
    List<Column> tableSchema = new ArrayList<Column>();

    public Table_Schema(DbAccess db, String tableName) throws SQLException{
        this.db = db;
        HashMap<String,String> mapSQL_JAVATypes = new HashMap<String, String>();
        //http://java.sun.com/j2se/1.3/docs/guide/jdbc/getstart/mapping.html
        mapSQL_JAVATypes.put("CHAR","string");
        mapSQL_JAVATypes.put("VARCHAR","string");
        mapSQL_JAVATypes.put("LONGVARCHAR","string");
        mapSQL_JAVATypes.put("BIT","string");
        mapSQL_JAVATypes.put("SHORT","number");
        mapSQL_JAVATypes.put("INT","number");
        mapSQL_JAVATypes.put("LONG","number");
        mapSQL_JAVATypes.put("FLOAT","number");
        mapSQL_JAVATypes.put("DOUBLE","number");

        Connection con = db.getConnection();
        DatabaseMetaData meta = con.getMetaData();
        ResultSet res = meta.getColumns(null, null, tableName, null);

        while (res.next()) {

            if(mapSQL_JAVATypes.containsKey(res.getString("TYPE_NAME")))
                tableSchema.add(new Column(
                        res.getString("COLUMN_NAME"),
                        mapSQL_JAVATypes.get(res.getString("TYPE_NAME")))
                );
        }
        res.close();
    }
    /**
     * Restituisce il numero totale di attributi (colonne) nello schema.
     *
     * @return il numero di colonne mappate.
     */

    public int getNumberOfAttributes(){
        return tableSchema.size();
    }
    /**
     * Recupera la colonna in base al suo indice.
     *
     * @param index l'indice (basato su zero) della colonna da recuperare.
     * @return l'oggetto {@link Column} corrispondente a quell'indice.
     * @throws IndexOutOfBoundsException se l'indice è fuori range
     * (index < 0 || index >= getNumberOfAttributes()).
     */
    public Column getColumn(int index){
        return tableSchema.get(index);
    }


}

		     


