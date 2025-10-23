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

