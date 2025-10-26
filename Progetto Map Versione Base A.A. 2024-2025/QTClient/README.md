# QTClient — Modulo Client

### Progetto MAP – Versione Base A.A. 2024/2025

Il modulo **QTClient** rappresenta la componente client-side del progetto universitario sviluppato per il corso di *Metodi di Analisi e Progetto (MAP)*.  
Il client fornisce l'interfaccia utente per interagire con il sistema, comunicando con il modulo **QTServer** tramite socket per inviare richieste ed elaborare risposte.

---

## 🎯 Ruolo del Client nel progetto

Il modulo Client ha le seguenti responsabilità principali:

- Gestione dell'**interfaccia utente** (GUI o CLI)
- Comunicazione con il server tramite socket
- Invio delle richieste dell'utente al server
- Ricezione ed elaborazione delle risposte
- Visualizzazione dei risultati in formato comprensibile

---

## 📂 Struttura del progetto

La struttura della cartella è organizzata come segue:

```
QTClient/
│
├── .vscode/          # Configurazioni del workspace (VS Code)
├── bin/              # Output compilato (.class)
├── lib/              # Librerie esterne (.jar)
└── src/              # Codice sorgente Java
```

All'interno della cartella `src/` sono presenti i package che contengono:

- Classi di comunicazione con il server
- Interfaccia utente (GUI/CLI)
- Gestione degli input utente
- Logica di presentazione dei dati
- Classe Main del Client

---

## ✅ Prerequisiti

Per compilare ed eseguire il modulo Client sono richiesti:

| Strumento | Versione           |
|-----------|--------------------|
| Java JDK  | 17 o superiore     |
| IDE       | (Opzionale) VS Code|
| Librerie  | Presenti in `lib/` |
| Server    | QTServer in esecuzione |

> **⚠️ Importante:** Il modulo Client richiede che il **QTServer** sia attivo e in ascolto sulla porta configurata.

---

## 🚀 Compilazione ed esecuzione

### ▶️ Metodo 1 – Con Visual Studio Code

1. Aprire la cartella `QTClient/` con VS Code
2. Verificare che l'estensione **Extension Pack for Java** sia installata
3. Individuare la classe che contiene il metodo `main`
4. Avviare con **Run → Start Debugging**

---

### ▶️ Metodo 2 – Da terminale (Java CLI)

```bash
# Posizionarsi dentro QTClient
javac -d bin -cp "lib/*" $(find src -name "*.java")
java -cp "bin:lib/*" <nome_completo_classe_main>
```

> **🔧 Nota per Windows:** sostituire i `:` con `;` nel classpath.

---

## 🔌 Configurazione della connessione

Prima di avviare il client, assicurarsi che:

1. Il **QTServer** sia in esecuzione
2. La porta del server sia correttamente configurata (default: `8080`)
3. L'indirizzo IP del server sia accessibile (default: `localhost`)

I parametri di connessione possono essere configurati:
- Nel codice sorgente (file di configurazione)
- Tramite input da tastiera all'avvio
- Come parametri da riga di comando

---

## 📚 Documentazione Javadoc

La documentazione del modulo Client è disponibile nella sezione `report/javadoc/client/` alla root del progetto:

📄 **Client:** `../report/javadoc/client/index.html`

Per consultare la documentazione del Server:

📄 **Server:** `../report/javadoc/server/index.html`

---

## 📖 Package principali

Il progetto è organizzato nei seguenti package:

- **`client`** - Logica principale del client e comunicazione
- **`ui`** - Interfaccia utente (GUI/CLI)
- **`network`** - Gestione delle connessioni di rete
- **`utils`** - Utility e classi di supporto

---

## 💡 Utilizzo del Client

### Flusso tipico di utilizzo:

1. **Avvio del client** - Il client si connette al server
2. **Menu principale** - L'utente seleziona l'operazione desiderata
3. **Input parametri** - L'utente fornisce i dati necessari
4. **Elaborazione** - La richiesta viene inviata al server
5. **Visualizzazione risultati** - I risultati vengono mostrati all'utente

---

## 🛠️ Troubleshooting

### Problemi comuni:

| Problema | Soluzione |
|----------|-----------|
| Connessione rifiutata | Verificare che il server sia attivo |
| Timeout connessione | Controllare firewall e configurazione di rete |
| Errore classpath | Verificare la presenza delle librerie in `lib/` |

---

> *Questa documentazione è parte integrante del progetto accademico svolto nell'ambito del corso di **Metodi di Analisi e Progetto (MAP)** – Università degli Studi di Bari Aldo Moro.*