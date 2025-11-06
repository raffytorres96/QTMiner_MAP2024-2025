package com.example.qtereshold

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.qtereshold.network.SocketQtClient
import com.example.qtereshold.repo.QtSocketRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.net.SocketException

private enum class Step {
    Connect, Choice, LoadFromFile, ClusterFromTable
}

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            com.example.qtereshold.ui.theme.QThresholdTheme {

                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }
                val sheetState = rememberModalBottomSheetState()

                var step by remember { mutableStateOf(Step.Connect) }
                var busy by remember { mutableStateOf(false) }
                var hostText by remember { mutableStateOf(TextFieldValue("10.0.2.2")) }
                var portText by remember { mutableStateOf(TextFieldValue("8080")) }

                val host = hostText.text.trim().ifBlank { "10.0.2.2" }
                val port = portText.text.toIntOrNull() ?: 8080
                val client = remember(host, port) { SocketQtClient(host, port) }
                val repo = remember(host, port) { QtSocketRepository(client) }

                DisposableEffect(client) {
                    onDispose {
                        repo.disconnect()
                        client.close()
                    }
                }

                var fileName by remember { mutableStateOf(TextFieldValue("clusters")) }
                var table by remember { mutableStateOf(TextFieldValue("playtennis")) }
                var radius by remember { mutableStateOf(TextFieldValue("1.0")) } // Modificato per essere un default valido
                var rawOutput by remember { mutableStateOf("") }
                var parsed by remember { mutableStateOf(listOf<ParsedCluster>()) }
                var showResults by remember { mutableStateOf(false) }
                var showRepeatDialog by remember { mutableStateOf(false) }
                var repeatTarget by remember { mutableStateOf<Step?>(null) }
                var showSaveDialog by remember { mutableStateOf(false) }
                var saveInitial by remember { mutableStateOf("") }
                var onSaveAction by remember { mutableStateOf<(String) -> Unit>({}) }

                var showSaveConfirmationDialog by remember { mutableStateOf(false) }
                var saveConfirmationMessage by remember { mutableStateOf("") }

                LaunchedEffect(step, repo) {
                    if (step != Step.Connect) {
                        while (true) {
                            delay(5_000)
                            try {
                                if (!busy) {
                                    repo.ping()
                                }
                            } catch (e: Exception) {
                                if (e is IOException || e is SocketException) {
                                    if (step != Step.Connect) {
                                        snackbarHostState.showSnackbar("Server disconnesso o non raggiungibile.")
                                        repo.disconnect()
                                        showResults = false
                                        showSaveDialog = false
                                        showRepeatDialog = false
                                        step = Step.Connect
                                        break
                                    }
                                }
                            }
                        }
                    }
                }


                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("QualityThreshold MAP") }
                        )
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { pad ->

                    Box(modifier = Modifier.padding(pad)) {

                        AnimatedContent(
                            targetState = step,
                            transitionSpec = {
                                slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                                        slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                            },
                            label = "ScreenTransition"
                        ) { targetStep ->

                            when (targetStep) {
                                // ... (Case Connect, Choice, LoadFromFile restano invariati) ...
                                Step.Connect -> ConnectScreen(
                                    hostText = hostText,
                                    portText = portText,
                                    busy = busy,
                                    isConnected = repo.isConnected(),
                                    onHostChange = { hostText = it },
                                    onPortChange = { portText = it },
                                    onConnect = {
                                        scope.launch {
                                            busy = true
                                            try {
                                                snackbarHostState.showSnackbar("Connessione a $host:$port...")
                                                withTimeout(4_000) { repo.connect() }
                                                step = Step.Choice
                                            } catch (e: TimeoutCancellationException) {
                                                snackbarHostState.showSnackbar("Server non raggiungibile (timeout).")
                                            } catch (e: IOException) {
                                                snackbarHostState.showSnackbar(e.message ?: "Connessione fallita.")
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar("Errore di connessione: ${e.message}")
                                            } finally {
                                                busy = false
                                            }
                                        }
                                    },
                                    onDisconnect = { /* non serve qui */ }
                                )

                                Step.Choice -> ChoiceScreen(
                                    onLoadFromFile = { step = Step.LoadFromFile },
                                    onClusterFromTable = { step = Step.ClusterFromTable },
                                    onDisconnect = {
                                        repo.disconnect()
                                        step = Step.Connect
                                    }
                                )

                                Step.LoadFromFile -> OperationScreen(
                                    title = "Carica cluster da file",
                                    busy = busy,
                                    onConfirm = {
                                        scope.launch {
                                            busy = true
                                            try {
                                                val res0 = repo.loadTable(table.text.trim())

                                                if (!res0.startsWith("OK")) {
                                                    val errorMessage = if (res0.contains("null")) {
                                                        "Errore: Server MySQL non in funzione o database non trovato."
                                                    } else {
                                                        res0
                                                    }
                                                    snackbarHostState.showSnackbar(errorMessage)

                                                    parsed = emptyList()
                                                    return@launch
                                                }

                                                val baseFileName = fileName.text.trim().removeSuffix(".dmp")
                                                val fullFileName = "$baseFileName.dmp"
                                                snackbarHostState.showSnackbar("Carico cluster da file ($fullFileName)...")
                                                val clusterText = repo.learnFromFile(fullFileName)

                                                if (clusterText.startsWith("Errore:") || clusterText.startsWith("ERRORE:")) {
                                                    snackbarHostState.showSnackbar(clusterText)
                                                    rawOutput = clusterText
                                                    parsed = emptyList()
                                                } else {
                                                    rawOutput = clusterText
                                                    parsed = parseClusters(text = clusterText)
                                                    repeatTarget = Step.LoadFromFile
                                                    showResults = true
                                                }
                                            } catch (e: Exception) {
                                                if (e is IOException || e is SocketException) {
                                                    snackbarHostState.showSnackbar("Connessione persa. Server non raggiungibile.")
                                                    repo.disconnect()
                                                    step = Step.Connect // Torna alla home
                                                } else {
                                                    snackbarHostState.showSnackbar("Errore: ${e.message}")
                                                    rawOutput = e.stackTraceToString()
                                                    parsed = emptyList()
                                                }
                                            } finally {
                                                busy = false
                                            }
                                        }
                                    },
                                    onDismiss = { step = Step.Choice }
                                ) {
                                    OutlinedTextField(
                                        value = table,
                                        onValueChange = { table = it },
                                        label = { Text("Nome tabella (Database)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        supportingText = { Text("La tabella a cui appartengono i cluster") }
                                    )
                                    OutlinedTextField(
                                        value = fileName,
                                        onValueChange = { fileName = it },
                                        label = { Text("Nome file (sul SERVER)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        supportingText = { Text("L'estensione .dmp verrà aggiunta in automatico.") }
                                    )
                                    Text(
                                        "Carica la tabella dati specificata, poi carica i cluster dal file .dmp e li mostra.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Step.ClusterFromTable -> OperationScreen(
                                    title = "Clustering da tabella (mapDb)",
                                    busy = busy,
                                    onConfirm = {
                                        scope.launch {
                                            busy = true
                                            try {
                                                snackbarHostState.showSnackbar("Carico tabella...")
                                                val res0 = repo.loadTable(table.text.trim())

                                                if (!res0.startsWith("OK")) {
                                                    val errorMessage = if (res0.contains("null")) {
                                                        "Errore: Server MySQL non in funzione o database non trovato."
                                                    } else {
                                                        res0
                                                    }
                                                    snackbarHostState.showSnackbar(errorMessage)

                                                    rawOutput = res0
                                                    parsed = emptyList()
                                                    return@launch
                                                }

                                                // --- INIZIO MODIFICA: Controllo Raggio ---
                                                // 1. Prova a convertire il raggio
                                                val radiusValue = radius.text.trim().toDoubleOrNull()

                                                // 2. Controlla se la conversione è fallita
                                                if (radiusValue == null) {
                                                    snackbarHostState.showSnackbar("Errore: Il raggio deve essere un numero valido (es. 1.0).")
                                                    return@launch // Interrompi l'operazione
                                                }
                                                // --- FINE MODIFICA ---

                                                snackbarHostState.showSnackbar("Eseguo clustering...")
                                                // 3. Usa il valore valido
                                                val clusterText = repo.learn(radiusValue)

                                                if (clusterText.startsWith("Errore:") || clusterText.startsWith("ERRORE:")) {
                                                    snackbarHostState.showSnackbar(clusterText)
                                                    rawOutput = clusterText
                                                    parsed = emptyList()
                                                } else {
                                                    rawOutput = clusterText
                                                    parsed = parseClusters(text = clusterText)
                                                    repeatTarget = Step.ClusterFromTable
                                                    showResults = true
                                                }
                                            } catch (e: Exception) {
                                                if (e is IOException || e is SocketException) {
                                                    snackbarHostState.showSnackbar("Connessione persa. Server non raggiungibile.")
                                                    repo.disconnect()
                                                    step = Step.Connect // Torna alla home
                                                } else {
                                                    snackbarHostState.showSnackbar("Errore: ${e.message}")
                                                    rawOutput = e.stackTraceToString()
                                                    parsed = emptyList()
                                                }
                                            } finally {
                                                busy = false
                                            }
                                        }
                                    },
                                    onDismiss = { step = Step.Choice }
                                ) {
                                    OutlinedTextField(
                                        value = table,
                                        onValueChange = { table = it },
                                        label = { Text("Nome tabella (Database)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // --- MODIFICA: Aggiunto supportingText ---
                                    OutlinedTextField(
                                        value = radius,
                                        onValueChange = { radius = it },
                                        label = { Text("Raggio") },
                                        modifier = Modifier.fillMaxWidth(),
                                        supportingText = { Text("Inserisci un valore numerico (es. 1.0)") }
                                    )
                                    // --- FINE MODIFICA ---

                                    Text(
                                        "Carica la tabella ed esegue il clustering con il raggio indicato. Potrai salvare i cluster successivamente in un file .dmp.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                            }
                        } // --- Fine AnimatedContent ---

                        // --- DIALOGS ---

                        if (showResults) {
                            ModalBottomSheet(
                                onDismissRequest = {
                                    showResults = false
                                    if (repeatTarget != null) showRepeatDialog = true
                                },
                                sheetState = sheetState
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .navigationBarsPadding(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("Cluster generati", style = MaterialTheme.typography.titleLarge)

                                    if (parsed.isEmpty()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.SearchOff,
                                                contentDescription = null,
                                                modifier = Modifier.size(64.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Nessun cluster trovato",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "Il risultato è vuoto. Prova a eseguire di nuovo il clustering con un raggio diverso o controlla il file.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f, fill = false),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            itemsIndexed(parsed) { i, c ->
                                                ClusterCard(idx = i, c = c)
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        TextButton(
                                            onClick = {
                                                showResults = false
                                                if (repeatTarget != null) showRepeatDialog = true
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("Chiudi") }

                                        Button(
                                            onClick = {
                                                val defaultBase = (table.text.trim().ifBlank { "clusters" }) + "_" + System.currentTimeMillis()
                                                saveInitial = defaultBase

                                                onSaveAction = { baseName ->
                                                    val safeBase = baseName.trim()
                                                        .removeSuffix(".dmp")
                                                        .replace(Regex("[^A-Za-z0-9._-]+"), "_")
                                                        .ifBlank { "clusters_${System.currentTimeMillis()}" }
                                                    val outFile = "$safeBase.dmp"
                                                    scope.launch {
                                                        try {
                                                            val res = repo.save(filename = outFile)

                                                            if (res.startsWith("OK")) {
                                                                saveConfirmationMessage = "Cluster salvati con successo sul server come:\n$outFile"
                                                            } else {
                                                                saveConfirmationMessage = "Errore durante il salvataggio:\n$res"
                                                            }
                                                        } catch (e: Exception) {
                                                            if (e is IOException || e is SocketException) {
                                                                snackbarHostState.showSnackbar("Connessione persa. Server non raggiungibile.")
                                                                repo.disconnect()
                                                                step = Step.Connect
                                                                showResults = false
                                                                showSaveDialog = false
                                                                return@launch
                                                            } else {
                                                                saveConfirmationMessage = "Errore critico durante il salvataggio:\n${e.message}"
                                                            }
                                                        } finally {
                                                            showSaveConfirmationDialog = true
                                                        }
                                                    }
                                                }
                                                showSaveDialog = true
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(16.dp)
                                        ) { Text("Salva su file (.dmp)") }
                                    }
                                }
                            }
                        }

                        if (showSaveDialog) {
                            PromptSaveDialog(
                                initial = saveInitial,
                                onDismiss = { showSaveDialog = false },
                                onSave = { baseName ->
                                    onSaveAction(baseName)
                                    showSaveDialog = false
                                }
                            )
                        }

                        if (showRepeatDialog) {
                            RepeatDialog(
                                onRepeat = {
                                    showRepeatDialog = false
                                    repeatTarget?.let { step = it }
                                    repeatTarget = null
                                },
                                onBackToChoice = {
                                    showRepeatDialog = false
                                    repeatTarget = null
                                    step = Step.Choice
                                }
                            )
                        }

                        if (showSaveConfirmationDialog) {
                            ConfirmationDialog(
                                title = "Esito Salvataggio",
                                message = saveConfirmationMessage,
                                onDismiss = { showSaveConfirmationDialog = false }
                            )
                        }

                    } // Fine Box
                } // Fine Scaffold
            } // Fine MaterialTheme
        } // Fine setContent
    }
}

/* ----------------- SCHERMATE (con icone e tipografia) ------------------ */

@Composable
fun ConnectScreen(
    hostText: TextFieldValue,
    portText: TextFieldValue,
    busy: Boolean,
    isConnected: Boolean,
    onHostChange: (TextFieldValue) -> Unit,
    onPortChange: (TextFieldValue) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Connessione al Server", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = hostText,
            onValueChange = onHostChange,
            label = { Text("Indirizzo IP/Host server") },
            supportingText = { Text("Per l'emulatore: 10.0.2.2") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy && !isConnected,
            leadingIcon = {
                Icon(Icons.Default.Dns, contentDescription = "Host")
            }
        )
        OutlinedTextField(
            value = portText,
            onValueChange = onPortChange,
            label = { Text("Porta") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy && !isConnected,
            leadingIcon = {
                Icon(Icons.Default.Computer, contentDescription = "Port")
            }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = !busy && !isConnected,
                onClick = onConnect
            ) { Text("Connetti") }

            OutlinedButton(
                enabled = isConnected && !busy,
                onClick = onDisconnect
            ) { Text("Disconnetti") }
        }

        if (busy) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun ChoiceScreen(
    onLoadFromFile: () -> Unit,
    onClusterFromTable: () -> Unit,
    onDisconnect: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Scegli un'operazione", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Caricare cluster da file esistente oppure eseguire il clustering da una tabella del Database locale (mapDb).",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onLoadFromFile,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Carica da file (.dmp)")
        }

        OutlinedButton(
            onClick = onClusterFromTable,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(Icons.Default.TableView, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Clustering da tabella (mapDb)")
        }

        Spacer(Modifier.weight(1f))

        TextButton(
            onClick = onDisconnect,
            modifier = Modifier.align(Alignment.End)
        ) { Text("Disconnetti") }
    }
}

@Composable
private fun OperationScreen(
    title: String,
    busy: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)

        content()

        if (busy) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Annulla") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onConfirm, enabled = !busy) { Text("Esegui") }
        }
    }
}


/* ----------------- DIALOG ------------------ */

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onDismiss) { Text("OK") }
        }
    )
}

@Composable
private fun RepeatDialog(
    onRepeat: () -> Unit,
    onBackToChoice: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onBackToChoice,
        title = { Text("Vuoi ripetere l'operazione?") },
        text = { Text("Puoi rifarla subito oppure tornare alla scelta tra le due opzioni.") },
        confirmButton = {
            Button(onClick = onRepeat) { Text("Sì, ripeti") }
        },
        dismissButton = {
            OutlinedButton(onClick = onBackToChoice) { Text("No, torna alla scelta") }
        }
    )
}

@Composable
fun PromptSaveDialog(
    initial: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onSkip: () -> Unit = onDismiss
) {
    var name by remember { mutableStateOf(initial.removeSuffix(".dmp")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Salva cluster") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome file") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("L'estensione .dmp verrà aggiunta automaticamente.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name) },
                enabled = name.isNotBlank()
            ) { Text("Salva (.dmp)") }
        },
        dismissButton = {
            TextButton(onClick = onSkip) { Text("Annulla") }
        }
    )
}

/* ------- Parsing + UI risultati (Corretti) ------- */

private data class ParsedCluster(
    val id: Int,
    val centroid: String,
    val examples: List<String>,
    val avgDistance: Double?
)

private fun parseClusters(text: String): List<ParsedCluster> {
    if (text.isBlank() || text.startsWith("Errore:") || text.startsWith("ERRORE:")) {
        return emptyList()
    }
    val clusterRegex = Regex(
        pattern = """(?m)^\s*(\d+):\s*Centroid=\((.*?)\)\s*\R(.*?)(?=^\s*\d+:\s*Centroid=|\z)""",
        options = setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE)
    )
    val results = mutableListOf<ParsedCluster>()
    for (m in clusterRegex.findAll(text)) {
        val id = m.groupValues[1].toIntOrNull() ?: (results.size + 1)
        val centroid = m.groupValues[2].trim()
        val body = m.groupValues[3]
        val lines = body.lines().map { it.trim() }
        val startIdx = lines.indexOfFirst { it.startsWith("Examples", ignoreCase = true) }
        val endIdx = lines.indexOfFirst { it.startsWith("AvgDistance", ignoreCase = true) }
        val examples = if (startIdx >= 0) {
            val to = if (endIdx > startIdx) endIdx else lines.size
            lines.subList(startIdx + 1, to).filter { it.isNotEmpty() && !it.startsWith("…") }
        } else emptyList()
        val avg = Regex("""AvgDistance\s*=\s*([0-9.+-Ee]+)""").find(body)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
        results += ParsedCluster(id, centroid, examples, avg)
    }
    return results
}

@Composable
private fun ClusterCard(idx: Int, c: ParsedCluster) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Cluster #${idx + 1}", style = MaterialTheme.typography.titleMedium)
            Text("Centroid: ${c.centroid}", fontFamily = FontFamily.Monospace)
            if (c.examples.isNotEmpty()) {
                Divider()
                Text("Examples:", style = MaterialTheme.typography.labelLarge)
                c.examples.forEach { ex -> Text("• $ex", fontFamily = FontFamily.Monospace) }
            }

            if (c.avgDistance != null) {
                Divider()
                Text(text = "AvgDistance: ${c.avgDistance}", fontFamily = FontFamily.Monospace)
            }
        }
    }
}