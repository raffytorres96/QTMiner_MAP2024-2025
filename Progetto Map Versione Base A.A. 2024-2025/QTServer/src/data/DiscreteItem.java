package data;

/**
 * Classe che rappresenta un item discreto, cioè un elemento con valore simbolico.
 * Estende la classe astratta {@link Item} specializzandola per gestire
 * valori discreti di tipo {@link String}.
 * 
 * <p>La classe implementa il calcolo della distanza tra item discreti
 * utilizzando la metrica binaria: distanza 0 se i valori sono uguali,
 * distanza 1 se i valori sono diversi.</p>
 * 
 * @see Item
 * @see DiscreteAttribute
 */
public class DiscreteItem extends Item {

    /**
     * Costruttore della classe DiscreteItem.
     * Inizializza l'attributo discreto e il valore simbolico usando il costruttore
     * della superclasse {@link Item}.
     * 
     * @param attribute l'attributo discreto associato all'item (deve essere un'istanza di {@link DiscreteAttribute})
     * @param value il valore simbolico (stringa) dell'item
     */
    DiscreteItem(DiscreteAttribute attribute, String value) {
        super(attribute, value);
    }

    /**
     * Calcola la distanza tra l'item corrente e un altro valore discreto.
     * 
     * <p>Utilizza la metrica di distanza binaria:
     * <ul>
     *   <li>Distanza = 0.0 se i due valori sono uguali</li>
     *   <li>Distanza = 1.0 se i due valori sono diversi</li>
     * </ul>
     * </p>
     * 
     * <p>Questa metrica è appropriata per attributi categorici dove non esiste
     * una nozione di "vicinanza" o ordine tra i valori.</p>
     * 
     * @param a l'oggetto con cui confrontare il valore (deve essere compatibile con il tipo del valore dell'item)
     * @return 0.0 se i valori sono uguali, 1.0 altrimenti
     */
    double distance(Object a) {
        if (getValue().equals(a)) {
            return 0.0;
        } else {
            return 1.0;
        }
    }
}