package data;

/**
 * Rappresenta un attributo continuo caratterizzato da valori numerici.
 * Estende la classe astratta {@link Attribute} specializzandola per gestire
 * dati numerici continui che possono essere normalizzati.
 * 
 * <p>Un attributo continuo è definito da un intervallo [min, max] che rappresenta
 * il range dei valori possibili. La classe fornisce funzionalità per normalizzare
 * i valori in un intervallo standardizzato [0, 1], utile per calcoli di distanza
 * e algoritmi di clustering.</p>
 * 
 * @author [Il tuo nome]
 * @version 1.0
 * @see Attribute
 * @see ContinuousItem
 */
public class ContinuousAttribute extends Attribute {
    
    /**
     * Valore massimo che l'attributo può assumere nel dataset.
     */
    private double max;

    /**
     * Valore minimo che l'attributo può assumere nel dataset.
     */
    private double min;

    /**
     * Costruttore della classe ContinuousAttribute.
     * Inizializza un attributo continuo con nome, indice e range di valori [min, max].
     * 
     * @param name il nome dell'attributo
     * @param index l'indice dell'attributo nello schema del dataset
     * @param min il valore minimo che l'attributo può assumere
     * @param max il valore massimo che l'attributo può assumere
     */
    public ContinuousAttribute(String name, int index, double min, double max) {
        super(name, index);
        this.min = min;
        this.max = max;
    }

    /**
     * Calcola il valore normalizzato dell'attributo nell'intervallo [0, 1].
     * La normalizzazione viene effettuata tramite la formula: (v - min) / (max - min).
     * 
     * <p>Questa operazione è utile per rendere comparabili attributi con scale diverse
     * e per calcolare distanze significative negli algoritmi di clustering.</p>
     * 
     * @param v il valore da normalizzare
     * @return il valore normalizzato nell'intervallo [0, 1], dove 0 corrisponde a min
     *         e 1 corrisponde a max
     * @throws ArithmeticException se max = min (divisione per zero)
     */
    protected double getScaledValue(double v) {
        return (v - min) / (max - min);
    }
}