package mining;

import data.Data;
import data.Tuple;
import java.io.Serializable;
import java.util.*;

/**
 * La classe `Cluster` rappresenta un cluster di dati nel contesto di un algoritmo di clustering.
 * Ogni cluster è definito da un centroide e contiene un insieme di tuple (esempi) che sono state
 * assegnate a quel cluster.
 * Implementa `Iterable<Tuple>` per permettere l'iterazione sulle tuple clusterizzate
 * e `Comparable<Cluster>` per confrontare i cluster in base alla loro dimensione.
 */
public class Cluster implements Iterable<Tuple>, Comparable<Cluster>, Serializable {
	/**
	 * Il centroide del cluster, rappresentato da un oggetto `Tuple`.
	 * Il centroide è il punto centrale o rappresentativo del cluster.
	 */
	private Tuple centroid;
	/**
	 * Un insieme di tuple (`Set<Tuple>`) che sono state assegnate a questo cluster.
	 * L'uso di `HashSet` garantisce l'unicità delle tuple all'interno del cluster.
	 */
	private Set<Tuple> clusteredData; 
	
	/*Cluster(){
	
	}*/

	/**
	 * Costruttore della classe `Cluster`.
	 * Inizializza un nuovo cluster con il centroide specificato e un insieme vuoto di dati clusterizzati.
	 * @param centroid la tupla che rappresenta il centroide del nuovo cluster.
	 */
	public Cluster(Tuple centroid){
		this.centroid = centroid;
		this.clusteredData = new HashSet<>();
		
	}

	/**
	 * Restituisce il centroide di questo cluster.
	 * @return la tupla che rappresenta il centroide del cluster.
	 */		
	public Tuple getCentroid(){
		return centroid;
	}
	
	/**
	 * Aggiunge una tupla all'insieme dei dati clusterizzati.
	 * @param tuple la tupla da aggiungere al cluster.
	 * @return `true` se la tupla è stata aggiunta con successo (cioè non era già presente),
	 *         `false` altrimenti.
	 */
	public boolean addData(Tuple tuple){
		return clusteredData.add(tuple);
	}
	
	/**
	 * Verifica se una tupla è contenuta in questo cluster.
	 * @param tuple la tupla da cercare.
	 * @return `true` se la tupla è presente nell'insieme dei dati clusterizzati,
	 *         `false` altrimenti.
	 */
	public boolean contain(Tuple tuple){
		return clusteredData.contains(tuple);
	}

	/**
	 * Rimuove una tupla dall'insieme dei dati clusterizzati.
	 * Questo metodo è utile quando una tupla cambia cluster.
	 * @param tuple la tupla da rimuovere dal cluster.
	 */
	public void removeTuple(Tuple tuple){
		clusteredData.remove(tuple);
		
	}

	/**
	 * Restituisce il numero di tuple presenti in questo cluster.
	 * @return la dimensione del cluster.
	 */
	public int getSize(){
		return clusteredData.size();
	}

	/**
	 * Restituisce un iteratore sulle tuple contenute in questo cluster.
	 * @return un `Iterator<Tuple>` per l'insieme dei dati clusterizzati.
	 */
	public Iterator<Tuple> iterator() {
        return clusteredData.iterator();
	}

		/**
	 * Compara questo cluster con un altro cluster in base alla loro dimensione.
	 * Un cluster è considerato più grande se contiene più tuple.
	 * @param o l'oggetto `Cluster` con cui confrontare.
	 * @return un valore negativo se questo cluster è più piccolo dell'altro,
	 *         un valore positivo se questo cluster è più grande dell'altro.
	 */
	public int compareTo(Cluster o) {
    		if (this.getSize() < o.getSize())
        		return -1;
    		else
        		return 1;
	}
	
	/**
	 * Restituisce una rappresentazione in formato stringa del centroide del cluster.
	 * @return una stringa che rappresenta il centroide.
	 */
	public String toString(){
		String str="Centroid=(";
		for(int i = 0; i < centroid.getLength(); i++)
			str += centroid.get(i);
		str += ")";
		return str;	
	}
	
	/**
	 * Restituisce una rappresentazione in formato stringa dettagliata del cluster,
	 * includendo il centroide, le tuple clusterizzate e la distanza media.
	 * @param data l'oggetto `Data` da cui provengono le tuple, utilizzato per calcoli di distanza.
	 * @return una stringa dettagliata che rappresenta il cluster.
	 */
	public String toString(Data data){
    String str = "Centroid=(";
    for(int i = 0; i < centroid.getLength(); i++)
        str += centroid.get(i) + " ";
    str += ")\nExamples:\n";

    for (Tuple t : clusteredData) { // usa l'iteratore!
        str += "[";
        for (int j = 0; j < t.getLength(); j++)
            str += t.get(j) + " ";
        str += "] dist=" + getCentroid().getDistance(t) + "\n";
    }
	
    str += "\nAvgDistance=" + getCentroid().avgDistance(data, clusteredData);
    return str;
	}	
}
