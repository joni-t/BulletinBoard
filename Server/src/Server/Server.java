package Server;

import java.io.*; // tiedonsiirtoresurssit
import java.net.*; // verkkoresursit (socket ym.)
import java.util.Date;	// ajank‰sittely
import javax.swing.*;	// grafiikkakomponentit
import java.awt.event.*;	// grafiikkakomponenttien tapahtumank‰sittely
import java.awt.FlowLayout; // komponenttien sijoittu

//Luokka hoitaa keskustelun yhden clientin kanssa
class ClientYhteysThread extends Thread {
    private Socket clientSocket = null;		// kaksisuuntainen yhteys toiseen p‰‰h‰n
    private UserDB udb = UserDB.access();	// user-tietokanta
    private BBThreadDB tdb = BBThreadDB.access();	// viestiketju-tietokanta
    private ObjectInputStream ois = null;	// yhteys sis‰‰n p‰in
    private ObjectOutputStream oos = null; // yhteys ulos p‰in
    private String inputLine=new String("alustus"), outputLine=null; // stringioliot ulos ja sis‰‰n tuleville tiedoille
    private User user=null;	// kirjautuneen k‰ytt‰j‰n tiedot
    private BBThread selectedThread = null;	// tarkasteltava viestiketju
    
    
    // luokan muodostin saa parametrina socketin, jonka serveri socketin accept palautti
    public ClientYhteysThread(Socket clientSocket) {
    	super("ClientYhteysThread"); // kutsutaan oliota yliluokan ilmentym‰n‰
    	this.clientSocket = clientSocket;
    	
    	// luodaan yhteydet kuntoon
    	try {
    	    ois = new ObjectInputStream(clientSocket.getInputStream());   // luodaan I/O olio serverille p‰in
    	    oos = new ObjectOutputStream(clientSocket.getOutputStream()); // luodaan I/O olio clientille p‰in    	    
    	} catch (IOException e) {
    	    e.printStackTrace(); // Tulostetaan herjat
    	}
    	
    	// k‰ynnistet‰‰n threadi
    	this.start();
    }
    
    // lukee socketista olion inputLine-olioon ja myˆs virheenk‰sittely
    private void ReadFromSocket() {
    	try {
    		inputLine = (String) ois.readObject();
	    } catch (IOException e) {
    	    e.printStackTrace(); // Tulostetaan herjat
    	    this.destroy();	// tapetaan threadi
    	} catch (ClassNotFoundException e) {
    	    e.printStackTrace(); // Tulostetaan herjat
    	}
    }
    
    // l‰hett‰‰ outputLine-olion socketin kautta ja myˆs virheenk‰sittely
    private void WriteToSocket() {
    	try {
    		oos.writeObject(outputLine);
	    } catch (IOException e) {
    	    e.printStackTrace(); // Tulostetaan herjat
    	    this.destroy();	// tapetaan threadi
    	}
    }
    
    // hoitaa k‰ytt‰j‰n todentamisen joko kirjautumisen tai rekisterˆitymisen kautta
    // siirtyy viestiketjujen hallintavalikkoon, mik‰li todentaminen hyv‰ksyt‰‰n
    // poistutaan, mik‰li k‰ytt‰j‰ sulki ohjelman
    private void Authenticating() {
    	String valikko = new String("Valitse toiminto:\n\t1. Kirjaudu sisaan\n\t2. Rekisteroidy\n\t0. Sulje");
    	while (!inputLine.equals("0")) { // kunnes ohjelma suljetaan
    		outputLine=valikko;
    		WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
    		ReadFromSocket();  // lukee inputLine-olioon
    		// toimitaan k‰ytt‰j‰n valinnan mukaan
    		if (inputLine.equals("1")) {
    			// kirjautuminen
    			this.LogIn();
    		} else if (inputLine.equals("2")) {
    			// rekisterˆinti
    			Registering();
    		} else if (inputLine.equals("0")) {
    			// ohjelman sulkeminen -> ei tehd‰ t‰ss‰ mit‰‰n
    			;
    		} else {
    			// tuntematon komento
    			outputLine = new String("Tuntematon komento! Paina enter");
    			WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
    			ReadFromSocket();  // lukee inputLine-olioon (kuitataan enter)
    		}
    		
    		// jos kirjautuminen hyv‰ksyt‰‰n, siirryt‰‰n viestiketjujen hallintavalikkoon
    		if (user != null) {
    			outputLine = new String("Kirjautuminen hyvaksytty! Paina enter");
    			WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
    	    	ReadFromSocket();  // lukee inputLine-olioon (kuitataan enter)
    	    	ThreadMenu();
    		}
    	}
    }
    
    // hoitaa kirjautumisen. mik‰li onnistuu, asettaa k‰ytt‰j‰n tiedot user-olioon
    private void LogIn() {
    	// pyydet‰‰n k‰ytt‰j‰nimi
    	outputLine = new String("Anna kayttajanimi: ");
	    WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
	    ReadFromSocket();  // lukee inputLine-olioon
	    String username = new String(inputLine.toString());
	    
	    // pyydet‰‰n salasana
	    outputLine = new String("Anna salasana: ");
	    WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
	    ReadFromSocket();  // lukee inputLine-olioon
	    String password = new String(inputLine.toString());
	    
	    // todennetaan k‰ytt‰j‰nimi ja salasana
	    User temp = udb.findUser(username);
		if (temp == null) {
			outputLine = new String("Kirjautumista ei hyvaksyta! Paina enter");
			WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
			ReadFromSocket();  // lukee inputLine-olioon (kuitataan enter)
		} else {
			// todennetaan viel‰ salasana
			if (temp.getPassword().equals(password)) {
				user = temp;
			}
		}
    }
    
    // hoitaa rekisterˆinnin. mik‰li onnistuu, asettaa k‰ytt‰j‰n tiedot user-olioon, joka tallennetaan myˆs user-tietokantaan
    private void Registering() {
    	// pyydet‰‰n k‰ytt‰j‰nimi
    	outputLine = new String("Anna kayttajanimi: ");
		WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
	    ReadFromSocket();  // lukee inputLine-olioon
	    String username = new String(inputLine.toString());
	    
	    // tarkistetaan ettei k‰ytt‰j‰nimi ole varattu
	    User temp = udb.findUser(username);
		if (temp != null) {
			outputLine = new String("Kayttajanimi on varattu! Paina enter");
			WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
			ReadFromSocket();  // lukee inputLine-olioon (kuitataan enter)
			return;
		}
	    
	    // pyydet‰‰n salasana
	    outputLine = new String("Anna salasana: ");
	    WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta	    
	    ReadFromSocket();  // lukee inputLine-olioon
	    String password = new String(inputLine.toString());
	    
	    // pyydet‰‰n oma nimi
	    outputLine = new String("Anna oma nimi: ");
	    WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
	    ReadFromSocket();  // lukee inputLine-olioon
	    String name = new String(inputLine.toString());
	    
	    // tallnnetaan tiedot user-olioon ja kopio myˆs user-tietokantaan
	    user = new User(name, username, password);
	    udb.addUser(user);
    }
    
    // viestiketjujen hallintavalikko. uusia ketjuja voidaan avata, vanhojen ketjujen viestej‰ lukea ja poistaa (vain admin)
    private void ThreadMenu() {
    	String valikko;
    	int ind;
    	inputLine = new String("alustettu");	// alustetaan mill‰ tahansa muulla kuin "/poistu"
    	if (user.getUsername().compareTo("admin") ==0) // admin voi myˆs poistaa
    		valikko = new String("Valitse toiminto:\n\t/lisaa\n\t/avaa <id>\n\t/poista <id>\n\t/poistu\n\n");
    	else
    		valikko = new String("Valitse toiminto:\n\t/lisaa\n\t/avaa <id>\n\t/poistu\n\n");
    	while (!inputLine.equals("/poistu")) { // kunnes poistutaan todennusvalikkoon
    		outputLine = new String(valikko + tdb.toString());	// valikko + lista viestiketjuista
    		WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
    		ReadFromSocket();  // lukee inputLine-olioon
    		// toimitaan k‰ytt‰j‰n valinnan mukaan
    		if (inputLine.equals("/lisaa")) {
    			// lis‰t‰‰n uusi viestiketju
    			AddNewThread();
    		} else if (inputLine.startsWith("/avaa") && inputLine.length()>6) {
    			// avataan olemassa oleva viestiketju (n‰ytt‰‰ viestit)
    			ind = Integer.parseInt(inputLine.substring(6, inputLine.length()));
    			MessageMenu(ind);	// siirryt‰‰n viestivalikkoon
    			inputLine = new String("alustettu");	// alustetaan mill‰ tahansa muulla kuin "/poistu"
    		} else if (inputLine.equals("/poistu")) {
    			// poistetaan k‰ytt‰j‰n tiedot ja poistutaan takaisin todennusvalikkoon
    			user = null;
    		} else if (inputLine.startsWith("/poista") && inputLine.length()>8 && user.getUsername().equals("admin")) {
    			// poistetaan viestiketju
    			ind = Integer.parseInt(inputLine.substring(8, inputLine.length()));
    			RemoveThread(ind);
    		} else {
    			// tuntematon komento
    			outputLine = new String("Tuntematon komento! Paina enter");
    			WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
    			ReadFromSocket();  // lukee inputLine-olioon (kuitataan enter)
    		}
    	}
    }
    
    // hoitaa uuden viestiketjun lis‰‰misen
    private void AddNewThread() {
    	// pyydet‰‰n aihe
    	outputLine = new String("Anna viestiketjun aihe: ");
		WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
	    ReadFromSocket();  // lukee inputLine-olioon
	    String topic = new String(inputLine.toString());
	    
	    // pyydet‰‰n tekstiosuus
	    outputLine = new String("Kirjoita viesti: ");
	    WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta	    
	    ReadFromSocket();  // lukee inputLine-olioon
	    String message = new String(inputLine.toString());
	    
	    // tallennetaan tiedot viestiketju-tietokantaan
	    tdb.addBBThread(new BBThread(user.getUsername(), topic, new Message(user.getUsername(), message)));
    }
    
    // hoitaa viestiketjun poistamisen
    private void RemoveThread(int ind) {
    	// tarkistetaan ett‰ on annettu sopiva indeksi
    	if (ind>-1 && ind<tdb.getSize()) {
    		tdb.removeBBThread(ind);
    		outputLine = new String("Poistettu. Paina enter");
			WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
			ReadFromSocket();  // lukee inputLine-olioon (kuitataan enter)
    	}
    	else {
    		outputLine = new String("Indeksi ei ole sopiva! Paina enter");
			WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
			ReadFromSocket();  // lukee inputLine-olioon (kuitataan enter)
    	}
    }
    
    // viestien hallintavalikko (threadin sis‰ll‰). uusia viestej‰ voidaan lis‰t‰, vanhoja viestej‰ lukea ja poistaa (vain admin)
    private void MessageMenu(int threadInd) {
    	String valikko;
    	int ind;
    	
    	// tarkistetaan ett‰ threadi on olemassa ja on poistamaton
    	if(threadInd < tdb.getSize() && (selectedThread = tdb.getBBThread(threadInd)).getRemovedStatus()==false)	{
    		outputLine = new String("Avataan viestiketjun viestit. Paina enter");
			WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
	    	ReadFromSocket();  // lukee inputLine-olioon (kuitataan enter)
    	}
    	else {
    		selectedThread = null;
    		outputLine = new String("Viestiketjua ei loytynyt! Paina enter");
			WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
	    	ReadFromSocket();  // lukee inputLine-olioon (kuitataan enter)
	    	return;
    	}
    	
    	// mik‰li viestiketju lˆytyi, siirryt‰‰n viestien haalintavalikkoon
    	inputLine = new String("alustettu");	// alustetaan mill‰ tahansa muulla kuin "/poistu"
    	if (user.getUsername().compareTo("admin") ==0) // admin voi myˆs poistaa
    		valikko = new String("Valitse toiminto:\n\t/lisaa\n\t/avaa <id>\n\t/poista <id>\n\t/poistu\n\n");
    	else
    		valikko = new String("Valitse toiminto:\n\t/lisaa\n\t/avaa <id>\n\t/poistu\n\n");
    	while (!inputLine.equals("/poistu")) { // kunnes poistutaan todennusvalikkoon
    		outputLine = new String(valikko + selectedThread.ListMessages());
    		WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
    		ReadFromSocket();  // lukee inputLine-olioon
    		// tarkistetaan ettei viestiketjua ole juuri ehditty poistaa
    		if(selectedThread.getRemovedStatus()==true && !inputLine.equals("/poistu")) {
    			outputLine = new String("Viestiketju on juuri poistettu! Paina enter");
    			WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
    			ReadFromSocket();  // lukee inputLine-olioon (kuitataan enter)
    		}
    		// toimitaan k‰ytt‰j‰n valinnan mukaan
    		else if (inputLine.equals("/lisaa")) {
    			// lis‰t‰‰n uusi viesti
    			AddNewMessage();
    		} else if (inputLine.startsWith("/avaa") && inputLine.length()>6) {
    			// avataan olemassa oleva viesti (Tulostaa viestin sis‰llˆn)
    			ind = Integer.parseInt(inputLine.substring(6, inputLine.length()));
    			outputLine = new String(selectedThread.getMessage(ind).toString() + "\nPaina enter");
    			WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
    			ReadFromSocket();  // lukee inputLine-olioon (kuitataan enter)    			
    		} else if (inputLine.equals("/poistu")) {
    			// poistutaan takaisin viestiketjuvalikkoon
    			;	// ei vaadi muita toimenpiteit‰
    		} else if (inputLine.startsWith("/poista") && inputLine.length()>8 && user.getUsername().equals("admin")) {
    			// poistetaan viesti
    			ind = Integer.parseInt(inputLine.substring(8, inputLine.length()));
    			RemoveMessage(ind);
    		} else {
    			// tuntematon komento
    			outputLine = new String("Tuntematon komento! Paina enter");
    			WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
    			ReadFromSocket();  // lukee inputLine-olioon (kuitataan enter)
    		}
    	}
    }
    
    // hoitaa viestin poistamisen valittuna olevasta viestiketjusta
    private void RemoveMessage(int ind) {
    	// tarkistetaan ett‰ on annettu sopiva indeksi
    	if (ind>-1 && ind<selectedThread.getMessagesSize()) {
    		selectedThread.removeMessage(ind);
    		outputLine = new String("Poistettu. Paina enter");
			WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
			ReadFromSocket();  // lukee inputLine-olioon (kuitataan enter)
    	}
    	else {
    		outputLine = new String("Indeksi ei ole sopiva! Paina enter");
			WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta
			ReadFromSocket();  // lukee inputLine-olioon (kuitataan enter)
    	}
    }
    
    // hoitaa uuden viestin lis‰‰misen valittuna olevaan viestiketjuun
    private void AddNewMessage() {
    	// pyydet‰‰n viestin teksti
	    outputLine = new String("Kirjoita viesti: ");
	    WriteToSocket();	// l‰hett‰‰ outputLine-olion socketin kautta	    
	    ReadFromSocket();  // lukee inputLine-olioon
	    String message = new String(inputLine.toString());
	    
	    // tallennetaan viesti viestiketjuun
	    selectedThread.addMessage(new Message(user.getUsername(), message));
    }
    
    // ajetaan t‰m‰ metodi, kun threadi k‰ynnistet‰‰n
    public void run() {      	
    	// todennetaan k‰ytt‰j‰
    	Authenticating(); 	// haarautuu myˆs syvemm‰lle
    	    
	    // suljetaan tiedonsiirtoresurssit
	    try {
	    	oos.close();
		    ois.close();
		    clientSocket.close();
	    } catch (IOException e) {
    	    e.printStackTrace(); // Tulostetaan herjat
    	}
    }
}

// serverin k‰yttˆliittym‰ss‰ on nappula uusien yhteyksien vastaanottamisen lopettamiseksi
class ServerGUI extends JFrame
                        implements ActionListener {
	private JButton stopListening, updateInfo, close;
	private JTextField info;
	private Server server;	// halutaan p‰‰st‰ server-olioon

	public ServerGUI(Server server) {  // konstruktori
		setLayout(new FlowLayout());	// kompoenttien sijoittelutapa
		
		// luodaan komponentit
		stopListening = new JButton("Lopeta uusien yhteyksien kuuntelu");
		updateInfo = new JButton("Paivita avoimien yhteyksien lkm");
		close = new JButton("Sulje serveri");
		info = new JTextField(15);
	    info.setEditable(false);
		add(stopListening);
		add(info);
		add(updateInfo);
		add(close);

		// asetetaan kuuntelijat:
		stopListening.addActionListener(this);
		updateInfo.addActionListener(this);
		close.addActionListener(this);
		
		// talletetaan viittaus serveri-olioon
		this.server=server;
		
		// m‰‰ritet‰‰n, mit‰ tehd‰‰n, kun ikkuna suljetaan: tallennetaan tiedot
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(WindowEvent winEvt) {
		        // Perhaps ask user if they want to save any unsaved files first.
		    	UserDB.access().saveDB();
		    	BBThreadDB.access().saveDB();
		    	System.out.println("Serveri on suljettu.");
		    }
		});
	}

	// tapahtumien k‰sittely:
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == stopListening) {
			server.stopListening();	// lopetetaan uusien yhteyksien kuuntelu
		} else if (event.getSource() == updateInfo) {
			// p‰ivitet‰‰n info avoimista yhteyksist‰ (jostain syyst‰ 4 yhteytt‰ tarkoittaa nollaa)
			info.setText("Avoimia yhteyksia on nyt " + (ClientYhteysThread.activeCount()-4) + " kpl.");
		} else if (event.getSource() == close) {
			// tallennetaan tiedot ja suljetaan ohjelma
			UserDB.access().saveDB();
	    	BBThreadDB.access().saveDB();
			System.out.println("Serveri on suljettu.");
			System.exit(0);
		}		
	}
} 


// serverin p‰‰-luokka: ottaa vastaan uudet yhteydet ja avaa uusille yhteyksille oman threadin
public class Server {
	private ServerSocket serverSocket = null; // serverityyppinen socketti joka kuuntelee porttia (uusia yhteyksi‰)
	private boolean listening = true; // kuunnellaanko uusia yhteyksi‰?
	
	// metodi kuuntelee ja ottaa vastaan uusia yhteyksi‰, kunnes listening asetetetaan false:ksi 
	private void listenNewConnections() {
		// yritet‰‰n kuunnella porttia 2000
		try {
		    serverSocket = new ServerSocket(2000);
		} catch (IOException e) {
		    System.err.println("Ei voida kuunnella porttia 2000.");
		    System.exit(-1);
		}
		
		System.out.println("Serveri on valmis ottamaan yhteyksi‰ vastaan.");
		
		// kuunnellaan uusia yhteyksi‰ niin kauan kuin halutaan
		while (listening) {
			// kun uusi yhteys tulee, luodaan uutta yhteytt‰ k‰sittelem‰‰n uusi threadi (joka k‰ynnist‰‰ itse itsens‰)
			try {
				new ClientYhteysThread(serverSocket.accept());
			} catch (IOException e) {
			    System.out.println("Ei oteta enaa vastaan uusia yhteyksia.");
			}
		}
	}
	
	// funktio asettaa palvelimen sulkemistilaan -> uusia yhteyksi‰ ei oteta vastaan, vanhat hoidetaan loppuun
	public void stopListening() {
		listening=false;
		// suljetaan serverSocket
		try { 
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace(); // Tulostetaan herjat
		}
	}
	
	// olio-muodostin ei tee mit‰‰n
	private Server() {		
	}
	
	// P‰‰ohjelma: serveri k‰ynnistyy t‰‰lt‰
	public static void main(String[] args) {
		Server theServer = new Server();
		
		// avataan lomake, jolta serveri voidaan asettaa lopettamaan uusien yhteyksien kuuntelun
		ServerGUI userInterface = new ServerGUI(theServer);
		userInterface.setSize(300, 200);
		userInterface.setTitle("Serveri");
		userInterface.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		userInterface.setVisible(true);
		
		theServer.listenNewConnections();	// kuunnellaan uusia yhteyksi‰
	}
}
