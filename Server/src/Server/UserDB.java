package Server;

import java.util.TreeMap;
import java.io.*;

// luokka k‰ytt‰jien tallentamiseen
// singleton eli on vain yksi olio
public class UserDB implements Serializable {
	private static final long serialVersionUID = 1L;   // universal version identifier
	// luetaan tietokantaolio tiedostosta
	private static UserDB udb = readSerializedObject();
	// K‰ytt‰j‰oliot on tallennettu treemap:iin
	private TreeMap<String, User> db = new TreeMap<String, User>();

	public static UserDB access() {
		return udb;
	}
	
	// lukee tietokantaolion tiedostosta
	private static UserDB readSerializedObject() {
		UserDB temp = null;
		try {
			// input streemi serialisoidun objektin lukemiseen tiedostosta
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					"userDb.out"));
			temp = (UserDB) in.readObject(); // luetaan serialisoitu UserDB
			System.out.println("User-tietokanta luettu tiedostosta.");
			in.close();
		} catch (IOException e) {
			temp = new UserDB(); // jos tiedostoa ei ole, tehd‰‰n tyhj‰
			System.out.println("Uusi user-tietokanta luotu.");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return temp;
	}
	
	// oliomuodostaja ei tee mit‰‰n
	private UserDB() {
	}
	
	// lis‰‰ uuden k‰ytt‰j‰n tietokantaan. synchronized avainsana auttaa selvi‰m‰‰n samanaikaisesta k‰ytˆst‰
	public synchronized void addUser(User user) {
		db.put(user.getUsername(), user);
	}
	
	// metodi tallentaa tietokantaolion tiedostoon
	public synchronized void saveDB() {
		try {
			// output striimi tiedostoon kirjoittamista varten
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream("userDb.out"));
			out.writeObject(udb);
			out.close();
			System.out.println("User-tietokanta tallennettu.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized User findUser(String username) {
		return db.get(username);
	}
}