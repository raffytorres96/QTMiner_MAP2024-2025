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
 * <p>
 * Estende {@link Thread} per permettere la gestione concorrente di più client.
 * Ogni istanza di questa classe gestisce un client specifico attraverso un socket dedicato
 * e rimane in ascolto dei comandi inviati dal client, elaborandoli di conseguenza.
 * </p>
 * 
 * <h2>Protocollo di Comunicazione</h2>
 * Il server supporta 4 comandi principali identificati da codici numerici:
 * <ul>
 *   <li><b>Comando 0</b>: Caricamento di una tabella dal database</li>
 *   <li><b>Comando 1</b>: Esecuzione del clustering QT sui dati caricati</li>
 *   <li><b>Comando 2</b>: Salvataggio dei cluster generati su file</li>
 *   <li><b>Comando 3</b>: Caricamento di cluster precedentemente salvati da file</li>
 * </ul>
 * 
 * <h2>Gestione degli Errori</h2>
 * Ogni comando gestisce le proprie eccezioni specifiche e invia messaggi di errore
 * dettagliati al client attraverso lo stream di output. In caso di errori critici
 * di comunicazione, la connessione viene chiusa automaticamente.
 * 
 * @author [Nome Autore]
 * @version 1.0
 * @see QTMiner
 * @see Data
 * @see MultiServer
 */
public class ServerOneClient extends Thread {
    
    /**
     * Socket di connessione con il client.
     */
    private Socket socket;
    
    /**
     * Stream di input per ricevere oggetti dal client.
     */
    private ObjectInputStream in;
    
    /**
     * Stream di output per inviare oggetti al client.
     */
    private ObjectOutputStream out;
    
    /**
     * Oggetto QTMiner utilizzato per l'algoritmo di clustering.
     * Viene inizializzato durante l'esecuzione del comando 1 o 3.
     */
    private QTMiner kmeans;
    
    /**
     * Dataset contenente i dati caricati dal database.
     * Viene inizializzato durante l'esecuzione del comando 0.
     */
    private Data data;
    
    /**
     * Costruttore di classe. Inizializza gli stream di comunicazione e avvia il thread.
     * <p>
     * Gli stream vengono inizializzati nell'ordine corretto: prima {@code ObjectOutputStream}
     * e poi {@code ObjectInputStream} per evitare deadlock nella comunicazione client-server.
     * </p>
     * 
     * @param s Socket già connesso al client
     * @throws IOException se si verifica un errore durante l'inizializzazione degli stream
     *                     di input/output sul socket
     */
    public ServerOneClient(Socket s) throws IOException {
        this.socket = s;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        
        // Avvia il thread
        start();
    }
    
    /**
     * Metodo principale del thread che gestisce le richieste del client.
     * <p>
     * Rimane in ascolto continuo dei comandi inviati dal client attraverso lo stream
     * di input. Per ogni comando ricevuto (codice intero), invoca il metodo handler
     * corrispondente che elabora la richiesta e invia la risposta al client.
     * </p>
     * 
     * <h3>Comandi Supportati</h3>
     * <ul>
     *   <li><b>0</b>: {@link #handleStoreTableFromDb()} - Carica tabella dal database</li>
     *   <li><b>1</b>: {@link #handleLearningFromDbTable()} - Esegue clustering QT</li>
     *   <li><b>2</b>: {@link #handleStoreClusterInFile()} - Salva cluster su file</li>
     *   <li><b>3</b>: {@link #handleLearningFromFile()} - Carica cluster da file</li>
     * </ul>
     * 
     * <p>
     * Il ciclo termina quando si verifica un'eccezione di I/O (tipicamente quando
     * il client chiude la connessione) o quando si riceve un oggetto non valido.
     * In ogni caso, le risorse vengono chiuse nel blocco finally.
     * </p>
     * 
     * @see #closeConnection()
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
     * Gestisce il comando 0: caricamento di una tabella dal database.
     * <p>
     * <b>Protocollo di comunicazione:</b>
     * </p>
     * <ul>
     *   <li><b>Input atteso</b>: Stringa contenente il nome della tabella da caricare</li>
     *   <li><b>Output in caso di successo</b>: Stringa "OK"</li>
     *   <li><b>Output in caso di errore</b>: Stringa contenente il messaggio di errore dettagliato</li>
     * </ul>
     * 
     * <p>
     * Il metodo valida il nome della tabella ricevuto, carica i dati dal database
     * utilizzando la classe {@link Data}, e aggiorna l'attributo {@code data} solo
     * se il caricamento ha successo. In caso di errore, invia un messaggio specifico
     * al client senza modificare lo stato interno del server.
     * </p>
     * 
     * <h3>Gestione degli Errori</h3>
     * <ul>
     *   <li><b>SQLException</b>: Tabella non trovata o nome non valido nel database</li>
     *   <li><b>EmptyDatasetException</b>: La tabella esiste ma non contiene dati</li>
     *   <li><b>DatabaseConnectionException</b>: Impossibile connettersi al database</li>
     *   <li><b>IllegalArgumentException</b>: Nome tabella nullo o vuoto</li>
     * </ul>
     * 
     * @see Data#Data(String)
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
     * Gestisce il comando 1: esecuzione dell'algoritmo di clustering QT sui dati caricati.
     * <p>
     * <b>Protocollo di comunicazione:</b>
     * </p>
     * <ul>
     *   <li><b>Input atteso</b>: Double rappresentante il raggio del clustering</li>
     *   <li><b>Output in caso di successo</b>: 
     *     <ol>
     *       <li>Stringa "OK"</li>
     *       <li>Integer rappresentante il numero di cluster generati</li>
     *       <li>Stringa contenente la rappresentazione testuale dei cluster</li>
     *     </ol>
     *   </li>
     *   <li><b>Output in caso di errore</b>: Stringa contenente il messaggio di errore</li>
     * </ul>
     * 
     * <p>
     * Il metodo verifica che sia stata precedentemente caricata una tabella (comando 0),
     * valida il raggio ricevuto, esegue il clustering utilizzando {@link QTMiner#compute(Data)},
     * e invia i risultati al client solo se l'operazione ha successo.
     * </p>
     * 
     * <h3>Prerequisiti</h3>
     * È necessario aver eseguito il comando 0 per caricare una tabella prima di invocare
     * questo comando, altrimenti viene lanciata un'eccezione {@code IllegalStateException}.
     * 
     * <h3>Gestione degli Errori</h3>
     * <ul>
     *   <li><b>IllegalStateException</b>: Nessuna tabella caricata</li>
     *   <li><b>IllegalArgumentException</b>: Raggio negativo o zero</li>
     *   <li><b>ClusteringRadiusException</b>: Raggio troppo grande (tutte le tuple in un cluster)</li>
     *   <li><b>EmptyDatasetException</b>: Dataset vuoto</li>
     * </ul>
     * 
     * @see QTMiner#QTMiner(double)
     * @see QTMiner#compute(Data)
     * @see QTMiner#getC()
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
                out.writeObject("Errore: Il raggio inserito è troppo grande! Tutte le tuple sono finite in un unico cluster. Prova con un raggio più piccolo (es. 1, 2, 3)");
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
     * Gestisce il comando 2: salvataggio dei cluster generati su file.
     * <p>
     * <b>Protocollo di comunicazione:</b>
     * </p>
     * <ul>
     *   <li><b>Input atteso</b>: Stringa contenente il nome/percorso del file di destinazione</li>
     *   <li><b>Output in caso di successo</b>: Stringa "OK"</li>
     *   <li><b>Output in caso di errore</b>: Stringa "ERROR: " seguita dal messaggio di errore</li>
     * </ul>
     * 
     * <p>
     * Il metodo verifica che sia stato precedentemente eseguito un clustering (comando 1),
     * quindi salva i risultati su file utilizzando il metodo {@link QTMiner#salva(String)}.
     * </p>
     * 
     * <h3>Prerequisiti</h3>
     * È necessario aver eseguito il comando 1 per generare i cluster prima di invocare
     * questo comando, altrimenti viene lanciata un'eccezione {@code IllegalStateException}.
     * 
     * <h3>Gestione degli Errori</h3>
     * <ul>
     *   <li><b>IllegalStateException</b>: Nessun clustering eseguito</li>
     *   <li><b>IOException</b>: Errore durante la scrittura del file</li>
     * </ul>
     * 
     * @see QTMiner#salva(String)
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
     * Gestisce il comando 3: caricamento di cluster precedentemente salvati da file.
     * <p>
     * <b>Protocollo di comunicazione:</b>
     * </p>
     * <ul>
     *   <li><b>Input atteso</b>: Stringa contenente il nome/percorso del file da caricare</li>
     *   <li><b>Output in caso di successo</b>:
     *     <ol>
     *       <li>Stringa "OK"</li>
     *       <li>Stringa contenente la rappresentazione testuale dei cluster caricati</li>
     *     </ol>
     *   </li>
     *   <li><b>Output in caso di errore</b>: Stringa contenente il messaggio di errore dettagliato</li>
     * </ul>
     * 
     * <p>
     * Il metodo verifica che sia stata precedentemente caricata una tabella (comando 0),
     * valida il nome del file ricevuto, carica i cluster utilizzando il costruttore
     * {@link QTMiner#QTMiner(String)}, e invia la rappresentazione dei cluster al client.
     * </p>
     * 
     * <h3>Prerequisiti</h3>
     * È necessario aver eseguito il comando 0 per caricare una tabella prima di invocare
     * questo comando, in quanto i cluster vengono visualizzati in relazione ai dati caricati.
     * 
     * <h3>Gestione degli Errori</h3>
     * <ul>
     *   <li><b>IllegalStateException</b>: Nessuna tabella caricata</li>
     *   <li><b>IllegalArgumentException</b>: Nome file nullo o vuoto</li>
     *   <li><b>FileNotFoundException</b>: File non trovato nel percorso specificato</li>
     *   <li><b>IOException</b>: Errore durante la lettura del file</li>
     *   <li><b>ClassNotFoundException</b>: File corrotto o formato non valido</li>
     * </ul>
     * 
     * @see QTMiner#QTMiner(String)
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
     * Chiude la connessione con il client e libera tutte le risorse associate.
     * <p>
     * Il metodo chiude nell'ordine: lo stream di input, lo stream di output e infine
     * il socket. Ogni operazione di chiusura è protetta da un try-catch per garantire
     * che eventuali errori non impediscano la chiusura delle altre risorse.
     * </p>
     * <p>
     * Viene automaticamente invocato nel blocco {@code finally} del metodo {@link #run()}
     * quando il thread termina, sia per disconnessione del client che per errori di comunicazione.
     * </p>
     * 
     * @see #run()
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