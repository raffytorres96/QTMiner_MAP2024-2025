package data;
import java.io.Serializable;
/**
 * classe che rappresenta un attributo di un dataset
 */

abstract class Attribute implements Serializable{

    public String name;
    public int index;

    public Attribute (String name, int index){
    /**
     *Costruisce una nuova istanza della classe Attribute
     */
        this.name = name;
        this.index = index;
    }

    /**
     * Restituisce il nome dell'attributo
     */
    public String getName(){
        return name;
    }

    /**
     * Restituisce l'indice dell'attributo nel dataset
     * @return
     */
    public int getIndex(){
        return index;
    }

    /**
     * Restituisce una rappresentazione in stringa dell'attributo
     */
    public String toString(){
        return name;
    }
}