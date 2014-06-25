package de.hsbremen.mds.server.valueobjects;

import org.java_websocket.WebSocket;
import org.json.JSONObject;

public class MdsPlayer {
	
	private String name;
	private WebSocket ws;
	private int id;
	private boolean isInitinal = false;
	
	public MdsPlayer (WebSocket ws, String name, int id, boolean initial) {
		this.name = name;
		this.ws = ws;
		this.setId(id);
		this.isInitinal = initial;
	}
	

	public WebSocket getWS() {
		return this.ws;
	}

	public String getName() {
		return this.name;
	}


	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("name", this.name);
		json.put("id", this.id);
		return json;
	}
	
	public String toString() {
		return this.name;
	}


	public int getId() {
		return this.id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public boolean isInitinal() {
		return this.isInitinal;
	}


	public void setInitinal(boolean isInitinal) {
		this.isInitinal = isInitinal;
	}

}
