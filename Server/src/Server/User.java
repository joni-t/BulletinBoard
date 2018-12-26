package Server;

import java.io.Serializable;

//luokka käyttäjien tiedoille
public class User implements Serializable {
	private static final long serialVersionUID = 1L;   // universal version identifier
	private String name;	// käyttäjän oikea nimi
	private String username;	// käyttäjätunnus, jolla käyttäjä tunnistetaan
	private String password;	// salasana, jolla käyttäjä todennetaan
	
	// olion muodostaja: kaikki parametrit on annettava
	public User(String pName, String pUsername, String pPassword) {
		name = pName;
		username = pUsername;
		password = pPassword;
	}

	// 3kpl metodeja tietojen saamiseksi
	public String getName() {
		return name;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}

	// 3kpl metodeja tietojen asettamiseksi
	public void setName(String name) {
		this.name = name;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
}
