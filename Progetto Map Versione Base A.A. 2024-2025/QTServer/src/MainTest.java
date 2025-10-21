import java.util.Scanner;
import data.Data;
import data.EmptyDatasetException;
import database.DatabaseConnectionException;
import mining.QTMiner;
import mining.Cluster;
import mining.ClusteringRadiusException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import database.EmptySetException;

public class MainTest {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Data data = null;
        boolean continua = true;
        
        while(continua) {
            System.out.println("Scegli una opzione");
            System.out.println("(1) Carica Cluster da File");
            System.out.println("(2) Carica Dati");
            System.out.print("Risposta:");

            try {
                int scelta = scanner.nextInt();
                scanner.nextLine();
                
                switch (scelta) {
                    case 1:
                        caricaClusterDaFile(data, scanner);
                        break;
                    case 2:
                        data = caricaDati(scanner);
                        if (data != null) {
                            eseguiClustering(data, scanner);
                        }
                        break;
                    default:
                        System.out.println("Opzione non valida");
                }
            } catch (Exception e) {
                System.out.println("Error reading int data, MIN_VALUE value returned.");
                scanner.nextLine();
                continue;
            }

            System.out.print("Would you choose another option from the menu?(y/n)");
            String risposta = scanner.nextLine();
            continua = risposta.equalsIgnoreCase("y");
        }
        
        scanner.close();
    }
    
    private static void caricaClusterDaFile(Data data, Scanner scanner) {
        System.out.print("Nome archivio:");
        String fileName = scanner.nextLine();
        
        try {
            QTMiner qt = new QTMiner(fileName + ".dmp");
				int i = 1;
            for (Cluster c : qt.getC()){
                System.out.println(i + ":" + c.toString());
				i++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("File non trovato: " + fileName + ".dmp");
        } catch (IOException e) {
            System.out.println("Errore di I/O durante il caricamento del file.");
        } catch (ClassNotFoundException e) {
            System.out.println("Errore nella deserializzazione del file.");
        }
    }
    
    private static Data caricaDati(Scanner scanner) {
    System.out.print("Nome della tabella: ");
    String tableName = scanner.nextLine();
    
    try {
        Data data = new Data(tableName); // Usa il costruttore con il nome della tabella
        System.out.println("Dati caricati dalla tabella: " + tableName);
        System.out.println(data);
        return data;
    } catch (EmptyDatasetException e) {
        System.out.println("Dataset vuoto: " + e.getMessage());
        return null;
    } catch (SQLException e) {
        System.out.println("Errore SQL: " + e.getMessage());
        return null;
    } catch (DatabaseConnectionException e) {
        System.out.println("Errore di connessione al database: " + e.getMessage());
        return null;
    } catch (EmptySetException e) {
        System.out.println("Set vuoto: " + e.getMessage());
        return null;
    }
}
    
    private static void eseguiClustering(Data data, Scanner scanner) {
        while(true) {
            double radius;
            
            do {
                System.out.print("Insert radius (>0):");
                radius = scanner.nextDouble();
                if(radius <= 0) {
                    System.out.println("Il raggio deve essere maggiore di 0!");
                }
            } while(radius <= 0);

            QTMiner qt = new QTMiner(radius);
            try {
                int numIter = qt.compute(data);
                System.out.println("Number of clusters:" + numIter);
                System.out.println(qt.getC().toString(data));
                
                // Salva i cluster
                System.out.print("Backup file name:");
                scanner.nextLine(); // Consuma il newline
                String backupName = scanner.nextLine();
                
                try {
                    qt.salva(backupName);
                    System.out.println("Saving clusters in " + backupName + ".dmp");
                    System.out.println("Saving transaction ended!");
                } catch (IOException e) {
                    System.out.println("Errore durante il salvataggio: " + e.getMessage());
                }
                
                System.out.print("New execution?(y/n)");
                String answer = scanner.nextLine();
                if(!answer.equalsIgnoreCase("y")) break;
                
            } catch(ClusteringRadiusException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}