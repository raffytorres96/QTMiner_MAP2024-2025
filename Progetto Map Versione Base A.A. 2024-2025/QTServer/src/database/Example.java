package database;

import java.util.ArrayList;
import java.util.List;

/**
 * Modella una transazione (esempio) letta dal database.
 * <p>
 * Ogni esempio è composto da una lista di valori (oggetti) che rappresentano
 * gli attributi di una singola tupla estratta dalla base di dati.
 * La classe implementa {@link Comparable} per permettere l'ordinamento
 * degli esempi tramite confronto lessicografico dei loro attributi.
 * </p>
 * 
 * @see Comparable
 */
public class Example implements Comparable<Example>{

    /**
     * Lista di oggetti (attributi) che compongono il singolo esempio (transazione).
     * Gli oggetti possono essere di tipo numerico o stringa, a seconda del tipo
     * di colonna nel database.
     */
    private List<Object> example = new ArrayList<Object>();

    /**
     * Aggiunge un oggetto (attributo) alla lista dell'esempio.
     * <p>
     * Gli oggetti vengono aggiunti in coda alla lista, nell'ordine in cui
     * vengono chiamati, tipicamente seguendo l'ordine delle colonne nella tabella.
     * </p>
     *
     * @param o l'oggetto da aggiungere (può essere Double, String, ecc.)
     */
    public void add(Object o){
        example.add(o);
    }

    /**
     * Restituisce l'oggetto (attributo) presente all'indice specificato.
     *
     * @param i l'indice dell'oggetto da recuperare (basato su zero)
     * @return l'oggetto all'indice {@code i}
     * @throws IndexOutOfBoundsException se l'indice è fuori range
     * (i < 0 || i >= dimensione della lista)
     */
    public Object get(int i){
        return example.get(i);
    }

    /**
     * Confronta questo Example con un altro Example specificato.
     * <p>
     * Il confronto avviene elemento per elemento (confronto lessicografico).
     * Per ogni coppia di elementi corrispondenti nelle due liste:
     * <ul>
     *   <li>Se gli elementi sono uguali, si passa al successivo</li>
     *   <li>Se sono diversi, si restituisce il risultato del confronto tra i due elementi</li>
     * </ul>
     * Se tutti gli elementi sono uguali, gli Example sono considerati uguali.
     * </p>
     *
     * @param ex l'Example da confrontare con l'istanza corrente
     * @return un intero negativo, zero o un intero positivo se questo oggetto
     * è rispettivamente minore, uguale o maggiore dell'oggetto specificato
     * @throws ClassCastException se gli oggetti nella lista non implementano {@link Comparable}
     */
    public int compareTo(Example ex) {
        int i = 0;
        for(Object o : ex.example){
            if(!o.equals(this.example.get(i)))
                return ((Comparable)o).compareTo(example.get(i));
            i++;
        }
        return 0;
    }

    /**
     * Restituisce una rappresentazione testuale dell'Example.
     * <p>
     * Gli oggetti della lista vengono concatenati in un'unica stringa,
     * separati da uno spazio. Ogni oggetto viene convertito in stringa
     * tramite il suo metodo {@code toString()}.
     * </p>
     *
     * @return la stringa che rappresenta l'esempio, con gli attributi separati da spazi
     */
    public String toString(){
        String str = "";
        for(Object o : example)
            str += o.toString() + " ";
        return str;
    }
}