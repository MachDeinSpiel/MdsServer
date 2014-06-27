package de.hsbremen.mds.server.valueobjects;

import java.io.File;
import java.util.List;

import org.java_websocket.WebSocket;
import org.json.JSONArray;
import org.json.JSONObject;

import de.hsbremen.mds.server.domain.MdsComServer;
import de.hsbremen.mds.server.domain.MdsServerInterpreter;

public abstract class MdsGame {
	
	protected MdsServerInterpreter interpreter;
	protected int templateID;
	protected int maxPlayers;
	protected int minPlayers;
	protected String name;
	protected String author;
	protected double version;
	protected String curl;
	protected String surl;
	protected String appTheme;
	protected boolean isRunning = false;
	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	protected boolean isTeamGame = false;
	protected int gameID;
	
	public MdsGame(int gameID,  int templateID, int maxp) {
		this.gameID = gameID;
		this.templateID = templateID;
		this.maxPlayers = maxp;
	}
	
	public abstract boolean startGame(WebSocket conn, MdsComServer mdsComServer, File file);
	
	public abstract void putPlayer(MdsPlayer p);

	public abstract void removePlayer(WebSocket conn);

	public abstract List<MdsPlayer> getAllPlayers();

	public MdsServerInterpreter getInterpreter() {
		return this.interpreter;
	}

	public abstract MdsPlayer getPlayer(String name);
	
	public abstract MdsPlayer getPlayer(WebSocket conn);
	
	public abstract JSONArray getAllPlayersInJSON();

	public int getTemplateID() {
		return this.templateID;
	}
	
	public abstract WebSocket kickPlayer(WebSocket conn, JSONObject mess);

	public abstract JSONObject toJSON();

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

	public String getAppTheme() {
		return appTheme;
	}

	public void setAppTheme(String appTheme) {
		this.appTheme = appTheme;
	}

	public boolean isRunning() {
		return this.isRunning;
	}

	public boolean isTeamGame() {
		return this.isTeamGame;
	}

	public abstract void exitPlayer(WebSocket conn);

	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	public int getMinPlayers() {
		return minPlayers;
	}

	public void setMinPlayers(int minPlayers) {
		this.minPlayers = minPlayers;
	}

	public abstract int getPlayerCount();

	public Integer getGameID() {
		return this.gameID;
	}

	public abstract void notifyLobby();
	
	public JSONObject getGameState(){
		JSONObject response = new JSONObject();
		response.put("mode", "gamelobby");
		response.put("action", "players");
		response.put("isteamgame", this.isTeamGame);
		JSONArray players = this.getAllPlayersInJSON();
		response.put("players", players);
		return response;
	};

	public abstract List<WebSocket> terminate();

}
