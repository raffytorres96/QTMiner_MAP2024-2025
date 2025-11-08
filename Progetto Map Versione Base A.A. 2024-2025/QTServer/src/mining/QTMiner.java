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
 * in cluster utilizzando un <b>raggio di vicinato</b> come parametro principale.
 * Determina automaticamente il numero di cluster garantendo che ogni punto in un cluster
 * si trovi entro una distanza massima (il raggio) dal centroide.
 * </p>
 * * <h3>Serializzazione</h3>
 * <p>
 * La classe implementa {@link Serializable} per permettere il salvataggio e il caricamento
 * dei risultati del clustering da file.
 * </p>
 * * @see Cluster
 * @see ClusterSet
 * @see ClusteringRadiusException
 */
public class QTMiner implements Serializable {
    
    /**
     * L'insieme dei cluster generati dall'algoritmo QT.
     */
    private ClusterSet C;
    
    /**
     * Il raggio di vicinato (distanza massima) per l'inclusione in un cluster.
     */
    private double radius;

    /**
     * Costruisce un nuovo oggetto {@code QTMiner} con il raggio di clustering specificato.
     * * @param radius il raggio di vicinato per il clustering.
     */
    public QTMiner(double radius){
        this.C = new ClusterSet();
        this.radius = radius;
    }

    /**
     * Costruisce un nuovo oggetto {@code QTMiner} caricando i cluster da un file serializzato.
     * * @param fileName il percorso completo del file da cui caricare i cluster.
     * @throws FileNotFoundException se il file specificato non esiste.
     * @throws IOException se si verifica un errore durante la lettura del file.
     * @throws ClassNotFoundException se la classe {@link ClusterSet} non viene trovata.
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
     * * @param fileName il percorso completo del file in cui salvare i cluster.
     * @throws FileNotFoundException se il percorso specificato non è valido.
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
     * * @return l'oggetto {@link ClusterSet} contenente tutti i cluster identificati.
     */
    public ClusterSet getC(){
        return C;
    }

    /**
     * Esegue l'algoritmo QT per il clustering del dataset fornito.
     * <p>
     * L'algoritmo procede iterativamente:
     * <ol>
     * <li><b>Costruzione cluster candidati</b>: Per ogni tupla non clusterizzata,
     * costruisce un cluster candidato usando quella tupla come centroide e includendo
     * gli <b>indici</b> dei punti (non clusterizzati) che ricadono nel raggio.</li>
     * <li><b>Selezione del miglior candidato</b>: Seleziona il cluster candidato più popoloso.</li>
     * <li><b>Aggiornamento stato</b>: Marca gli indici delle tuple del cluster 
     * selezionato come già clusterizzati.</li>
     * <li><b>Iterazione</b>: Ripete finché tutte le tuple sono state assegnate
     * <b>o finché non è più possibile formare nuovi cluster</b> (il cluster 
     * migliore restituito è vuoto, ad es. a causa di un raggio non valido).</li>
     * </ol>
     * </p>
     * <p>
     * Questa logica impedisce loop infiniti nel caso in cui il clustering non
     * riesca a progredire.
     * </p>
     * * <h3>Validazione del Raggio</h3>
     * <p>
     * Se tutte le tuple finiscono in un unico cluster, il raggio è troppo grande
     * e viene lanciata {@link ClusteringRadiusException}.
     * </p>
     * * @param data l'oggetto {@link Data} contenente il dataset da clusterizzare.
     * @return il numero totale di cluster generati dall'algoritmo.
     * @throws EmptyDatasetException se il dataset è vuoto.
     * @throws ClusteringRadiusException se tutte le tuple finiscono in un unico cluster
     * (raggio troppo grande).
     * @see #buildCandidateCluster(Data, boolean[])
     */
    public int compute(Data data) throws ClusteringRadiusException, EmptyDatasetException {
    
        // Controllo dataset vuoto robusto
        if (data.getNumberOfExamples() == 0) {
            throw new EmptyDatasetException("Il Dataset è vuoto!");
        }
        
        int numclusters = 0;
        boolean[] isClustered = new boolean[data.getNumberOfExamples()];
        int countClustered = 0;
        
        while (countClustered != data.getNumberOfExamples()) {
            
            // 1. Costruisce il cluster candidato più popoloso
            Cluster c = buildCandidateCluster(data, isClustered);
            
            if (c == null || c.getSize() == 0) {
                break;
            }
            
            C.add(c);
            numclusters++;
            
            Iterator it = c.iterator();
            while (it.hasNext()) {
                Integer index = (Integer) it.next(); 
                isClustered[index] = true;
            }
            countClustered += c.getSize();
        } 
        
        // 3. Controllo finale sul raggio
        if (numclusters == 1 && countClustered == data.getNumberOfExamples()) {
            throw new ClusteringRadiusException(data.getNumberOfExamples() + " tuple in un cluster!");
        }
        
        return numclusters;
    }

    /**
     * Costruisce i cluster candidati per tutte le tuple non ancora clusterizzate
     * e restituisce quello con la dimensione maggiore (popolosità).
     * <p>
     * Per ogni tupla non clusterizzata {@code i}:
     * <ol>
     * <li>La considera come potenziale centroide.</li>
     * <li>Crea un cluster e aggiunge l'<b>indice</b> {@code j} di ogni tupla
     * (non clusterizzata) che si trova entro il {@code radius}.</li>
     * <li>Tiene traccia del cluster più grande trovato.</li>
     * </ol>
     * </p>
     * * @param data l'oggetto {@link Data} contenente il dataset.
     * @param isClustered array booleano che traccia gli indici già clusterizzati.
     * @return il cluster candidato più popoloso.
     * @see Cluster#addData(int) 
     * @see Tuple#getDistance(Tuple)
     */
    public Cluster buildCandidateCluster(Data data, boolean isClustered[]) {
        Cluster cD = null; // Cluster migliore (più popoloso)
        int maxSize = -1; // Dimensione massima trovata
        
        for (int i = 0; i < isClustered.length; i++) {
            
            // Considera 'i' come centroide solo se non è già clusterizzato
            if (!isClustered[i]) {
                Cluster C = new Cluster(data.getItemSet(i));
                
                // Controlla tutti gli altri punti 'j'
                for (int j = 0; j < isClustered.length; j++) {
                    if (!isClustered[j]) { // Considera 'j' solo se non clusterizzato
                        if (data.getItemSet(i).getDistance(data.getItemSet(j)) <= radius) {
                            C.addData(j);
                        }
                    }
                }
                
                // Aggiorna il cluster migliore
                if (C.getSize() > maxSize) {
                    maxSize = C.getSize();
                    cD = C;
                }
            }
        }
        return cD;
    }

    /**
     * Restituisce una rappresentazione in formato stringa dell'insieme dei cluster.
     * <p>
     * Questo metodo delega la chiamata al metodo {@code toString()} 
     * dell'oggetto {@link ClusterSet} interno, fornendo una 
     * rappresentazione testuale dei centroidi di tutti i cluster trovati.
     * </p>
     *
     * @return una stringa che rappresenta l'insieme dei cluster.
     * @see ClusterSet#toString()
     */
    public String toString() {
        return C.toString();
    }
}