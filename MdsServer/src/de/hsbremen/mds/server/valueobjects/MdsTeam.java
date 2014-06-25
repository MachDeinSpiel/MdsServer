package de.hsbremen.mds.server.valueobjects;

import java.util.List;
import java.util.Vector;

import org.java_websocket.WebSocket;
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
	
	public void removePlayer(WebSocket ws) {
		MdsPlayer pl = null;
		for (MdsPlayer p : this.teamPlayer) {
			if (p.getWS().equals(ws)) {
				pl = p;
				break;
			}
		}
		
		if (pl != null) {
			this.teamPlayer.remove(pl);
		}
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


	public int getMaxPlayers() {
		return maxPlayers;
	}


	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}
	
	public boolean isPlayerOfTeam(MdsPlayer p) {
		if (this.teamPlayer.contains(p)) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isPlayerOfTeam(WebSocket ws) {
		for (MdsPlayer p : this.teamPlayer) {
			if (p.getWS().equals(ws)) {
				return true;
			}
		}
		return false;
	}
	
	public MdsPlayer getPlayer(String name) {
		for (MdsPlayer p : this.teamPlayer) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}
	
	public int getPlayerCount() {
		return this.teamPlayer.size();
	}


	public MdsPlayer getPlayer(WebSocket conn) {
		for (MdsPlayer p : this.teamPlayer) {
			System.out.println("# Player: " + p.getWS().toString() + " vergl: " + conn.toString() );
			if (p.getWS() == conn) {
				return p;
				
			}
		}
		return null;
	}

}
