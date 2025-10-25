package mining;

import data.Data;
import data.EmptyDatasetException;
import data.Tuple;

import java.util.Iterator;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * La classe {@code QTMiner} implementa l'algoritmo QT (Quality Threshold) per il clustering dei dati.
 * <p>
 * L'algoritmo QT raggruppa i dati in cluster basandosi su un raggio di vicinato specificato,
 * garantendo che ogni cluster contenga punti entro una distanza massima dal centroide.
 * A differenza di altri algoritmi come k-means, QT non richiede di specificare a priori
 * il numero di cluster, ma utilizza il raggio come parametro di qualità.
 * </p>
 * <p>
 * La classe supporta la serializzazione per salvare e caricare i risultati del clustering
 * da file.
 * </p>
 *
 */
public class QTMiner implements Serializable {
    
    /**
     * L'insieme dei cluster generati dall'algoritmo QT.
     * Contiene tutti i cluster identificati durante l'esecuzione dell'algoritmo.
     */
    private ClusterSet C;
    
    /**
     * Il raggio di vicinato utilizzato per determinare l'appartenenza di una tupla a un cluster.
     * Rappresenta la distanza massima entro cui una tupla può essere inclusa in un cluster.
     */
    private double radius;

    /**
     * Costruisce un nuovo oggetto {@code QTMiner} con il raggio di clustering specificato.
     * Inizializza l'insieme dei cluster come vuoto.
     * 
     * @param radius il raggio di vicinato per il clustering. Deve essere un valore positivo.
     *               Un raggio troppo grande può portare a cluster eccessivamente popolosi,
     *               mentre un raggio troppo piccolo può generare molti cluster piccoli.
     */
    public QTMiner(double radius){
        this.C = new ClusterSet();
        this.radius = radius;
    }

    /**
     * Costruisce un nuovo oggetto {@code QTMiner} caricando i cluster da un file precedentemente salvato.
     * Questo costruttore permette di ripristinare lo stato di un clustering già eseguito
     * senza dover ricalcolare i cluster.
     * 
     * @param fileName il percorso completo (path + nome) del file da cui caricare i cluster.
     * @throws FileNotFoundException se il file specificato non esiste.
     * @throws IOException se si verifica un errore durante la lettura del file.
     * @throws ClassNotFoundException se la classe degli oggetti deserializzati non viene trovata.
     */
    public QTMiner(String fileName) throws FileNotFoundException, IOException, ClassNotFoundException{
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
        try {
            C = (ClusterSet) in.readObject();
        } finally {
            in.close();
        }
    }

    /**
     * Salva l'insieme dei cluster corrente su file mediante serializzazione.
     * I cluster possono essere successivamente ricaricati utilizzando il costruttore
     * {@link #QTMiner(String)}.
     * 
     * @param fileName il percorso completo (path + nome) del file in cui salvare i cluster.
     * @throws FileNotFoundException se il percorso specificato non è valido.
     * @throws IOException se si verifica un errore durante la scrittura del file.
     */
    public void salva(String fileName) throws FileNotFoundException, IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
        try {
            out.writeObject(C);
        } finally {
            out.close();
        }
    }

    /**
     * Restituisce l'insieme dei cluster generati dall'algoritmo.
     * 
     * @return l'oggetto {@code ClusterSet} contenente tutti i cluster identificati.
     */
    public ClusterSet getC(){
        return C;
    }

    /**
     * Esegue l'algoritmo QT per il clustering del dataset fornito.
     * <p>
     * L'algoritmo procede iterativamente secondo i seguenti passi:
     * <ol>
     *   <li>Per ogni tupla non ancora clusterizzata, costruisce un cluster candidato
     *       includendo tutti i punti (non ancora clusterizzati) che ricadono nel vicinato
     *       sferico della tupla, definito dal raggio specificato.</li>
     *   <li>Seleziona il cluster candidato più popoloso e lo aggiunge all'insieme
     *       dei cluster finali {@code C}.</li>
     *   <li>Marca tutte le tuple del cluster selezionato come già clusterizzate.</li>
     *   <li>Ripete i passi precedenti finché tutte le tuple sono state assegnate a un cluster.</li>
     * </ol>
     * </p>
     * 
     * @param data l'oggetto {@code Data} contenente il dataset da clusterizzare.
     * @return il numero totale di cluster generati.
     * @throws EmptyDatasetException se il dataset è vuoto (non contiene esempi).
     * @throws ClusteringRadiusException se tutte le tuple finiscono in un unico cluster,
     *         indicando che il raggio di clustering è troppo grande per il dataset fornito.
     */
    public int compute(Data data) throws ClusteringRadiusException, EmptyDatasetException {
    // Controllo dataset vuoto
    if (data.getNumberOfExamples() == 0) {
        throw new EmptyDatasetException("Dataset is empty!");
    }
    
    int numclusters = 0;
    boolean[] isClustered = new boolean[data.getNumberOfExamples()];
    int countClustered = 0;
    
    while (countClustered != data.getNumberOfExamples()) {
        Cluster c = buildCandidateCluster(data, isClustered);
        
        if (c == null || c.getSize() == 0) {
            break;
        }
        
        C.add(c);
        numclusters++;
        
        Iterator<Tuple> it = c.iterator();
        while (it.hasNext()) {
            Tuple t = it.next();
            for (int i = 0; i < data.getNumberOfExamples(); i++) {
                if (!isClustered[i] && data.getItemSet(i).equals(t)) {
                    isClustered[i] = true;
                    countClustered++;
                    break;
                }
            }
        }
    }       
    
        if (numclusters == 1 && countClustered == data.getNumberOfExamples()) {
            throw new ClusteringRadiusException(data.getNumberOfExamples() + " tuples in one cluster!");
        }
    return numclusters;
    }

    /**
     * Costruisce i cluster candidati per tutte le tuple non ancora clusterizzate
     * e restituisce quello con la dimensione maggiore.
     * <p>
     * Per ogni tupla non clusterizzata, il metodo:
     * <ul>
     *   <li>La considera come potenziale centroide di un nuovo cluster.</li>
     *   <li>Aggiunge al cluster tutte le tuple non ancora clusterizzate che si trovano
     *       entro il raggio specificato dal centroide.</li>
     *   <li>Confronta la dimensione di questo cluster candidato con il migliore trovato finora.</li>
     * </ul>
     * </p>
     * 
     * @param data l'oggetto {@code Data} contenente il dataset.
     * @param isClustered array booleano che indica quali tuple sono già state assegnate a un cluster.
     *                    {@code true} indica che la tupla all'indice corrispondente è già clusterizzata.
     * @return il cluster candidato con il maggior numero di tuple, oppure {@code null} se non
     *         ci sono tuple disponibili per formare cluster.
     */
    public Cluster buildCandidateCluster(Data data, boolean[] isClustered){
        Cluster bestCluster = null;
        int maxSize = -1;

        for (int i = 0; i < data.getNumberOfExamples(); i++){
            if(!isClustered[i]){
                Tuple center = data.getItemSet(i);
                Cluster candidate = new Cluster(center);
            
                for (int j = 0; j < data.getNumberOfExamples(); j++) {
                    if (!isClustered[j]) {
                        Tuple point = data.getItemSet(j);
                        double distance = center.getDistance(point);
                        if (distance <= radius) {
                            candidate.addData(data.getItemSet(j));
                        }
                    }
                }
                // Aggiorna il cluster migliore se il candidato corrente è più grande.
                if (candidate.getSize() > maxSize) {
                    bestCluster = candidate;
                    maxSize = candidate.getSize();
                }                
            }
        }
    return bestCluster;
    }
}