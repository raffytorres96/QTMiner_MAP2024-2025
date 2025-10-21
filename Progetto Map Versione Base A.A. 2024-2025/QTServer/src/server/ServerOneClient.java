// package server;

// import data.Data;
// import java.io.IOException;
// import java.io.ObjectInputStream;
// import java.io.ObjectOutputStream;
// import java.net.Socket;

// import mining.QTMiner;

// public class ServerOneClient extends Thread {
//     private Socket socket;
//     private ObjectInputStream in;
//     private ObjectOutputStream out;
//     private QTMiner kmeans;

//     public ServerOneClient(Socket s) throws IOException{
//         this.socket = s;
//         this.out = new ObjectOutputStream(socket.getOutputStream());
//         this.in = new ObjectInputStream(socket.getInputStream());

//         start();
//     }

//     public void run(){
//     try {
//         while(true) {
//             // Leggi il codice operazione dal client
//             int operation = (Integer) in.readObject();
            
//             switch(operation) {
//                 case 0: { // storeTableFromDb
//                     try {
//                         // Leggi il nome della tabella dal client
//                         String tableName = (String) in.readObject();
                        
//                         // Carica i dati dalla tabella del database
//                         Data data = new Data(tableName);
                        
//                         // Non salviamo i dati, solo confermiamo il caricamento
//                         out.writeObject("OK");
                        
//                     } catch (Exception e) {
//                         out.writeObject("ERROR: " + e.getMessage());
//                     }
//                     break;
//                 }
                
//                 case 1: { // learningFromDbTable
//                     try {
//                         // Leggi il radius dal client
//                         double radius = (Double) in.readObject();
                        
//                         // Assumiamo che ci sia una tabella di default o l'ultima caricata
//                         Data data = new Data("playtennis"); // Usa una tabella di esempio
                        
//                         // Crea il QTMiner ed esegui clustering
//                         kmeans = new QTMiner(radius);
//                         int numClusters = kmeans.compute(data);
                        
//                         out.writeObject("OK");
//                         out.writeObject(numClusters);
//                         out.writeObject(kmeans.getC().toString(data));
                        
//                     } catch (Exception e) {
//                         out.writeObject("ERROR: " + e.getMessage()); 
//                     }
//                     break;
//                 }
                
//                 case 2: { // storeClusterInFile
//                     try {
//                         if (kmeans != null) {
//                             // Salva i cluster su file
//                             String fileName = "clusters_" + System.currentTimeMillis();
//                             kmeans.salva(fileName);
//                             out.writeObject("OK");
//                         } else {
//                             out.writeObject("ERROR: No clusters available to save");
//                         }
//                     } catch (Exception e) {
//                         out.writeObject("ERROR: " + e.getMessage());
//                     }
//                     break;
//                 }
                
//                 case 3: { // learningFromFile
//                     try {
//                         // Leggi nome tabella e radius dal client
//                         String tableName = (String) in.readObject();
//                         double radius = (Double) in.readObject();
                        
//                         // Carica i dati dalla tabella del database
//                         Data data = new Data(tableName);
                        
//                         // Esegui clustering
//                         kmeans = new QTMiner(radius);
//                         int numClusters = kmeans.compute(data);
                        
//                         out.writeObject("OK");
//                         out.writeObject(kmeans.getC().toString(data));
                        
//                     } catch (Exception e) {
//                         out.writeObject("ERROR: " + e.getMessage());
//                     }
//                     break;
//                 }
                
//                 default:
//                     out.writeObject("ERROR: Invalid operation code");
//                     break;
//             }
//         }
//     } catch (IOException | ClassNotFoundException e) {
//         System.out.println("Client disconnesso: " + e.getMessage());
//     } finally {
//         try {
//             if (in != null) in.close();
//             if (out != null) out.close();
//             if (socket != null) socket.close();
//             } catch (IOException e) {
//             System.out.println("Errore chiusura connessione: " + e.getMessage());
//                 }       
//         }
//     }
// }


package server;

import data.Data;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import mining.QTMiner;

/**
 * Classe ServerOneClient che estende Thread per gestire 
 * le richieste di un singolo client in modo concorrente
 */
public class ServerOneClient extends Thread {
    
    /**
     * Socket di connessione con il client
     */
    private Socket socket;
    
    /**
     * Stream di input per ricevere oggetti dal client
     */
    private ObjectInputStream in;
    
    /**
     * Stream di output per inviare oggetti al client
     */
    private ObjectOutputStream out;
    
    /**
     * Istanza di QTMiner per l'esecuzione del clustering
     */
    private QTMiner kmeans;
    
    /**
     * Dati caricati dal database per il clustering
     */
    private Data data;

    /**
     * Costruttore di classe
     * Inizializza gli attributi socket, in e out. Avvia il thread.
     * @param s Socket di connessione con il client
     * @throws IOException se si verifica un errore nell'inizializzazione degli stream
     */
    public ServerOneClient(Socket s) throws IOException {
        this.socket = s;
        
        // Inizializzazione degli stream di comunicazione
        // IMPORTANTE: ObjectOutputStream deve essere creato prima di ObjectInputStream
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        
        System.out.println("Client connesso: " + socket.getInetAddress().getHostAddress());
        
        // Avvia il thread
        start();
    }

    /**
     * Riscrive il metodo run della superclasse Thread 
     * al fine di gestire le richieste del client
     */
    @Override
    public void run() {
        try {
            while (true) {
                // Leggi il codice operazione dal client
                int operation = (Integer) in.readObject();
                
                switch (operation) {
                    case 0: // storeTableFromDb
                        handleStoreTableFromDb();
                        break;
                        
                    case 1: // learningFromDbTable
                        handleLearningFromDbTable();
                        break;
                        
                    case 2: // storeClusterInFile
                        handleStoreClusterInFile();
                        break;
                        
                    case 3: // learningFromFile
                        handleLearningFromFile();
                        break;
                        
                    default:
                        out.writeObject("ERROR: Invalid operation code");
                        out.flush();
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnesso: " + socket.getInetAddress().getHostAddress());
        } finally {
            closeConnection();
        }
    }
    
    /**
     * Gestisce il comando 0: caricamento tabella dal database
     */
    private void handleStoreTableFromDb() {
        try {
            // Leggi il nome della tabella dal client
            String tableName = (String) in.readObject();
            System.out.println("Caricamento tabella: " + tableName);
            
            // Carica i dati dalla tabella del database
            this.data = new Data(tableName);
            
            // Conferma il caricamento
            out.writeObject("OK");
            out.flush();
            System.out.println("Tabella " + tableName + " caricata con successo");
            
        } catch (Exception e) {
            try {
                out.writeObject("ERROR: " + e.getMessage());
                out.flush();
            } catch (IOException ioException) {
                System.err.println("Errore nell'invio del messaggio di errore: " + ioException.getMessage());
            }
            System.err.println("Errore nel caricamento della tabella: " + e.getMessage());
        }
    }
    
    /**
     * Gestisce il comando 1: clustering dalla tabella del database
     */
    private void handleLearningFromDbTable() {
        try {
            // Leggi il radius dal client
            double radius = (Double) in.readObject();
            System.out.println("Esecuzione clustering con raggio: " + radius);
            
            // Verifica che i dati siano stati caricati
            if (this.data == null) {
                throw new Exception("Nessuna tabella caricata. Eseguire prima il comando 0.");
            }
            
            // Crea il QTMiner ed esegui clustering
            this.kmeans = new QTMiner(radius);
            int numClusters = kmeans.compute(data);
            
            // Invia la risposta al client secondo il protocollo
            out.writeObject("OK");
            out.flush();
            out.writeObject(numClusters);
            out.flush();
            out.writeObject(kmeans.getC().toString(data));
            out.flush();
            
            System.out.println("Clustering completato: " + numClusters + " cluster trovati");
            
        } catch (Exception e) {
            try {
                out.writeObject("ERROR: " + e.getMessage());
                out.flush();
            } catch (IOException ioException) {
                System.err.println("Errore nell'invio del messaggio di errore: " + ioException.getMessage());
            }
            System.err.println("Errore nel clustering: " + e.getMessage());
        }
    }
    
    /**
     * Gestisce il comando 2: salvataggio cluster su file
     */
    private void handleStoreClusterInFile() {
        try {
            // Verifica che sia stato eseguito un clustering
            if (kmeans == null || kmeans.getC() == null) {
                throw new Exception("Nessun clustering disponibile da salvare. Eseguire prima il comando 1.");
            }
            
            // Genera un nome file univoco basato sul timestamp
            String fileName = "clusters_" + System.currentTimeMillis();
            
            // Utilizza il metodo salva() di QTMiner per serializzare i cluster
            kmeans.salva(fileName);
            
            out.writeObject("OK");
            out.flush();
            System.out.println("Cluster salvati nel file: " + fileName + ".dmp");
            
        } catch (Exception e) {
            try {
                out.writeObject("ERROR: " + e.getMessage());
                out.flush();
            } catch (IOException ioException) {
                System.err.println("Errore nell'invio del messaggio di errore: " + ioException.getMessage());
            }
            System.err.println("Errore nel salvataggio: " + e.getMessage());
        }
    }
    
    // /**
    //  * Gestisce il comando 3: caricamento cluster da file
    //  * NOTA: Questo comando dovrebbe caricare cluster già salvati, 
    //  * ma dal MainTest sembra che richieda tableName e radius per fare clustering.
    //  * Implemento secondo il comportamento atteso dal client.
    //  */
    // private void handleLearningFromFile() {
    //     try {
    //         // Leggi nome tabella e radius dal client
    //         String tableName = (String) in.readObject();
    //         double radius = (Double) in.readObject();
            
    //         System.out.println("Learning from file - Tabella: " + tableName + ", Raggio: " + radius);
            
    //         // Carica i dati dalla tabella del database
    //         Data fileData = new Data(tableName);
            
    //         // Esegui clustering
    //         QTMiner fileMiner = new QTMiner(radius);
    //         fileMiner.compute(fileData);
            
    //         // Invia la risposta al client
    //         out.writeObject("OK");
    //         out.flush();
    //         out.writeObject(fileMiner.getC().toString(fileData));
    //         out.flush();
            
    //         System.out.println("Clustering da file completato");
            
    //     } catch (Exception e) {
    //         try {
    //             out.writeObject("ERROR: " + e.getMessage());
    //             out.flush();
    //         } catch (IOException ioException) {
    //             System.err.println("Errore nell'invio del messaggio di errore: " + ioException.getMessage());
    //         }
    //         System.err.println("Errore nel learning from file: " + e.getMessage());
    //     }
    // }

    /**
 * Gestisce il comando 3: caricamento cluster da file
 */
private void handleLearningFromFile() {
    try {
        // Leggi il nome del file dal client
        String fileName = (String) in.readObject();
        
        System.out.println("Caricamento cluster dal file: " + fileName);
        
        // Verifica che i dati siano stati caricati (necessari per toString)
        if (this.data == null) {
            throw new Exception("Nessuna tabella caricata. Eseguire prima il comando 0.");
        }
        
        // Carica i cluster dal file usando il costruttore
        this.kmeans = new QTMiner(fileName + ".dmp"); // Aggiungi estensione .dmp
        
        // Invia la risposta al client
        out.writeObject("OK");
        out.flush();
        out.writeObject(kmeans.getC().toString(data));
        out.flush();
        
        System.out.println("Cluster caricati dal file con successo");
        
    } catch (Exception e) {
        try {
            out.writeObject("ERROR: " + e.getMessage());
            out.flush();
        } catch (IOException ioException) {
            System.err.println("Errore nell'invio del messaggio di errore: " + ioException.getMessage());
        }
        System.err.println("Errore nel caricamento dal file: " + e.getMessage());
    }
}
    
    /**
     * Chiude la connessione e le risorse associate
     */
    private void closeConnection() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Connessione chiusa per il client");
        } catch (IOException e) {
            System.err.println("Errore nella chiusura della connessione: " + e.getMessage());
        }
    }
}