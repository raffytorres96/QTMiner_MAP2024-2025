package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import database.Table_Schema.Column;

/**
 * Gestisce l'estrazione e l'elaborazione dei dati da una tabella del database.
 * <p>
 * Questa classe fornisce metodi per:
 * <ul>
 *   <li>Estrarre tutte le transazioni distinte da una tabella</li>
 *   <li>Ottenere i valori distinti di una specifica colonna</li>
 *   <li>Calcolare valori aggregati (MIN, MAX) per una colonna</li>
 * </ul>
 * Le singole transazioni sono modellate dalla classe {@link Example}.
 * </p>
 * 
 * @see Example
 * @see Table_Schema
 */
public class Table_Data {

    /**
     * Oggetto di accesso alla base di dati per l'esecuzione delle query.
     */
    DbAccess db;

    /**
     * Costruisce un'istanza di Table_Data con l'oggetto di accesso al database specificato.
     * 
     * @param db l'oggetto DbAccess per la connessione al database
     */
    public Table_Data(DbAccess db) {
        this.db = db;
    }

    /**
     * Estrae tutte le transazioni distinte dalla tabella specificata.
     * <p>
     * Questo metodo:
     * <ol>
     *   <li>Ricava lo schema della tabella tramite {@link Table_Schema}</li>
     *   <li>Costruisce ed esegue una query SQL SELECT DISTINCT</li>
     *   <li>Per ogni tupla nel ResultSet, crea un oggetto {@link Example}</li>
     *   <li>Estrae i valori dei singoli campi (usando getDouble() o getString()
     *       in base al tipo di colonna) e li aggiunge all'Example</li>
     * </ol>
     * </p>
     * 
     * @param table il nome della tabella da cui estrarre le transazioni
     * @return una lista ({@link LinkedList}) di oggetti {@link Example},
     * ognuno rappresentante una tupla (transazione) distinta
     * @throws SQLException se si verifica un errore durante l'interrogazione SQL
     * (es. tabella non trovata, errore di sintassi, tabella senza attributi)
     * @throws EmptySetException se la tabella specificata risulta essere vuota
     * (non contiene tuple)
     */
    public List<Example> getDistinctTransazioni(String table) throws SQLException, EmptySetException{

        LinkedList<Example> transSet = new LinkedList<Example>();
        Statement statement;
        Table_Schema tSchema = new Table_Schema(db, table);

        String query = "select distinct ";

        for(int i = 0; i < tSchema.getNumberOfAttributes(); i++){
            Column c = tSchema.getColumn(i);
            if(i > 0)
                query += ",";
            query += c.getColumnName();
        }

        if(tSchema.getNumberOfAttributes() == 0)
            throw new SQLException("La tabella '" + table + "' non ha attributi.");

        query += (" FROM " + table);

        statement = db.getConnection().createStatement();
        ResultSet rs = statement.executeQuery(query);
        boolean empty = true;

        while (rs.next()) {
            empty = false;
            Example currentTuple = new Example();
            for(int i = 0; i < tSchema.getNumberOfAttributes(); i++)
                if(tSchema.getColumn(i).isNumber())
                    currentTuple.add(rs.getDouble(i + 1));
                else
                    currentTuple.add(rs.getString(i + 1));
            transSet.add(currentTuple);
        }
        rs.close();
        statement.close();
        if(empty) throw new EmptySetException("La tabella '" + table + "' è vuota.");

        return transSet;
    }

    /**
     * Estrae i valori distinti ordinati di una colonna specificata.
     * <p>
     * Formula ed esegue una query SQL per estrarre i valori distinti dalla colonna,
     * ordinandoli in modo crescente. I valori vengono inseriti in un {@link TreeSet}
     * che mantiene l'ordinamento naturale.
     * </p>
     * 
     * @param table il nome della tabella da cui estrarre i valori
     * @param column l'oggetto {@link Column} che rappresenta la colonna di interesse
     * @return un insieme ({@link Set}) ordinato ({@link TreeSet}) contenente
     * i valori distinti trovati nella colonna
     * @throws SQLException se si verifica un errore durante l'interrogazione SQL
     */
    public Set<Object> getDistinctColumnValues(String table, Column column) throws SQLException{

        Set<Object> valueSet = new TreeSet<Object>();
        Statement statement;
        Table_Schema tSchema = new Table_Schema(db, table);

        String query = "select distinct ";
        query += column.getColumnName();
        query += (" FROM " + table);
        query += (" ORDER BY " + column.getColumnName());

        statement = db.getConnection().createStatement();
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()) {
            if(column.isNumber())
                valueSet.add(rs.getDouble(1));
            else
                valueSet.add(rs.getString(1));
        }
        rs.close();
        statement.close();

        return valueSet;
    }

    /**
     * Calcola un valore aggregato (MIN o MAX) per una colonna specificata.
     * <p>
     * Formula ed esegue una query SQL per estrarre il valore minimo o massimo
     * dalla colonna specificata. Il tipo di aggregazione è determinato dal
     * parametro {@code aggregate}.
     * </p>
     * 
     * @param table il nome della tabella su cui eseguire l'aggregazione
     * @param column l'oggetto {@link Column} che rappresenta la colonna di interesse
     * @param aggregate il tipo di aggregazione da eseguire
     * ({@link QUERY_TYPE#MIN} o {@link QUERY_TYPE#MAX})
     * @return l'oggetto (Double o String) risultato dell'operazione di aggregazione
     * @throws SQLException se si verifica un errore durante l'interrogazione SQL
     * @throws NoValueException se il risultato della query è vuoto (nessun valore trovato)
     * o se il valore aggregato calcolato è {@code null}
     */
    public Object getAggregateColumnValue(String table, Column column, QUERY_TYPE aggregate) throws SQLException, NoValueException{

        Statement statement;
        Table_Schema tSchema = new Table_Schema(db, table);
        Object value = null;
        String aggregateOp = "";

        String query = "select ";
        if(aggregate == QUERY_TYPE.MAX)
            aggregateOp += "max";
        else
            aggregateOp += "min";
        query += aggregateOp + "(" + column.getColumnName() + ") FROM " + table;

        statement = db.getConnection().createStatement();
        ResultSet rs = statement.executeQuery(query);
        if (rs.next()) {
            if(column.isNumber())
                value = rs.getFloat(1);
            else
                value = rs.getString(1);
        }
        else {
            rs.close();
            statement.close();
            throw new NoValueException("Nessun dato trovato nella tabella " + table + " per la colonna " + column.getColumnName());
        }

        rs.close();
        statement.close();
        if(value == null)
            throw new NoValueException("Il valore aggregato (" + aggregateOp + ") per " + column.getColumnName() + " è nullo.");

        return value;
    }
}