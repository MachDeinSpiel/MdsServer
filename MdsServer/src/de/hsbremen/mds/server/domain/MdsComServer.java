package de.hsbremen.mds.server.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.NotYetConnectedException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import de.hsbremen.mds.server.persistence.MdsFileWriter;
import de.hsbremen.mds.server.valueobjects.MdsGame;
import de.hsbremen.mds.server.valueobjects.MdsPVPGame;
import de.hsbremen.mds.server.valueobjects.MdsPlayer;
import de.hsbremen.mds.server.valueobjects.MdsTeamGame;

/**
 * 
 */
public class MdsComServer extends WebSocketServer implements ComServerInterface {
	
	private static final String version = "MdsComServer 14.7.1 (devTeam)";
	private JSONObject gameTemplates;
	private List<WebSocket> loggedInClients;
	private List<WebSocket> waitingClients;
	private Map<WebSocket, Integer> monitors;
	private Map<WebSocket, Integer> playingClients;
	private Map<Integer, MdsGame> games;
	private Map<WebSocket, String> playerNames;
	private Connection dbConnection;
	private MdsFileWriter fileWriter;
	private boolean isLoginActivated = true;
	private final String sessionToken = new SessionIdentifierGenerator().nextSessionId();
		
	
	public MdsComServer(int port, String configURL, boolean userauth) throws UnknownHostException {
		super(new InetSocketAddress(port));
		System.out.println(this.version);
		this.isLoginActivated = userauth;
		this.initServer(configURL);
	}
	
	private void initServer(String url) {
		
		this.waitingClients = new Vector<WebSocket>();
		this.loggedInClients = new Vector<WebSocket>();
		this.playingClients = new HashMap<WebSocket, Integer>();
		this.monitors = new HashMap<WebSocket, Integer>();
		this.games = new HashMap<Integer, MdsGame>();
		this.playerNames = new HashMap<WebSocket, String>();
		this.fileWriter = new MdsFileWriter();
		
		File file = this.readJSON(url);
		JSONObject json = null;
		JSONParser jP = new JSONParser();
		try {
			json = new JSONObject(jP.parse(new FileReader(file)).toString());
		} catch (JSONException | IOException | ParseException e) {
			e.printStackTrace();
		}
		
		this.initGames(json);
		this.testDbConnection();

	}

	private void initGames(JSONObject json) {
		
		this.gameTemplates = json;
		
		if (!this.games.isEmpty()) {
			
			List<MdsGame> openGames = new Vector<MdsGame>();
			
			for (Entry<Integer, MdsGame> g : this.games.entrySet()) {
				List<WebSocket> playersFormCanceledGames = null;
				MdsGame game = g.getValue();
				if (!game.isRunning()) {
					playersFormCanceledGames = game.terminate();
					openGames.add(game);
				}
				if (!playersFormCanceledGames.isEmpty()) {
					for (WebSocket ws : playersFormCanceledGames) {
						this.movePlayerToLobby(ws);
					}
				}
					
			}
			
			for (MdsGame g : openGames) {
				this.games.remove(g);
			}
			
		}
		
		this.notifyLobbyActiveGames();
		
		
	}

	private void testDbConnection() {
		
		//System.out.println("Checking DB connection ... ");
		
		//System.out.println("Session Token: " + this.sessionToken);
		
		if (!this.isLoginActivated) {
			System.err.println("## WARNING: USER-AUTHENTICATION DISABLED! ##");
		}
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			System.out.println("JDBC Driver not found!");
			e1.printStackTrace();
		}
		this.dbConnection = null;
	 
		try {
			dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306","mds_accounts", "mdsistfett");
	 
		} catch (SQLException e) {
			System.err.println("DB connection test failed!");
			return;
		}
	 
		if (dbConnection != null) {
			System.out.println("DB connection OK.");
			try {
				this.dbConnection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.err.println("DB connection test failed!");
		}
		
		
		
	}
	
	private boolean connectDB() throws SQLException {
				
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			System.out.println("JDBC Driver not found!");
			e1.printStackTrace();
		}
		this.dbConnection = null;
		this.dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306","mds_accounts", "mdsistfett");
	 
		if (this.dbConnection == null) {
			System.out.println("Unable to establish DB connection.");
			return false;
		}
		
		return true;
	}

	public MdsComServer(InetSocketAddress address) {
		super(address);
	}
	
	private synchronized boolean createGame(WebSocket conn, JSONObject mess) {
		
		int gameTemplateId = mess.getInt("id");
		boolean isTeamGame = (Boolean) this.getGameTemplateValue(gameTemplateId, "isteamgame");
		String playerName = mess.getString("name");
		String teamName = null;
		JSONArray teamNames = null;
		if (isTeamGame) {
			if (mess.has("teamname")) {
				teamName = mess.getString("teamname");
			}
			teamNames = (JSONArray) this.getGameTemplateValue(gameTemplateId, "teamnames");
		}
		
		//int maxp = mess.getInt("maxplayers");
		
		
		MdsPlayer p = new MdsPlayer(conn, playerName, true);
		
		String surl = (String) this.getGameTemplateValue(gameTemplateId, "serverurl");
		String curl = (String) this.getGameTemplateValue(gameTemplateId, "clienturl");
		String name = (String) this.getGameTemplateValue(gameTemplateId, "name");
		String author = (String) this.getGameTemplateValue(gameTemplateId, "author");
		String appTheme = (String) this.getGameTemplateValue(gameTemplateId, "apptheme");
		double version = (Double) this.getGameTemplateValue(gameTemplateId, "version");
		int maxp = (Integer) this.getGameTemplateValue(gameTemplateId, "maxplayers");
		int minp = (Integer) this.getGameTemplateValue(gameTemplateId, "minplayers");
		int numberOfTeams = (Integer) this.getGameTemplateValue(gameTemplateId, "teams");
		int gameID = 0;
		while(this.games.containsKey(gameID)) {
			gameID++;
		}
		
		MdsGame g = null;
		
		if (numberOfTeams >= 2 && isTeamGame) {
			g = new MdsTeamGame(gameID, gameTemplateId, maxp, numberOfTeams);
			((MdsTeamGame) g).createTeams(teamNames);
			if (teamName != null) {
				boolean success = ((MdsTeamGame) g).putPlayerIntoTeam(p, teamName);
				if (!success) {
					this.sendError(conn, "You can't join Team '" + teamName + "'");
					return false;
				}
			} else {
				g.putPlayer(p);
			}
		} else {
			g = new MdsPVPGame(gameID, gameTemplateId, maxp);
			g.putPlayer(p);
		}
		
		g.setName(name);
		g.setAuthor(author);
		g.setVersion(version);
		g.setClientURL(curl);
		g.setServerURL(surl);
		g.setAppTheme(appTheme);
		this.playingClients.put(conn, gameID);
		this.loggedInClients.remove(conn);
		this.games.put(gameID, g);
		this.notifyLobbyActiveGames();
		g.notifyLobby();
		return true;
		
	}
	
	private synchronized boolean joinGame(WebSocket conn, JSONObject mess) {
		int gameID = mess.getInt("id");
		String name = mess.getString("name");
		boolean isTeamJoinRequest = mess.has("teamname");
		if (this.games.containsKey(gameID)) {
			MdsGame g = this.games.get(gameID);
			if (!g.isRunning() && (g.getMaxPlayers() > g.getPlayerCount() )) {
				MdsPlayer p = new MdsPlayer(conn, name, false);
				if (g instanceof MdsTeamGame && isTeamJoinRequest) {
					String teamname = mess.getString("teamname");
					boolean success = ((MdsTeamGame)g).putPlayerIntoTeam(p, teamname);
					if (!success) {
						this.sendError(conn, "Team '" + teamname +  "' doesn't exist");
						return false;
					}
				} else {
					g.putPlayer(p);
				}
				this.playingClients.put(conn, gameID);
				this.loggedInClients.remove(conn);
				this.notifyLobbyActiveGames();
				g.notifyLobby();
			} else {
				this.sendError(conn, "This game is already running.");
				return false;
			}
		} else {
			this.sendError(conn, "There is no game with ID " + gameID);
			return false;
		}
		return true;
			
	}
	
	private boolean startGame(WebSocket conn, JSONObject mess) {
		int gameId = this.playingClients.get(conn);
		MdsGame g = this.games.get(gameId);
		
		int minPlayers = g.getMinPlayers();
		int lobbyPlayers = g.getPlayerCount();
		
		if (lobbyPlayers >= minPlayers) {
			String url = g.getServerURL();
			File file = this.readJSON(url); 
			
			boolean isGameStarted = g.startGame(conn, this, file);
			
			if (isGameStarted) {
				System.out.println("\nStarting new game with " + url);
				return true;
			} else {
				return false;
			}
		
		} else {
			this.sendError(conn, "You need more players to start this game.");
		}
		return false;
		
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
			this.playerNames.remove(conn);
		
		}
		
		if (this.waitingClients.contains(conn)) {
			this.waitingClients.remove(conn);
			this.playerNames.remove(conn);
		
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
			this.playerNames.remove(conn);
			
			int activePlayers = g.getPlayerCount();
			
			if(activePlayers < 1) {
				this.games.remove(gameID);
			}
			
			this.notifyLobbyActiveGames();
			
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
		this.printState();
		try {
			JSONObject mes = new JSONObject(message);
			String mode = (String) mes.get("mode");
			
			if (mode.equals("gametemplates")) {
				conn.send(this.gameTemplates.toString());
				return;
			}
			
			if (mode.equals("activegames")) {
				JSONObject activeGames = this.getActiveGames();
				conn.send(activeGames.toString());	
				return;
			}
			
			if (mode.equals("runninggames")) {
				JSONObject activeGames = this.getRunningGames();
				conn.send(activeGames.toString());	
				return;
			}
			
			if (mode.equals("allgames")) {
				JSONObject activeGames = this.getGames();
				conn.send(activeGames.toString());	
				return;
			}
			
			if (this.waitingClients.contains(conn)) {
				if (mode.equals("login")) {
					this.loginClient(conn, mes);
					return;
				}
			}
			
			if (mode.equals("config")) {
				this.updateGameTemplates(conn, mes);
			}
			
			if (this.loggedInClients.contains(conn)) {
				if (mode.equals("join")) {
					this.joinGame(conn, mes);
					return;
				}
				
				if (mode.equals("create")) {
					this.createGame(conn, mes);
					return;
				}
				
				if (mode.equals("sessiontoken")) {
					this.sendSessionToken(conn);
					return;
				}
				
				if (mode.equals("reloadgames")) {
					this.updateGameTemplates(conn, mes);
					return;
				}

			}
			
			if (mode.equals("monitor")) {
				System.out.println("mode: Monitor");
				int gameID = (Integer) mes.get("id");
				String name = (String) mes.get("name");
				
				if (this.games.isEmpty()) {
					this.sendError(conn, "There are no games on this server.");
					return;
				}
				
				MdsGame g = this.games.get(gameID);
				
				if (g.isRunning()) {
					System.out.println("Attaching Monitor");
					this.monitors.put(conn, gameID);
					this.games.get(gameID).getInterpreter().attachMonitor(name, conn);
					this.loggedInClients.remove(conn);
					return;
				
				} else {
					JSONObject response = new JSONObject();
					response.put("mode", "error");
					response.put("message", "Game " + gameID + " is not running yet.");
					conn.send(response.toString());
					return;
				}
				
			}
			
			
			if(this.playingClients.containsKey(conn)) {
				
				int gameID = this.playingClients.get(conn);
				
				if (mode.equals("single") || mode.equals("full")) {
					List<WhiteboardUpdateObject> wObj = WhiteboardHandler.toObject(message);
					if(wObj.size() == 1) {
						this.games.get(gameID).getInterpreter().onWhiteboardUpdate(conn, wObj.get(0).getKeys(), wObj.get(0).getValue());
						return;
							
					} else {
						this.games.get(gameID).getInterpreter().onFullWhiteboardUpdate(conn, wObj);
						return;
					}
					
				}
				if (mode.equals("gamelobby")) {
					String action = mes.getString("action");
					MdsGame g = this.games.get(gameID);
					if (action.equals("players")) {
						String response = g.getGameState().toString();
						conn.send(response.toString());
						return;
					}
					
					if (action.equals("start")) {
						if (this.startGame(conn, mes)) {
							this.notifyLobbyActiveGames();
						}
						return;
					}
					
					if (action.equals("changeteam")) {
						if (g instanceof MdsTeamGame) {
							MdsPlayer p = g.getPlayer(conn);
							((MdsTeamGame) g).changeTeam(p, mes.getString("team"));
						}
						return;
					}
					
					if (action.equals("kick")) {
						WebSocket kicked = g.kickPlayer(conn, mes);
						if (kicked != null) {
							
							int activePlayers = g.getPlayerCount();
							
							if(activePlayers < 1) {
								this.games.remove(gameID);
							}							
							this.notifyLobbyActiveGames();
							g.notifyLobby();
							this.movePlayerToLobby(kicked);							
							return;
						}
						
					}
					
					if (action.equals("leave")) {
						g.exitPlayer(conn);
						int activePlayers = g.getPlayerCount();
						
						if(activePlayers < 1) {
							this.games.remove(gameID);
						}
						
						this.movePlayerToLobby(conn);
						this.notifyLobbyActiveGames();
						g.notifyLobby();
						return;
					}
					
				}
			} 
			
		} catch (JSONException e) {
			System.err.println("Can't create JSONObject from message: '" + message + "'");
			this.sendError(conn, "JSON message corrupted or parameters missing");
			System.err.println(e.getMessage());
		}
		
		
		
	}

	private void updateGameTemplates(WebSocket conn, JSONObject mes) {
		
		if (!mes.has("username")) {
			this.sendError(conn, "Please provide a username!");
			return;
		}
		if (!mes.has("password")) {
			this.sendError(conn, "Please provide a password!");
			return;
		}
		if (!mes.has("config")) {
			this.sendError(conn, "Configuration missing.");
			return;
		}
		
		String username = mes.getString("username");
		String password = mes.getString("password");
		
		try {
			if (this.checkUserLogin(username, password)) {
				JSONObject newGame = mes.getJSONObject("config");
				JSONArray theGames = this.gameTemplates.getJSONArray("games");
				JSONArray newGames = new JSONArray();
				
				int i = 0;
				for (;i < theGames.length(); i++) {
					JSONObject oneGame = theGames.getJSONObject(i);
					oneGame.remove("id");
					oneGame.put("id", i);
					newGames.put(oneGame);
				}
				if (newGame.has("id")) {
					newGame.remove("id");
				}
				newGame.put("id", i);
				newGames.put(newGame);
				
				this.gameTemplates.remove("games");
				this.gameTemplates.put("games", newGames);
				
				JSONObject response = new JSONObject();
				response.put("mode", "success");
				conn.send(response.toString());
				this.notifyLobbyGameTemplates();
				
			} else {
				this.sendError(conn, "Login credentials incorrect.");
			}
		} catch (NotYetConnectedException e) {
			System.err.println("Unable to establish DB connection.");
			this.sendError(conn, "Unable to establish DB connection.");
			e.printStackTrace();
		} catch (JSONException e) {
			this.sendError(conn, "JSON message: corrupted or parameter missing.");
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println("Unable to establish DB connection.");
			this.sendError(conn, "Unable to establish DB connection.");
			e.printStackTrace();
		}
		
	}

	private void sendSessionToken(WebSocket conn) {
		try {
			byte[] bytesOfToken = this.sessionToken.getBytes("UTF-8");
	
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest(bytesOfToken);
			BigInteger hash = new BigInteger(1,thedigest);
			
			JSONObject response = new JSONObject();
			response.put("token", hash.toString());
			conn.send(response.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	private void loginClient(WebSocket conn, JSONObject mes) {
		
		try {
			
			String username = mes.getString("username");
			String password = mes.getString("password");
			
			for (Entry<WebSocket, String> playerName : this.playerNames.entrySet()) {
				if (playerName.getValue().equals(username)) {
					this.sendError(conn, "User '" + username + "' already logged in.");
					return;
				}
			}
			
			if(this.checkUserLogin(username, password)) {
				this.waitingClients.remove(conn);
				this.loggedInClients.add(conn);
				this.playerNames.put(conn, mes.getString("username"));
				conn.send(this.gameTemplates.toString());
			} else {
				this.sendError(conn, "Login credentials incorrect.");
			}
		} catch (NotYetConnectedException e) {
			System.err.println("Unable to establish DB connection.");
			this.sendError(conn, "Unable to establish DB connection.");
			e.printStackTrace();
		} catch (JSONException e) {
			this.sendError(conn, "JSON message: corrupted or parameter missing.");
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println("Unable to establish DB connection.");
			this.sendError(conn, "Unable to establish DB connection.");
			e.printStackTrace();
		}
			
	}
	
	private void sendError(WebSocket conn, String message) {
		JSONObject error = new JSONObject();
		error.put("mode", "error");
		error.put("message", message);
		conn.send(error.toString());
		
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
	
	private JSONObject getRunningGames() {
		
		JSONObject runningGames = new JSONObject();
		runningGames.put("mode", "runninggames");
		JSONArray games = new JSONArray();
		for (MdsGame g : this.games.values()) {
			if (g.isRunning()) {
				games.put(g.toJSON());
			}
		}
		runningGames.put("games", games);
		return runningGames;
	}
	

	private JSONObject getGames() {
		JSONObject runningGames = new JSONObject();
		runningGames.put("mode", "allgames");
		JSONArray games = new JSONArray();
		for (MdsGame g : this.games.values()) {
			games.put(g.toJSON());
		}
		runningGames.put("games", games);
		return runningGames;
	}

	private void notifyLobbyActiveGames() {
		String response = this.getActiveGames().toString();
		for(WebSocket ws : this.loggedInClients) {
			ws.send(response);
		}
		
		if (!this.monitors.isEmpty()) {
			for(Entry<WebSocket, Integer> ws : this.monitors.entrySet()) {
				ws.getKey().send(response);
			}
		}
	}
	
	private void notifyLobbyGameTemplates() {
		String response = this.gameTemplates.toString();
		for(WebSocket ws : this.loggedInClients) {
			ws.send(response);
		}
		
		if (!this.monitors.isEmpty()) {
			for(Entry<WebSocket, Integer> ws : this.monitors.entrySet()) {
				ws.getKey().send(response);
			}
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
	public boolean onNewConnection(WebSocket conn, String name, String teamName) {
		
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
			System.out.println("\nReading URL successful: 		" + json.length() + " Bytes");
		} else {
			System.out.println("\nReading URL failed.");
		}
		
		try {
			JSONParser jp = new JSONParser();
			jp.parse(new FileReader(json));
		} catch (ParseException | IOException e) {
			System.err.println("\nError: '"+ url + "' contains no valid JSON data.\n\nServer start aborted.");
			System.exit(0);
		}

		return json;

	}
	
	private void printState() {
		System.out.println("\n---------------- STATS ----------------");
		
		System.out.println(this.monitors.size() + " Monitors(s) watching");
		System.out.println(this.loggedInClients.size() + " Player(s) in the main lobby");
		System.out.println(this.playingClients.size() + " Player(s) in games");
		System.out.println(this.games.size() + " Game(s) active\n");
		System.out.println(version + "\n\n");
		
	}

	public void movePlayerToLobby(WebSocket ws) {
		this.playingClients.remove(ws);
		this.loggedInClients.add(ws);
		ws.send(this.gameTemplates.toString());
		
		
	}
	
	private boolean checkUserLogin(String username, String password) throws SQLException {
		
		if (this.isLoginActivated) {
			if (this.connectDB()) {
				Statement stmt = dbConnection.createStatement(); 
				ResultSet rs = stmt.executeQuery( "SELECT password FROM mds_accounts.users WHERE username LIKE '" + username + "' LIMIT 1;");
				while ( rs.next() ) {
					int numColumns = rs.getMetaData().getColumnCount();
					for ( int i = 1 ; i <= numColumns ; i++ ) {
						if(password.equals(rs.getObject(i))){
							this.dbConnection.close();
							return true;
					    }
					}
				}
			}
			this.dbConnection.close();	
			return false;
		} else {
			System.err.println("WARNING: USER-AUTHENTICATION DISABLED - PASSWORD UNVERIFIED!");
			return true;
		}
		
	}
	
	public final class SessionIdentifierGenerator {
		  private SecureRandom random = new SecureRandom();

		  public String nextSessionId() {
		    return new BigInteger(130, random).toString(32);
		  }
	}

	public void shutdown() {
		for (WebSocket conn : this.connections()) {
			this.sendError(conn, "Server disconnected.");
		}
		
	}
	
	
}