package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestisce l'accesso e la connessione al database MySQL.
 * <p>
 * Questa classe fornisce metodi per inizializzare, ottenere e chiudere
 * una connessione al database. Le credenziali e i parametri di connessione
 * sono configurati come costanti private.
 * </p>
 * <p>
 * Configurazione di default:
 * <ul>
 *   <li>Server: localhost</li>
 *   <li>Porta: 3306</li>
 *   <li>Database: MapDB</li>
 *   <li>Utente: MapUser</li>
 * </ul>
 * </p>
 * 
 * @see DatabaseConnectionException
 */
public class DbAccess {
    
    /** Nome completo della classe del driver JDBC MySQL */
    private String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    
    /** Protocollo JDBC per MySQL */
    private final String DBMS = "jdbc:mysql";
    
    /** Indirizzo del server database */
    private final String SERVER = "localhost";
    
    /** Nome del database a cui connettersi */
    private final String DATABASE = "MapDB";
    
    /** Porta su cui è in ascolto il server MySQL */
    private final String PORT = "3306";
    
    /** Username per l'autenticazione al database */
    private final String USER_ID = "MapUser";
    
    /** Password per l'autenticazione al database */
    private final String PASSWORD = "map";
    
    /** Oggetto Connection che rappresenta la connessione attiva al database */
    private Connection conn;

    /**
     * Inizializza la connessione al database.
     * <p>
     * Questo metodo esegue due operazioni:
     * <ol>
     *   <li>Carica il driver JDBC MySQL tramite il class loader</li>
     *   <li>Stabilisce la connessione al database usando i parametri configurati</li>
     * </ol>
     * La connessione viene configurata senza SSL e con timezone UTC.
     * </p>
     * 
     * @throws DatabaseConnectionException se il driver non viene trovato o se
     * si verifica un errore durante la connessione al database
     */
    public void initConnection() throws DatabaseConnectionException{

        try {
            Class.forName(DRIVER_CLASS_NAME);
        } catch(ClassNotFoundException e) {
            System.out.println("[!] Driver non trovato: " + e.getMessage());
            throw new DatabaseConnectionException();
        }

        String connectionString = DBMS + "://" + SERVER + ":" + PORT + "/" + DATABASE +
                     "?user=" + USER_ID + "&password=" + PASSWORD + "&useSSL=false&serverTimezone=UTC";

        try {
            conn = DriverManager.getConnection(connectionString);
            System.out.println("Connesso al database.");
        } catch (SQLException e) {
            System.out.println("[!] Errore nello stabilire la connessione al database: " + e.getMessage());
            throw new DatabaseConnectionException();
        }
    }

    /**
     * Restituisce l'oggetto Connection attivo.
     * <p>
     * Questo metodo deve essere chiamato dopo aver invocato {@link #initConnection()},
     * altrimenti restituirà null.
     * </p>
     * 
     * @return l'oggetto Connection rappresentante la connessione al database,
     * o null se la connessione non è stata ancora inizializzata
     */
    public Connection getConnection(){
        return conn;
    }

    /**
     * Chiude la connessione al database.
     * <p>
     * Se si verifica un errore durante la chiusura, viene stampato un messaggio
     * di errore ma non viene lanciata alcuna eccezione.
     * </p>
     */
    public void closeConnection(){
        try {
            conn.close();
        } catch (java.sql.SQLException e) {
            System.out.println("[!] Errore nel chiudere la connessione: " + e.getMessage());
        }
    }
}