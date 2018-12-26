package Server;

import java.util.ArrayList;
import java.io.*;

// luokka viestiketjujen tallentamiseen
// singleton eli on vain yksi olio
public class BBThreadDB implements Serializable {
	private static final long serialVersionUID = 1L;   // universal version identifier
	// luetaan tietokantaolio tiedostosta
	private static BBThreadDB tdb = readSerializedObject();
	// Viestiketjuoliot on tallennettu ArrayList:iin
	private ArrayList<BBThread> db = new ArrayList<BBThread>();

	public static BBThreadDB access() {
		return tdb;
	}
	
	// lukee tietokantaolion tiedostosta
	// poistaa myös poistettavaksi merkityt viestiketjut ja yksittäiset viestit
	private static BBThreadDB readSerializedObject() {
		BBThreadDB temp = null;
		BBThread thread = null;
		try {
			// input streemi serialisoidun objektin lukemiseen tiedostosta
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					"threadDb.out"));
			temp = (BBThreadDB) in.readObject(); // luetaan serialisoitu BBThreadDB
			// poistetaan poistettavaksi merkityt viestiketjut ja yksittäiset viestit
			for(int i=0; i<temp.getSize(); i++) {
				thread=temp.getBBThread(i);
				if(thread.getRemovedStatus() == true) {
					// poistetaan koko ketju
					temp.removeBBThread(i--);
				}
				else {
					// tarkistetaan yksittäiset viestit
					for(int j=0; j<thread.getMessagesSize(); j++) {
						if(thread.getMessage(j).getRemovedStatus()==true)
							thread.removeMessage(j--);
					}
				}
			}
			System.out.println("BB-tietokanta luettu tiedostosta.");
			in.close();
		} catch (IOException e) {
			temp = new BBThreadDB(); // jos tiedostoa ei ole, tehdään tyhjä
			System.out.println("Tyhja BB-tietokanta luotu.");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return temp;
	}
	
	// oliomuodostaja ei tee mitään
	private BBThreadDB() {
	}
	
	// lisää uuden viestiketjun tietokantaan. synchronized avainsana auttaa selviämään samanaikaisesta käytöstä
	public synchronized void addBBThread(BBThread thread) {
		db.add(thread);
	}
	
	// metodi tallentaa tietokantaolion tiedostoon
	public synchronized void saveDB() {
		try {
			// output striimi tiedostoon kirjoittamista varten
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream("threadDb.out"));
			out.writeObject(tdb);
			out.close();
			System.out.println("BB-tietokanta tallennettu.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// palauttaa yhden ketjun (ind)
	public synchronized BBThread getBBThread(int ind) {
		return db.get(ind);
	}
	
	// palauttaa viestiketjujen lukumäärän
	public synchronized int getSize() {
		return db.size();
	}
	
	// palauttaa poistamattomien viestiketjujen lukumäärän
	public synchronized int getNonRemovedSize() {
		int size=0;
		for (int i=0; i<db.size(); i++)
			if (db.get(i).getRemovedStatus() == false)
				size++;
		return size;
	}
	
	// poistaa viestiketjun (merkitsee poistetuksi)
	public synchronized void removeBBThread(int ind) {
		db.get(ind).setRemovedStatus(true);
	}
	
	// ylikirjoitetaan toString-metodi: tekee listan poistamattomista viestiketjuista
	public String toString()	{
    	StringBuffer list;
    	BBThread bbthread;
    	if (getNonRemovedSize()>0) { // onko poistamattomia viestiketjuja
    		list = new StringBuffer("Lista viestiketjuista:\nid\tTopic\t\tViesteja");
    		for (int i=0; i<getSize(); i++) {
    			bbthread = getBBThread(i);
    			if(bbthread.getRemovedStatus() == false)
    				list.append("\n" + i + ".\t" + bbthread.getTopic() + "\t\t" + bbthread.getMessagesSize());
    		}
    	}
    	else
    		list = new StringBuffer("Ei viestiketjuja");
    	return list.toString();
    }
}
