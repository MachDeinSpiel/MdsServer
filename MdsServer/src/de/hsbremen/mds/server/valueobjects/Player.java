	package de.hsbremen.mds.server.valueobjects;

import org.json.JSONObject;

public class Player extends MDSObject {

	private String name;
	private String passwort;
	
	public Player(JSONObject player, int id, String name, String password) {
		super(player, id);
		this.name = name;
		this.passwort = password;
	}
	
	public Player(JSONObject player, int id) {
		super(player, id);

	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getPasswort() {
		return passwort;
	}

	public void setPasswort(String passwort) {
		this.passwort = passwort;
	}

}