package mining;

/**
 * La classe `ClusteringRadiusException` è un'eccezione controllata
 * che viene lanciata quando si verifica un problema legato al raggio di clustering,
 * ad esempio se un raggio troppo grande porta a clusterizzare tutti i dati in un unico cluster.
 */
public class ClusteringRadiusException extends Exception{
    /**
     * Costruttore della classe `ClusteringRadiusException`.
     * Crea una nuova eccezione con il messaggio di dettaglio specificato.
     * @param message il messaggio di dettaglio (che può essere recuperato dal metodo `getMessage()`).
     */
    public ClusteringRadiusException(String message){
        super(message);
    }
}
