package Server;

import java.io.Serializable;
import java.util.ArrayList;

// luokka viestiketjujen tiedoille
public class BBThread implements Serializable {
	private static final long serialVersionUID = 1L;   // universal version identifier
	private String username;	// k‰ytt‰j‰ joka on ketjun avannut
	private String topic;	// ketjun aihe
	private ArrayList<Message> messages = new ArrayList<Message>();	// ketjun viestit
	private Boolean removed = new Boolean(false);	// onko viestiketju poistettu
	
	// olion muodostaja: kaikki parametrit on annettava (vain eka viesti)
	public BBThread(String pUsername, String pTopic, Message pMessage) {
		username = pUsername;
		topic = pTopic;
		messages.add(pMessage);
	}

	// 4kpl metodeja tietojen saamiseksi
	public String getUsername() {
		return username;
	}
	
	public String getTopic() {
		return topic;
	}
	
	public ArrayList<Message> getMessages() {
		return messages;
	}
	
	public Boolean getRemovedStatus() {
		return removed;
	}

	// 4kpl metodeja tietojen asettamiseksi
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public void setMessages(ArrayList<Message> messages) {
		this.messages = messages;
	}
	
	public void setRemovedStatus(Boolean removed) {
		this.removed = removed;
	}
	
	// palauttaa viestien lkm:n
	public int getMessagesSize() {
		return this.messages.size();
	}
	
	// lis‰‰ uuden viestin viestiketjuun. synchronized avainsana auttaa selvi‰m‰‰n samanaikaisesta k‰ytˆst‰
	public synchronized void addMessage(Message message) {
		messages.add(message);
	}
	
	// palauttaa yhden viestin (ind)
	public synchronized Message getMessage(int ind) {
		return messages.get(ind);
	}
	
	// palauttaa poistamattomien viestien lukum‰‰r‰n
	public synchronized int getNonRemovedMessagesSize() {
		int size=0;
		for (int i=0; i<messages.size(); i++)
			if (messages.get(i).getRemovedStatus() == false)
				size++;
		return size;
	}
	
	// poistaa viestin (merkitsee poistetuksi)
	public synchronized void removeMessage(int ind) {
		messages.get(ind).setRemovedStatus(true);
	}
	
	// tekee listan poistamattomista viesteist‰
	public String ListMessages() {
    	StringBuffer list;
    	Message message;
    	if(removed==true)	// tarkistetaan, onko viestiketju poistettu
    		return "Viestiketju on poistettu!";
    	if (getNonRemovedMessagesSize()>0) { // onko poistamattomia viestej‰
    		list = new StringBuffer("Lista viesteista:\nid\tUsername\t\tTime");
    		for (int i=0; i<getMessagesSize(); i++) {
    			message = getMessage(i);
    			if(message.getRemovedStatus() == false)
    				list.append("\n" + i + ".\t" + message.getUsername() + "\t\t" + message.getDate().toLocaleString());
    		}
    	}
    	else
    		list = new StringBuffer("Ei viesteja");
    	return list.toString();
    }
}
