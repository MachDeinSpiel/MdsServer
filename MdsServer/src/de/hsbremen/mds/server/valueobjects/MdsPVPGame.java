package de.hsbremen.mds.server.valueobjects;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.java_websocket.WebSocket;
import org.json.JSONArray;
import org.json.JSONObject;

import de.hsbremen.mds.server.domain.MdsComServer;
import de.hsbremen.mds.server.domain.MdsServerInterpreter;

public class MdsPVPGame extends MdsGame{
	
	protected List<MdsPlayer> players;
	
	public MdsPVPGame(int gameID,  int templateID, int maxp) {
		super(gameID,  templateID, maxp);
		this.players = new Vector<MdsPlayer>();
	}
	
	@Override
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
	
	@Override
	public synchronized void putPlayer(MdsPlayer p) {
		this.players.add(p);
	}

	@Override
	public synchronized void removePlayer(WebSocket conn) {
		this.players.remove(conn);
	}

	@Override
	public List<MdsPlayer> getAllPlayers() {
		return this.players;
	}

	@Override
	public MdsServerInterpreter getInterpreter() {
		return this.interpreter;
	}


	@Override
	public MdsPlayer getPlayer(String name) {
		MdsPlayer p = null;
		
		for (MdsPlayer pl : this.players) {
			if (pl.getName().equals(name)) {
				return pl;
			}
		}
		return p;
	}
	
	@Override
	public MdsPlayer getPlayer(WebSocket conn) {
		MdsPlayer p = null;
		
		for (MdsPlayer pl : this.players) {
			if (pl.getWS() == conn) {
				return pl;
			}
		}
		return p;
	}
	
	@Override
	public JSONArray getAllPlayersInJSON() {
		JSONArray allPlayers = new JSONArray();
		for (MdsPlayer p : this.players) {
			allPlayers.put(p.toJSON());
		}
		return allPlayers;
	}

	@Override
	public int getTemplateID() {
		return this.templateID;
	}
	
	@Override
	public WebSocket kickPlayer(WebSocket conn, JSONObject mess) {
		if (this.getPlayer(conn).isInitinal()){
			String kick = (String) mess.get("player");
			MdsPlayer p = this.getPlayer(kick);
			this.players.remove(p);
			return p.getWS();
		}
		return null;
		
	}

	@Override
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
		game.put("isteamgame", this.isTeamGame);
		
		String playernames = "";
		
		if (this.players.size() > 0) {
			playernames = this.players.get(0).toString();
			
			for (int i = 1; i < this.players.size(); i++) {
				playernames = playernames + ", " + this.players.get(i).toString();
			}
		}
		
		game.put("players", playernames);
		return game;
	}


	@Override
	public void setName(String name) {
		this.name = name;
		
	}

	@Override
	public void setAuthor(String author) {
		this.author = author;
		
	}

	@Override
	public void setVersion(double version) {
		this.version = version;
		
	}

	@Override
	public void setClientURL(String curl) {
		this.curl = curl;
		
	}

	@Override
	public void setServerURL(String surl) {
		this.surl = surl;
	}

	@Override
	public String getServerURL() {
		return this.surl;
	}

	@Override
	public boolean isRunning() {
		return this.isRunning ;
	}

	@Override
	public boolean isTeamGame() {
		return isTeamGame;
	}

	@Override
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

	@Override
	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	@Override
	public int getPlayerCount() {
		return this.players.size();
	}

	@Override
	public Integer getGameID() {
		return this.gameID;
	}

	@Override
	public void notifyLobby() {
		String message = this.getGameState().toString();
		for(MdsPlayer pl : this.players) {
			pl.getWS().send(message);
		}
		
	}

	@Override
	public List<WebSocket> terminate() {
		Vector<WebSocket> gameLobbyPlayers = new Vector<WebSocket>();
		for(MdsPlayer pl : this.players) {
			gameLobbyPlayers.add(pl.getWS());
		}
		return gameLobbyPlayers;
	}
		

}
