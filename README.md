# 🧠 QTMiner

> A client/server application for clustering data using the **Quality Threshold (QT)** algorithm — with a Java Windows client, a Java server backed by MySQL, and a native Android client.

---

## 📌 Overview

QTMiner is a distributed data mining system developed for the course **Metodi Avanzati di Programmazione** (University of Bari, Prof. A. Appice). It implements the **QT (Quality Threshold) clustering algorithm** in a client/server architecture, allowing one or more clients — either desktop (Windows/Java) or mobile (Android/Kotlin) — to connect to a central server that executes the algorithm against a MySQL database and returns the resulting clusters.

**Authors:** Gatta Raffaele · Lastella Nicola · Carbone Giuseppe Emanuele

---

## 🔬 The QT Algorithm

The Quality Threshold algorithm groups data points into clusters based on a user-defined **radius** (maximum intra-cluster distance). Unlike k-means, it does not require specifying the number of clusters upfront.

**Steps:**

1. **Initialization** — For each example `x` in the dataset, build a candidate cluster `Cx` containing all examples `y` such that `d(x, y) ≤ radius`
2. **Selection** — Choose the candidate cluster with the highest number of elements
3. **Removal** — Remove all assigned examples from the dataset
4. **Repetition** — Repeat until no more clusters can be formed
5. **Output** — Return the set of discovered clusters

> ⚠️ A large radius produces few, broad clusters. A small radius produces many, precise clusters.

---

## 🏗️ Architecture

```
┌────────────────────────────────────────────────────────┐
│                        SERVER                          │
│   Java · Socket (port 8080) · MySQL (MAPDB)            │
│   - Loads table data from DB                           │
│   - Executes QT clustering                             │
│   - Serializes clusters to .dmp files                  │
└──────────────────┬──────────┬──────────────────────────┘
                   │          │
        (TCP Socket)          (TCP Socket)
                   │          │
   ┌───────────────┘          └──────────────────┐
   │                                             │
   ▼                                             ▼
┌──────────────────┐                  ┌───────────────────┐
│  CLIENT (Windows)│                  │  APP-CLIENT       │
│  Java · CLI      │                  │  Android · Kotlin │
│  - Discover from │                  │  - Discover from  │
│    DB            │                  │    DB             │
│  - Load from .dmp│                  │  - Load from .dmp │
└──────────────────┘                  └───────────────────┘
```

> ⚠️ **Network requirement:** Server and all clients must be on the **same local network** (e.g. same Wi-Fi).

---

## 📁 Project Structure

```
QTMiner/
│
├── distribution/
│   ├── start.bat               # One-click launcher: DB setup + Server + Client
│   ├── server/
│   │   └── MAPDB.sql           # MySQL schema + demo table (playtennis)
│   └── android/
│       └── qtminer.apk         # Android client APK
│
├── Server/                     # Java server source
├── Client/                     # Java Windows client source
├── AndroidClient/              # Kotlin Android client source
│
└── README.md
```

---

## 🛠️ Requirements

| Component | Requirement |
|---|---|
| **Server** | JRE 8+, MySQL (running) |
| **Client (Windows)** | JRE 8+, Server already running |
| **App-Client (Android)** | Android API 16+ (Android 4.1+), Server already running |
| **Network** | All devices on the same local network |

---

## 🚀 Getting Started

### 1 — Database Setup

Before the first run, import the SQL schema into MySQL:

```bash
mysql -u root -p < distribution/server/MAPDB.sql
```

This creates the `MAPDB` database and a demo table `playtennis` (14 examples) usable immediately for testing.

### 2 — Start Server + Client (Windows)

Run the one-click launcher from the `distribution/` directory:

```bash
start.bat
```

The script will:

1. Prompt for your MySQL password
2. Initialize the `MAPDB` database
3. Launch the **Server** terminal (listening on port `8080`)
4. Launch the **Client** terminal (interactive CLI)

The launcher window remains open and logs all events until manually closed.

### 3 — Install Android Client

1. On your Android device: `Settings → Advanced Settings → Security → Enable Unknown Sources`
2. Transfer `distribution/android/qtminer.apk` to the device
3. Open the APK and tap **Install** (choose *Install anyway* if Play Protect intervenes)
4. Make sure the Server is already running before connecting

---

## 🖥️ Client Usage (Windows CLI)

Once connected to the server, the client presents two options:

```
(1) Load clusters from file
(2) Discover clusters from database
```

### Option 1 — Load from File

Loads and displays clusters previously saved as a `.dmp` file.

| Prompt | Example input |
|---|---|
| Table name | `playtennis` |
| File name (no extension) | `prova` |

The client prints each cluster with its centroid, examples, and average distance. The server logs the file load and disconnection events.

### Option 2 — Discover from Database

Runs the QT algorithm on a MySQL table and returns the generated clusters.

| Prompt | Example input |
|---|---|
| Table name | `playtennis` |
| Radius | `3` |

After clustering, the client offers to **save results** to a `.dmp` file:

- Press `n` → auto-generated name (e.g. `playtennis.3.dmp`)
- Press `y` → enter a custom name (no extension needed)

After each operation, the client asks:

```
Vuoi ripetere l'operazione? (y/n)
Vuoi scegliere una nuova operazione dal menu? (y/n)
```

---

## 📱 Android Client Usage

### Phase 1 — Connect to Server

1. Open the **QTMiner** app
2. Enter the server IP address (e.g. `127.0.0.1` for localhost, `10.0.2.2` for Android Studio emulator)
3. Enter the port (default: `8080`)
4. Tap **Connetti**

### Phase 2 — Choose Operation

| Button | Action |
|---|---|
| `Carica da file (.dmp)` | Load previously saved clusters from a server-side file |
| `Clustering da tabella (mapDb)` | Run QT clustering on a database table |

### Phase 3 — Clustering from Database

1. Enter the **table name** (e.g. `playtennis`)
2. Enter the **radius** (e.g. `1.0`)
3. Tap **Esegui**
4. Results appear in a bottom panel showing each cluster's centroid, examples, and average distance
5. Optionally tap **Salva su file (.dmp)** → enter a file name → tap **Salva (.dmp)**

### Phase 4 — Load Clusters from File

1. Enter the **table name** the clusters belong to
2. Enter the **file name** (stored server-side, `.dmp` extension added automatically)
3. Tap **Esegui** — results are displayed identically to clustering output

### Phase 5 — Repeat or Exit

After viewing results, a dialog asks:

- **Sì, ripeti** → stay on current screen and change parameters
- **No, torna alla scelta** → return to the main operation menu

### Phase 6 — Disconnect

From the main operation screen, tap **Disconnetti** to close the socket connection and return to the connection screen.

---

## 📊 Output Format

Each cluster is printed in the following format:

```
N: Centroid=(feature1 feature2 ... featureN)
Examples:
[feature1 feature2 ... featureN] dist=X.XXXXX
[feature1 feature2 ... featureN] dist=X.XXXXX
...
AvgDistance=X.XXXXXXXXXXXXX
```

**Example output (playtennis, radius=3):**

```
1:Centroid=(sunny 12.5 normal strong yes)
Examples:
[rain 0.0 normal strong no ] dist=2.4125412645130035
[sunny 12.5 normal strong yes] dist=0.0
AvgDistance=2.00000000629488

2:Centroid=(overcast 30.0 high weak yes)
Examples:
[overcast 0.1 normal weak yes ] dist=2.9867987047151043
...
AvgDistance=2.029339947321925
```

---

## 🗄️ Database

The `MAPDB.sql` script creates the `MapDB` database and a demo table `playtennis` with **14 examples** and the following attributes:

| Column | Type | Description |
|---|---|---|
| `outlook` | VARCHAR | Weather outlook (sunny, overcast, rain) |
| `temperature` | FLOAT | Temperature value |
| `humidity` | VARCHAR | Humidity level (normal, high) |
| `wind` | VARCHAR | Wind strength (weak, strong) |
| `play` | VARCHAR | Target class (yes, no) |

The algorithm supports any table with numeric and/or categorical attributes stored in `MapDB`.

---

## 🧪 Quick Test

To verify the full pipeline end-to-end:

1. Run `start.bat` and enter your MySQL password
2. In the Client terminal, select destination `1` (Localhost) and port `8080`
3. Choose option `2` (Scopri cluster da un database)
4. Table: `playtennis`, Radius: `3`
5. Confirm 2 clusters are generated with correct centroids and average distances
6. Save results to a file (e.g. `test`)
7. Choose option `1` (Carica cluster da un file)
8. Table: `playtennis`, File: `test`
9. Confirm the same clusters load correctly from the `.dmp` file

---

## 👥 Authors

| Name | Role |
|---|---|
| **Raffaele Gatta** | Development |

**Course:** Metodi Avanzati di Programmazione — University of Bari
**Supervisor:** Prof. A. Appice

---

## 📄 License

This project was developed for academic purposes only.

---

<p align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white"/>
  <img src="https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white"/>
  <img src="https://img.shields.io/badge/Socket-TCP%2FIP-0A66C2?style=flat-square"/>
</p>
