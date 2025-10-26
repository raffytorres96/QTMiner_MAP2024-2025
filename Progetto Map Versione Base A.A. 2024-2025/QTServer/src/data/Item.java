package data;

import java.io.Serializable;

/**
 * Classe astratta che rappresenta un item generico di un dataset.
 * Un item è composto da un attributo e dal suo valore corrispondente
 * in una specifica osservazione.
 * 
 * <p>Questa classe è la base per item specifici:
 * <ul>
 *   <li>{@link ContinuousItem} - per valori numerici continui</li>
 *   <li>{@link DiscreteItem} - per valori simbolici discreti</li>
 * </ul>
 * </p>
 * 
 * <p>Implementa {@link Serializable} per permettere la serializzazione degli item,
 * necessaria per salvare tuple e cluster su file.</p>
 * 
 * @author [Il tuo nome]
 * @version 1.0
 * @see ContinuousItem
 * @see DiscreteItem
 * @see Attribute
 */
public abstract class Item implements Serializable {
    
    /**
     * Attributo associato all'item.
     * Definisce la natura e le caratteristiche dell'item (continuo o discreto).
     */
    private Attribute attribute;

    /**
     * Valore associato all'item.
     * Il tipo effettivo dipende dal tipo di attributo (Double per continui, String per discreti).
     */
    private Object value;

    /**
     * Costruttore della classe Item.
     * Associa un attributo e un valore per creare un item.
     * 
     * @param attribute l'attributo a cui l'item fa riferimento
     * @param value il valore concreto dell'item
     */
    Item(Attribute attribute, Object value) {
        this.attribute = attribute;
        this.value = value;
    }

    /**
     * Restituisce l'attributo associato all'item.
     * 
     * @return l'attributo dell'item
     */
    public Attribute getAttribute() {
        return attribute;
    }

    /**
     * Restituisce il valore associato all'item.
     * 
     * @return il valore dell'item (può essere Double, String, o altro tipo appropriato)
     */
    public Object getValue() {
        return value;
    }

    /**
     * Restituisce la rappresentazione in stringa dell'item.
     * La rappresentazione corrisponde alla rappresentazione in stringa del valore.
     * 
     * @return rappresentazione in stringa del valore dell'item
     */
    public String toString() {
        return value.toString();
    }

    /**
     * Calcola la distanza tra l'item corrente e un altro oggetto.
     * 
     * <p>Il metodo è astratto e deve essere implementato dalle sottoclassi
     * in base al tipo specifico di item:
     * <ul>
     *   <li>{@link ContinuousItem} - usa la distanza assoluta tra valori scalati</li>
     *   <li>{@link DiscreteItem} - usa la distanza binaria (0 o 1)</li>
     * </ul>
     * </p>
     * 
     * @param a l'oggetto con cui calcolare la distanza
     * @return la distanza tra l'item corrente e l'oggetto specificato
     */
    public abstract double distance(Object a);
}