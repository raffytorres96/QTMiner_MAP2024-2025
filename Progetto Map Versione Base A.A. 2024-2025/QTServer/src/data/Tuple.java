package data;

import java.io.Serializable;
import java.util.Set;

/**
 * Classe che rappresenta una tupla di un dataset.
 * <p>
 * Una tupla è composta da un array di {@link Item} e fornisce metodi
 * per calcolare distanze tra tuple, sia puntuali che medie rispetto
 * a un insieme di tuple.
 * </p>
 * 
 * @see Item
 */
public class Tuple implements Serializable {
    
    /** Array di elementi che costituiscono la tupla */
    private Item[] tuple;

    /**
     * Costruisce una tupla con la dimensione specificata.
     * <p>
     * Gli elementi della tupla devono essere aggiunti successivamente
     * tramite il metodo {@link #add(Item, int)}.
     * </p>
     * 
     * @param size numero di elementi della tupla
     */
    public Tuple(int size){
        tuple = new Item[size];
    }

    /**
     * Restituisce la lunghezza della tupla.
     * 
     * @return numero di elementi della tupla
     */
    public int getLength(){
        return tuple.length;
    }

    /**
     * Restituisce l'elemento alla posizione specificata della tupla.
     * 
     * @param i indice dell'elemento da restituire
     * @return item corrispondente all'indice i
     * @throws ArrayIndexOutOfBoundsException se l'indice è fuori range
     * (i < 0 || i >= getLength())
     */
    public Item get(int i){
        return tuple[i];
    }

    /**
     * Aggiunge un elemento alla tupla nella posizione specificata.
     * <p>
     * Se nella posizione specificata è già presente un elemento,
     * questo viene sovrascritto.
     * </p>
     * 
     * @param c item da aggiungere
     * @param i indice in cui aggiungere l'item
     * @throws ArrayIndexOutOfBoundsException se l'indice è fuori range
     * (i < 0 || i >= getLength())
     */
    public void add(Item c, int i){
        tuple[i] = c;
    }

    /**
     * Calcola la distanza tra la tupla corrente e un'altra tupla.
     * <p>
     * La distanza è calcolata come somma delle distanze tra i corrispondenti
     * item delle due tuple. Ogni item deve implementare un metodo
     * {@code distance()} che calcola la distanza tra valori.
     * </p>
     * 
     * @param obj tupla con cui calcolare la distanza
     * @return somma delle distanze tra i corrispondenti item delle due tuple
     * @throws NullPointerException se obj è null o se uno degli item è null
     */
    public double getDistance(Tuple obj){
        double sum = 0.0;
        for (int i = 0; i < this.getLength(); i++) {
            sum += this.get(i).distance(obj.get(i).getValue());
        }
        return sum;
    }

    /**
     * Calcola la distanza media tra la tupla corrente e un insieme di tuple.
     * <p>
     * La distanza media è calcolata come somma delle distanze tra la tupla
     * corrente e ciascuna tupla nell'insieme, divisa per la dimensione
     * dell'insieme.
     * </p>
     * 
     * @param data dataset di riferimento (parametro attualmente non utilizzato)
     * @param clusteredData insieme di tuple con cui calcolare la distanza media
     * @return distanza media tra la tupla corrente e le tuple in clusteredData
     * @throws ArithmeticException se clusteredData è vuoto (divisione per zero)
     * @throws NullPointerException se clusteredData è null
     */
    public double avgDistance(Data data, Set<Tuple> clusteredData) {
        double sumD = 0.0;
        for (Tuple t : clusteredData) {
            sumD += this.getDistance(t);
        }
        return sumD / clusteredData.size();
    }
}