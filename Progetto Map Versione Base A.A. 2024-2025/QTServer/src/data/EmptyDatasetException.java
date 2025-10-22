package data;

/**
 * Eccezione lanciata quando un dataset è vuoto.
 */
public class EmptyDatasetException extends Exception {
    public EmptyDatasetException(String message){
        super(message);
    }
}
