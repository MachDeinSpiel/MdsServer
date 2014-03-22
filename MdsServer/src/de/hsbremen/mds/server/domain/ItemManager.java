package de.hsbremen.mds.server.domain;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;

import de.hsbremen.mds.server.valueobjects.Item;

public class ItemManager implements ValueObjectInterface{
	
	private Set<Item> items;
	private int id = 0;
	private String response;
	
	public ItemManager() {
		this.items = new HashSet<Item>();
		
		// Zum Testen: HashSet befuellen
		/*
		JSONObject json = new JSONObject("{json:true;}");
		this.addObject(json);
		this.addObject(json);
		*/
		this.updateResponse();
	}
	
	public int addObject(JSONObject json) {
		this.items.add(new Item(json, id++));
		this.updateResponse();
		return id;
		
	}
	
	 public void updateResponse() {
		String response = "{";
		Iterator<Item> it = items.iterator();
		while(it.hasNext()) {
			Item itm = it.next();
			//TODO: letztes Komma weglassen!
			response = response + itm.toString() +",";
			System.out.println(itm.toString());
		}
		response = response + "}";
		this.response = response;
	 }
	
	@SuppressWarnings("unchecked")
	@Override
	public Item findObjectById(int id) {

		Iterator<Item> it = items.iterator();
		int i = 0;
		while(it.hasNext()) {
			Item itm = it.next();
			System.out.println("IT " + i++);
			if ( itm.getId() == id) {
				return itm;
			}
		}
		return null;
		
	}
	
	public String getJson() {
		return this.response;
	}
	
	public String getJson(int id) {
		return this.findObjectById(id).toString();
	}


}
