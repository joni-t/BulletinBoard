package Client;

import java.io.*; // tiedonsiirto k‰ytt‰j‰n kanssa
import java.net.*; // verkkoresursit (socket ym.)

// TCP client
public class Client {
	private Socket clientSocket = null; // linkki kaksisuuntaiseen tiedonsiirtoon clientin ja serverin v‰lill‰
    private ObjectOutputStream oos = null;	// hoitaa tiedonsiirron serverille p‰in
    private ObjectInputStream ois = null;	// hoitaa tiedonsiirron  serverilt‰ clientille
    private BufferedReader stdIn = new BufferedReader(
            new InputStreamReader(System.in)); // hoitaa tiedonsiirron k‰ytt‰j‰lt‰ clienttiin
    private String userInput; // luetaan k‰ytt‰j‰n syˆte t‰h‰n
    private String serverOutput; // luetaan palvelimen viesti t‰h‰n
    private String menuCheck = new String("Valitse toiminto:");  // tarkistetaan palvelimen viestist‰, ett‰ ollaan valikossa (lˆytyy viestin alusta)
    private String logInCheck = new String("Kirjautuminen hyvaksytty!");	// kirjautuminen hyv‰ksytty, jos t‰m‰ lˆytyy palvelimen viestin alusta
    private String openThreadCheck = new String("Avataan viestiketjun viestit");	// viestiketjun tarkastelu avattu, jos t‰m‰ lˆytyy palvelimen viestin alusta
    
    private Client() {
    	// yritet‰‰n muodostaa tiedonsiirtoyhteys serverille (localhost)
        try {
            clientSocket = new Socket("localhost", 2000); // serveri kuuntelee porttia 2000
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ois = new ObjectInputStream(clientSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Tuntematon host.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Ei saada muodostettua I/O yhteytt‰ serverille.");
            System.exit(1);
        }
    }
    
    // lukee socketista olion serverOutput-olioon ja myˆs virheenk‰sittely
    private void ReadFromSocket() {
    	try {
	    	serverOutput = (String) ois.readObject();
	    } catch (IOException e) {
    	    e.printStackTrace(); // Tulostetaan herjat
    	} catch (ClassNotFoundException e) {
    	    e.printStackTrace(); // Tulostetaan herjat
    	}
    }
    
    // l‰hett‰‰ userInput-olion socketin kautta ja myˆs virheenk‰sittely
    private void WriteToSocket() {
    	try {
    		oos.writeObject(userInput);
	    } catch (IOException e) {
    	    e.printStackTrace(); // Tulostetaan herjat
    	}
    }
    
    // lukee k‰ytt‰j‰n valinnan tai syˆtteen userInput-olioon ja myˆs virheenk‰sittely
    private void ReadFromUser() {
    	try {
	    	this.userInput = new String(stdIn.readLine());
	    } catch (IOException e) {
    	    e.printStackTrace(); // Tulostetaan herjat
    	}
    }
    
    // hoitaa viestiliikenteen k‰ytt‰j‰n todennusvaiheessa
    // ohjelma voidaan sulkea vain, kun ei olla kirjoittamassa tietoja (ohjelma seuraa tilaa palvelimen viesteist‰)
    // homma menee niin, ett‰ palvelin l‰hett‰‰ viestin (tulostetaan) ja client (user) vastaa: t‰t‰ jatketaan vuorotellen
    private void Authenticating() {
    	Boolean closingPossible=true;
    	
    	// luetaan eka valikko: kirjautuminen, rekisterˆityminen tai ohjelman sulkeminen
    	ReadFromSocket();  // lukee serverOutput-olioon
    	System.out.println(serverOutput);
    	ReadFromUser(); // luetaan k‰ytt‰j‰n valinta (userInput-olioon)
    	
    	// toistetaan silmukkaa kunnes ohjelma suljetaan (nollalla ja ohjelma on valikossa)
    	while (!(closingPossible && userInput.equals("0"))) {
    		closingPossible=false;
    		WriteToSocket();	// l‰hetet‰‰n k‰ytt‰j‰n valinta serverille
    		ReadFromSocket();  // lukee serverOutput-olioon
    		System.out.println(serverOutput);
    		// tarkistetaan tila palvelimen vastauksen perusteella
    		if (serverOutput.startsWith(logInCheck)) {
    			// kirjautuminen hyv‰ksytty -> siirryt‰‰n viestiketjuejen hallintavalikkoon
    			ReadFromUser();		// luetaan enter
    			WriteToSocket();	// l‰hetet‰‰n k‰ytt‰j‰n valinta serverille (kuitataan enter)
    			BBThreadsHandling(); // viestiketjujen hallintavalikko
    			ReadFromSocket();  // lukee serverOutput-olioon
    	    	System.out.println(serverOutput);
    		}
    		if (serverOutput.startsWith(menuCheck))
    			closingPossible=true;	// ohjelman sulkemine mahdollista
    		ReadFromUser(); // luetaan k‰ytt‰j‰n valinta (userInput-olioon)
    	}    	
    	WriteToSocket();	// l‰hetet‰‰n k‰ytt‰j‰n valinta serverille (lopetus)
    	
    	// suljetaan tiedonsiirtoresurssit
        try {
        	oos.close();
		    ois.close();
	    	stdIn.close();
	    	clientSocket.close();
        } catch (IOException e) {
    	    e.printStackTrace(); // Tulostetaan herjat
    	}
    }
    
    // hoitaa viestiliikenteen viestiketjujen hallintavalikosssa
    // valikosta voidaan poistua vain, kun ei olla lis‰‰m‰ss‰ uutta ketjua (ohjelma seuraa tilaa palvelimen viesteist‰)
    // homma menee niin, ett‰ palvelin l‰hett‰‰ viestin (tulostetaan) ja client (user) vastaa: t‰t‰ jatketaan vuorotellen
    private void BBThreadsHandling() {
    	Boolean closingPossible=true;
    	// luetaan eka valikko: viestiketjujen hallinta
    	ReadFromSocket();  // lukee serverOutput-olioon
    	System.out.println(serverOutput);
    	ReadFromUser(); // luetaan k‰ytt‰j‰n valinta (userInput-olioon)
    	
    	// toistetaan silmukkaa kunnes poistutaan todennusvalikkoon (/poistu ja ohjelma on valikossa)
    	while (!(closingPossible && userInput.equals("/poistu"))) {
    		closingPossible=false;
    		WriteToSocket();	// l‰hetet‰‰n k‰ytt‰j‰n valinta serverille
    		ReadFromSocket();  // lukee serverOutput-olioon
    		System.out.println(serverOutput);
    		// tarkistetaan tila palvelimen vastauksen perusteella
    		if (serverOutput.startsWith(openThreadCheck)) {
	    		// viestiketjun tarkastelu valittu -> siirryt‰‰n viestien hallintavalikkoon
    			ReadFromUser();		// luetaan enter
				WriteToSocket();	// l‰hetet‰‰n k‰ytt‰j‰n valinta serverille (kuitataan enter)
				MessagesHandling(); // viestien hallintavalikko
				ReadFromSocket();  // lukee serverOutput-olioon
		    	System.out.println(serverOutput);
    		}
    		if (serverOutput.startsWith(menuCheck))
    			closingPossible=true;	// ohjelman sulkemine mahdollista
    		ReadFromUser(); // luetaan k‰ytt‰j‰n valinta (userInput-olioon)
    	}    	
    	WriteToSocket();	// l‰hetet‰‰n k‰ytt‰j‰n valinta serverille (poistuminen)
    }
    
    // hoitaa viestiliikenteen viestien hallintavalikosssa
    // valikosta voidaan poistua vain, kun ei olla lis‰‰m‰ss‰ uutta viesti‰ (ohjelma seuraa tilaa palvelimen viesteist‰)
    // homma menee niin, ett‰ palvelin l‰hett‰‰ viestin (tulostetaan) ja client (user) vastaa: t‰t‰ jatketaan vuorotellen
    private void MessagesHandling() {
    	Boolean closingPossible=true;
    	// luetaan eka valikko: viestien hallinta
    	ReadFromSocket();  // lukee serverOutput-olioon
    	System.out.println(serverOutput);
    	ReadFromUser(); // luetaan k‰ytt‰j‰n valinta (userInput-olioon)
    	
    	// toistetaan silmukkaa kunnes poistutaan viestiketjujen hallintavalikkoon (/poistu ja ohjelma on valikossa)
    	while (!(closingPossible && userInput.equals("/poistu"))) {
    		closingPossible=false;
    		WriteToSocket();	// l‰hetet‰‰n k‰ytt‰j‰n valinta serverille
    		ReadFromSocket();  // lukee serverOutput-olioon
    		System.out.println(serverOutput);
    		// tarkistetaan tila palvelimen vastauksen perusteella
    		if (serverOutput.startsWith(menuCheck))
    			closingPossible=true;	// ohjelman sulkemine mahdollista
    		ReadFromUser(); // luetaan k‰ytt‰j‰n valinta (userInput-olioon)
    	}    	
    	WriteToSocket();	// l‰hetet‰‰n k‰ytt‰j‰n valinta serverille (poistuminen)
    }
    
    // p‰‰ohjelma k‰ynnist‰‰ clientin k‰ytt‰j‰n todennuksella, josta siirryt‰‰n eteen p‰in
    // lopulta client palaa takaisin todennusvalikkoon, josta ohjelma voidaan sulkea
	public static void main(String[] args) throws IOException {
		Client client = new Client();

		client.Authenticating();
		System.out.println("Ohjelma on suljettu.");
	}
}
