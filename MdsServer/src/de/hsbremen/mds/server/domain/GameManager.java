package de.hsbremen.mds.server.domain;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;

import de.hsbremen.mds.server.valueobjects.Game;

public class GameManager implements ValueObjectInterface{
	
	private Set<Game> games;
	private int id = 0;
	private String response;
	
	public GameManager() {
		this.games = new HashSet<Game>();
		

		// Zum Testen: HashSet befuellen
		/*
		this.addObject(json);
		json = new JSONObject("{json:true, Game: "+id+";}");
		this.addObject(json);
		*/
		this.updateResponse();
		
	}
	
	public int addObject(JSONObject json) {
		this.games.add(new Game(json, id++));
		this.updateResponse();
		return id;
		
	}
	
	 public void updateResponse() {
		String response = "{";
		Iterator<Game> it = games.iterator();
		while(it.hasNext()) {
			Game ga = it.next();
			//TODO: letztes Komma weglassen!
			response = response + ga.toString() +",";
			System.out.println(ga.toString());
		}
		response = response + "}";
		this.response = response;
	 }

	@SuppressWarnings("unchecked")
	@Override
	public Game findObjectById(int id) {

		Iterator<Game> it = games.iterator();
		int i = 0;
		while(it.hasNext()) {
			Game ga = it.next();
			System.out.println("IT " + i++);
			if ( ga.getId() == id) {
				return ga;
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
