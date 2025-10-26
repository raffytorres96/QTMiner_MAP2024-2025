package mining;

import data.Data;
import data.Tuple;
import java.io.Serializable;
import java.util.*;

/**
 * Rappresenta un cluster di dati nel contesto di algoritmi di clustering.
 * <p>
 * Un cluster è definito da un <b>centroide</b> (punto rappresentativo centrale) e contiene
 * un insieme di tuple (esempi) che sono state assegnate a quel cluster in base a criteri
 * di similarità o distanza. La classe fornisce metodi per gestire l'aggiunta, la rimozione
 * e la verifica della presenza di tuple nel cluster.
 * </p>
 * <p>
 * La classe implementa {@link Iterable} per permettere l'iterazione sulle tuple contenute,
 * {@link Comparable} per confrontare cluster in base alla loro dimensione (numero di tuple),
 * e {@link Serializable} per consentire la persistenza su file.
 * </p>
 * <p>
 * L'unicità delle tuple all'interno del cluster è garantita dall'uso di {@link HashSet}.
 * </p>
 * 
 * @see Tuple
 * @see ClusterSet
 * @see QTMiner
 */
public class Cluster implements Iterable<Tuple>, Comparable<Cluster>, Serializable {
	/**
	 * Il centroide del cluster, rappresentato da un oggetto {@link Tuple}.
	 * Il centroide è il punto centrale o rappresentativo del cluster, utilizzato
	 * per determinare l'appartenenza di nuove tuple al cluster in base alla distanza.
	 */
	private Tuple centroid;
	
	/**
	 * Un insieme di tuple ({@link Set}) che sono state assegnate a questo cluster.
	 * L'uso di {@link HashSet} garantisce l'unicità delle tuple all'interno del cluster
	 * e operazioni di inserimento e ricerca efficienti in tempo O(1) medio.
	 */
	private Set<Tuple> clusteredData; 
	
	/*Cluster(){
	
	}*/

	/**
	 * Costruisce un nuovo cluster con il centroide specificato.
	 * <p>
	 * Inizializza un cluster vuoto (senza tuple) con il centroide dato.
	 * Le tuple possono essere successivamente aggiunte tramite il metodo {@link #addData(Tuple)}.
	 * </p>
	 * 
	 * @param centroid la tupla che rappresenta il centroide del nuovo cluster.
	 *                 Non può essere {@code null}.
	 */
	public Cluster(Tuple centroid){
		this.centroid = centroid;
		this.clusteredData = new HashSet<>();
		
	}

	/**
	 * Restituisce il centroide di questo cluster.
	 * 
	 * @return la tupla che rappresenta il centroide del cluster.
	 */		
	public Tuple getCentroid(){
		return centroid;
	}
	
	/**
	 * Aggiunge una tupla all'insieme dei dati clusterizzati.
	 * <p>
	 * Se la tupla è già presente nel cluster (in base all'implementazione di {@code equals()}
	 * di {@link Tuple}), non viene aggiunta nuovamente.
	 * </p>
	 * 
	 * @param tuple la tupla da aggiungere al cluster. Non può essere {@code null}.
	 * @return {@code true} se la tupla è stata aggiunta con successo (cioè non era già presente),
	 *         {@code false} se la tupla era già contenuta nel cluster.
	 */
	public boolean addData(Tuple tuple){
		return clusteredData.add(tuple);
	}
	
	/**
	 * Verifica se una tupla è contenuta in questo cluster.
	 * 
	 * @param tuple la tupla da cercare. Non può essere {@code null}.
	 * @return {@code true} se la tupla è presente nell'insieme dei dati clusterizzati,
	 *         {@code false} altrimenti.
	 */
	public boolean contain(Tuple tuple){
		return clusteredData.contains(tuple);
	}

	/**
	 * Rimuove una tupla dall'insieme dei dati clusterizzati.
	 * <p>
	 * Questo metodo è utile quando una tupla deve essere riassegnata a un altro cluster
	 * durante operazioni di reclusterizzazione o raffinamento.
	 * </p>
	 * 
	 * @param tuple la tupla da rimuovere dal cluster. Se la tupla non è presente,
	 *              l'operazione non ha effetto.
	 */
	public void removeTuple(Tuple tuple){
		clusteredData.remove(tuple);
		
	}

	/**
	 * Restituisce il numero di tuple presenti in questo cluster.
	 * <p>
	 * La dimensione del cluster è un indicatore della sua "densità" e viene utilizzata
	 * per confrontare cluster tramite il metodo {@link #compareTo(Cluster)}.
	 * </p>
	 * 
	 * @return la dimensione del cluster (numero di tuple contenute).
	 */
	public int getSize(){
		return clusteredData.size();
	}

	/**
	 * Restituisce un iteratore sulle tuple contenute in questo cluster.
	 * <p>
	 * Permette di iterare su tutte le tuple del cluster utilizzando il costrutto
	 * {@code for-each} o manualmente tramite l'iteratore.
	 * </p>
	 * 
	 * @return un {@code Iterator<Tuple>} per l'insieme dei dati clusterizzati.
	 */
	public Iterator<Tuple> iterator() {
        return clusteredData.iterator();
	}

	/**
	 * Compara questo cluster con un altro cluster in base alla loro dimensione.
	 * <p>
	 * Un cluster è considerato "maggiore" se contiene più tuple. Questo metodo
	 * è utilizzato per ordinare cluster in strutture dati come {@link TreeSet}.
	 * </p>
	 * <p>
	 * <b>Nota:</b> Questo metodo restituisce sempre -1 o 1, mai 0, anche quando
	 * i cluster hanno la stessa dimensione. Questo comportamento evita che cluster
	 * con la stessa dimensione vengano considerati "uguali" e quindi uno dei due
	 * venga scartato da strutture dati basate su {@link Set}.
	 * </p>
	 * 
	 * @param o l'oggetto {@code Cluster} con cui confrontare.
	 * @return un valore negativo (-1) se questo cluster è più piccolo dell'altro,
	 *         un valore positivo (1) se questo cluster è più grande o uguale all'altro.
	 */
	public int compareTo(Cluster o) {
    		if (this.getSize() < o.getSize())
        		return -1;
    		else
        		return 1;
	}
	
	/**
	 * Restituisce una rappresentazione in formato stringa del centroide del cluster.
	 * <p>
	 * Formato di output: {@code Centroid=(v1v2...vn)} dove {@code v1, v2, ..., vn}
	 * sono i valori degli attributi del centroide concatenati senza spazi.
	 * </p>
	 * 
	 * @return una stringa che rappresenta il centroide nel formato specificato.
	 */
	public String toString(){
		String str="Centroid=(";
		for(int i = 0; i < centroid.getLength(); i++)
			str += centroid.get(i);
		str += ")";
		return str;	
	}
	
	/**
	 * Restituisce una rappresentazione dettagliata del cluster in formato stringa.
	 * <p>
	 * Include il centroide, tutte le tuple appartenenti al cluster con le loro
	 * distanze dal centroide, e la distanza media di tutte le tuple dal centroide.
	 * </p>
	 * <p>
	 * Formato di output:
	 * <pre>
	 * Centroid=(v1 v2 ... vn)
	 * Examples:
	 * [t1_v1 t1_v2 ... ] dist=d1
	 * [t2_v1 t2_v2 ... ] dist=d2
	 * ...
	 * AvgDistance=avg
	 * </pre>
	 * </p>
	 * 
	 * @param data l'oggetto {@link Data} da cui provengono le tuple, utilizzato
	 *             per calcoli di distanza e medie.
	 * @return una stringa dettagliata che rappresenta il cluster con tutte le informazioni.
	 * @see Tuple#getDistance(Tuple)
	 * @see Tuple#avgDistance(Data, Set)
	 */
	public String toString(Data data){
    String str = "Centroid=(";
    for(int i = 0; i < centroid.getLength(); i++)
        str += centroid.get(i) + " ";
    str += ")\nExamples:\n";

    for (Tuple t : clusteredData) {
        str += "[";
        for (int j = 0; j < t.getLength(); j++)
            str += t.get(j) + " ";
        str += "] dist=" + getCentroid().getDistance(t) + "\n";
    }
	
    str += "\nAvgDistance=" + getCentroid().avgDistance(data, clusteredData);
    return str;
	}	
}