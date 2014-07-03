package de.hsbremen.mds.server.valueobjects;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.java_websocket.WebSocket;
import org.json.JSONArray;
import org.json.JSONObject;

import de.hsbremen.mds.server.domain.MdsComServer;
import de.hsbremen.mds.server.domain.MdsServerInterpreter;


public class MdsTeamGame extends MdsGame {
	
	private int teams;
	private List<MdsTeam> theTeams;
	private int maxTeamplayer;
	
	
	public MdsTeamGame(int gameID,  int templateID, int maxp, int teams) {
		super(gameID,  templateID, maxp);
		this.isTeamGame = true;
		this.teams = teams;
		this.maxTeamplayer = maxp / 2;
		this.theTeams = new Vector<MdsTeam>();
	}
	
	public boolean createTeam(String teamName) {
		
		if (this.teams > this.theTeams.size()) {
			
			if (!this.checkTeamName(teamName)) {
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
			return true;
		}
		return false;
	}
	
	public boolean changeTeam(MdsPlayer p, String teamName) {
		
		this.removePlayer(p);
		
		MdsTeam theTeam = this.getTeamByName(teamName);
		
		if (theTeam != null) {
			theTeam.addPlayer(p);
			this.notifyLobby();
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
				t.removePlayer(p);
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
	public JSONArray getAllPlayersInJSON() {
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

	@Override
	public boolean startGame(WebSocket conn, MdsComServer mdsComServer,	File file) {
		MdsPlayer p = this.getPlayer(conn);
		Map<MdsPlayer, String> allPlayers = this.getPlayersInTeams();
		if (p.isInitinal()) {
			try {
				this.interpreter = new MdsServerInterpreter(mdsComServer, file, this.getPlayerCount());
				for (Entry<MdsPlayer, String> pl : allPlayers.entrySet()) {
					MdsPlayer ply = pl.getKey();
					this.interpreter.onNewConnection(ply.getWS(), ply.getName(), pl.getValue());
				}
				this.setRunning(true);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	public String getTeamName(MdsPlayer p) {
		for (MdsTeam t : this.theTeams) {
			if (t.isPlayerOfTeam(p)) {
				return t.getName();
			}
		}
		return null;
	}

	@Override
	public void putPlayer(MdsPlayer p) {
		int playerCount = Integer.MAX_VALUE;
		MdsTeam smallestTeam = null;
		for (MdsTeam t : this.theTeams) {
			int teamPlayerCount = t.getPlayerCount();
			if (teamPlayerCount < playerCount) {
				playerCount = teamPlayerCount;
				smallestTeam = t;
			}	
		}
		if (smallestTeam != null) {
			smallestTeam.addPlayer(p);
		}
	}
	

	@Override
	public void removePlayer(WebSocket conn) {
		for (MdsTeam t : this.theTeams) {
			if (t.isPlayerOfTeam(conn)) {
				t.removePlayer(conn);
			}
		}
		
	}

	@Override
	public List<MdsPlayer> getAllPlayers() {
		List<MdsPlayer> allPlayers = new Vector<MdsPlayer>();
		for (MdsTeam t : this.theTeams) {
			allPlayers.addAll(t.getAllPlayers());
		}
		return allPlayers;
	}
	
	private Map<MdsPlayer, String> getPlayersInTeams() {
		Map<MdsPlayer, String> playersInTeams = new HashMap<MdsPlayer, String>();
		for (MdsTeam t : this.theTeams) {
			for (MdsPlayer p : t.getAllPlayers()) {
				playersInTeams.put(p, t.getName());
			}
		}
		return playersInTeams;
	}


	@Override
	public MdsPlayer getPlayer(String name) {
		for (MdsTeam t : this.theTeams) {
			MdsPlayer p = t.getPlayer(name);
			if (p != null) {
				return p;
			}
		}
		
		return null;
	}

	@Override
	public MdsPlayer getPlayer(WebSocket conn) {
		for (MdsTeam t : this.theTeams) {
			MdsPlayer p = t.getPlayer(conn);
			
			if (p != null) {
				return p;
			}
		}
		return null;
	}

	@Override
	public WebSocket kickPlayer(WebSocket conn, JSONObject mess) {
		if (this.getPlayer(conn).isInitinal()){
			String kick = (String) mess.get("player");
			MdsPlayer p = this.getPlayer(kick);
			this.removePlayer(p);
			if (p != null) {
				return p.getWS();
			}
		} else {
			// TODO: Player is cheating
		}
		return null;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject game = new JSONObject();
		game.put("activeplayers", this.getPlayerCount());
		game.put("maxplayers", this.maxPlayers);
		game.put("minplayers", this.minPlayers);
		game.put("id", this.gameID);
		game.put("name", this.name);
		game.put("author", this.author);
		game.put("version", this.version);
		game.put("clienturl", this.curl);
		game.put("serverurl", this.surl);
		game.put("isteamgame", this.isTeamGame);
		game.put("teams", this.teams);
		game.put("apptheme", this.appTheme);
		//game.put("previewimg", this.previewImgUrl);
		
		String playernames = "";
		List<MdsPlayer> allPlayers = this.getAllPlayers();
		if (allPlayers.size() > 0) {
			playernames = allPlayers.get(0).toString();
			
			for (int i = 1; i < allPlayers.size(); i++) {
				playernames = playernames + ", " + allPlayers.get(i).toString();
			}
		}
		
		game.put("players", playernames);
		return game;
	}


	@Override
	public int getPlayerCount() {
		return this.getAllPlayers().size();
	}

	@Override
	public boolean exitPlayer(WebSocket conn) {
		MdsPlayer p = this.getPlayer(conn);
	
		if (p != null) {
			if (p.isInitinal()) {
				return true;
				/*
				this.removePlayer(p);
				for (MdsPlayer pl : this.getAllPlayers()) {
					if (!pl.isInitinal()) {
						pl.setInitinal(true);
						break;
					}
				}*/
			} else {
				this.removePlayer(p);
				return false;
			}
		}
		return false;
	}

	@Override
	public List<WebSocket> terminate() {
		Vector<WebSocket> gameLobbyPlayers = new Vector<WebSocket>();
		for(MdsPlayer pl : this.getAllPlayers()) {
			gameLobbyPlayers.add(pl.getWS());
		}
		return gameLobbyPlayers;
	}

	public void createTeams(JSONArray teamNames) {
		for (int i = 0; i < teamNames.length();i++) {
			this.createTeam(teamNames.getString(i));
		}
	}

}
