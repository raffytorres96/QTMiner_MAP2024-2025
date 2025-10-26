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
 * Implementa l'algoritmo QT (Quality Threshold) per il clustering dei dati.
 * <p>
 * L'algoritmo QT è un metodo di clustering basato sulla qualità che raggruppa i dati
 * in cluster utilizzando un <b>raggio di vicinato</b> come parametro principale. A differenza
 * di algoritmi come k-means che richiedono di specificare a priori il numero di cluster,
 * QT determina automaticamente il numero di cluster garantendo che ogni cluster soddisfi
 * un criterio di qualità: tutti i punti in un cluster devono trovarsi entro una distanza
 * massima (il raggio) dal centroide.
 * </p>
 * 
 * <h2>Caratteristiche dell'algoritmo QT</h2>
 * <ul>
 *   <li><b>Non richiede il numero di cluster a priori</b>: il numero di cluster emerge
 *       naturalmente dall'applicazione del criterio di qualità basato sul raggio</li>
 *   <li><b>Garantisce la qualità dei cluster</b>: ogni punto è entro il raggio dal centroide</li>
 *   <li><b>Deterministico</b>: produce sempre lo stesso risultato per gli stessi dati e raggio</li>
 *   <li><b>Gestisce automaticamente outlier</b>: punti molto distanti formano cluster piccoli</li>
 * </ul>
 * 
 * <h2>Scelta del raggio</h2>
 * <p>
 * Il parametro del raggio è cruciale per il risultato del clustering:
 * <ul>
 *   <li><b>Raggio troppo piccolo</b>: genera molti cluster piccoli, potenzialmente un cluster
 *       per ogni punto del dataset</li>
 *   <li><b>Raggio troppo grande</b>: tutti i punti finiscono in un unico cluster, lanciando
 *       {@link ClusteringRadiusException}</li>
 *   <li><b>Raggio ottimale</b>: dipende dalla distribuzione e scala dei dati; valori
 *       tipici sono 0.1, 0.3, 0.5 per dati normalizzati in [0,1]</li>
 * </ul>
 * </p>
 * 
 * <h2>Serializzazione</h2>
 * <p>
 * La classe implementa {@link Serializable} per permettere il salvataggio e il caricamento
 * dei risultati del clustering da file, evitando di dover ricalcolare i cluster per analisi
 * successive.
 * </p>
 * 
 * @see Cluster
 * @see ClusterSet
 * @see ClusteringRadiusException
 */
public class QTMiner implements Serializable {
    
    /**
     * L'insieme dei cluster generati dall'algoritmo QT.
     * <p>
     * Contiene tutti i cluster identificati durante l'esecuzione del metodo
     * {@link #compute(Data)}. Inizialmente vuoto, viene popolato progressivamente
     * durante l'esecuzione dell'algoritmo.
     * </p>
     */
    private ClusterSet C;
    
    /**
     * Il raggio di vicinato utilizzato per determinare l'appartenenza di una tupla a un cluster.
     * <p>
     * Rappresenta la <b>distanza massima</b> entro cui una tupla può essere inclusa in un cluster
     * dal suo centroide. Questo parametro definisce il criterio di qualità dell'algoritmo QT:
     * solo le tuple che si trovano entro questo raggio dal centroide possono far parte del cluster.
     * </p>
     * <p>
     * Valori tipici dipendono dalla scala e normalizzazione dei dati. Per dati normalizzati
     * in [0,1], raggi comuni sono 0.1, 0.3, 0.5.
     * </p>
     */
    private double radius;

    /**
     * Costruisce un nuovo oggetto {@code QTMiner} con il raggio di clustering specificato.
     * <p>
     * Inizializza un nuovo miner con l'insieme dei cluster vuoto. Il clustering effettivo
     * viene eseguito successivamente tramite il metodo {@link #compute(Data)}.
     * </p>
     * 
     * @param radius il raggio di vicinato per il clustering. Deve essere un valore positivo.
     *               <ul>
     *                 <li>Un raggio troppo grande può portare a cluster eccessivamente popolosi
     *                     (tutti i dati in un unico cluster)</li>
     *                 <li>Un raggio troppo piccolo può generare molti cluster piccoli o addirittura
     *                     un cluster per ogni tupla</li>
     *               </ul>
     */
    public QTMiner(double radius){
        this.C = new ClusterSet();
        this.radius = radius;
    }

    /**
     * Costruisce un nuovo oggetto {@code QTMiner} caricando i cluster da un file precedentemente salvato.
     * <p>
     * Questo costruttore permette di ripristinare lo stato di un clustering già eseguito
     * senza dover ricalcolare i cluster. È utile per:
     * <ul>
     *   <li>Analizzare risultati di clustering precedenti</li>
     *   <li>Confrontare diversi clustering</li>
     *   <li>Risparmiare tempo di calcolo su dataset grandi</li>
     * </ul>
     * </p>
     * <p>
     * <b>Nota:</b> Il file deve essere stato creato tramite il metodo {@link #salva(String)}
     * e contenere un oggetto {@link ClusterSet} serializzato. Il raggio originale utilizzato
     * per generare i cluster non viene caricato dal file.
     * </p>
     * 
     * @param fileName il percorso completo (path + nome) del file da cui caricare i cluster.
     *                 Esempio: {@code "clustering_results.dat"} o {@code "data/clusters.ser"}
     * @throws FileNotFoundException se il file specificato non esiste nel percorso indicato.
     * @throws IOException se si verifica un errore durante la lettura del file
     *                     (file corrotto, permessi insufficienti, etc.).
     * @throws ClassNotFoundException se la classe {@link ClusterSet} non viene trovata durante
     *                                la deserializzazione (problema di classpath o versioni).
     * @see #salva(String)
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
     * <p>
     * Serializza l'oggetto {@link ClusterSet} contenente tutti i cluster generati
     * e lo scrive su file. I cluster possono essere successivamente ricaricati
     * utilizzando il costruttore {@link #QTMiner(String)}.
     * </p>
     * <p>
     * <b>Nota:</b> Il file viene sovrascritto se esiste già. Il raggio utilizzato
     * per generare i cluster non viene salvato nel file.
     * </p>
     * 
     * @param fileName il percorso completo (path + nome) del file in cui salvare i cluster.
     *                 Esempio: {@code "clustering_results.dat"} o {@code "data/clusters.ser"}
     * @throws FileNotFoundException se il percorso specificato non è valido o non si hanno
     *                               permessi di scrittura nella directory.
     * @throws IOException se si verifica un errore durante la scrittura del file.
     * @see #QTMiner(String)
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
     * @return l'oggetto {@link ClusterSet} contenente tutti i cluster identificati
     *         dall'ultima esecuzione di {@link #compute(Data)} o caricati da file
     *         tramite {@link #QTMiner(String)}.
     */
    public ClusterSet getC(){
        return C;
    }

    /**
     * Esegue l'algoritmo QT per il clustering del dataset fornito.
     * <p>
     * L'algoritmo procede iterativamente secondo i seguenti passi:
     * <ol>
     *   <li><b>Costruzione cluster candidati</b>: Per ogni tupla non ancora clusterizzata,
     *       costruisce un cluster candidato usando quella tupla come centroide e includendo
     *       tutti i punti (non ancora clusterizzati) che ricadono nel vicinato sferico
     *       definito dal raggio specificato.</li>
     *   <li><b>Selezione del miglior candidato</b>: Seleziona il cluster candidato più popoloso
     *       (con il maggior numero di tuple) e lo aggiunge all'insieme dei cluster finali {@code C}.</li>
     *   <li><b>Aggiornamento stato</b>: Marca tutte le tuple del cluster selezionato come
     *       già clusterizzate, in modo che non vengano considerate nelle iterazioni successive.</li>
     *   <li><b>Iterazione</b>: Ripete i passi precedenti finché tutte le tuple sono state
     *       assegnate a un cluster.</li>
     * </ol>
     * </p>
     * 
     * <h3>Complessità Computazionale</h3>
     * <p>
     * La complessità temporale dell'algoritmo è <b>O(n³)</b> nel caso peggiore, dove n è il numero
     * di tuple nel dataset. Questo perché:
     * <ul>
     *   <li>Per ogni iterazione (O(n) nel caso peggiore, se ogni tupla forma un cluster)</li>
     *   <li>Si considera ogni tupla non clusterizzata come potenziale centroide (O(n))</li>
     *   <li>Per ogni centroide, si calcola la distanza da tutte le altre tuple (O(n))</li>
     * </ul>
     * </p>
     * 
     * <h3>Validazione del Raggio</h3>
     * <p>
     * Il metodo verifica se il raggio è appropriato per il dataset:
     * <ul>
     *   <li>Se tutte le tuple finiscono in un unico cluster, il raggio è troppo grande
     *       e viene lanciata {@link ClusteringRadiusException}</li>
     *   <li>Questo controllo aiuta a identificare parametri inappropriati e suggerisce
     *       di ridurre il valore del raggio</li>
     * </ul>
     * </p>
     * 
     * @param data l'oggetto {@link Data} contenente il dataset da clusterizzare.
     *             Deve contenere almeno una tupla.
     * @return il numero totale di cluster generati dall'algoritmo.
     * @throws EmptyDatasetException se il dataset è vuoto (non contiene esempi).
     *                               Un dataset vuoto non può essere clusterizzato.
     * @throws ClusteringRadiusException se tutte le tuple finiscono in un unico cluster,
     *         indicando che il raggio di clustering è troppo grande per il dataset fornito.
     *         In questo caso, si consiglia di ridurre il valore del raggio.
     * @see #buildCandidateCluster(Data, boolean[])
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
                if (!isClustered[i] && data.getItemSet(i).getDistance(t) == 0.0) {
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
     * Questo metodo implementa il cuore dell'algoritmo QT. Per ogni tupla non ancora
     * clusterizzata nel dataset:
     * <ol>
     *   <li>La considera come potenziale centroide di un nuovo cluster candidato</li>
     *   <li>Aggiunge al cluster tutte le tuple non ancora clusterizzate che si trovano
     *       entro il raggio specificato dal centroide (distanza ≤ radius)</li>
     *   <li>Confronta la dimensione di questo cluster candidato con il migliore trovato finora</li>
     *   <li>Mantiene il cluster candidato con il maggior numero di tuple</li>
     * </ol>
     * </p>
     * <p>
     * Alla fine del processo, restituisce il cluster candidato più popoloso, che sarà
     * quello aggiunto definitivamente all'insieme dei cluster finali nel metodo {@link #compute(Data)}.
     * </p>
     * 
     * <h3>Strategia Greedy</h3>
     * <p>
     * L'algoritmo utilizza una strategia greedy (golosa): ad ogni iterazione sceglie il cluster più grande
     * possibile tra tutti quelli candidati. Questo garantisce che il numero totale di cluster
     * sia minimizzato, dato il vincolo del raggio. La strategia greedy non garantisce
     * necessariamente la soluzione ottimale globale, ma fornisce una buona approssimazione
     * in tempo ragionevole.
     * </p>
     * 
     * <h3>Complessità</h3>
     * <p>
     * La complessità temporale di questo metodo è <b>O(n²)</b>, dove n è il numero di tuple
     * non ancora clusterizzate, poiché per ogni tupla (O(n)) calcola le distanze da tutte
     * le altre tuple non clusterizzate (O(n)).
     * </p>
     * 
     * @param data l'oggetto {@link Data} contenente il dataset da clusterizzare.
     * @param isClustered array booleano che indica quali tuple sono già state assegnate a un cluster.
     *                    {@code isClustered[i] = true} indica che la tupla all'indice {@code i}
     *                    è già stata clusterizzata e non deve essere considerata come centroide
     *                    né aggiunta a nuovi cluster candidati.
     * @return il cluster candidato con il maggior numero di tuple, oppure {@code null} se non
     *         ci sono tuple disponibili per formare cluster (tutte le tuple sono già clusterizzate).
     * @see Cluster#addData(Tuple)
     * @see Tuple#getDistance(Tuple)
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