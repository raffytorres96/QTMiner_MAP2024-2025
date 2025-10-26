# QTServer — Modulo Server

### Progetto MAP – Versione Base A.A. 2024/2025

Il modulo **QTServer** rappresenta la componente server-side del progetto universitario sviluppato per il corso di *Metodi di Analisi e Progetto (MAP)*.  
Il server gestisce la logica applicativa, l'elaborazione dei dati e la comunicazione con il modulo **QTClient**, fornendo i servizi necessari tramite socket o protocollo definito nel progetto.

---

## 🎯 Ruolo del Server nel progetto

Il modulo Server ha le seguenti responsabilità principali:

- Gestione delle **connessioni client**
- Elaborazione delle richieste e invio delle risposte
- Coordinamento della logica di calcolo del sistema
- Validazione dei dati in ingresso
- Eventuale gestione della concorrenza e multithreading

---

## 📂 Struttura del progetto

La struttura della cartella è organizzata come segue:

```
QTServer/
│
├── .vscode/          # Configurazioni del workspace (VS Code)
├── bin/              # Output compilato (.class)
├── lib/              # Librerie esterne (.jar)
└── src/              # Codice sorgente Java
```

All'interno della cartella `src/` sono presenti i package che contengono:

- Classi di comunicazione e gestione connessioni
- Servizi applicativi
- Utilità e costanti
- Classe Main del Server

---

## ✅ Prerequisiti

Per compilare ed eseguire il modulo Server sono richiesti:

| Strumento | Versione           |
|-----------|--------------------|
| Java JDK  | 17 o superiore     |
| IDE       | (Opzionale) VS Code|
| Librerie  | Presenti in `lib/` |

---

## 🚀 Compilazione ed esecuzione

### ▶️ Metodo 1 – Con Visual Studio Code

1. Aprire la cartella `QTServer/` con VS Code
2. Verificare che l'estensione **Extension Pack for Java** sia installata
3. Individuare la classe che contiene il metodo `main`
4. Avviare con **Run → Start Debugging**

---

### ▶️ Metodo 2 – Da terminale (Java CLI)

```bash
# Posizionarsi dentro QTServer
javac -d bin -cp "lib/*" $(find src -name "*.java")
java -cp "bin:lib/*" <nome_completo_classe_main>
```

> **🔧 Nota per Windows:** sostituire i `:` con `;` nel classpath.

---

## 📚 Documentazione Javadoc

La documentazione del modulo Server è disponibile nella sezione `report/javadoc/server/` alla root del progetto:

📄 **Server:** `../report/javadoc/server/index.html`

Per consultare la documentazione del Client:

📄 **Client:** `../report/javadoc/client/index.html`

---

## 📖 Package principali

Il progetto è organizzato nei seguenti package:

- **`data`** - Gestione delle strutture dati e tuple
- **`database`** - Accesso e manipolazione del database
- **`keyboardinput`** - Gestione dell'input da tastiera
- **`mining`** - Algoritmi di clustering e data mining
- **`server`** - Logica del server e gestione connessioni


---

> *Questa documentazione è parte integrante del progetto accademico svolto nell'ambito del corso di **Metodi di Analisi e Progetto (MAP)** – Università degli Studi di Bari Aldo Moro.*