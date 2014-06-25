package de.hsbremen.mds.server.valueobjects;

import org.java_websocket.WebSocket;
import org.json.JSONObject;

public class MdsPlayer {
	
	private String name;
	private WebSocket ws;
	private boolean isInitinal = false;
	
	public MdsPlayer (WebSocket ws, String name, boolean initial) {
		this.name = name;
		this.ws = ws;
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
		return json;
	}
	
	public String toString() {
		return this.name;
	}


	public boolean isInitinal() {
		return this.isInitinal;
	}


	public void setInitinal(boolean isInitinal) {
		this.isInitinal = isInitinal;
	}

}
