package data;

import java.io.Serializable;

/**
 * Classe astratta che rappresenta un oggetto generico di un dataset .
 */
abstract class Item implements Serializable{
    /** Attributo associato all'item. */
    private Attribute attribute;

    /** Valore associato all'item. */
    private Object value;

    /**
     * Costruttore della classe Item associando un attributo e un valore.
     * @param attribute atributo a cui l'item fa riferimento
     * @param value valore dell'item
     */
    Item(Attribute attribute, Object value){
        this.attribute = attribute;
        this.value = value;
    }

    /**
     * Restituisce l'attributo associato all'item.
     * 
     * @return attributo dell'item
     */
    public Attribute getAttribute(){
        return attribute;
    }

    /**
     * Restituisce il valore associato all'item.
     * 
     * @return valore dell'item
     */
    public Object getValue(){
        return value;
    }

    /**
     * Restituisce la rappresentazione in stringa dell'item.
     * 
     * @return rappresentazione in stringa dell'item
     */
    public String toString(){
        return value.toString();
    }

    /**
     * Calcola la distanza tra l'item corrente e un altro oggetto.
     * 
     * @param a oggetto con cui calcolare la distanza
     * @return distanza tra l'item corrente e l'oggetto a
     */
    public abstract double distance(Object a);

}