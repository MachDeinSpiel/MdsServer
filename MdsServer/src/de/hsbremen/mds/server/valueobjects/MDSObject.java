package de.hsbremen.mds.server.valueobjects;

import org.json.JSONObject;

abstract class MDSObject {
	
	protected JSONObject json;
	protected int id;
	
	public MDSObject(JSONObject json, int id) {
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
