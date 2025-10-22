package data;

/**
 * Rappresenta un item continuo caratterizzato da un valore numerico.
 */
public class ContinuousItem extends Item {

     /**
     * Costruttore: inizializza l'attributo e il valore usando il costruttore della superclasse.
     * @param attribute attributo continuo (deve essere istanza di ContinuousAttribute)
     * @param value     valore numerico continuo
     */
    public ContinuousItem(Attribute attribute, Double value){
        super(attribute, value);
    }

    /**
     * Calcola la distanza assoluta tra il valore scalato memorizzato e quello associato al parametro.
     * @param a oggetto il cui valore sarà scalato e confrontato (di tipo compatibile con Double)
     * @return distanza assoluta tra valori scalati
     */
    public double distance(Object a) {
        ContinuousAttribute attr = (ContinuousAttribute) getAttribute();
        double scaledThis = attr.getScaledValue((Double) getValue());
        double scaledOther = attr.getScaledValue((Double) a);
        return Math.abs(scaledThis - scaledOther);
    }
}
