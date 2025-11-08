package mining;

import data.Data;
import data.Tuple;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 * Rappresenta un cluster di dati nel contesto di algoritmi di clustering.
 * <p>
 * Un cluster è definito da un <b>centroide</b> (un oggetto {@link Tuple}) e
 * contiene un insieme di <b>indici</b> che puntano alle tuple (nel dataset {@link Data})
 * assegnate a questo cluster.
 * </p>
 * <p>
 * La classe implementa {@link Iterable} per permettere l'iterazione sugli indici contenuti,
 * {@link Comparable} per confrontare cluster in base alla loro popolosità (dimensione),
 * e {@link Serializable} per consentire la persistenza su file.
 * </p>
 * <p>
 * L'unicità degli indici all'interno del cluster è garantita dall'uso di {@link HashSet}.
 * </p>
 * * @see Tuple
 * @see Data
 * @see ClusterSet
 * @see QTMiner
 */
class Cluster implements Iterable<Integer>, Comparable<Cluster>, Serializable {
    /**
     * Il centroide del cluster, rappresentato da un oggetto {@link Tuple}.
     * Il centroide è il punto centrale o rappresentativo del cluster.
     */
    private Tuple centroid;
    
    /**
     * Un insieme di <b>indici</b> ({@link Set}) che puntano alle tuple nel dataset
     * che sono state assegnate a questo cluster.
     * <p>
     * L'uso di {@link HashSet} garantisce l'unicità degli indici e operazioni
     * di inserimento e ricerca efficienti.
     * </p>
     */
    private Set<Integer> clusteredData;

    /**
     * Costruisce un nuovo cluster con il centroide specificato.
     * <p>
     * Inizializza un cluster vuoto (senza indici) con il centroide dato.
     * Gli indici delle tuple possono essere successivamente aggiunti tramite
     * il metodo {@link #addData(int)}.
     * </p>
     * * @param centroid la tupla che rappresenta il centroide del nuovo cluster.
     * Non può essere {@code null}.
     * @throws NullPointerException se centroid è null
     */
    Cluster(Tuple centroid){
        if (centroid == null) {
            throw new NullPointerException("Il Centroide non può essere nullo");
        }
        this.centroid = centroid;
        this.clusteredData = new HashSet<Integer>();
        
    }

    /**
     * Restituisce il centroide di questo cluster.
     * * @return la tupla che rappresenta il centroide del cluster.
     */     
    Tuple getCentroid(){
        return centroid;
    }
    
    /**
     * Aggiunge l'indice di una tupla all'insieme dei dati clusterizzati.
     * <p>
     * Se l'indice è già presente nel cluster, non viene aggiunto nuovamente.
     * </p>
     * * @param id l'indice della tupla da aggiungere al cluster.
     * @return {@code true} se l'indice è stato aggiunto con successo (cioè non era già presente),
     * {@code false} se l'indice era già contenuto nel cluster.
     */
    boolean addData(int id){
        return clusteredData.add(id);
    }
    
    /**
     * Verifica se l'indice di una tupla è contenuto in questo cluster.
     * * @param id l'indice della tupla da cercare.
     * @return {@code true} se l'indice è presente nell'insieme dei dati clusterizzati,
     * {@code false} altrimenti.
     */
    boolean contain(int id){
        return clusteredData.contains(id);
    }

    /**
     * Rimuove l'indice di una tupla dall'insieme dei dati clusterizzati.
     * <p>
     * Questo metodo è utile quando una tupla deve essere riassegnata a un altro cluster
     * durante operazioni di reclusterizzazione o raffinamento.
     * </p>
     * * @param id l'indice della tupla da rimuovere dal cluster. Se l'indice non è presente,
     * l'operazione non ha effetto.
     */
    void removeTuple(int id){
        clusteredData.remove(id);
    }

    /**
     * Restituisce il numero di indici (e quindi di tuple) presenti in questo cluster.
     * <p>
     * La dimensione del cluster è un indicatore della sua "popolosità" e viene utilizzata
     * per confrontare cluster tramite il metodo {@link #compareTo(Cluster)}.
     * </p>
     * * @return la dimensione del cluster (numero di indici contenuti).
     */
    int getSize(){
        return clusteredData.size();
    }

    /**
     * Restituisce un iteratore sugli <b>indici</b> delle tuple contenute in questo cluster.
     * <p>
     * Permette di iterare su tutti gli indici ({@code Integer}) del cluster utilizzando
     * il costrutto {@code for-each} o manualmente tramite l'iteratore.
     * </p>
     * * @return un {@code Iterator<Integer>} per l'insieme degli indici.
     */
    public Iterator<Integer> iterator() {
        return clusteredData.iterator();
    }

    /**
     * Compara questo cluster con un altro cluster esclusivamente in base alla popolosità (dimensione).
     * <p>
     * Se la dimensione di questo cluster è maggiore di quella dell'altro, restituisce +1.
     * Se è minore o uguale, restituisce -1.
     * </p>
     * * @param o l'oggetto {@code Cluster} con cui confrontare.
     * @return +1 se questo cluster è più popoloso, -1 se è meno popoloso o ugualmente popoloso.
     * @throws NullPointerException se o è null
     */
    @Override
    public int compareTo(Cluster o) {
        if (o == null) {
            throw new NullPointerException("Non si può confrontare un centroide vuoto");
        }
        
        if (clusteredData.size() > o.clusteredData.size()) {
            return +1;
        } else {
            return -1;
        }
    }
    
    /**
     * Restituisce una rappresentazione in formato stringa del centroide del cluster.
     * <p>
     * Formato di output: {@code Centroid=(v1v2...vn)} dove {@code v1, v2, ..., vn}
     * sono i valori degli attributi del centroide concatenati senza spazi.
     * </p>
     * * @return una stringa che rappresenta il centroide nel formato specificato.
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
     * Include il centroide, tutte le tuple (recuperate tramite indice da {@code data})
     * appartenenti al cluster con le loro distanze dal centroide, e la distanza
     * media di tutte le tuple dal centroide.
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
     * * @param data l'oggetto {@link Data} da cui provengono le tuple, utilizzato
     * per recuperare le tuple effettive a partire dai loro indici
     * e per calcoli di distanza e medie.
     * @return una stringa dettagliata che rappresenta il cluster con tutte le informazioni.
     * @see Tuple#getDistance(Tuple)
     * @see Tuple#avgDistance(Data, Set)
     */
    public String toString(Data data){
    String str = "Centroid=(";
    for(int i = 0; i < centroid.getLength(); i++)
        str += centroid.get(i) + " ";
    str += ")\nExamples:\n";

    for (Integer it: clusteredData) {
        Tuple t = data.getItemSet(it);
        str += "[";
        str += t.toString();
        str += "] dist=" + getCentroid().getDistance(t) + "\n";
    }
    
    str += "\nAvgDistance=" + getCentroid().avgDistance(data, clusteredData);
    return str;
    }   
}