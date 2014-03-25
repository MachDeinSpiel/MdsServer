package de.hsbremen.mds.server.valueobjects;

import org.json.JSONObject;

public class Item extends MDSServerObject{

	
	public Item(JSONObject item, int id) {
		super(item, id);
	}
	
	
	public int getItem(int id) {
		return this.id;
	}

}
