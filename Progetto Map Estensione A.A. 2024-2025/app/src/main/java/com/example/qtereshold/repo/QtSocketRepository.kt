package com.example.qtereshold.repo

import com.example.qtereshold.network.SocketQtClient

class QtSocketRepository(private val client: SocketQtClient) {

    suspend fun connect() = client.connect()

    /**
     * Esegue il COMANDO 10 (protocollo semplice)
     * Ritorna "OK" o "Errore: ..."
     */
    suspend fun loadTable(table: String): String =
    // Dobbiamo presumere che tu crei/modifichi questa funzione
        // nel tuo SocketQtClient per inviare il COMANDO 10
        client.storeTableFromDb_Simple(table)

    /**
     * Esegue il COMANDO 11 (protocollo semplice)
     * Ritorna la stringa dei cluster o "Errore: ..."
     */
    suspend fun learn(radius: Double): String {
        // Questa è la modifica principale:
        // non abbiamo più bisogno del 'Result' object.
        // Chiamiamo una nuova funzione nel client che usa il COMANDO 11.
        return client.learningFromDbTable_Simple(radius)
    }

    /**
     * Esegue il COMANDO 12 (protocollo semplice)
     * Ritorna "OK" o "Errore: ..."
     */
    suspend fun save(filename: String): String =
        // Chiama la funzione client che usa il COMANDO 12
        client.saveClustersToFile_Simple(filename)

    /**
     * Esegue il COMANDO 13 (protocollo semplice)
     * Ritorna la stringa dei cluster o "Errore: ..."
     */
    suspend fun learnFromFile(filename: String): String =
        // Chiama la funzione client che usa il COMANDO 13
        client.learningFromFile_Simple(filename)

    suspend fun ping() = client.ping()

    fun disconnect() = client.disconnect()
    fun isConnected() = client.isConnected()

}