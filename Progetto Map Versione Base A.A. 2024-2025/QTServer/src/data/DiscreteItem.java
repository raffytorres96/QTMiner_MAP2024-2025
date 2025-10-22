package data;

/**
 * Classe che rappresenta un oggetto discreto.
 */
public class DiscreteItem extends Item {

    /**
     * Costruttore della classe DiscreteItem.       
     * @param attribute
     * @param value
     */
    DiscreteItem(DiscreteAttribute attribute, String value){
        super(attribute, value);
    }

    /**
     * Calcola la distanza tra l'oggetto corrente e un altro oggetto discreto.
     * @param a
     * @return
     */
    public double distance(Object a){
        if (getValue().equals(a)){
            return 0.0;
        }
        else {
            return 1.0;
        }
    }
}
