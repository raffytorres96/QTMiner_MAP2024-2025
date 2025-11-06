package com.example.qtereshold.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.io.IOException
import java.net.*

class SocketQtClient(
    private val host: String,
    private val port: Int = 8080,
    private val timeoutMs: Int = 10_000
) : Closeable {

    private var socket: Socket? = null
    private var out: ObjectOutputStream? = null
    private var `in`: ObjectInputStream? = null

    suspend fun connect() = withContext(Dispatchers.IO) {
        if (socket?.isConnected == true && socket?.isClosed == false) return@withContext
        try {
            val s = Socket()
            s.connect(InetSocketAddress(host, port), timeoutMs)
            s.soTimeout = timeoutMs
            socket = s
            out = ObjectOutputStream(s.getOutputStream())
            out!!.flush()
            `in` = ObjectInputStream(s.getInputStream())
        } catch (e: ConnectException) {
            throw IOException("Server offline o porta $port chiusa", e)
        } catch (e: SocketTimeoutException) {
            throw IOException("Timeout di connessione: nessuna risposta dal server", e)
        } catch (e: UnknownHostException) {
            throw IOException("Host non valido: $host", e)
        }
    }

    // ===================================================================
    // --- PROTOCOLLO SEMPLICE (per Client Android) ---
    // ===================================================================

    /**
     * Invia il COMANDO 10 (protocollo semplice).
     * Si aspetta UNA SOLA stringa in risposta ("OK" o "Errore: ...")
     */
    suspend fun storeTableFromDb_Simple(table: String): String = withContext(Dispatchers.IO) {
        try {
            requireNotNull(out).writeObject(10)
            requireNotNull(out).flush()
            requireNotNull(out).writeObject(table)
            requireNotNull(out).flush()
            // Legge la singola risposta
            requireNotNull(`in`).readObject() as String
        } catch (e: Exception) {
            "Errore: ${e.message ?: e.javaClass.simpleName}"
        }
    }

    /**
     * Invia il COMANDO 11 (protocollo semplice).
     * Si aspetta UNA SOLA stringa in risposta (i cluster o "Errore: ...")
     */
    suspend fun learningFromDbTable_Simple(radius: Double): String = withContext(Dispatchers.IO) {
        try {
            requireNotNull(out).writeObject(11)
            requireNotNull(out).flush()
            requireNotNull(out).writeObject(radius)
            requireNotNull(out).flush()
            // Legge la singola risposta
            requireNotNull(`in`).readObject() as String
        } catch (e: Exception) {
            "Errore: ${e.message ?: e.javaClass.simpleName}"
        }
    }

    /**
     * Invia il COMANDO 12 (protocollo semplice).
     * Si aspetta UNA SOLA stringa in risposta ("OK" o "Errore: ...")
     */
    suspend fun saveClustersToFile_Simple(filename: String): String = withContext(Dispatchers.IO) {
        try {
            requireNotNull(out).writeObject(12)
            requireNotNull(out).flush()
            requireNotNull(out).writeObject(filename)
            requireNotNull(out).flush()
            // Legge la singola risposta
            requireNotNull(`in`).readObject() as String
        } catch (e: Exception) {
            "Errore: ${e.message ?: e.javaClass.simpleName}"
        }
    }

    /**
     * Invia il COMANDO 13 (protocollo semplice).
     * Si aspetta UNA SOLA stringa in risposta (i cluster o "Errore: ...")
     */
    suspend fun learningFromFile_Simple(filename: String): String = withContext(Dispatchers.IO) {
        try {
            requireNotNull(out).writeObject(13)
            requireNotNull(out).flush()
            requireNotNull(out).writeObject(filename)
            requireNotNull(out).flush()
            // Legge la singola risposta
            requireNotNull(`in`).readObject() as String
        } catch (e: Exception) {
            "Errore: ${e.message ?: e.javaClass.simpleName}"
        }
    }

    /**
     * Invia il COMANDO 99 (Heartbeat/Ping) per controllare se la connessione è attiva.
     * Si aspetta "PONG" in risposta.
     * Lancia una IOException se la connessione è caduta.
     */
    suspend fun ping() = withContext(Dispatchers.IO) {
        // Blocco try-catch per intercettare qualsiasi errore (incluso un timeout
        // se il server è bloccato, o una SocketException se è chiuso)
        try {
            requireNotNull(out).writeObject(99)
            requireNotNull(out).flush()
            val response = requireNotNull(`in`).readObject() as String
            if (response != "PONG") {
                // Il server ha risposto in modo strano
                throw IOException("Risposta Heartbeat non valida dal server.")
            }
        } catch (e: Exception) {
            // Qualsiasi errore qui (SocketException, EOFException, ClassCastException)
            // significa che la connessione è morta. Rilanciamo l'eccezione
            // per farla gestire al chiamante (MainActivity).
            throw IOException("Heartbeat fallito: connessione persa.", e)
        }
    }


    // =================================================================
    // --- Funzioni di utilità ---
    // =================================================================

    fun isConnected(): Boolean = socket?.isConnected == true && socket?.isClosed == false

    fun disconnect() = close()

    override fun close() {
        try { `in`?.close() } catch (_: Exception) {}
        try { out?.close() } catch (_: Exception) {}
        try { socket?.close() } catch (_: Exception) {}
    }
}