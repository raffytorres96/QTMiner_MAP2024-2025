package database;

/**
 * Classe che modella la restituzione di un resultset vuoto.
 */
public class EmptySetException extends Exception {

    public EmptySetException(){
        super();
    }

    public EmptySetException(String message){
        super(message);
    }

}
