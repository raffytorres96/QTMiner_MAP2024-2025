package database;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che modella una transazione (un "esempio") letta dalla base di dati.
 * Ogni esempio è composto da una lista di valori (oggetti) e implementa
 * l'interfaccia {@link Comparable} per poter essere ordinato.
 */
public class Example implements Comparable<Example>{

    /**
     * Lista di oggetti (attributi) che compongono il singolo esempio (transazione).
     */
    private List<Object> example = new ArrayList<Object>();

    /**
     * Aggiunge un oggetto (attributo) alla lista dell'esempio.
     *
     * @param o L'oggetto da aggiungere.
     */
    public void add(Object o){
        example.add(o);
    }

    /**
     * Restituisce l'oggetto (attributo) presente all'indice specificato.
     *
     * @param i L'indice dell'oggetto da recuperare.
     * @return L'oggetto all'indice {@code i}.
     */
    public Object get(int i){
        return example.get(i);
    }

    /**
     * Confronta questo Example con un altro Example specificato.
     * Il confronto avviene elemento per elemento (confronto lessicografico).
     *
     * @param ex L'Example da confrontare con l'istanza corrente.
     * @return Un intero negativo, zero o un intero positivo se questo oggetto
     * è rispettivamente minore, uguale o maggiore dell'oggetto specificato.
     * @throws ClassCastException se gli oggetti nella lista non implementano {@link Comparable}.
     */
    public int compareTo(Example ex) {

        int i=0;
        for(Object o : ex.example){
            if(!o.equals(this.example.get(i)))
                return ((Comparable)o).compareTo(example.get(i));
            i++;
        }
        return 0;
    }

    /**
     * Restituisce una rappresentazione testuale dell'Example.
     * Gli oggetti della lista vengono concatenati in un'unica stringa,
     * separati da uno spazio.
     *
     * @return La stringa che rappresenta l'esempio.
     */
    public String toString(){
        String str="";
        for(Object o:example)
            str+=o.toString()+ " ";
        return str;
    }
}