package data;

/**
 * Rappresenta un item continuo caratterizzato da un valore numerico.
 * Estende la classe astratta {@link Item} specializzandola per gestire
 * valori continui di tipo {@link Double}.
 * 
 * <p>La classe implementa il calcolo della distanza tra item continui
 * utilizzando il valore assoluto della differenza tra i valori scalati
 * (normalizzati) degli attributi.</p>
 * 
 * @author [Il tuo nome]
 * @version 1.0
 * @see Item
 * @see ContinuousAttribute
 */
public class ContinuousItem extends Item {

    /**
     * Costruttore della classe ContinuousItem.
     * Inizializza l'attributo e il valore usando il costruttore della superclasse {@link Item}.
     * 
     * @param attribute l'attributo continuo associato all'item (deve essere un'istanza di {@link ContinuousAttribute})
     * @param value il valore numerico continuo dell'item
     */
    public ContinuousItem(Attribute attribute, Double value) {
        super(attribute, value);
    }

    /**
     * Calcola la distanza assoluta tra il valore dell'item corrente e un altro valore.
     * 
     * <p>Il metodo normalizza entrambi i valori utilizzando la funzione di scaling
     * dell'attributo continuo associato ({@link ContinuousAttribute#getScaledValue(double)}),
     * e quindi calcola la distanza come valore assoluto della differenza.</p>
     * 
     * <p>Esempio: Se l'attributo ha range [0, 100], un valore di 25 e uno di 75 avranno
     * valori scalati di 0.25 e 0.75, con distanza risultante di 0.5.</p>
     * 
     * @param a l'oggetto il cui valore sarà scalato e confrontato (deve essere compatibile
     *          con il tipo {@link Double})
     * @return la distanza assoluta tra i valori scalati nell'intervallo [0, 1]
     * @throws ClassCastException se l'attributo non è di tipo {@link ContinuousAttribute}
     *                            o se il parametro non è convertibile a {@link Double}
     */
    public double distance(Object a) {
        ContinuousAttribute attr = (ContinuousAttribute) getAttribute();
        double scaledThis = attr.getScaledValue((Double) getValue());
        double scaledOther = attr.getScaledValue((Double) a);
        return Math.abs(scaledThis - scaledOther);
    }
}