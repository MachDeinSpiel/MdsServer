	package de.hsbremen.mds.server.valueobjects;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

public class Player extends MDSServerObject {

	private String name;
	private String passwort;
	private Set<Item> backpack;
	
	public Player(JSONObject player, int id, String name, String password) {
		super(player, id);
		this.name = name;
		this.passwort = password;
		
	}
	
	public Player(JSONObject player, int id) {
		super(player, id);
		this.backpack = new HashSet<Item>();
		

	}
	
	
	public void addToBackpack(Item item) {
		this.backpack.add(item);
	}
	
	public Set<Item> getBackpack(){
		return this.backpack;
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