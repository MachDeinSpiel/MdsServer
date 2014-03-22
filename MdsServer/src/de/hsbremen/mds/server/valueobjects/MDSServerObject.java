package de.hsbremen.mds.server.valueobjects;

import org.json.JSONObject;

abstract class MDSServerObject {
	
	protected JSONObject json;
	protected int id;
	
	public MDSServerObject(JSONObject json, int id) {
		this.json = json;
		this.id = id;
	}
		
	public int getId() {
		return this.id;

	}
	
	public String toString() {
		return this.json.toString();
	}


}
