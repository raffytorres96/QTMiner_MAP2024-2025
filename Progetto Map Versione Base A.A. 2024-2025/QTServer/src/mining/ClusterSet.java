package mining;

import java.io.Serializable;
import java.util.*;
import data.Data;

/**
 * La classe `ClusterSet` rappresenta un insieme di cluster.
 * Implementa l'interfaccia `Iterable<Cluster>` per permettere l'iterazione
 * su tutti i cluster contenuti nell'insieme.
 */
public class ClusterSet implements Iterable<Cluster>, Serializable {
    /**
     * Un `Set` di oggetti `Cluster` che memorizza l'insieme dei cluster.
     * L'uso di `TreeSet` garantisce che i cluster siano unici e ordinati
     * in base all'implementazione di `compareTo` nella classe `Cluster`.
     */
    private Set<Cluster> C = new TreeSet<>();

    /**
     * Costruttore della classe `ClusterSet`.
     * Inizializza un nuovo insieme di cluster vuoto.
     */
    public ClusterSet(){}

    /**
     * Aggiunge un cluster all'insieme dei cluster.
     * @param c il cluster da aggiungere.
     */
    public void add(Cluster c){
        C.add(c);
    }

    /**
     * Restituisce un iteratore sui cluster contenuti in questo insieme.
     * @return un `Iterator<Cluster>` per l'insieme dei cluster.
     */
    public Iterator<Cluster> iterator(){
        return C.iterator();
    }

    /**
    * Restituisce una stringa fatta da ciascun centroide 
    * dell’insieme dei cluster.
    * @return una stringa che rappresenta i centroidi di tutti i cluster.
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
     * Restituisce una rappresentazione in formato stringa dettagliata di tutti i cluster,
     * includendo i centroidi, le tuple clusterizzate e le distanze medie per ciascun cluster.
     * @param data l'oggetto `Data<Object>` da cui provengono le tuple, utilizzato per calcoli di distanza.
     * @return una stringa dettagliata che rappresenta l'intero insieme di cluster.
     */
    public String toString(Data data){
        String str="";
        Iterator<Cluster> it = C.iterator();
        int i = 1;
        while (it.hasNext()) {
                Cluster c = it.next();
            if (c != null){
            str+= i + ":" + c.toString(data) + "\n";
            i++;
            }
        }
    return str;
    }
}
