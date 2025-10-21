package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiServer {

    private int PORT = 8080;

    public MultiServer(int port){
        this.PORT = port;
        run();
    }

    /*public void run(){
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while(true){
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuova connessione da " + clientSocket.getInetAddress());

                new ServerOneClient(clientSocket);
            }
        } catch (IOException e) {
            System.out.println("Errore nel server: " + e.getMessage());
        }
    }*/

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

    public static void main(String[] args){
        int PORT = 8080;
        System.out.println("Avvio server sulla porta " + PORT + "...");
        MultiServer multiserver = new MultiServer(PORT);
    }

}
