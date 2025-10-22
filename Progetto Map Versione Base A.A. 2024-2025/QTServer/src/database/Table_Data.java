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
 * Modella l’insieme di transazioni collezionate in una tabella.
 * La singola transazione è modellata dalla classe Example.
 */
public class Table_Data {

    /**
     * Oggetto di accesso alla base di dati per l'esecuzione delle query.
     */
    DbAccess db;

    /**
     * Costruttore della classe Table_Data.
     * @param db l'oggetto DbAccess per la connessione al database.
     */
    public Table_Data(DbAccess db) {
        this.db=db;
    }

    /**
     * Ricava lo schema della tabella con nome table.
     * Esegue una interrogazione per estrarre le tuple distinte da tale tabella.
     * Per ogni tupla del resultset, si crea un oggetto, istanza della classe Example,
     * il cui riferimento va incluso nella lista da restituire.
     * In particolare, per la tupla corrente nel resultset, si estraggono i valori
     * dei singoli campi (usando getFloat() o getString()), e li si aggiungono
     * all’oggetto istanza della classe Example che si sta costruendo.
     * * @param table Il nome della tabella da cui estrarre le transazioni.
     * @return Una lista ({@link LinkedList}) di oggetti {@link Example},
     * ognuno rappresentante una tupla (transazione) distinta.
     * @throws SQLException Se si verifica un errore durante l'interrogazione SQL
     * (es. tabella non trovata, errore di sintassi).
     * @throws EmptySetException Se la tabella specificata risulta essere vuota (non contiene tuple).
     */
    public List<Example> getDistinctTransazioni(String table) throws SQLException, EmptySetException{

        LinkedList<Example> transSet = new LinkedList<Example>();
        Statement statement;
        Table_Schema tSchema = new Table_Schema(db,table);

        String query = "select distinct ";

        for(int i = 0; i < tSchema.getNumberOfAttributes(); i++){
            Column c = tSchema.getColumn(i);
            if(i > 0)
                query += ",";
            query += c.getColumnName();
        }

        if(tSchema.getNumberOfAttributes()==0)
            throw new SQLException("La tabella '" + table + "' non ha attributi.");

        query += (" FROM "+table);

        statement = db.getConnection().createStatement();
        ResultSet rs = statement.executeQuery(query);
        boolean empty=true;

        while (rs.next()) {

            empty=false;
            Example currentTuple = new Example();
            for(int i = 0; i < tSchema.getNumberOfAttributes(); i++)
                if(tSchema.getColumn(i).isNumber())
                    currentTuple.add(rs.getDouble(i+1));
                else
                    currentTuple.add(rs.getString(i+1));
            transSet.add(currentTuple);
        }
        rs.close();
        statement.close();
        if(empty) throw new EmptySetException("La tabella '" + table + "' è vuota.");

        return transSet;
    }

    /**
     * Formula ed esegue una interrogazione SQL per estrarre
     * i valori distinti ordinati di column e popolare un insieme da restituire.
     * * @param table Il nome della tabella da cui estrarre i valori.
     * @param column L'oggetto {@link Column} che rappresenta la colonna di interesse.
     * @return Un insieme ({@link Set}) ordinato ({@link TreeSet}) contenente
     * i valori distinti trovati nella colonna.
     * @throws SQLException Se si verifica un errore durante l'interrogazione SQL.
     */
    public Set<Object> getDistinctColumnValues(String table,Column column) throws SQLException{

        Set<Object> valueSet = new TreeSet<Object>();
        Statement statement;
        Table_Schema tSchema = new Table_Schema(db,table);

        String query="select distinct ";
        query+= column.getColumnName();
        query += (" FROM "+table);
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
     * Formula ed esegue una interrogazione SQL per estrarre il valore
     * aggregato (valore minimo o valore massimo) cercato nella colonna di
     * nome column della tabella di nome table. Il metodo solleva e propaga
     * una NoValueException se il resultset è vuoto o il valore calcolato è pari a null.
     * * @param table Il nome della tabella su cui eseguire l'aggregazione.
     * @param column L'oggetto {@link Column} che rappresenta la colonna di interesse.
     * @param aggregate Il tipo di aggregazione da eseguire ({@link QUERY_TYPE#MIN} o {@link QUERY_TYPE#MAX}).
     * @return L'oggetto (Double o String) risultato dell'operazione di aggregazione (MIN o MAX).
     * @throws SQLException Se si verifica un errore durante l'interrogazione SQL.
     * @throws NoValueException Se il risultato della query è vuoto (nessun valore trovato)
     * o se il valore aggregato calcolato è {@code null}.
     */
    public Object getAggregateColumnValue(String table,Column column,QUERY_TYPE aggregate) throws SQLException, NoValueException{

        Statement statement;
        Table_Schema tSchema = new Table_Schema(db,table);
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
                value=rs.getFloat(1);
            else
                value=rs.getString(1);
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