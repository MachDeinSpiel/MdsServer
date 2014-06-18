package de.hsbremen.mds.server.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.hsbremen.mds.common.communication.WhiteboardHandler;
import de.hsbremen.mds.common.exception.UnknownWhiteboardTypeException;
import de.hsbremen.mds.common.interfaces.ComServerInterface;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.common.whiteboard.WhiteboardUpdateObject;
import de.hsbremen.mds.server.valueobjects.MdsGame;
import de.hsbremen.mds.server.valueobjects.MdsPlayer;

/**
 * 
 */
public class MdsComServer extends WebSocketServer implements ComServerInterface {
	
	private static final String version = "MdsComServer 06.18 (devLogin Branch)";
	private JSONObject gameTemplates;
	private List<WebSocket> loggedInClients;
	private List<WebSocket> waitingClients;
	private Map<WebSocket, Integer> monitors;
	private Map<WebSocket, Integer> playingClients;
	private Map<Integer, MdsGame> games;
		
	
	public MdsComServer(int port, File file) throws UnknownHostException {
		super(new InetSocketAddress(port));
		this.initGames(file);
		this.waitingClients = new Vector<WebSocket>();
		this.loggedInClients = new Vector<WebSocket>();
		this.playingClients = new HashMap<WebSocket, Integer>();
		this.monitors = new HashMap<WebSocket, Integer>();
		this.games = new HashMap<Integer, MdsGame>();
		
	}

	public MdsComServer(InetSocketAddress address) {
		super(address);
	}
	
	private void initGames(File file) {
		JSONObject json = null;
		JSONParser jP = new JSONParser();
		try {
			json = new JSONObject(jP.parse(new FileReader(file)).toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.gameTemplates = json;
	}
	
	
	private synchronized void createGame(WebSocket conn, JSONObject mess) {
		int gameTemplateId = mess.getInt("id");
				
		String playerName = mess.getString("name");
		int maxp = mess.getInt("maxplayers");
		
		MdsPlayer p = new MdsPlayer(conn, playerName, 0, true);
		
		String surl = (String) this.getGameTemplateValue(gameTemplateId, "serverurl");
		String curl = (String) this.getGameTemplateValue(gameTemplateId, "clienturl");
		String name = (String) this.getGameTemplateValue(gameTemplateId, "name");
		String author = (String) this.getGameTemplateValue(gameTemplateId, "author");
		double version = (Double) this.getGameTemplateValue(gameTemplateId, "version");
		int gameID = 0;
		while(this.games.containsKey(gameID)) {
			gameID++;
		}
		
		MdsGame g = new MdsGame(this, gameID, gameTemplateId, maxp);
		g.putPlayer(p);
		g.setName(name);
		g.setAuthor(author);
		g.setVersion(version);
		g.setClientURL(curl);
		g.setServerURL(surl);
		this.playingClients.put(conn, gameID);
		this.loggedInClients.remove(conn);
		this.games.put(gameID, g);
		this.notifyLobby();
		g.notifyLobby();
		
	}
	
	private synchronized void joinGame(WebSocket conn, JSONObject mess) {
		int gameID = mess.getInt("id");
		String name = mess.getString("name");
		if (this.games.containsKey(gameID)) {
			MdsGame g = this.games.get(gameID);
			if (!g.isRunning() && (g.getMaxPlayers() > g.getActivePlayers() )) {
				MdsPlayer p = new MdsPlayer(conn, name, g.getPlayerID(), false);
				g.putPlayer(p);
				this.playingClients.put(conn, gameID);
				this.loggedInClients.remove(conn);
				this.notifyLobby();
				g.notifyLobby();
			} else {
				// TODO: send error game running
			}
		} else {
			// TODO: send error no game with id
		}
		
			
	}
	
	private boolean startGame(WebSocket conn, JSONObject mess) {
		int gameId = this.playingClients.get(conn);
		MdsGame g = this.games.get(gameId);
		String url = g.getServerURL();
		System.out.println("\nStarting new game with " + url);
		File file = this.readJSON(url); 
		this.notifyLobby();
		return g.startGame(conn, this, file);
		
	}
	
	
	@SuppressWarnings("unused") // Wird vermutlich spaeter noch benoetigt
	private synchronized boolean updateGamesTemplate(int gameID, String key, Object value){
		JSONArray gamesArray = this.gameTemplates.getJSONArray("games");
		
		// Aenderung an einem bestehenden Spiel
		if (gameID < gamesArray.length()-1) {
			JSONObject theGame = (JSONObject) gamesArray.get(gameID);
			theGame.remove(key);
			theGame.put(key, value);
			return true;
		}
		
		// neues Spiel hinzufuegen
		if (gameID > gamesArray.length()-1 && value.getClass().equals(JSONObject.class)) {
			gamesArray.put(value);	
			return true;
		}
		
		// Spiel entfernen
		if (gameID < gamesArray.length()-1 && key == null && value == null) {
			gamesArray.remove(gameID);
			return true;
		}
	
		return false;
		
	}
	
	
	private Object getGameTemplateValue(int gameID, String key) {
		JSONObject theGame = (JSONObject) this.gameTemplates.getJSONArray("games").get(gameID);
		return theGame.get(key);
	}
	

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		this.waitingClients.add(conn);
		System.out.println("New Client: " + handshake.getResourceDescriptor());
		this.printState();
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		if (this.loggedInClients.contains(conn)) {
			this.loggedInClients.remove(conn);
		
		}
		
		if (this.waitingClients.contains(conn)) {
			this.waitingClients.remove(conn);
		
		}
		
		if (this.playingClients.containsKey(conn)) {
			int gameID = this.playingClients.get(conn);
			MdsGame g = this.games.get(gameID);
			if (g.isRunning()) {
				g.exitPlayer(conn);
				g.getInterpreter().onLostConnection(conn);
			} else {
				g.exitPlayer(conn);
			}
			g.notifyLobby();
			
			this.playingClients.remove(conn);
			if(!this.playingClients.containsValue(gameID)) {
				this.games.remove(gameID);
			}
			
			this.notifyLobby();
			
		}
		
		if (this.monitors.containsKey(conn)) {
			int gameID = this.monitors.get(conn);
			this.games.get(gameID).getInterpreter().detachMonitor(conn);
			this.monitors.remove(conn);
			
		}
		
		this.printState();
		
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		try {
			JSONObject mes = new JSONObject(message);
			String mode = (String) mes.get("mode");
			if(this.playingClients.containsKey(conn)) {
				
				int gameID = this.playingClients.get(conn);
				
				if (mode.equals("single") || mode.equals("full")) {
					List<WhiteboardUpdateObject> wObj = WhiteboardHandler.toObject(message);
					if(wObj.size() == 1) {
						this.games.get(gameID).getInterpreter().onWhiteboardUpdate(conn, wObj.get(0).getKeys(), wObj.get(0).getValue());
						//mdsServerInterpreter.onWhiteboardUpdate(conn, wObj.get(0).getKeys(), wObj.get(0).getValue());
							
					} else {
						this.games.get(gameID).getInterpreter().onFullWhiteboardUpdate(conn, wObj);
						//mdsServerInterpreter.onFullWhiteboardUpdate(conn, wObj);
					}
					
				}
				if (mode.equals("gamelobby")) {
					String action = mes.getString("action");
					MdsGame g = this.games.get(gameID);
					if (action.equals("players")) {
						String response = g.getGameState().toString();
						conn.send(response.toString());
					}
					
					if (action.equals("start")) {
						this.startGame(conn, mes);
						this.notifyLobby();
					}
					
					if (action.equals("kick")) {
						WebSocket kicked = g.kickPlayer(conn, mes);
						if (kicked != null) {
							this.notifyLobby();
							g.notifyLobby();
							this.movePlayerToLobby(kicked);
						}
						
					}
					
					if (action.equals("leave")) {
						g.exitPlayer(conn);
						this.movePlayerToLobby(conn);
						
						if(!this.playingClients.containsValue(gameID)) {
							this.games.remove(gameID);
						}
						
						this.notifyLobby();
						g.notifyLobby();
					}
					
				}
			} 
			
			
			if (this.waitingClients.contains(conn)) {
				if (mode.equals("login")) {
					this.loginClient(conn, mes);
				}

			}
			
			if (this.loggedInClients.contains(conn)) {
				if (mode.equals("join")) {
					this.joinGame(conn, mes);
				}
				
				if (mode.equals("create")) {
					this.createGame(conn, mes);
				}
				if (mode.equals("gametemplates")) {
					conn.send(this.gameTemplates.toString());
				}
				
				if (mode.equals("activegames")) {
					JSONObject activeGames = this.getActiveGames();
					conn.send(activeGames.toString());	
				}
			}
			
			if (mode.equals("monitor")) {
				int gameID = (Integer) mes.get("id");
				String name = (String) mes.get("name");
				
				MdsGame g = this.games.get(gameID);
				if (g.isRunning()) {
				
					this.monitors.put(conn, gameID);
					this.games.get(gameID).getInterpreter().attachMonitor(name, conn);
					this.loggedInClients.remove(conn);
				
				} else {
					JSONObject response = new JSONObject();
					response.put("mode", "error");
					response.put("message", "Game " + gameID + " is not running yet.");
					conn.send(response.toString());
				}
				
			}
			
		} catch (JSONException e) {
			System.out.println("Can't create JSONObject from message: '" + message + "'");
			e.printStackTrace();
		}
		
		this.printState();
		
	}
	

	private void loginClient(WebSocket conn, JSONObject mes) {
		// TODO Auto-generated method stub
		
		this.waitingClients.remove(conn);
		this.loggedInClients.add(conn);
		conn.send(this.gameTemplates.toString());
		
		
	}

	private JSONObject getActiveGames() {
		
		JSONObject activeGames = new JSONObject();
		activeGames.put("mode", "activegames");
		JSONArray games = new JSONArray();
		for (MdsGame g : this.games.values()) {
			if (!g.isRunning()) {
				games.put(g.toJSON());
			}
		}
		activeGames.put("games", games);
		return activeGames;
	}
	

	private void notifyLobby() {
		for(WebSocket ws : this.loggedInClients) {
			ws.send(this.getActiveGames().toString());
		}		
	}

	public void onFragment(WebSocket conn, Framedata fragment) {
		//		System.out.println("received fragment: " + fragment);
	}
	
	
	@Override
	public void onError(WebSocket conn, Exception ex) {
		ex.printStackTrace();
		if(conn != null) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}

	@Override
	public boolean onNewConnection(WebSocket conn, String name) {
		
		//return mdsServerInterpreter.onNewConnection(conn, handshake);
		return false;
	}

	@Override
	public void onWhiteboardUpdate(WebSocket conn, List<String> keys, WhiteboardEntry value) {
		System.err.println("onWhitboardUpdate on ComServer");
		
	}
	
	public void sendUpdate(WebSocket conn, List<String> keys, WhiteboardEntry entry) {
		String message = "";
		try {
			message = WhiteboardHandler.toJson(keys, entry);
			conn.send(message);
		} catch (UnknownWhiteboardTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void onFullWhiteboardUpdate(WebSocket conn, List<WhiteboardUpdateObject> wObj) {
		String message = "";
		try {
			message = WhiteboardHandler.toJson(wObj);
		} catch (UnknownWhiteboardTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		conn.send(message);
	}
	
	private File readJSON(String url) {

		InputStream is = null;
		
		try {
			is = new URL(url).openStream();
		} catch (MalformedURLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		// Temporäre Datei anlegen
		File json = null;
		try {
			json = File.createTempFile("App", ".json");
		} catch (IOException e1) {
			e1.printStackTrace();
		}


		try {
			// Inputstream zum einlesen der Json
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			// Json wird zeilenweise eingelesn uns in das File json geschrieben
			FileWriter writer = new FileWriter(json, true);

			String t = "";

			while ((t = br.readLine()) != null) {
				//System.out.println(t);
				writer.write(t);
			}

			writer.flush();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Ueberprüfung, ob es geklappt hat
		if (json.exists()) {
			System.out.println("Reading JSON successful: " + json.length());
		} else {
			System.out.println("Reading JSON failed.");
		}

		return json;

	}
	
	private void printState() {
		System.out.println("\n---------------- STATS ----------------");
		
		System.out.println(this.monitors.size() + " Monitors(s) watching");
		System.out.println(this.loggedInClients.size() + " Player(s) in the main lobby");
		System.out.println(this.playingClients.size() + " Player(s) in games");
		System.out.println(this.games.size() + " Game(s) active\n");
		System.out.println("Version: " + version + "\n\n");
		
	}

	public void movePlayerToLobby(WebSocket ws) {
		this.playingClients.remove(ws);
		this.loggedInClients.add(ws);
		ws.send(this.gameTemplates.toString());
		
		
	}


}