package Server;

import java.io.Serializable;
import java.util.Date;

// luokka yksitt�isten viestien tiedoille
public class Message implements Serializable {
	private static final long serialVersionUID = 1L;   // universal version identifier
	private String username;	// k�ytt�j� joka on viestin kirjoittanut
	private String message;		// viestin teksti
	private Date date;			// viestin l�hetysaika
	private Boolean removed = new Boolean(false);	// onko viesti poistettu
	
	// olion muodostaja: kaikki parametrit paitsi aika on annettava
	public Message(String pUsername, String pMessage) {
		username = pUsername;
		message = pMessage;
		date = new Date();
	}

	// 4kpl metodeja tietojen saamiseksi
	public String getUsername() {
		return username;
	}
	
	public String getMessage() {
		return message;
	}
	
	public Date getDate() {
		return date;
	}
	
	public Boolean getRemovedStatus() {
		return removed;
	}

	// 4kpl metodeja tietojen asettamiseksi
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public void setRemovedStatus(Boolean removed) {
		this.removed = removed;
	}
	
	// ylikirjoitetaan toString-metodi: palauttaa viestin sis�ll�n
	public String toString() {
		if(removed==false) {
			return "Viesti:\n" + message + "\nLahettanyt: " + username + " " + date.toLocaleString();
		}
		else {
			return "Viesti on poistettu!";
		}
	}
}
