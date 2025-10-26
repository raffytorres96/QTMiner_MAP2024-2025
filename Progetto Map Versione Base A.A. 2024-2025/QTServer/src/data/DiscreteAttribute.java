package data;

import java.util.*;

/**
 * La classe DiscreteAttribute estende la classe astratta {@link Attribute}
 * e rappresenta un attributo discreto, ovvero un attributo che può assumere
 * un insieme finito e predefinito di valori simbolici (categorie).
 * 
 * <p>Implementa l'interfaccia {@link Iterable}{@code <String>} per permettere l'iterazione
 * sui valori distinti che l'attributo può assumere, facilitando l'esplorazione
 * dell'insieme di valori possibili.</p>
 * 
 * <p>Esempi di attributi discreti: colore (rosso, verde, blu), taglia (S, M, L, XL),
 * categoria (A, B, C), booleano (vero, falso).</p>
 * 
 * @author [Il tuo nome]
 * @version 1.0
 * @see Attribute
 * @see DiscreteItem
 */
public class DiscreteAttribute extends Attribute implements Iterable<String> {
    
    /**
     * Un TreeSet di stringhe che memorizza l'insieme dei valori distinti
     * che l'attributo discreto può assumere.
     * 
     * <p>L'uso di {@link TreeSet} garantisce che:
     * <ul>
     *   <li>I valori siano unici (nessun duplicato)</li>
     *   <li>I valori siano ordinati lessicograficamente</li>
     * </ul>
     * </p>
     */
    private TreeSet<String> values;

    /**
     * Costruttore della classe DiscreteAttribute.
     * Inizializza il nome, l'indice e l'insieme dei valori distinti dell'attributo.
     * 
     * @param name il nome dell'attributo (es. "Colore", "Categoria")
     * @param index l'indice numerico dell'attributo nello schema del dataset
     * @param values un {@link TreeSet} contenente i valori distinti che l'attributo può assumere
     */
    public DiscreteAttribute(String name, int index, TreeSet<String> values) {
        super(name, index);
        this.values = values;
    }

    /**
     * Restituisce il numero di valori distinti che l'attributo può assumere.
     * 
     * <p>Questo valore corrisponde alla cardinalità dell'insieme dei valori
     * possibili per l'attributo.</p>
     * 
     * @return il numero di valori distinti (cardinalità dell'attributo)
     */
    public int getNumberOfDistinctValues() {
        return values.size();
    }

    /**
     * Restituisce un iteratore sui valori distinti dell'attributo.
     * Questo permette di percorrere l'insieme dei valori possibili in modo ordinato.
     * 
     * <p>Esempio di utilizzo:
     * <pre>{@code
     * DiscreteAttribute color = new DiscreteAttribute("Color", 0, colorSet);
     * for (String value : color) {
     *     System.out.println(value);
     * }
     * }</pre>
     * </p>
     * 
     * @return un {@link Iterator}{@code <String>} per i valori distinti dell'attributo
     */
    public Iterator<String> iterator() {
        return values.iterator();
    }
}