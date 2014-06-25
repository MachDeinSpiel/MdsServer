package de.hsbremen.mds.server.valueobjects;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.java_websocket.WebSocket;
import org.json.JSONArray;
import org.json.JSONObject;

import de.hsbremen.mds.server.domain.MdsComServer;
import de.hsbremen.mds.server.domain.MdsServerInterpreter;

public class MdsGame {
	
	protected List<MdsPlayer> players;
	protected MdsServerInterpreter interpreter;
	protected int templateID;
	protected int maxPlayers;
	protected int playerID;
	protected String name;
	protected String author;
	protected double version;
	protected String curl;
	protected String surl;
	protected boolean isRunning = false;
	protected boolean isTeamGame = false;
	protected int gameID;
	
	public MdsGame(int gameID,  int templateID, int maxp) {
		this.players = new Vector<MdsPlayer>();
		this.gameID = gameID;
		this.templateID = templateID;
		this.maxPlayers = maxp;
		this.playerID = 0;
	}
	
	public boolean startGame(WebSocket conn, MdsComServer mdsComServer, File file) {
		MdsPlayer p = this.getPlayer(conn);
		if (p.isInitinal()) {
			try {
				this.interpreter = new MdsServerInterpreter(mdsComServer, file);
				for (MdsPlayer pl : this.players) {
					this.interpreter.onNewConnection(pl.getWS(), pl.getName());
				}
				this.isRunning = true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		} else {
			return false;
		}
	}
	
	public synchronized void putPlayer(MdsPlayer p) {
		this.players.add(p);
		this.playerID++;
	}

	
	public synchronized void removePlayer(WebSocket conn) {
		this.players.remove(conn);
	}

	
	public List<MdsPlayer> getPlayers() {
		return this.players;
	}

	public MdsServerInterpreter getInterpreter() {
		return this.interpreter;
	}


	public MdsPlayer getPlayer(String name) {
		MdsPlayer p = null;
		
		for (MdsPlayer pl : this.players) {
			if (pl.getName().equals(name)) {
				return pl;
			}
		}
		return p;
	}
	
	public MdsPlayer getPlayer(WebSocket conn) {
		MdsPlayer p = null;
		
		for (MdsPlayer pl : this.players) {
			if (pl.getWS() == conn) {
				return pl;
			}
		}
		return p;
	}
	
	public MdsPlayer getPlayer(int id) {
		MdsPlayer p = null;
		
		for (MdsPlayer pl : this.players) {
			if (pl.getId() == id) {
				return pl;
			}
		}
		return p;
	}
	
	public JSONArray getAllPlayers() {
		JSONArray allPlayers = new JSONArray();
		for (MdsPlayer p : this.players) {
			allPlayers.put(p.toJSON());
		}
//		System.out.println(allPlayers.toString());
		return allPlayers;
	}

	public int getTemplateID() {
		return this.templateID;
	}
	
	public WebSocket kickPlayer(WebSocket conn, JSONObject mess) {
		if (this.getPlayer(conn).isInitinal()){
			int kick = mess.getInt("player");
			MdsPlayer p = this.getPlayer(kick);
			this.players.remove(p);
			return p.getWS();
		}
		return null;
		
	}

	public JSONObject toJSON() {
		JSONObject game = new JSONObject();
		game.put("activeplayers", this.players.size());
		game.put("maxplayers", this.maxPlayers);
		game.put("id", this.gameID);
		game.put("name", this.name);
		game.put("author", this.author);
		game.put("version", this.version);
		game.put("clienturl", this.curl);
		game.put("serverurl", this.surl);
		
		String playernames = "";
		
		if (this.players.size() > 0) {
			playernames = this.players.get(0).toString();
			
			for (int i = 1; i < this.players.size(); i++) {
				playernames = playernames + ", " + this.players.get(i).toString();
			}
		}
		System.out.println(playernames);
		
		game.put("players", playernames);
		return game;
	}

	public int getPlayerID() {
		return this.playerID;
	}

	public void setName(String name) {
		this.name = name;
		
	}

	public void setAuthor(String author) {
		this.author = author;
		
	}

	public void setVersion(double version) {
		this.version = version;
		
	}

	public void setClientURL(String curl) {
		this.curl = curl;
		
	}

	public void setServerURL(String surl) {
		this.surl = surl;
	}

	public String getServerURL() {
		return this.surl;
	}

	public boolean isRunning() {
		return this.isRunning ;
	}

	public boolean isTeamGame() {
		return isTeamGame;
	}

	public void setTeamGame(boolean isTeamGame) {
		this.isTeamGame = isTeamGame;
	}

	public void exitPlayer(WebSocket conn) {
		MdsPlayer p = null;
		for(MdsPlayer pl : this.players) {
			if (pl.getWS() == conn) {
				p = pl;
			}
		}
		
		if (p != null) {
			if (p.isInitinal()) {
				this.players.remove(p);
				for (MdsPlayer pl : this.players) {
					if (!pl.isInitinal()) {
						pl.setInitinal(true);
						break;
					}
				}
			} else {
				this.players.remove(p);
			}
		}
		
	}

	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	public int getActivePlayers() {
		return this.players.size();
	}

	public Integer getGameID() {
		return this.gameID;
	}

	public void notifyLobby() {
		String message = this.getGameState().toString();
		for(MdsPlayer pl : this.players) {
			pl.getWS().send(message);
		}
		
	}
	
	public JSONObject getGameState() {
		JSONObject response = new JSONObject();
		response.put("mode", "gamelobby");
		response.put("action", "players");
		response.put("isteamgame", this.isTeamGame);
		JSONArray players = this.getAllPlayers();
		response.put("players", players);
		return response;
	}

	public List<WebSocket> terminate() {
		Vector<WebSocket> gameLobbyPlayers = new Vector<WebSocket>();
		for(MdsPlayer pl : this.players) {
			gameLobbyPlayers.add(pl.getWS());
		}
		return gameLobbyPlayers;
	}
		

}
