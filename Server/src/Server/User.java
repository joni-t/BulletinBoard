package Server;

import java.io.Serializable;

//luokka k�ytt�jien tiedoille
public class User implements Serializable {
	private static final long serialVersionUID = 1L;   // universal version identifier
	private String name;	// k�ytt�j�n oikea nimi
	private String username;	// k�ytt�j�tunnus, jolla k�ytt�j� tunnistetaan
	private String password;	// salasana, jolla k�ytt�j� todennetaan
	
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
