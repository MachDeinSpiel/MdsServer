package de.hsbremen.mds.server.domain;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;
import org.restlet.resource.ServerResource;

import de.hsbremen.mds.server.valueobjects.Item;
import de.hsbremen.mds.server.valueobjects.Player;

public class PlayerManager extends ServerResource implements ValueObjectInterface{
	
	private Set<Player> player;
	private int id = 0;
	private String response;
	
	public PlayerManager() {
		this.player = new HashSet<Player>();
		

		// Zum Testen: HashSet befuellen
				
		JSONObject item = new JSONObject("{}");
		JSONObject json = new JSONObject("{json:true, Player: "+id+";}");
		this.testBackpack(json, item);
		
		
		this.updateResponse();
		
	}
	
	public int addObject(JSONObject json) {
		this.player.add(new Player(json, id++));
		this.updateResponse();
		return id;
		
	}
	
	public Set<Item> getBackpack(int id){
		Player player = this.findPlayerById(id);
		return player.getBackpack();
	}
	
	
	public int testBackpack(JSONObject json,JSONObject item){
		Player pl = new Player(json, id++);
		this.player.add(pl);
		
		Item it = new Item(item, 0);
		pl.addToBackpack(it);
		it = new Item(item, 1);
		pl.addToBackpack(it);
		it = new Item(item, 2);
		pl.addToBackpack(it);
		
	
		this.updateResponse();
		return id;
	}
	
	
	
	 public void updateResponse() {
		String response = "{";
		Iterator<Player> it = player.iterator();
		while(it.hasNext()) {
			Player pa = it.next();
			//TODO: letztes Komma weglassen!
			response = response + pa.toString() +",";
			System.out.println(pa.toString());
		}
		response = response + "}";
		this.response = response;
	 }
	
	public Player findPlayerById(int id) {
		System.out.println("Find ID " + id);
		Iterator<Player> it = player.iterator();
		int i = 0;
		while(it.hasNext()) {
			Player pa = it.next();
			System.out.println("IT " + i++);
			if ( pa.getId() == id) {
				return pa;
			}
		}
		return null;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public Player findObjectById(int id) {

		Iterator<Player> it = player.iterator();
		int i = 0;
		while(it.hasNext()) {
			Player pl = it.next();
			System.out.println("IT " + i++);
			if ( pl.getId() == id) {
				return pl;
			}
		}
		return null;
		
	}

	@Override
	public String getJson() {
		return this.response;
	}

	@Override
	public String getJson(int id) {
		return this.findObjectById(id).toString();
	}

}
