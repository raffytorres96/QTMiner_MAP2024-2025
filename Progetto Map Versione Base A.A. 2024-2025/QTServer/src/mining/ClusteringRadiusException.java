package mining;

/**
 * Eccezione controllata che viene lanciata quando si verifica un problema
 * legato al raggio di clustering nell'algoritmo QT (Quality Threshold).
 * <p>
 * Questa eccezione viene tipicamente sollevata quando il raggio di clustering
 * specificato è troppo grande, causando la clusterizzazione di tutti i dati
 * in un unico cluster. Questo scenario indica che il parametro del raggio
 * non è appropriato per il dataset fornito e dovrebbe essere ridotto.
 * </p>
 * 
 * <h3>Esempio di utilizzo:</h3>
 * <pre>
 * try {
 *     QTMiner miner = new QTMiner(10.0); // raggio troppo grande
 *     int numClusters = miner.compute(data);
 * } catch (ClusteringRadiusException e) {
 *     System.err.println("Raggio troppo grande: " + e.getMessage());
 *     // Ritenta con un raggio più piccolo
 * }
 * </pre>
 * 
 * @see QTMiner
 * @see QTMiner#compute(data.Data)
 */
public class ClusteringRadiusException extends Exception{
    /**
     * Costruisce una nuova {@code ClusteringRadiusException} con il messaggio di dettaglio specificato.
     * <p>
     * Il messaggio dovrebbe fornire informazioni specifiche sul problema riscontrato,
     * come il numero di tuple che sono finite in un unico cluster o suggerimenti
     * per valori di raggio più appropriati.
     * </p>
     * 
     * @param message il messaggio di dettaglio che descrive la causa dell'eccezione.
     *                Il messaggio può essere recuperato successivamente tramite
     *                il metodo {@link #getMessage()}.
     */
    public ClusteringRadiusException(String message){
        super(message);
    }
}