package com.example.qtereshold.network;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class SocketQtClient implements Closeable {

    private final String host;
    private final int port;
    private final int timeoutMs;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public SocketQtClient(String host, int port) {
        this(host, port, 10_000); // Default timeout
    }

    public SocketQtClient(String host, int port, int timeoutMs) {
        this.host = host;
        this.port = port;
        this.timeoutMs = timeoutMs;
    }

    /**
     * Si connette al server.
     * @throws IOException se la connessione fallisce.
     */
    public void connect() throws IOException {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            return;
        }
        try {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(host, port), timeoutMs);
            s.setSoTimeout(timeoutMs); // Timeout anche sulla lettura
            socket = s;
            out = new ObjectOutputStream(s.getOutputStream());
            out.flush();
            in = new ObjectInputStream(s.getInputStream());
        } catch (ConnectException e) {
            throw new IOException("Server offline o porta " + port + " chiusa", e);
        } catch (SocketTimeoutException e) {
            throw new IOException("Timeout di connessione: nessuna risposta dal server", e);
        } catch (UnknownHostException e) {
            throw new IOException("Host non valido: " + host, e);
        }
    }

    /**
     * Invia un comando e un parametro, riceve una singola stringa.
     * @param command Il comando (es. 10, 12, 13)
     * @param param Il parametro (es. nome tabella, nome file)
     * @return La stringa di risposta dal server
     * @throws IOException se la scrittura/lettura fallisce
     * @throws ClassNotFoundException se la risposta non è una Stringa
     */
    private String simpleRequestResponse(int command, Object param) throws IOException, ClassNotFoundException {
        if (out == null || in == null) throw new IOException("Client non connesso.");

        out.writeObject(command);
        out.writeObject(param);
        out.flush();
        return (String) in.readObject();
    }

    // --- Metodi del Protocollo Semplice ---

    public String storeTableFromDb_Simple(String table) throws IOException, ClassNotFoundException {
        return simpleRequestResponse(10, table);
    }

    public String learningFromDbTable_Simple(double radius) throws IOException, ClassNotFoundException {
        return simpleRequestResponse(11, radius);
    }

    public String saveClustersToFile_Simple(String filename) throws IOException, ClassNotFoundException {
        return simpleRequestResponse(12, filename);
    }

    public String learningFromFile_Simple(String filename) throws IOException, ClassNotFoundException {
        return simpleRequestResponse(13, filename);
    }

    public void ping() throws IOException, ClassNotFoundException {
        if (out == null || in == null) throw new IOException("Client non connesso.");

        out.writeObject(99);
        out.flush();
        String response = (String) in.readObject();
        if (!"PONG".equals(response)) {
            throw new IOException("Risposta Heartbeat non valida dal server.");
        }
    }

    // --- Metodi di utilità ---

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void disconnect() {
        close();
    }

    @Override
    public void close() {
        try { if (in != null) in.close(); } catch (Exception e) {}
        try { if (out != null) out.close(); } catch (Exception e) {}
        try { if (socket != null) socket.close(); } catch (Exception e) {}
    }
}