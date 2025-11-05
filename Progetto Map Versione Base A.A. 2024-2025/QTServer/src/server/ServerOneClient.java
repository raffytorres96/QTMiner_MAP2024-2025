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
 * * <h2>Protocollo di Comunicazione</h2>
 * Il server supporta protocolli paralleli per servire client diversi:
 * <br><br>
 * <h3>Protocollo Complesso (per client Java <code>MainTest</code>)</h3>
 * <p>Utilizza risposte multiple (es. "OK" seguito dai dati).</p>
 * <ul>
 * <li><b>Comando 0</b>: {@link #handleStoreTableFromDb()} - Caricamento tabella dal database</li>
 * <li><b>Comando 1</b>: {@link #handleLearningFromDbTable()} - Esecuzione clustering QT</li>
 * <li><b>Comando 2</b>: {@link #handleStoreClusterInFile()} - Salvataggio cluster su file</li>
 * <li><b>Comando 3</b>: {@link #handleLearningFromFile()} - Caricamento cluster da file</li>
 * </ul>
 * * <h3>Protocollo Semplice (per client Android)</h3>
 * <p>Utilizza una singola stringa come risposta (dati o messaggio di errore).</p>
 * <ul>
 * <li><b>Comando 10</b>: {@link #handleStoreTableFromDb_Simple()} - Caricamento tabella</li>
 * <li><b>Comando 11</b>: {@link #handleLearningFromDbTable_Simple()} - Esecuzione clustering QT</li>
 * <li><b>Comando 12</b>: {@link #handleStoreClusterInFile_Simple()} - Salvataggio cluster</li>
 * <li><b>Comando 13</b>: {@link #handleLearningFromFile_Simple()} - Caricamento cluster</li>
 * </ul>
 * * <h3>Protocollo di Utilità (Heartbeat)</h3>
 * <p>Utilizzato per il monitoraggio della connessione.</p>
 * <ul>
 * <li><b>Comando 99</b>: Controlla la connessione (Ping). Il server risponde con la stringa "PONG".</li>
 * </ul>
 * * <h2>Gestione degli Errori</h2>
 * <p>
 * Ogni comando gestisce le proprie eccezioni specifiche e invia messaggi di errore
 * dettagliati al client attraverso lo stream di output. In caso di errori critici
 * di comunicazione (es. <code>SocketException</code>), la connessione viene chiusa automaticamente.
 * </p>
 *
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
     * * <h3>Comandi Supportati</h3>
     * <p>
     * Il server gestisce protocolli paralleli per supportare diversi client:
     * </p>
     * * <b>Protocollo Complesso (es. per client Java <code>MainTest</code>):</b>
     * <ul>
     * <li><b>0</b>: {@link #handleStoreTableFromDb()} - Carica tabella (protocollo a risposta multipla)</li>
     * <li><b>1</b>: {@link #handleLearningFromDbTable()} - Esegue clustering QT (protocollo a risposta multipla)</li>
     * <li><b>2</b>: {@link #handleStoreClusterInFile()} - Salva cluster su file (protocollo a risposta multipla)</li>
     * <li><b>3</b>: {@link #handleLearningFromFile()} - Carica cluster da file (protocollo a risposta multipla)</li>
     * </ul>
     * * <b>Protocollo Semplice (es. per client Android):</b>
     * <ul>
     * <li><b>10</b>: {@link #handleStoreTableFromDb_Simple()} - Carica tabella (protocollo a risposta singola)</li>
     * <li><b>11</b>: {@link #handleLearningFromDbTable_Simple()} - Esegue clustering QT (protocollo a risposta singola)</li>
     * <li><b>12</b>: {@link #handleStoreClusterInFile_Simple()} - Salva cluster (protocollo a risposta singola)</li>
     * <li><b>13</b>: {@link #handleLearningFromFile_Simple()} - Carica cluster (protocollo a risposta singola)</li>
     * </ul>
     *
     * <b>Protocollo di Utilità (Heartbeat):</b>
     * <ul>
     * <li><b>99</b>: Controlla la connessione (Ping). Il server risponde con la stringa "PONG".</li>
     * </ul>
     * * <p>
     * Il ciclo termina quando si verifica un'eccezione di I/O (tipicamente quando
     * il client chiude la connessione) o quando si riceve un oggetto non valido.
     * In ogni caso, le risorse vengono chiuse nel blocco finally.
     * </p>
     * * @see #closeConnection()
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

                    // Comandi Android Client
                    case 10: 
                        handleStoreTableFromDb_Simple();
                        break;
                    case 11: 
                        handleLearningFromDbTable_Simple();
                        break;
                    case 12: 
                        handleStoreClusterInFile_Simple();
                        break;
                    case 13: 
                        handleLearningFromFile_Simple();
                        break;

                    case 99: // Comando di PING
                        out.writeObject("PONG");
                        out.flush();
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
                // 1. Leggi il nome della tabella dal client
                tableName = (String) in.readObject();
                
                // 2. Valida l'input
                if (tableName == null || tableName.trim().isEmpty()) {
                    throw new IllegalArgumentException("Nome tabella non valido.");
                }
                
                // 3. Carica i dati
                Data newData = new Data(tableName);
                
                System.out.println("Caricamento tabella: " + tableName);
                
                // 5. Aggiorna lo stato solo se il caricamento ha successo
                this.data = newData;
                
                // 6. Conferma il caricamento
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
     * <li><b>Input atteso</b>: Double rappresentante il raggio del clustering</li>
     * <li><b>Output in caso di successo</b>: 
     * <ol>
     * <li>Stringa "OK"</li>
     * <li>Integer rappresentante il numero di cluster generati</li>
     * <li>Stringa contenente la rappresentazione testuale dei cluster</li>
     * </ol>
     * </li>
     * <li><b>Output in caso di errore</b>: Stringa contenente un messaggio di errore specifico</li>
     * </ul>
     * * <p>
     * Il metodo verifica che sia stata precedentemente caricata una tabella (comando 0),
     * valida il raggio ricevuto, esegue il clustering utilizzando {@link QTMiner#compute(Data)},
     * e invia i risultati al client solo se l'operazione ha successo.
     * </p>
     * * <h3>Prerequisiti</h3>
     * È necessario aver eseguito il comando 0 per caricare una tabella prima di invocare
     * questo comando, altrimenti viene lanciata un'eccezione {@code IllegalStateException}.
     * * <h3>Gestione degli Errori</h3>
     * <ul>
     * <li><b>IllegalStateException</b>: Nessuna tabella caricata (messaggio inviato al client)</li>
     * <li><b>IllegalArgumentException</b>: Raggio negativo o zero (messaggio inviato al client)</li>
     * <li><b>ClusteringRadiusException</b>: Raggio troppo grande (messaggio specifico inviato al client)</li>
     * <li><b>EmptyDatasetException</b>: Dataset vuoto (messaggio inviato al client)</li>
     * <li><b>ClassNotFoundException</b>: Il client ha inviato un tipo di dato non valido per il raggio (messaggio inviato al client)</li>
     * <li><b>IOException</b>: Errore di comunicazione (messaggio inviato al client)</li>
     * </ul>
     * * @see QTMiner#QTMiner(double)
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
                throw new IllegalArgumentException("Il raggio deve essere maggiore di zero.");
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
                
                String errorMsg = "ERROR: ";

                // Controlla il TIPO di errore per dare un messaggio valido
                if (e instanceof IllegalStateException || 
                    e instanceof IllegalArgumentException || 
                    e instanceof EmptyDatasetException) {
                    
                    // Errori lanciati da noi
                    errorMsg += e.getMessage();

                } else if (e instanceof ClassNotFoundException) {
                    // Errore di protocollo: il client ha inviato un tipo di dato errato
                    errorMsg += "Il client ha inviato un tipo di dato non valido per il raggio (atteso Double).";

                } else if (e instanceof IOException) {
                    // Errore di connessione (es. il client si è chiuso)
                    errorMsg += "Errore di comunicazione durante il clustering.";
                
                } else {
                    // Fallback per qualsiasi altro errore
                    String msg = e.getMessage();
                    errorMsg += (msg != null ? msg : "Errore sconosciuto durante il clustering.");
                }
            
            try {
                out.writeObject(errorMsg);
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
     * <li><b>Input atteso</b>: Stringa contenente il nome/percorso del file di destinazione</li>
     * <li><b>Output in caso di successo</b>: Stringa "OK"</li>
     * <li><b>Output in caso di errore</b>: Stringa "ERROR: " seguita da un messaggio di errore specifico</li>
     * </ul>
     * * <p>
     * Il metodo verifica che sia stato precedentemente eseguito un clustering (comando 1),
     * quindi salva i risultati su file utilizzando il metodo {@link QTMiner#salva(String)}.
     * </p>
     * * <h3>Prerequisiti</h3>
     * È necessario aver eseguito il comando 1 per generare i cluster prima di invocare
     * questo comando, altrimenti viene lanciata un'eccezione {@code IllegalStateException}.
     * * <h3>Gestione degli Errori</h3>
     * <ul>
     * <li><b>IllegalStateException</b>: Nessun clustering eseguito (messaggio inviato al client)</li>
     * <li><b>IOException</b>: Errore durante la scrittura del file (messaggio inviato al client)</li>
     * <li><b>ClassNotFoundException</b>: Il client ha inviato un tipo di dato non valido per il nome del file (messaggio inviato al client)</li>
     * </ul>
     * * @see QTMiner#salva(String)
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
            
                String errorMsg = "Errore: ";
                
                if (e instanceof IllegalStateException) {
                    // Questo errore lo abbiamo lanciato noi
                    errorMsg += e.getMessage();
                
                } else if (e instanceof ClassNotFoundException) {
                    // Errore di protocollo: il client ha inviato un tipo di dato errato
                    errorMsg += "Il client ha inviato un tipo di dato non valido per il nome del file.";

                } else if (e instanceof IOException) {
                    // Errore di I/O
                    errorMsg += "Errore di I/O sul server. Impossibile scrivere il file.";
                
                } else {
                    // Qualsiasi altro errore
                    String msg = e.getMessage();
                    errorMsg += (msg != null ? msg : "Errore sconosciuto durante il salvataggio.");
                }
            
            try {
                out.writeObject(errorMsg);
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
            if (data == null) {
                throw new IllegalStateException("Nessuna tabella caricata. Eseguire prima il comando 0.");
            }
            
            fileName = (String) in.readObject();
            System.out.println("Caricamento cluster dal file: " + fileName);
            
            if (fileName == null || fileName.trim().isEmpty()) {
                throw new IllegalArgumentException("Nome file non valido");
            }
            
            kmeans = new QTMiner(fileName);
            
            // --- 4. INVIO RISPOSTA (SUCCESSO) ---
            out.writeObject("OK");
            out.writeObject(kmeans.getC().toString(data));
            
            System.out.println("Cluster caricati con successo dal file: " + fileName);
            
        } catch (ClassNotFoundException | IOException | IllegalStateException | IllegalArgumentException e) {
            
            // --- 5. GESTIONE ERRORE ---
            String errorMsg = "Errore: ";
            if (e instanceof java.io.FileNotFoundException) {
                // Messaggio più specifico per un errore comune
                errorMsg += "File '" + fileName + "' non trovato. Assicurati che esista e sia nella directory corretta.";
                System.err.println("File non trovato: " + fileName);
            } else {
                // Messaggio generico per tutti gli altri errori
                errorMsg += e.getMessage();
                System.err.println("Errore nel caricamento da file: " + e.getMessage());
            }
            
            // --- 6. INVIO RISPOSTA (ERRORE) ---
            try {
                out.writeObject(errorMsg);
            } catch (IOException ioException) {
                // Se anche inviare l'errore fallisce, non possiamo fare altro
                System.err.println("Errore critico: impossibile comunicare con il client");
            }   
            
        } finally {
            // Questo blocco viene eseguito SEMPRE, sia dopo il successo (4)
            // sia dopo aver tentato di inviare un errore (6).
            // Garantisce che qualsiasi cosa scritta venga spedita.
            try {
                out.flush();
            } catch (IOException e) {
                System.err.println("Errore durante il flush della connessione: " + e.getMessage());
            }
        }
    }

    /**
     * Gestisce il COMANDO 10 (Android - Protocollo Semplice).
     * <p>
     * Carica i dati da una tabella specificata del database. Questo metodo è
     * necessario per inizializzare l'oggetto {@code data} prima di poter
     * eseguire operazioni di clustering (11) o caricamento da file (13).
     * </p>
     * <p>
     * A differenza del protocollo complesso, questo metodo invia una singola stringa
     * come risposta.
     * </p>
     * <b>Protocollo Semplice:</b>
     * <ul>
     * <li><b>Input Atteso:</b> String - Nome della tabella (es. "playtennis").</li>
     * <li><b>Output in caso di successo:</b> Stringa "OK".</li>
     * <li><b>Output in caso di errore:</b> Stringa "Errore: " seguita da un messaggio specifico.</li>
     * </ul>
     *
     * <h3>Gestione degli Errori</h3>
     * <p>
     * Il metodo intercetta le eccezioni e invia un messaggio chiaro al client:
     * </p>
     * <ul>
     * <li><b>SQLException / DatabaseConnectionException</b>: Invia un messaggio generico per problemi di connessione al DB (es. MySQL spento, credenziali errate).</li>
     * <li><b>EmptyDatasetException</b>: Comunica che la tabella è vuota.</li>
     * <li><b>IllegalArgumentException / ClassNotFoundException</b>: Comunica un errore di input o di protocollo.</li>
     * </ul>
     *
     * @see Data#Data(String)
     */
    private void handleStoreTableFromDb_Simple() {
        String tableName = null;
        try {
            tableName = (String) in.readObject();
            
            if (tableName == null || tableName.trim().isEmpty()) {
                throw new IllegalArgumentException("Nome tabella non valido.");
            }
            
            Data newData = new Data(tableName); // Chiama DbAccess.initConnection()
            
            System.out.println("Caricamento tabella (simple): " + tableName);
        
            this.data = newData;
            
            out.writeObject("OK");
            System.out.println("Tabella " + tableName + " (simple) caricata con successo (" + 
                                data.getNumberOfExamples() + " esempi)");

        } catch (ClassNotFoundException | IOException | EmptyDatasetException | 
                 SQLException | DatabaseConnectionException | EmptySetException | 
                 IllegalArgumentException e) {
            
            // Log dettagliato sul server (come nel metodo complesso)
            System.err.println("Errore nel caricamento della tabella (simple): " + e.getMessage());
            
            try {
                // Creazione messaggio di errore pulito (come nel metodo complesso)
                String errorMsg = "Errore: ";
                if (e instanceof SQLException || e instanceof DatabaseConnectionException) {
                    errorMsg = "Errore: Server MySQL non in funzione o database non trovato.";
                } else if (e instanceof EmptyDatasetException) {
                    errorMsg = "Errore: La tabella '" + tableName + "' è vuota.";
                } else {
                    errorMsg += e.getMessage();
                }
                
                // Invio errore (protocollo semplice)
                out.writeObject(errorMsg);

            } catch (IOException ioException) {
                System.err.println("Errore critico (simple): impossibile comunicare con il client");
                ioException.printStackTrace();
            }

        } finally {
            try { out.flush(); } catch (IOException ignore) {}
        }
    }

    /**
     * Gestisce il COMANDO 11 (Android - Protocollo Semplice).
     * <p>
     * Esegue l'algoritmo di clustering QT sui dati correntemente caricati
     * (impostati dal comando 10) utilizzando il raggio specificato.
     * </p>
     * <b>Protocollo Semplice:</b>
     * <ul>
     * <li><b>Input Atteso:</b> Double - Il raggio per il clustering.</li>
     * <li><b>Output:</b> String - La rappresentazione testuale dei cluster (<code>kmeans.getC().toString(data)</code>) 
     * in caso di successo, o "Errore: [messaggio]" in caso di fallimento.</li>
     * </ul>
     * <b>Prerequisiti:</b>
     * <ul>
     * <li>È necessario aver eseguito con successo il comando 10 per inizializzare l'oggetto {@code data}.</li>
     * </ul>
     *
     * <h3>Gestione degli Errori</h3>
     * <p>Il metodo intercetta le eccezioni e invia un messaggio chiaro al client:</p>
     * <ul>
     * <li><b>ClusteringRadiusException</b>: Invia un messaggio specifico se il raggio è troppo grande.</li>
     * <li><b>IllegalStateException</b>: Se la tabella non è stata caricata (<code>data</code> è <code>null</code>).</li>
     * <li><b>IllegalArgumentException</b>: Se il raggio è <code>&lt;= 0</code>.</li>
     * <li><b>ClassNotFoundException</b>: Se il client invia un tipo di dato errato.</li>
     * </ul>
     *
     * @see QTMiner#QTMiner(double)
     * @see QTMiner#compute(Data)
     */
    private void handleLearningFromDbTable_Simple() {
        try {
            double radius = (double) in.readObject();
            
            System.out.println("Esecuzione clustering (simple) con raggio: " + radius);

            if (data == null) {
                throw new IllegalStateException("Nessuna tabella caricata. Eseguire prima il comando 10.");
            }
            if (radius <= 0) {
                throw new IllegalArgumentException("Il raggio deve essere maggiore di zero.");
            }

            kmeans = new QTMiner(radius);
            int numClusters = kmeans.compute(data); // Può lanciare ClusteringRadiusException
            
            String clusterString = kmeans.getC().toString(data);
            String modifiedString = clusterString.replaceAll("(?m)^AvgDistance.*", "$0\n");
            out.writeObject(modifiedString); // Invia stringa di successo
            
            System.out.println("Clustering completato (simple): " + numClusters + " cluster generati");
            
        } catch (ClusteringRadiusException e) {
            // --- GESTIONE ERRORE SPECIFICO ---
            System.err.println("Errore nel learning (simple): " + e.getMessage());
            try { 
                out.writeObject("Errore: Raggio troppo alto. Prova con un valore più piccolo."); 
            } catch (IOException ignore) {} // Invia stringa di errore specifica

        } catch (Exception e) {
            // --- GESTIONE ERRORE GENERICO ---
            System.err.println("Errore nel learning (simple): " + e.getMessage());
            String errorMsg = "Errore: ";
            
            if (e instanceof IllegalStateException || e instanceof IllegalArgumentException) {
                errorMsg += e.getMessage(); // Usa il messaggio chiaro
            } else if (e instanceof ClassNotFoundException) {
                errorMsg += "Il client ha inviato un tipo di dato non valido per il raggio.";
            } else if (e instanceof IOException) {
                errorMsg += "Errore di comunicazione durante il clustering.";
            } else {
                errorMsg += (e.getMessage() != null ? e.getMessage() : "Errore sconosciuto.");
            }
            
            try { 
                out.writeObject(errorMsg); 
            } catch (IOException ignore) {}
        
        } finally {
            try { out.flush(); } catch (IOException ignore) {}
        }
    }

    /**
     * Gestisce il COMANDO 12 (Android - Protocollo Semplice).
     * <p>
     * Salva su file (sul server) i cluster generati dall'ultima operazione
     * di clustering (comando 11) o di caricamento (comando 13).
     * </p>
     * <b>Protocollo Semplice:</b>
     * <ul>
     * <li><b>Input Atteso:</b> String - Il nome del file (es. "clusters.dmp") in cui salvare i cluster.</li>
     * <li><b>Output:</b> String - "OK" in caso di successo, o "Errore: [messaggio]" in caso di fallimento.</li>
     * </ul>
     * <b>Prerequisiti:</b>
     * <ul>
     * <li>È necessario aver eseguito con successo il comando 11 o 13 per inizializzare l'oggetto {@code kmeans}.</li>
     * </ul>
     *
     * <h3>Gestione degli Errori</h3>
     * <ul>
     * <li><b>IllegalStateException</b>: Se non è stato eseguito nessun clustering.</li>
     * <li><b>IllegalArgumentException</b>: Se il nome del file è nullo o vuoto.</li>
     * <li><b>IOException</b>: Se si verifica un errore durante la scrittura del file sul server.</li>
     * <li><b>ClassNotFoundException</b>: Se il client invia un tipo di dato errato.</li>
     * </ul>
     *
     * @see QTMiner#salva(String)
     */
    private void handleStoreClusterInFile_Simple() {
        String fileName = null;
        try {
            fileName = (String) in.readObject();

            System.out.println("Salvataggio cluster (simple) su file: " + fileName);

            if (kmeans == null) {
                throw new IllegalStateException("Nessun clustering eseguito. Eseguire prima il clustering (comando 11).");
            }
            if (fileName == null || fileName.trim().isEmpty()) {
                throw new IllegalArgumentException("Nome file non valido.");
            }

            kmeans.salva(fileName);
            out.writeObject("OK"); // Invia stringa di successo
            
            System.out.println("Cluster salvati (simple) con successo.");
            
        } catch (Exception e) {
            // --- GESTIONE ERRORE ---
            System.err.println("Errore nel salvataggio (simple): " + e.getMessage());
            String errorMsg = "Errore: ";
            
            if (e instanceof IllegalStateException || e instanceof IllegalArgumentException) {
                errorMsg += e.getMessage(); // Usa il messaggio chiaro
            } else if (e instanceof ClassNotFoundException) {
                errorMsg += "Il client ha inviato un tipo di dato non valido per il nome del file.";
            } else if (e instanceof IOException) {
                errorMsg += "Errore di I/O sul server. Impossibile scrivere il file.";
            } else {
                errorMsg += (e.getMessage() != null ? e.getMessage() : "Errore sconosciuto durante il salvataggio.");
            }

            try { 
                out.writeObject(errorMsg); 
            } catch (IOException ignore) {}
        } finally {
            try { out.flush(); } catch (IOException ignore) {}
        }
    }

    /**
     * Gestisce il COMANDO 13 (Android - Protocollo Semplice).
     * <p>
     * Carica i cluster da un file precedentemente salvato (sul server) e li
     * applica ai dati correntemente in memoria (caricati con il comando 10)
     * per restituirne la rappresentazione testuale.
     * </p>
     * <b>Protocollo Semplice:</b>
     * <ul>
     * <li><b>Input Atteso:</b> String - Il nome del file (es. "clusters.dmp") da cui caricare i cluster.</li>
     * <li><b>Output:</b> String - La rappresentazione testuale dei cluster (<code>kmeans.getC().toString(data)</code>) 
     * in caso di successo, o "Errore: [messaggio]" in caso di fallimento.</li>
     * </ul>
     * <b>Prerequisiti:</b>
     * <ul>
     * <li>È necessario aver eseguito con successo il comando 10 per inizializzare l'oggetto {@code data}.</li>
     * </ul>
     *
     * <h3>Gestione degli Errori</h3>
     * <ul>
     * <li><b>IllegalStateException</b>: Se la tabella non è stata caricata (<code>data</code> è <code>null</code>).</li>
     * <li><b>IllegalArgumentException</b>: Se il nome del file è nullo o vuoto.</li>
     * <li><b>FileNotFoundException</b>: Se il file non viene trovato sul server.</li>
     * <li><b>IOException / ClassNotFoundException</b>: Se il file è corrotto o si verifica un errore di I/O.</li>
     * </ul>
     *
     * @see QTMiner#QTMiner(String)
     */
    private void handleLearningFromFile_Simple() {
        String fileName = null;
        try {
            fileName = (String) in.readObject(); 

            System.out.println("Caricamento cluster (simple) dal file: " + fileName);

            if (data == null) {
                throw new IllegalStateException("Nessuna tabella caricata. Eseguire prima 'Clustering da tabella' almeno una volta.");
            }
            if (fileName == null || fileName.trim().isEmpty()) {
                throw new IllegalArgumentException("Nome file non valido.");
            }
            
            kmeans = new QTMiner(fileName);
            
            String clusterString = kmeans.getC().toString(data);
            String modifiedString = clusterString.replaceAll("(?m)^AvgDistance.*", "$0\n");
            out.writeObject(modifiedString); // Invia stringa di successo
            
            System.out.println("Cluster caricati (simple) con successo.");

        } catch (Exception e) {
            // --- GESTIONE ERRORE ---
            System.err.println("Errore nel caricamento da file (simple): " + e.getMessage());
            String errorMsg = "Errore: ";

            if (e instanceof java.io.FileNotFoundException) {
                errorMsg = "Errore: File '" + fileName + "' non trovato sul server.";
            } else if (e instanceof IllegalStateException || e instanceof IllegalArgumentException) {
                errorMsg += e.getMessage(); // Usa il nostro messaggio chiaro
            } else if (e instanceof ClassNotFoundException || e instanceof IOException) {
                errorMsg += "Errore di I/O o file corrotto. Impossibile leggere il file cluster.";
            } else {
                errorMsg += (e.getMessage() != null ? e.getMessage() : "Errore sconosciuto durante il caricamento.");
            }

            try { 
                out.writeObject(errorMsg); 
            } catch (IOException ignore) {}
        
        } finally {
            try { out.flush(); } catch (IOException ignore) {}
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