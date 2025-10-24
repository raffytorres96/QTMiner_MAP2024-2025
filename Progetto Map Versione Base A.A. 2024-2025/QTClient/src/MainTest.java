import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import keyboardinput.Keyboard;

/**
 * Classe client per l'interazione con un server di clustering.
 * Gestisce la comunicazione tramite socket e permette di eseguire operazioni
 * di clustering come il caricamento di dati da database remoto, il salvataggio di cluster
 * su file e il caricamento di cluster esistenti.
 * 
 * @author [Raffaele Gatta]
 * @version 1.0
 */

public class MainTest {

	/**
	 * @param args
	 */
	private ObjectOutputStream out;
	private ObjectInputStream in ; // stream con richieste del client
	
	/**
	 * Costruttore che inizializza la connessione con il server.
	 * Crea un socket e configura gli stream di input/output per la comunicazione.
	 * 
	 * @param ip Indirizzo IP del server a cui connettersi
	 * @param port Numero di porta su cui il server è in ascolto
	 * @throws IOException se si verifica un errore durante la connessione o l'inizializzazione degli stream
	 */
	
	public MainTest(String ip, int port) throws IOException{
		InetAddress addr = InetAddress.getByName(ip); //ip
		System.out.println("addr = " + addr);
		Socket socket = new Socket(addr, port); //Port
		System.out.println(socket);
		
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());	; // stream con richieste del client
	}
	
	/**
	 * Visualizza il menu principale e legge la scelta dell'utente.
	 * Le opzioni disponibili sono:
	 * <ul>
	 *   <li>1 - Carica cluster da file</li>
	 *   <li>2 - Carica dati da database</li>
	 * </ul>
	 * 
	 * @return la scelta dell'utente (1 o 2)
	 */

	private int menu(){
		int answer;
		
		do{
			System.out.println("\nScegli una opzione:");
			System.out.println("(1) Load clusters from file");
			System.out.println("(2) Load data from db");
			System.out.print("(1/2):");
			answer = Keyboard.readInt();
		}
		while(answer<=0 || answer>2);
		return answer;		
	}
	
	/**
	 * Carica cluster precedentemente salvati da un file.
	 * Invia al server il comando 3 seguito dal nome del file da cui caricare i cluster.
	 * 
	 * @return una stringa contenente la rappresentazione dei cluster caricati
	 * @throws SocketException se si verifica un errore nel socket
	 * @throws ServerException se il server restituisce un errore
	 * @throws IOException se si verifica un errore di I/O
	 * @throws ClassNotFoundException se la classe dell'oggetto ricevuto non viene trovata
	 */

	private String learningFromFile() throws SocketException,ServerException,IOException,ClassNotFoundException{
    out.writeObject(3);
    
    System.out.print("Nome del file:"); // Cambiato da "Table Name"
    String fileName=Keyboard.readString(); // Cambiato da tabName
    out.writeObject(fileName);
    
    String result = (String)in.readObject();
    if(result.equals("OK"))
        return (String)in.readObject();
    else {
		throw new ServerException(result);
		}
	}

	
	private void storeTableFromDb() throws SocketException,ServerException,IOException,ClassNotFoundException{
		out.writeObject(0);
		System.out.print("Nome della tabella:");
		String tabName=Keyboard.readString();
		out.writeObject(tabName);
		
		String result = (String)in.readObject();
		if(!result.equals("OK"))
			throw new ServerException(result);
		
	}
	private String learningFromDbTable() throws SocketException,ServerException,IOException,ClassNotFoundException{
		out.writeObject(1);
		double r=1.0;
		do{
			System.out.print("Inserisci raggio:");
			r=Keyboard.readDouble();
		} while(r<=0);
		out.writeObject(r);

		String result = (String)in.readObject();
		if(result.equals("OK")){
			System.out.println("Numero cluster:"+in.readObject());
			return (String)in.readObject();
		}
			else {
				throw new ServerException(result);
			}
	}
	
	private void storeClusterInFile() throws SocketException,ServerException,IOException,ClassNotFoundException{
		out.writeObject(2);
		
		
		String result = (String)in.readObject();
		if(!result.equals("OK"))
			 throw new ServerException(result);
		
	}
	public static void main(String[] args) {
		String ip=args[0];
        int port=new Integer(args[1]).intValue();
		MainTest main=null;
		
		try{
			main=new MainTest(ip,port);
		}
		catch (IOException e){
			System.out.println(e);
			return;
		}
		
		
		do{
			int menuAnswer=main.menu();
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
        String kmeans=main.learningFromFile();
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
						
					char answer='y';//itera per learning al variare di k
					do{
						try
						{
							String clusterSet=main.learningFromDbTable();
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
						answer=Keyboard.readChar();
					}
					while(Character.toLowerCase(answer)=='y');
					break; //fine case 2
					default:
					System.out.println("Opzione non valida!");
			}
			
			System.out.print("Vuoi scegliere una nuova operazione dal menu?(y/n)");
			if(Keyboard.readChar()!='y')
				break;
			}
		while(true);
		}
	}



