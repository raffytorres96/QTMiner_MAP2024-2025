package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * La classe DbAccess realizza l'accesso alla base di dati.
 */
public class DbAccess {
    private String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    private final String DBMS = "jdbc:mysql";
    private final String SERVER = "localhost";
    private final String DATABASE = "MapDB";
    private final String PORT = "3306";
    private final String USER_ID = "MapUser";
    private final String PASSWORD = "map";
    private Connection conn;

    /**
     * Impartisce al class loader l’ordine di caricare il driver mysql,
     * inizializza la connessione riferita da conn.
     * @throws DatabaseConnectionException
     */
    public void initConnection() throws DatabaseConnectionException{

        try {
			Class.forName(DRIVER_CLASS_NAME);
		} catch(ClassNotFoundException e) {
			System.out.println("[!] Driver not found: " + e.getMessage());
			throw new DatabaseConnectionException();
        }

            String connectionString = DBMS + "://" + SERVER + ":" + PORT + "/" + DATABASE +
                         "?user=" + USER_ID + "&password=" + PASSWORD + "&useSSL=false&serverTimezone=UTC";
        

            System.out.println("Connection's String: " + connectionString);

            try {
                conn = DriverManager.getConnection(connectionString);
            System.out.println("Connection established successfully.");
            } catch (SQLException e) {
                System.out.println("[!] Error establishing connection: " + e.getMessage());
            throw new DatabaseConnectionException();
    }
    }

    /**
     * Restituisce l'attributo conn di tipo Connection.
     * @return conn
     */
    public Connection getConnection(){
        return conn;
    }

    /**
     * Chiude la connessione conn.
     */
    public void closeConnection(){
            try {
                conn.close();
            } catch (java.sql.SQLException e) {
                System.out.println("[!] Error closing connection: " + e.getMessage());
            }
    }
}
