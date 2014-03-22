package de.hsbremen.mds.server.valueobjects;

import org.json.JSONObject;

public class Game extends MDSServerObject{

	private String name;

	
	public Game(JSONObject game, int id, String name) {
		super(game, id);
		this.name = name;
	}
	
	public Game(JSONObject game, int id) {
		super(game, id);
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}
	
	
}