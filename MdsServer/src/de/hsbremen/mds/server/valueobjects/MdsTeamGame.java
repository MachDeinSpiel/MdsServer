package de.hsbremen.mds.server.valueobjects;

import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;


public class MdsTeamGame extends MdsGame {
	
	private int teams;
	private List<MdsTeam> theTeams;
	private int maxTeamplayer;
	
	
	public MdsTeamGame(int gameID,  int templateID, int maxp, int teams) {
		super(gameID,  templateID, maxp);
		this.teams = teams;
		this.maxTeamplayer = maxp / 2;
		this.theTeams = new Vector<MdsTeam>();
	}
	
	public boolean createTeam(String teamName) {
		
		if (this.teams > this.theTeams.size()) {
			
			if (this.checkTeamName(teamName)) {
				teamName = teamName + "1";
			}
			
			MdsTeam newTeam = new MdsTeam(teamName, this.maxTeamplayer);
			this.theTeams.add(newTeam);
			return true;
		}
		
		return false;
	}
	
	public boolean putPlayerIntoTeam(MdsPlayer p, String teamName) {
					
		MdsTeam theTeam = this.getTeamByName(teamName);
		
		if (theTeam != null) {
			theTeam.addPlayer(p);
			this.playerID++;
			return true;
		} else {
			return false;
		}
		
	
	}
	
	private MdsTeam getTeamByName(String teamName) {
		for (MdsTeam t : this.theTeams) {
			if(t.getName().equals(teamName)){
				return t;
			}
		}
		return null;
	}

	public void removePlayer(MdsPlayer p) {
		for (MdsTeam t : this.theTeams) {
			List<MdsPlayer> teamPlayers = t.getAllPlayers();
			if (teamPlayers.contains(p)) {
				teamPlayers.remove(p);
				return;
			}
		}
		
	}
	
	private boolean checkTeamName(String name) {

		for (MdsTeam t : this.theTeams) {
			if(t.getName().equals(name)){
				return false;
			}
		}
		
		return true;
		
	}
	
	@Override
	public void notifyLobby() {
		String message = this.getGameState().toString();
		for (MdsTeam t : this.theTeams) {
			t.notifyTeam(message);
		}	
	}
	
	@Override
	public JSONArray getAllPlayers() {
		JSONArray teams = new JSONArray();
		
		// Fuer jedes Team des Spiels
		for (MdsTeam t : this.theTeams) {
			// Team Objekt erzeugen
			JSONObject oneTeam = new JSONObject();
			// Namen einfuegen
			oneTeam.put("name", t.getName());	
			// Spieler in Array schreiben
			JSONArray allPlayers = new JSONArray();
			for (MdsPlayer p : t.getAllPlayers()) {
				allPlayers.put(p.toJSON());
			}
			// Spieler-Array in Team schreiben
			oneTeam.put("players", allPlayers);
			// Team ins Team Array schreiben
			teams.put(oneTeam);
		}
		
		// Team Array zurueckgeben
		return teams;
	}

}
