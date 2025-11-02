package mining;

import java.io.Serializable;
import java.util.*;
import data.Data;

/**
 * Rappresenta un insieme di cluster generati da un algoritmo di clustering.
 * <p>
 * Questa classe gestisce una collezione di oggetti {@link Cluster} e fornisce
 * metodi per aggiungere cluster, iterare su di essi e ottenere rappresentazioni
 * testuali dell'insieme completo dei cluster.
 * </p>
 * <p>
 * La classe implementa {@link Iterable} per permettere l'iterazione su tutti i cluster
 * e {@link Serializable} per consentire la persistenza dell'insieme di cluster su file.
 * </p>
 * <p>
 * Internamente utilizza un {@link TreeSet} per memorizzare i cluster, garantendo:
 * <ul>
 *   <li><b>Unicità</b>: non ci possono essere cluster duplicati</li>
 *   <li><b>Ordinamento automatico</b>: i cluster sono ordinati in base alla loro dimensione
 *       (numero di tuple) grazie all'implementazione di {@link Comparable} in {@link Cluster}</li>
 * </ul>
 * </p>
 * 
 * @see Cluster
 * @see QTMiner
 */
public class ClusterSet implements Iterable<Cluster>, Serializable {
    /**
     * Un {@link Set} di oggetti {@link Cluster} che memorizza l'insieme dei cluster.
     * <p>
     * L'uso di {@link TreeSet} garantisce che i cluster siano:
     * <ul>
     *   <li>Unici (senza duplicati)</li>
     *   <li>Ordinati automaticamente in base alla dimensione (dal più piccolo al più grande)
     *       secondo l'implementazione di {@link Cluster#compareTo(Cluster)}</li>
     * </ul>
     * </p>
     */
    private Set<Cluster> C = new TreeSet<>();

    /**
     * Costruisce un nuovo insieme di cluster vuoto.
     * <p>
     * I cluster possono essere successivamente aggiunti utilizzando il metodo {@link #add(Cluster)}.
     * </p>
     */
    public ClusterSet(){}

    /**
     * Aggiunge un cluster all'insieme dei cluster.
     * <p>
     * Il cluster viene inserito mantenendo l'ordinamento automatico basato sulla dimensione.
     * Se il cluster è già presente (secondo l'implementazione di {@code equals()}), 
     * potrebbe non essere aggiunto nuovamente (comportamento standard di {@link Set}).
     * </p>
     * 
     * @param c il cluster da aggiungere. Non può essere {@code null}.
     */
    public void add(Cluster c){
        C.add(c);
    }

    /**
     * Restituisce un iteratore sui cluster contenuti in questo insieme.
     * <p>
     * L'iteratore restituisce i cluster nell'ordine determinato dal {@link TreeSet},
     * cioè ordinati per dimensione crescente.
     * </p>
     * 
     * @return un {@code Iterator<Cluster>} per l'insieme dei cluster.
     */
    public Iterator<Cluster> iterator(){
        return C.iterator();
    }

    /**
     * Restituisce una rappresentazione in formato stringa dei centroidi di tutti i cluster.
     * <p>
     * Concatena le rappresentazioni testuali dei centroidi di ciascun cluster contenuto
     * nell'insieme. Ogni centroide viene rappresentato nel formato restituito da
     * {@link Cluster#toString()}.
     * </p>
     * <p>
     * Formato di output: {@code Centroid=(...)Centroid=(...)...} dove ogni {@code Centroid=(...)}
     * rappresenta un cluster diverso.
     * </p>
     * 
     * @return una stringa contenente tutti i centroidi concatenati.
     * @see Cluster#toString()
     */ 
    public String toString(){
        String str = "";
        Iterator<Cluster> it = C.iterator();
            while (it.hasNext()) {
                Cluster c = it.next();
                if (c != null) {
                    str += c.getCentroid().toString();
                }
            }
    return str;
    }   

    /**
     * Restituisce una rappresentazione dettagliata in formato stringa di tutti i cluster.
     * <p>
     * Per ogni cluster nell'insieme, include:
     * <ul>
     *   <li>Il numero progressivo del cluster</li>
     *   <li>Il centroide del cluster</li>
     *   <li>Tutte le tuple appartenenti al cluster con le loro distanze dal centroide</li>
     *   <li>La distanza media delle tuple dal centroide</li>
     * </ul>
     * </p>
     * <p>
     * Formato di output:
     * <pre>
     * 1:Centroid=(...)
     * Examples:
     * [tuple1] dist=...
     * [tuple2] dist=...
     * AvgDistance=...
     * 
     * 2:Centroid=(...)
     * Examples:
     * ...
     * </pre>
     * </p>
     * 
     * @param data l'oggetto {@link Data} da cui provengono le tuple, utilizzato
     *             per calcoli di distanza e rappresentazioni.
     * @return una stringa dettagliata che rappresenta l'intero insieme di cluster.
     * @see Cluster#toString(Data)
     */
    public String toString(Data data){
        String str="";
        Iterator<Cluster> it = C.iterator();
        int i = 1;
        while (it.hasNext()) {
                Cluster c = it.next();
            if (c != null){
            str+= i + ":" + c.toString(data) + "\n\n";
            i++;
            }
        }
    return str;
    }
}