import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import keyboardinput.Keyboard;

/**
 * Classe client per l'interazione con un server di clustering basato sull'algoritmo QT.
 * Gestisce la comunicazione tramite socket e permette di eseguire operazioni
 * di clustering come il caricamento di dati da database remoto, l'esecuzione del clustering,
 * il salvataggio di cluster su file e il caricamento di cluster esistenti.
 * 
 * <p>Il client comunica con il server attraverso quattro comandi principali:
 * <ul>
 *   <li>Comando 0: Caricamento tabella dal database</li>
 *   <li>Comando 1: Esecuzione clustering con raggio specificato</li>
 *   <li>Comando 2: Salvataggio cluster su file</li>
 *   <li>Comando 3: Caricamento cluster da file</li>
 * </ul>
 * </p>
 *
 */
public class MainTest {

	/**
	 * Stream di output per inviare oggetti al server.
	 */
	private ObjectOutputStream out;
	
	/**
	 * Stream di input per ricevere oggetti dal server.
	 */
	private ObjectInputStream in;
	
	/**
	 * Costruttore che inizializza la connessione con il server.
	 * Crea un socket e configura gli stream di input/output per la comunicazione.
	 * 
	 * @param ip indirizzo IP del server a cui connettersi
	 * @param port numero di porta su cui il server è in ascolto
	 * @throws IOException se si verifica un errore durante la connessione o l'inizializzazione degli stream
	 */
	public MainTest(String ip, int port) throws IOException{
		InetAddress addr = InetAddress.getByName(ip);
		System.out.println("addr = " + addr);
		Socket socket = new Socket(addr, port);
		System.out.println(socket);
		
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
	}
	
	/**
	 * Visualizza il menu principale e legge la scelta dell'utente.
	 * Le opzioni disponibili sono:
	 * <ol>
	 *   <li>Carica cluster da file</li>
	 *   <li>Carica dati da database ed esegui clustering</li>
	 * </ol>
	 * 
	 * <p>Nota: Il metodo non valida l'input; valori diversi da 1 o 2
	 * vengono gestiti dallo switch nel metodo main con un messaggio di errore.</p>
	 * 
	 * @return la scelta dell'utente
	 */
	private int menu(){
		int answer;
		
		System.out.println("\nScegli un'opzione:");
		System.out.println("(1) Carica cluster da un file");
		System.out.println("(2) Scopri cluster da un database");
		System.out.print("(1/2):");
		answer = Keyboard.readInt();
		
		return answer;		
	}
	
	/**
	 * Carica cluster precedentemente salvati da un file.
	 * Invia al server il comando 3 seguito dal nome del file da cui caricare i cluster.
	 * Il file deve avere estensione .dmp e trovarsi nella directory di esecuzione del server.
	 * 
	 * @return una stringa contenente la rappresentazione dei cluster caricati
	 * @throws SocketException se si verifica un errore nel socket
	 * @throws ServerException se il server restituisce un errore (es. file non trovato)
	 * @throws IOException se si verifica un errore di I/O durante la comunicazione
	 * @throws ClassNotFoundException se la classe dell'oggetto ricevuto non viene trovata
	 */
	private String learningFromFile() throws SocketException, ServerException, IOException, ClassNotFoundException{
		out.writeObject(3);
		
		System.out.print("Nome del file (senza estensione): ");
		String fileName = Keyboard.readString() + ".dmp";
		
		out.writeObject(fileName);
		
		String result = (String)in.readObject();
		if(result.equals("OK"))
			return (String)in.readObject();
		else {
			throw new ServerException(result);
		}
	}

	/**
	 * Carica una tabella dal database remoto.
	 * Invia al server il comando 0 seguito dal nome della tabella da caricare.
	 * La tabella deve esistere nel database configurato sul server.
	 * 
	 * @throws SocketException se si verifica un errore nel socket
	 * @throws ServerException se il server restituisce un errore (es. tabella non esistente)
	 * @throws IOException se si verifica un errore di I/O durante la comunicazione
	 * @throws ClassNotFoundException se la classe dell'oggetto ricevuto non viene trovata
	 */
	private void storeTableFromDb() throws SocketException, ServerException, IOException, ClassNotFoundException{
		out.writeObject(0);
		System.out.print("Nome della tabella:");
		String tabName = Keyboard.readString();
		out.writeObject(tabName);
		
		String result = (String)in.readObject();
		if(!result.equals("OK"))
			throw new ServerException(result);	
	}
	
	/**
	 * Esegue il clustering sui dati caricati dal database utilizzando l'algoritmo QT.
	 * Invia al server il comando 1 seguito dal raggio di clustering desiderato.
	 * Il raggio determina la distanza massima dei punti dal centroide del cluster.
	 * 
	 * <p>Il metodo richiede iterativamente un raggio positivo fino a quando l'utente
	 * non inserisce un valore valido.</p>
	 * 
	 * @return una stringa contenente la rappresentazione dei cluster generati
	 * @throws SocketException se si verifica un errore nel socket
	 * @throws ServerException se il server restituisce un errore (es. raggio troppo grande,
	 *         nessuna tabella caricata)
	 * @throws IOException se si verifica un errore di I/O durante la comunicazione
	 * @throws ClassNotFoundException se la classe dell'oggetto ricevuto non viene trovata
	 */
	private String learningFromDbTable() throws SocketException, ServerException, IOException, ClassNotFoundException{
		out.writeObject(1);
		double r = 1.0;
		do{
			System.out.print("Inserisci raggio:");
			r = Keyboard.readDouble();
		} while(r <= 0);
		out.writeObject(r);

		String result = (String)in.readObject();
		if(result.equals("OK")){
			System.out.println("Numero cluster:" + in.readObject());
			return (String)in.readObject();
		} else {
			throw new ServerException(result);
		}
	}
	
	/**
	 * Salva i cluster generati su un file.
	 * Invia al server il comando 2 seguito dal nome del file in cui salvare.
	 * L'utente può scegliere di specificare un nome personalizzato o utilizzare
	 * un nome generato automaticamente con timestamp.
	 * 
	 * <p>Il file viene salvato con estensione .dmp nella directory di esecuzione del server.</p>
	 * 
	 * @throws SocketException se si verifica un errore nel socket
	 * @throws ServerException se il server restituisce un errore (es. nessun clustering eseguito)
	 * @throws IOException se si verifica un errore di I/O durante la comunicazione o il salvataggio
	 * @throws ClassNotFoundException se la classe dell'oggetto ricevuto non viene trovata
	 */
	private void storeClusterInFile() throws SocketException, ServerException, IOException, ClassNotFoundException{
		out.writeObject(2);
		
		System.out.print("Vuoi specificare il nome del file? (y/n): ");
		char choice = Keyboard.readChar();
		
		String fileName;
		if(Character.toLowerCase(choice) == 'y') {
			System.out.print("Nome del file (senza estensione): ");
			fileName = Keyboard.readString() + ".dmp";
		} else {
			fileName = "clusters_" + System.currentTimeMillis() + ".dmp";
		}
		
		out.writeObject(fileName);
		
		String result = (String)in.readObject();
		if(!result.equals("OK"))
			throw new ServerException(result);
		else
			System.out.println("Cluster salvati nel file: " + fileName);
	}

	/**
	 * Metodo principale che avvia il client e gestisce l'interazione con l'utente.
	 * 
	 * <p>Il metodo si aspetta due argomenti dalla riga di comando:
	 * <ol>
	 *   <li>Indirizzo IP del server</li>
	 *   <li>Numero di porta del server</li>
	 * </ol>
	 * </p>
	 * 
	 * <p>Presenta un menu interattivo che permette di:
	 * <ul>
	 *   <li>Opzione 1: Caricare una tabella dal database e poi caricare cluster da file</li>
	 *   <li>Opzione 2: Caricare una tabella, eseguire il clustering e salvare i risultati</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>Dopo ogni operazione, l'utente può scegliere di ripetere l'operazione
	 * o tornare al menu principale.</p>
	 * 
	 * @param args array contenente l'indirizzo IP del server (args[0]) e la porta (args[1])
	 */
	public static void main(String[] args) {
		String ip = args[0];
		int port = Integer.parseInt(args[1]);
		MainTest main = null;
		
		try{
			main = new MainTest(ip, port);
		}
		catch (IOException e){
			System.out.println(e);
			return;
		}
		
		do{
			int menuAnswer = main.menu();
			switch(menuAnswer)
			{
				case 1: // Load clusters from file
					// Prima carica la tabella (comando 0)
					while(true){
						try{
							main.storeTableFromDb();
							break; //esce fuori dal while
						}
						catch (SocketException e) {
							System.out.println(e);
							return;
						}
						catch (FileNotFoundException e) {
							System.out.println(e);
							return;
						} catch (IOException e) {
							System.out.println(e);
							return;
						} catch (ClassNotFoundException e) {
							System.out.println(e);
							return;
						}
						catch (ServerException e) {
							System.out.println(e.getMessage());
						}
					} //end while
					
					// Poi carica i cluster dal file (comando 3)
					try {
						String kmeans = main.learningFromFile();
						System.out.println(kmeans);
					}
					catch (SocketException e) {
						System.out.println(e);
						return;
					}
					catch (FileNotFoundException e) {
						System.out.println(e);
						return ;
					} catch (IOException e) {
						System.out.println(e);
						return;
					} catch (ClassNotFoundException e) {
						System.out.println(e);
						return;
					}
					catch (ServerException e) {
						System.out.println(e.getMessage());
					}
					break;
					
				case 2: // learning from db
					while(true){
						try{
							main.storeTableFromDb();
							break; //esce fuori dal while
						}
						catch (SocketException e) {
							System.out.println(e);
							return;
						}
						catch (FileNotFoundException e) {
							System.out.println(e);
							return;
						} catch (IOException e) {
							System.out.println(e);
							return;
						} catch (ClassNotFoundException e) {
							System.out.println(e);
							return;
						}
						catch (ServerException e) {
							System.out.println(e.getMessage());
						}
					} //end while [viene fuori dal while con un db (in alternativa il programma termina)
					
					char answer = 'y'; //itera per learning al variare di k
					do{
						try
						{
							String clusterSet = main.learningFromDbTable();
							System.out.println(clusterSet);
							
							main.storeClusterInFile();
						}
						catch (SocketException e) {
							System.out.println(e);
							return;
						}
						catch (FileNotFoundException e) {
							System.out.println(e);
							return;
						} 
						catch (ClassNotFoundException e) {
							System.out.println(e);
							return;
						}catch (IOException e) {
							System.out.println(e);
							return;
						}
						catch (ServerException e) {
							System.out.println(e.getMessage());
						}
						System.out.print("Vuoi ripetere l'operazione? (y/n)");
						answer = Keyboard.readChar();
					}
					while(Character.toLowerCase(answer) == 'y');
					break; //fine case 2
					
				default:
					System.out.println("Opzione non valida!");
			}
			
			System.out.print("Vuoi scegliere una nuova operazione dal menu?(y/n)");
			if(Keyboard.readChar() != 'y')
				break;
		}
		while(true);
	}
}