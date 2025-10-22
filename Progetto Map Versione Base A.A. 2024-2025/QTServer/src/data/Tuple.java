package data;

import java.io.Serializable;
import java.util.Set;

/**
 * Classe che rappresenta una tupla di un dataset.
 */

public class Tuple implements Serializable {
    /** Array di elementi che costituiscono la  tupla */
    private Item[] tuple;

    /**
     * Costruttore della classe Tuple.
     * @param size numero di eleementi della tupla
     */
    public Tuple(int size){
        tuple = new Item[size];
    }

    /**
     * Restituisce la lunghezza della tupla.
     * @return numero di elementi della tupla
    */
    public int getLength(){
        return tuple.length;
    }

    /**
     * Restituisce l'elemento alla posizione i della tupla.
     * @param i indice dell'elemento da restituire
     * @return item corrispondente all'indice i
     */

    public Item get(int i){
        return tuple[i];
    }

    /**
     * Aggiunge un elemento alla tupla nella posizione i.
     * @param c item da aggiungere
     * @param i indice in cui aggiungere l'item
     */
    public void add(Item c, int i){
        tuple[i] = c;
    }

    /**
     * Calcola la distanza tra la tupla corrente e un'altra tupla.
     * @param obj tupla con cui calcolare la distanza
     * @return somma delle distanze tra i corrispondenti item delle due tuple
     */

    public double getDistance(Tuple obj){
        double sum= 0.0;
            for (int i = 0; i < this.getLength(); i++) {
                sum += this.get(i).distance(obj.get(i).getValue());
            }
        return sum;
    }

    /**
     * Calcola la distanza media tra la tupla corrente e un insieme di tuple.
     * @param data dataset di riferimento
     * @param clusteredData insieme di tuple con cui calcolare la distanza media
     * @return distanza media tra la tupla corrente e le tuple in clusteredData
     */
    
    public double avgDistance(Data data, Set<Tuple> clusteredData) {
    double sumD = 0.0;
        for (Tuple t : clusteredData) {
            sumD += this.getDistance(t);
        }
    return sumD / clusteredData.size();
    }

    // @Override
    // public String toString(){
    //     String str = "";
    //         for (int i = 0; i < tuple.length; i++){
    //             str += tuple[i] + " ";
    //         }
    //     return str;
    // }
}

