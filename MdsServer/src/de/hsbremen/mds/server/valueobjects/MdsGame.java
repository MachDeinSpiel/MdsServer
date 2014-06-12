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
	
	private List<MdsPlayer> players;
	private MdsServerInterpreter interpreter;
	private int templateID;
	private int maxPlayers;
	private MdsComServer wsServ;
	private int playerID;
	private String name;
	private String author;
	private double version;
	private String curl;
	private String surl;
	private boolean isRunning = false;
	private int gameID;
	
	public MdsGame(MdsComServer wsServ, int gameID,  int templateID, int maxp) {
		this.players = new Vector<MdsPlayer>();
		this.gameID = gameID;
		this.templateID = templateID;
		this.maxPlayers = maxp;
		this.wsServ = wsServ;
		this.playerID = 0;
	}
	
	public boolean startGame(WebSocket conn, MdsComServer mdsComServer, File file) {
		MdsPlayer p = this.getPlayer(conn);
		if (p.isInitinal()) {
			this.interpreter = new MdsServerInterpreter(mdsComServer, file);
			for (MdsPlayer pl : this.players) {
				this.interpreter.onNewConnection(pl.getWS(), pl.getName());
			}
			this.isRunning = true;
			return true;
		} else {
			return false;
		}
	}
	
	public synchronized void putPlayer(MdsPlayer p) {
		this.players.add(p);
		this.playerID++;
	}
	
	public synchronized void removePlayer(MdsPlayer p) {
		this.players.remove(p);
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
		System.out.println(allPlayers.toString());
		return allPlayers;
	}

	public int getTemplateID() {
		return this.templateID;
	}
	
	public void kickPlayer(WebSocket conn, JSONObject mess) {
		if (this.getPlayer(conn).isInitinal()){
			int kick = mess.getInt("player");
			MdsPlayer p = this.getPlayer(kick);
			this.removePlayer(p);
		}
		
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

	private void exitPlayer(WebSocket conn, JSONObject mes) {
		MdsPlayer p = null;
		for(MdsPlayer pl : this.players) {
			if (pl.getWS() == conn) {
				p = pl;
			}
		}
		
		if (p != null) {
			if (p.isInitinal()) {
				this.players.remove(conn);
				for (MdsPlayer pl : this.players) {
					if (!pl.isInitinal()) {
						pl.setInitinal(true);
						break;
					}
				}
			} else {
				this.players.remove(conn);
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
		

}
