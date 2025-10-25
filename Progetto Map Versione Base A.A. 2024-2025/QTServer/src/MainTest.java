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
            System.out.println("\nScegli una opzione:");
            System.out.println("(1) Carica Cluster da File");
            System.out.println("(2) Carica Dati da Database");
            System.out.print("Risposta: ");

            try {
                int scelta = scanner.nextInt();
                scanner.nextLine(); // Consuma il newline dopo nextInt()
                
                switch (scelta) {
                    case 1:
                        caricaClusterDaFile(scanner);
                        break;
                    case 2:
                        data = caricaDati(scanner);
                        if (data != null) {
                            eseguiClustering(data, scanner);
                        }
                        break;
                    default:
                        System.out.println("Opzione non valida. Scegli 1 o 2.");
                }
            } catch (Exception e) {
                System.out.println("Errore: input non valido. Inserisci un numero.");
                scanner.nextLine(); // Pulisce il buffer
                continue;
            }

            System.out.print("\nVuoi scegliere un'altra opzione? (y/n): ");
            String risposta = scanner.nextLine();
            continua = risposta.equalsIgnoreCase("y");
        }
        
        System.out.println("Programma terminato.");
        scanner.close();
    }
    
    /**
     * Carica i cluster da un file serializzato e li visualizza.
     * 
     * @param scanner lo scanner per leggere l'input dell'utente
     */
    private static void caricaClusterDaFile(Scanner scanner) {
        System.out.print("Nome archivio (senza estensione): ");
        String fileName = scanner.nextLine();
        
        try {
            QTMiner qt = new QTMiner(fileName + ".dmp");
            System.out.println("\nCluster caricati da " + fileName + ".dmp:");
            System.out.println("----------------------------------------");
            
            int i = 1;
            for (Cluster c : qt.getC()) {
                System.out.println("Cluster " + i + ":");
                System.out.println(c.toString());
                System.out.println();
                i++;
            }
            System.out.println("Totale cluster: " + (i-1));
            
        } catch (FileNotFoundException e) {
            System.out.println("Errore: File '" + fileName + ".dmp' non trovato.");
        } catch (IOException e) {
            System.out.println("Errore di I/O durante il caricamento del file: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Errore nella deserializzazione del file: " + e.getMessage());
        }
    }
    
    /**
     * Carica i dati da una tabella del database.
     * 
     * @param scanner lo scanner per leggere l'input dell'utente
     * @return l'oggetto Data caricato, oppure null in caso di errore
     */
    private static Data caricaDati(Scanner scanner) {
        System.out.print("Nome della tabella: ");
        String tableName = scanner.nextLine();
        
        try {
            Data data = new Data(tableName);
            System.out.println("\nDati caricati con successo dalla tabella: " + tableName);
            System.out.println("----------------------------------------");
            System.out.println(data);
            return data;
            
        } catch (EmptyDatasetException e) {
            System.out.println("Errore: Dataset vuoto - " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Errore SQL: " + e.getMessage());
        } catch (DatabaseConnectionException e) {
            System.out.println("Errore di connessione al database: " + e.getMessage());
        } catch (EmptySetException e) {
            System.out.println("Errore: Set vuoto - " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Esegue l'algoritmo di clustering sul dataset caricato.
     * 
     * @param data il dataset su cui eseguire il clustering
     * @param scanner lo scanner per leggere l'input dell'utente
     */
    private static void eseguiClustering(Data data, Scanner scanner) {
        boolean continuaClustering = true;
        
        while(continuaClustering) {
            double radius;
            
            // Richiesta e validazione del raggio
            do {
                System.out.print("\nInserisci il raggio (>0): ");
                while (!scanner.hasNextDouble()) {
                    System.out.println("Errore: inserisci un numero valido.");
                    System.out.print("Inserisci il raggio (>0): ");
                    scanner.next();
                }
                radius = scanner.nextDouble();
                
                if(radius <= 0) {
                    System.out.println("Errore: Il raggio deve essere maggiore di 0!");
                }
            } while(radius <= 0);

            scanner.nextLine(); // Consuma il newline
            
            // Esecuzione clustering
            QTMiner qt = new QTMiner(radius);
            try {
                System.out.println("\nEsecuzione clustering in corso...");
                int numClusters = qt.compute(data);
                
                System.out.println("\n========================================");
                System.out.println("Clustering completato!");
                System.out.println("Numero di cluster generati: " + numClusters);
                System.out.println("========================================");
                System.out.println(qt.getC().toString(data));
                
                // Salvataggio cluster
                System.out.print("\nNome file di backup (senza estensione): ");
                String backupName = scanner.nextLine();
                
                try {
                    qt.salva(backupName + ".dmp");
                    System.out.println("✓ Cluster salvati con successo in: " + backupName + ".dmp");
                    
                } catch (IOException e) {
                    System.out.println("✗ Errore durante il salvataggio: " + e.getMessage());
                }
                
                // Richiesta nuova esecuzione
                System.out.print("\nNuova esecuzione con raggio diverso? (y/n): ");
                String answer = scanner.nextLine();
                continuaClustering = answer.equalsIgnoreCase("y");
                
            } catch(ClusteringRadiusException e) {
                System.out.println("\n✗ Errore: " + e.getMessage());
                System.out.println("Suggerimento: Prova con un raggio più piccolo.");
                
                System.out.print("\nRitentare con un raggio diverso? (y/n): ");
                String answer = scanner.nextLine();
                continuaClustering = answer.equalsIgnoreCase("y");
                
            } catch(EmptyDatasetException e) {
                System.out.println("✗ Errore: Dataset vuoto durante il clustering - " + e.getMessage());
                continuaClustering = false;
            }
        }
    }
}