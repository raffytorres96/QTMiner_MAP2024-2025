package mining;

import data.Data;
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
 * La classe `QTMiner` implementa l'algoritmo QT (Quality Threshold) per il clustering dei dati.
 * Questo algoritmo raggruppa i dati in cluster basati su un raggio di vicinato specificato.
 */
public class QTMiner implements Serializable {
    /**
     * L'insieme dei cluster (`ClusterSet`) generati dall'algoritmo QT.
     */
    private ClusterSet C;
    /**
     * Il raggio di vicinato utilizzato per determinare l'appartenenza di una tupla a un cluster.
     */
    private double radius;

    /**
     * Costruttore della classe `QTMiner`.
     * Inizializza un nuovo miner QT con il raggio specificato.
     * @param radius il raggio di vicinato per il clustering.
     */
    public QTMiner(double radius){
        this.C = new ClusterSet();
        this.radius = radius;
    }

    public QTMiner(String fileName) throws FileNotFoundException, IOException, ClassNotFoundException{
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
        this.C = (ClusterSet)in.readObject();
        in.close();
    }

    public void salva(String fileName) throws FileNotFoundException, IOException{
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName + ".dmp"));
        out.writeObject(this.C);
        out.close();
    }

    /**
     * Restituisce l'insieme dei cluster (`ClusterSet`) generati.
     * @return l'oggetto `ClusterSet` contenente tutti i cluster.
     */
    public ClusterSet getC(){
        return C;
    }

    /**
     * Esegue l'algoritmo QT per il clustering dei dati.
     * L'algoritmo procede iterativamente:
     * 1. Costruisce un cluster candidato per ciascuna tupla non ancora clusterizzata,
     *    includendo i punti (non ancora clusterizzati) che ricadono nel vicinato
     *    sferico della tupla con il raggio specificato.
     * 2. Salva il cluster candidato più popoloso nell'insieme dei cluster finali (`C`)
     *    e rimuove tutte le tuple di tale cluster dall'elenco delle tuple ancora da clusterizzare.
     * 3. Ripete i passi 1 e 2 finché ci sono ancora tuple da assegnare a un cluster.
     * @param data l'oggetto `Data<Object>` contenente il dataset da clusterizzare.
     * @return il numero totale di cluster generati.
     * @throws ClusteringRadiusException se tutte le tuple finiscono in un unico cluster,
     *                                   indicando un raggio di clustering troppo grande.
     */
    public int compute(Data data) throws ClusteringRadiusException {
        int numclusters=0;
        // Array booleano per tenere traccia delle tuple già clusterizzate.
        boolean[] isClustered = new boolean[data.getNumberOfExamples()];
            for (int i = 0; i < isClustered.length; i++)
            isClustered[i]=false;

        int countClustered=0;
        while(countClustered != data.getNumberOfExamples()){
        
        // Ricerca del cluster più popoloso tra i candidati.
        Cluster c = buildCandidateCluster(data, isClustered);
        if (c == null || c.getSize() == 0) {
        break;
        }
        C.add(c);
        numclusters++;

        // Rimuove le tuple clusterizzate dal dataset (le marca come già assegnate).
        for (Tuple t : c) {
            for (int i = 0; i < data.getNumberOfExamples(); i++) {
                // Confronta la tupla corrente con quelle nel dataset per marcare come clusterizzate.
                if (data.getItemSet(i).getDistance(t) == 0.0) {
                    isClustered[i] = true;
                    countClustered++;
                    break;
                }
            }
        }
        // Verifica se tutti i dati sono stati clusterizzati in un unico cluster,
        // il che potrebbe indicare un raggio eccessivo.
            Iterator<Cluster> it = C.iterator();
                if (it.hasNext()) {
            Cluster first = it.next();
                if (numclusters == 1 && first.getSize() == data.getNumberOfExamples()) {
                    throw new ClusteringRadiusException(data.getNumberOfExamples() + " tuples in one cluster!");
                }
            }
        }
    return numclusters;
    }

    /**
     * Costruisce un cluster candidato per ciascuna tupla non ancora clusterizzata
     * e restituisce il cluster candidato più popoloso.
     * @param data l'oggetto `Data<Object>` contenente il dataset.
     * @param isClustered un array booleano che indica quali tuple sono già state clusterizzate.
     * @return il cluster candidato più popoloso.
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