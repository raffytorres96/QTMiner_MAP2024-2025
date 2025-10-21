package data;

import java.sql.SQLException;
import java.util.*;

import database.DatabaseConnectionException;
import database.DbAccess;
import database.Example;
import database.NoValueException;
import database.Table_Data;
import database.Table_Schema;
import database.Table_Schema.Column;
import database.EmptySetException;
import database.QUERY_TYPE;

/**
 * La classe `Data` rappresenta un dataset generico contenente esempi e attributi.
 */
public class Data {
	/**
	 * Matrice che memorizza i dati del dataset. Ogni riga rappresenta un esempio,
	 * ogni colonna un attributo.
	 */
	private List<Example> data = new ArrayList<Example>();
	/**
	 * Il numero di esempi (righe) presenti nel dataset.
	 */
	private int numberOfExamples;
	/**
	 * L'insieme degli attributi che descrivono il dataset.
	 */
	private List<Attribute> attributeSet;
	
/**
 * Costruttore della classe `Data`.
 * Carica il dataset da una tabella del database specificata.
 * @param tableName il nome della tabella da cui caricare i dati.
 * @throws EmptyDatasetException se il dataset risulta vuoto dopo il caricamento.
 * @throws SQLException se si verifica un errore durante l'accesso al database.
 * @throws DatabaseConnectionException se non è possibile connettersi al database.
 * @throws EmptySetException se la tabella non contiene dati.
 */
public Data(String tableName) throws EmptyDatasetException, SQLException, DatabaseConnectionException, EmptySetException {
    // Inizializza la connessione al database
    DbAccess db = new DbAccess();
    db.initConnection();
    
    try {
        // Carica lo schema della tabella
        Table_Schema tableSchema = new Table_Schema(db, tableName);
        
        // Inizializza l'insieme degli attributi
        attributeSet = new ArrayList<Attribute>();
        
        // Per ogni colonna nella tabella, crea l'attributo corrispondente
        for (int i = 0; i < tableSchema.getNumberOfAttributes(); i++) {
            Column column = tableSchema.getColumn(i);
            
            if (column.isNumber()) {
                // Attributo continuo - calcola min e max
                Table_Data tableData = new Table_Data(db);
                Object minVal = tableData.getAggregateColumnValue(tableName, column, QUERY_TYPE.MIN);
                Object maxVal = tableData.getAggregateColumnValue(tableName, column, QUERY_TYPE.MAX);
                
                double min = ((Number) minVal).doubleValue();
                double max = ((Number) maxVal).doubleValue();
                
                attributeSet.add(new ContinuousAttribute(column.getColumnName(), i, min, max));
            } else {
                // Attributo discreto - carica i valori distinti
                Table_Data tableData = new Table_Data(db);
                Set<Object> distinctValues = tableData.getDistinctColumnValues(tableName, column);
                
                TreeSet<String> values = new TreeSet<String>();
                for (Object value : distinctValues) {
                    values.add(value.toString());
                }
                
                attributeSet.add(new DiscreteAttribute(column.getColumnName(), i, values));
            }
        }
        
        // Carica i dati dalla tabella
        Table_Data tableData = new Table_Data(db);
        List<Example> examples = tableData.getDistinctTransazioni(tableName);
        
        // Inizializza la lista dei dati
        data = new ArrayList<Example>();
        data.addAll(examples);
        
        // Imposta il numero di esempi
        numberOfExamples = data.size();
        
        // Verifica che il dataset non sia vuoto
        if (numberOfExamples == 0) {
            throw new EmptyDatasetException("Dataset vuoto per la tabella: " + tableName);
        }
        
    } catch (NoValueException e) {
        throw new EmptyDatasetException("Errore nel caricamento dei valori aggregati: " + e.getMessage());
    } finally {
        // Chiudi la connessione al database
        db.closeConnection();
    }
}
	
	/**
	 * Restituisce il numero di esempi (righe) nel dataset.
	 * @return il numero di esempi.
	 */
	public int getNumberOfExamples(){
		return numberOfExamples;
	}
	
	/**
	 * Restituisce il numero di attributi (colonne) nel dataset.
	 * @return il numero di attributi.
	 */
	public int getNumberOfAttributes(){
		return attributeSet.size();
	}
	
	/**
	 * Restituisce il valore di un attributo specifico per un dato esempio.
	 * @param exampleIndex l'indice dell'esempio (riga).
	 * @param attributeIndex l'indice dell'attributo (colonna).
	 * @return il valore dell'attributo specificato.
	 */
	public Object getAttributeValue(int exampleIndex, int attributeIndex){
        return data.get(exampleIndex).get(attributeIndex);
	}
	
	/**
	 * Restituisce lo schema degli attributi del dataset.
	 * @return un array di oggetti `Attribute` che rappresentano lo schema.
	 */
	Attribute[] getAttributeSchema(){
		return attributeSet.toArray(new Attribute[0]);
	}
	
	
	/**
	 * Crea e restituisce un oggetto di Tuple che modella come sequenza di coppie
	 * Attributo-valore la i-esima riga in data.
	 * 
	 * @param index indice di riga index in data
	 * @return riferimento a un istanza di Tuple che modelli la transazione
	*/
	public Tuple getItemSet(int index) {
    Tuple tuple = new Tuple(attributeSet.size());
    Example example = data.get(index); // Ottieni l'Example

		for (int i = 0; i < attributeSet.size(); i++) {
			Attribute attr = attributeSet.get(i);
			Object value = example.get(i); // Ottieni il valore i-esimo dall'Example
			
			if (attr instanceof ContinuousAttribute) {
				ContinuousAttribute cAttr = (ContinuousAttribute) attr;
				Double val = ((Number) value).doubleValue();
				tuple.add(new ContinuousItem(cAttr, val), i);
			} else if (attr instanceof DiscreteAttribute) {
				DiscreteAttribute dAttr = (DiscreteAttribute) attr;
				String val = value.toString();
				tuple.add(new DiscreteItem(dAttr, val), i);
			}
		}
    return tuple;
	}
	
	/**
	 * Restituisce una rappresentazione in formato stringa del dataset.
	 * Include gli attributi e tutti gli esempi.
	 * @return una stringa che rappresenta il dataset.
	 */
	public String toString(){
        String str = "";
        for (int i = 0; i < attributeSet.size(); i++){
                str += attributeSet.get(i);
					if (i < attributeSet.size() - 1) 
					str += ",";}
			for (int j = 0; j < numberOfExamples; j++){
				str += "\n" + j + ":";
					for (int k = 0; k < attributeSet.size(); k++) {
						str += data.get(j).get(k);
						if (k < attributeSet.size() - 1) str += ",";
					}
		}
        return str + "\n";
	}

	/**
	 * Metodo main per testare la classe `Data`.
	 * Crea un'istanza di `Data` e la stampa a console.
	 * @param args argomenti della riga di comando (non utilizzati).
	 */
	public static void main(String args[]){
		try{
			Data trainingSet = new Data("your_table_name_here");
			System.out.println(trainingSet);
		} catch(EmptyDatasetException e) {
			System.out.println(e.getMessage());
		} catch(SQLException e) {
			System.out.println("SQL Exception: " + e.getMessage());
		} catch(DatabaseConnectionException e) {
			System.out.println("Database Connection Exception: " + e.getMessage());
		} catch(EmptySetException e) {
			System.out.println("Empty Set Exception: " + e.getMessage());
		}
	}
}
