package data;

/**
 * Rappresenta un attributo continuo caratterizzato da valori numerici.
 * Estende la classe Attribute.
 * permette di normalizzare i valori in un intervallo [0,1].
 */
public class ContinuousAttribute extends Attribute {
    /** Valore massimo dell'attributo */
    private double max;

    /** Valore minimo dell'attributo */
    private double min;

    /**
     * Costruttore della classe ContinuousAttribute.
     * @param name nome dell'attributo
     * @param index indice dell'attributo
     * @param min valore minimo dell'attributo
     * @param max valore massimo dell'attributo
     */
    public ContinuousAttribute(String name, int index, double min, double max){
        super(name, index);
        this.min = min;
        this.max = max;
    }

    /**
     * Calcola il valore normalizzato dell'attributo in un intervallo [0,1].
     * @param v valore da normalizzare
     * @return valore normalizzato nell'intervallo [0,1]
     */
    public double getScaledValue(double v){
        return (v-min)/(max-min);
    }
}
