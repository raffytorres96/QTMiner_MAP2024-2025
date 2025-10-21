package data;

import java.util.*;

/**
 * La classe `DiscreteAttribute` estende la classe astratta `Attribute`
 * e rappresenta un attributo discreto, ovvero un attributo che può assumere
 * un insieme finito di valori simbolici.
 * Implementa l'interfaccia `Iterable<String>` per permettere l'iterazione
 * sui valori distinti che l'attributo può assumere.
 */
public class DiscreteAttribute extends Attribute implements Iterable<String> {
    /**
     * Un `TreeSet` di stringhe che memorizza l'insieme dei valori distinti
     * che l'attributo discreto può assumere. L'uso di `TreeSet` garantisce
     * che i valori siano unici e ordinati.
     */
    private TreeSet<String> values;

    /**
     * Costruttore della classe `DiscreteAttribute`.
     * Inizializza il nome, l'indice e l'insieme dei valori distinti dell'attributo.
     * @param name il nome dell'attributo.
     * @param index l'indice numerico dell'attributo.
     * @param values un `TreeSet` contenente i valori distinti che l'attributo può assumere.
     */
    public DiscreteAttribute(String name, int index, TreeSet<String> values){
        super(name, index);
        this.values = values;
    }

    /**
     * Restituisce il numero di valori distinti che l'attributo può assumere.
     * @return il numero di valori distinti.
     */
    public int getNumberOfDistinctValues(){
        return values.size();
    }

    /**
     * Restituisce un iteratore sui valori distinti dell'attributo.
     * Questo permette di percorrere l'insieme dei valori.
     * @return un `Iterator<String>` per i valori distinti.
     */
    public Iterator<String> iterator(){
        return values.iterator();
    };
}
