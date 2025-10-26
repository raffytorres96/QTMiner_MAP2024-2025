package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;

import data.Data;
import data.EmptyDatasetException;
import database.DatabaseConnectionException;
import database.EmptySetException;
import mining.ClusteringRadiusException;
import mining.QTMiner;

/**
 * Classe che gestisce la comunicazione con un singolo client in un thread separato.
 * Estende Thread per permettere la gestione concorrente di più client.
 *
 */
public class ServerOneClient extends Thread {
    
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private QTMiner kmeans;
    private Data data;
    
    /**
     * Costruttore di classe. Inizializza gli attributi socket, in e out. Avvia il thread.
     * 
     * @param s Socket connesso al client
     * @throws IOException se si verifica un errore durante l'inizializzazione degli stream
     */
    public ServerOneClient(Socket s) throws IOException {
        this.socket = s;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        
        // Avvia il thread
        start();
    }
    
    /**
     * Riscrive il metodo run della superclasse Thread al fine di gestire le richieste del client.
     * Rimane in ascolto dei comandi inviati dal client e li elabora di conseguenza.
     */
    @Override
    public void run() {
        try {
            while (true) {
                // Leggi il comando dal client
                int command = (int) in.readObject();
                
                switch (command) {
                    case 0:
                        handleStoreTableFromDb();
                        break;
                    case 1:
                        handleLearningFromDbTable();
                        break;
                    case 2:
                        handleStoreClusterInFile();
                        break;
                    case 3:
                        handleLearningFromFile();
                        break;
                    default:
                        out.writeObject("Comando non valido!");
                        out.flush();
                }
            }
            } catch (IOException | ClassNotFoundException e) {
                // Nessun messaggio - gestione silenziosa
            } finally {
                // Chiusura risorse
                closeConnection();
            }
    }
    
    /**
     * Gestisce il comando 0: caricamento tabella dal database
     */
    private void handleStoreTableFromDb() {
        String tableName = null;
            try {
                // Leggi il nome della tabella dal client
                tableName = (String) in.readObject();
                System.out.println("Caricamento tabella: " + tableName);
                
                // Valida l'input
                if (tableName == null || tableName.trim().isEmpty()) {
                    throw new IllegalArgumentException("Nome tabella non valido");
                }
                
                // Carica i dati dalla tabella del database
                Data newData = new Data(tableName);
                
                // Aggiorna lo stato solo se il caricamento ha successo
                this.data = newData;
                
                // Conferma il caricamento
                out.writeObject("OK");
                out.flush();
                System.out.println("Tabella " + tableName + " caricata con successo (" + 
                                data.getNumberOfExamples() + " esempi)");
                
        } catch (ClassNotFoundException | IOException | EmptyDatasetException | 
                    SQLException | DatabaseConnectionException | EmptySetException | 
                    IllegalArgumentException e) {
                System.err.println("Errore nel caricamento della tabella: " + e.getMessage());
                
                try {
                    String errorMsg = "Errore: ";
                    if (e instanceof SQLException) {
                        errorMsg += "Tabella non trovata o non valida nel database. Verifica che il nome sia corretto.";
                    } else {
                        errorMsg += e.getMessage();
                    }
                    out.writeObject(errorMsg);
                    out.flush();
                } catch (IOException ioException) {
                    System.err.println("Errore critico: impossibile comunicare con il client");
                    ioException.printStackTrace();
                }
        }
    }
        
        /**
         * Gestisce il comando 1: learning dal database con algoritmo QT
         */
    private void handleLearningFromDbTable() {
        try {
            // Verifica che i dati siano stati caricati
            if (data == null) {
                throw new IllegalStateException("Nessuna tabella caricata. Eseguire prima il comando 0.");
            }
            
            // Leggi il raggio dal client
            double radius = (double) in.readObject();
            System.out.println("Esecuzione clustering con raggio: " + radius);
            
            // Valida il raggio
            if (radius <= 0) {
                throw new IllegalArgumentException("Il raggio deve essere maggiore di zero");
            }
            
            // Crea l'oggetto QTMiner ed esegui il clustering
            kmeans = new QTMiner(radius);
            int numClusters = kmeans.compute(data);  // QUI può lanciare ClusteringRadiusException
            
            // SOLO SE NON CI SONO ECCEZIONI, invia OK e risultati
            out.writeObject("OK");
            out.writeObject(numClusters);
            out.writeObject(kmeans.getC().toString(data));
            out.flush();
            
            System.out.println("Clustering completato: " + numClusters + " cluster generati");
            
        } catch (ClusteringRadiusException e) {
            // Gestione specifica per raggio troppo grande
            System.err.println("Raggio troppo grande: " + e.getMessage());
            
            try {
                out.writeObject("Errore: Il raggio inserito è troppo grande! Tutte le tuple sono finite in un unico cluster. Prova con un raggio più piccolo (es. 0.5, 0.3, 0.1)");
                out.flush();
            } catch (IOException ioException) {
                System.err.println("Errore critico: impossibile comunicare con il client");
            }
        } catch (ClassNotFoundException | IOException | EmptyDatasetException | 
                IllegalStateException | IllegalArgumentException e) {
            System.err.println("Errore nel learning: " + e.getMessage());
            
            try {
                out.writeObject("ERROR: " + e.getMessage());
                out.flush();
            } catch (IOException ioException) {
                System.err.println("Errore critico: impossibile comunicare con il client");
            }
        }
    }
    
    /**
     * Gestisce il comando 2: salvataggio cluster su file
     */
    private void handleStoreClusterInFile() {
	try {
		// Verifica che il clustering sia stato eseguito
		if (kmeans == null) {
			throw new IllegalStateException("Nessun clustering eseguito. Eseguire prima il comando 1.");
		}
		
		// Leggi il nome del file dal client
		String fileName = (String) in.readObject();
		
		// Salva i cluster su file
		kmeans.salva(fileName);
		
		// Conferma il salvataggio
		out.writeObject("OK");
		out.flush();
		System.out.println("Cluster salvati su file: " + fileName);
		
        } catch (ClassNotFoundException | IOException | IllegalStateException e) {
            System.err.println("Errore nel salvataggio: " + e.getMessage());
            e.printStackTrace();
            
            try {
                out.writeObject("ERROR: " + e.getMessage());
                out.flush();
            } catch (IOException ioException) {
                System.err.println("Errore critico: impossibile comunicare con il client");
                ioException.printStackTrace();
            }
        }
    }
    
    /**
     * Gestisce il comando 3: caricamento cluster da file
     */
    private void handleLearningFromFile() {
        String fileName = null;
        try {
            // Verifica che i dati siano stati caricati
            if (data == null) {
                throw new IllegalStateException("Nessuna tabella caricata. Eseguire prima il comando 0.");
            }
            
            // Leggi il nome del file dal client
            fileName = (String) in.readObject();
            System.out.println("Caricamento cluster dal file: " + fileName);
            
            // Valida l'input
            if (fileName == null || fileName.trim().isEmpty()) {
                throw new IllegalArgumentException("Nome file non valido");
            }
            
            // Carica i cluster dal file
            kmeans = new QTMiner(fileName);
            
            // Invia conferma e risultati
            out.writeObject("OK");
            out.writeObject(kmeans.getC().toString(data));
            out.flush();
            
            System.out.println("Cluster caricati con successo dal file: " + fileName);
            
        } catch (ClassNotFoundException | IOException | IllegalStateException | IllegalArgumentException e) {
    
            try {
                String errorMsg = "Errore: ";
                if (e instanceof java.io.FileNotFoundException) {
                    errorMsg += "File '" + fileName + "' non trovato. Assicurati che il file esista e sia nella directory corretta.";
                    System.err.println("File non trovato: " + fileName);
                } else {
                    errorMsg += e.getMessage();
                    System.err.println("Errore nel caricamento da file: " + e.getMessage());
                }
                out.writeObject(errorMsg);
                out.flush();
            } catch (IOException ioException) {
                System.err.println("Errore critico: impossibile comunicare con il client");
                }   
        }
    }
    
    /**
     * Chiude la connessione e libera le risorse
     */
    private void closeConnection() {
    try {
        if (in != null) in.close();
        if (out != null) out.close();
            if (socket != null) {
                System.out.println("Connessione chiusa con il client " + 
                                socket.getInetAddress().getHostAddress() + 
                                ":" + socket.getPort());
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Errore durante la chiusura della connessione: " + e.getMessage());
        }
    }
}