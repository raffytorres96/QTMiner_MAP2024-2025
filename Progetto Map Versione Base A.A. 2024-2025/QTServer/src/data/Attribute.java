package data;
import java.io.Serializable;

/**
 * Classe astratta che rappresenta un attributo generico di un dataset.
 * Un attributo è caratterizzato da un nome e da un indice che ne identifica
 * la posizione all'interno dello schema del dataset.
 * 
 * <p>Questa classe è la base per attributi specifici come attributi continui
 * ({@link ContinuousAttribute}) e attributi discreti ({@link DiscreteAttribute}).</p>
 * 
 * <p>Implementa {@link Serializable} per permettere la serializzazione degli attributi.</p>
 * 
 * @author [Il tuo nome]
 * @version 1.0
 * @see ContinuousAttribute
 * @see DiscreteAttribute
 */
abstract class Attribute implements Serializable {

    /**
     * Nome dell'attributo.
     */
    public String name;
    
    /**
     * Indice della posizione dell'attributo nello schema del dataset.
     */
    public int index;

    /**
     * Costruisce una nuova istanza della classe Attribute.
     * Inizializza il nome e l'indice dell'attributo con i valori specificati.
     * 
     * @param name il nome identificativo dell'attributo
     * @param index la posizione dell'attributo nello schema (parte da 0)
     */
    public Attribute(String name, int index) {
        this.name = name;
        this.index = index;
    }

    /**
     * Restituisce il nome dell'attributo.
     * 
     * @return il nome dell'attributo
     */
    public String getName() {
        return name;
    }

    /**
     * Restituisce l'indice dell'attributo nel dataset.
     * L'indice rappresenta la posizione della colonna corrispondente
     * all'attributo nello schema del dataset.
     * 
     * @return l'indice dell'attributo (posizione nello schema)
     */
    public int getIndex() {
        return index;
    }

    /**
     * Restituisce una rappresentazione in stringa dell'attributo.
     * La rappresentazione corrisponde al nome dell'attributo.
     * 
     * @return il nome dell'attributo come stringa
     */
    public String toString() {
        return name;
    }
}