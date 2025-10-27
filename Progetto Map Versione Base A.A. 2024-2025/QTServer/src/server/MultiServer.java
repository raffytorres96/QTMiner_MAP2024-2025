package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Classe che implementa un server multi-client in grado di gestire 
 * connessioni simultanee da più client.
 * <p>
 * Il server rimane in ascolto su una porta specificata e per ogni nuova 
 * connessione client accettata, delega la gestione a un'istanza di 
 * {@link ServerOneClient}.
 * </p>
 *
 */
public class MultiServer {

    /**
     * Porta su cui il server rimane in ascolto per le connessioni client.
     */
    private int PORT = 8080;

    /**
     * Costruttore che inizializza il server sulla porta specificata 
     * e avvia immediatamente il server.
     * 
     * @param port la porta su cui il server deve rimanere in ascolto
     */
    public MultiServer(int port){
        this.PORT = port;
        run();
    }

    /**
     * Metodo principale che avvia il server e gestisce le connessioni client.
     * <p>
     * Il server crea un {@link ServerSocket} sulla porta specificata e rimane 
     * in un ciclo infinito in attesa di connessioni. Per ogni connessione 
     * accettata, viene creata una nuova istanza di {@link ServerOneClient} 
     * che gestisce la comunicazione con quel client specifico.
     * </p>
     * <p>
     * In caso di errore durante l'accettazione di una connessione client, 
     * l'errore viene loggato ma il server continua a rimanere in ascolto.
     * </p>
     */
    public void run() {
        ServerSocket serverSocket = null;
        
        try {
            // Crea il ServerSocket sulla porta specificata
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server in ascolto sulla porta " + PORT + "...");
            
            // Ciclo infinito per accettare connessioni
            while (true) {
                try {
                    // Attende una richiesta di connessione dal client
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Nuova connessione accettata: " + 
                                     clientSocket.getInetAddress().getHostAddress() + 
                                     ":" + clientSocket.getPort());
                    
                    // Per ogni nuova connessione, istanzia ServerOneClient
                    new ServerOneClient(clientSocket);
                    
                } catch (IOException e) {
                    System.err.println("Errore nell'accettare la connessione del client: " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            System.err.println("Errore nell'avvio del server sulla porta " + PORT + ": " + e.getMessage());
        } finally {
            // Chiude il ServerSocket se è stato creato
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                    System.out.println("Server chiuso.");
                } catch (IOException e) {
                    System.err.println("Errore nella chiusura del server: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Metodo main che avvia il server multi-client.
     * <p>
     * Crea un'istanza di {@link MultiServer} sulla porta 8080 di default.
     * </p>
     * 
     * @param args argomenti della riga di comando (non utilizzati)
     */
    public static void main(String[] args){
        int PORT = 8080;
        System.out.println("Avvio server sulla porta " + PORT + "...");
        new MultiServer(PORT);
    }

}