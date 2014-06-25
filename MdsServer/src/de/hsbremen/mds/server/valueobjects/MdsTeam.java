package de.hsbremen.mds.server.valueobjects;

import java.util.List;
import java.util.Vector;

import org.json.JSONObject;

public class MdsTeam {
	
	private List<MdsPlayer> teamPlayer;
	private String name;
	private int maxPlayers;
	
	
	public MdsTeam (String name, int maxPlayers) {
		this.name = name;
		this.maxPlayers = maxPlayers;
		this.teamPlayer = new Vector<MdsPlayer>();
	}
	
	
	public String getName() {
		return this.name;
	}

	
	public void addPlayer(MdsPlayer p) {
		this.teamPlayer.add(p);
		
	}
	
	public void removePlayer(MdsPlayer p) {
		this.teamPlayer.remove(p);
	}
	
	public List<MdsPlayer> getAllPlayers() {
		return this.teamPlayer;
	}
	
	public JSONObject toJSON() {
		return null;
	}
	
	public void notifyTeam(String message) {
		for (MdsPlayer p : this.teamPlayer) {
			p.getWS().send(message);
		}
	}

}
