package com.example.qtereshold.repo;

import com.example.qtereshold.network.SocketQtClient;
import java.io.IOException;

/**
 * Repository che gestisce la logica di comunicazione.
 * Fa da wrapper sincrono/bloccante per il SocketQtClient.
 */
public class QtSocketRepository {

    private final SocketQtClient client;

    public QtSocketRepository(SocketQtClient client) {
        this.client = client;
    }

    public void connect() throws IOException {
        client.connect();
    }

    // Questi metodi sono bloccanti e lanciano eccezioni

    public String loadTable(String table) throws IOException, ClassNotFoundException {
        return client.storeTableFromDb_Simple(table);
    }

    public String learn(double radius) throws IOException, ClassNotFoundException {
        return client.learningFromDbTable_Simple(radius);
    }

    public String save(String filename) throws IOException, ClassNotFoundException {
        return client.saveClustersToFile_Simple(filename);
    }

    public String learnFromFile(String filename) throws IOException, ClassNotFoundException {
        return client.learningFromFile_Simple(filename);
    }

    public void ping() throws IOException, ClassNotFoundException {
        client.ping();
    }

    public void disconnect() {
        client.disconnect();
    }

    public boolean isConnected() {
        return client.isConnected();
    }
}