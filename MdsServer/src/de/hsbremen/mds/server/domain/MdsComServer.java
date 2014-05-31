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
import java.util.concurrent.ConcurrentHashMap;

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

/**
 * 
 */
public class MdsComServer extends WebSocketServer implements ComServerInterface {
	
	private JSONObject gamesJSON;
	private Map<Integer, MdsServerInterpreter> mdsInterpreters;
	private List<WebSocket> waitingClients;
	private Map<WebSocket, Integer> playingClients;
		
	
	public MdsComServer(int port, File file) throws UnknownHostException {
		super(new InetSocketAddress(port));
		this.initInterpreters(file);
		this.waitingClients = new Vector<WebSocket>();
		this.playingClients = new HashMap<WebSocket, Integer>();
	}

	public MdsComServer(InetSocketAddress address) {
		super(address);
	}
	
	private void initInterpreters(File file) {
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
		
		this.gamesJSON = json;
		
		JSONArray gamesArray = (JSONArray) this.gamesJSON.get("games");
		this.mdsInterpreters = new ConcurrentHashMap<Integer, MdsServerInterpreter>();
		/*
		for	(int i = 0; i < gamesArray.length(); i++) {
			JSONObject game = (JSONObject) gamesArray.get(i);
			String url = game.get("serverurl").toString();
			System.out.println("\nNew Interpreter with " + url);
			MdsServerInterpreter mdsSI = new MdsServerInterpreter(this, this.jsonEinlesen(url));
			this.mdsInterpreters.add(mdsSI);	
		}
		*/
	}
	
	private MdsServerInterpreter createInterpreterFromURL(String url) {
		System.out.println("\nNew Interpreter with " + url);
		return new MdsServerInterpreter(this, this.jsonEinlesen(url));
		
	}
	
	
	private void updateGameInfo(int gameID, String key, Object value){
		JSONObject theGame = (JSONObject) this.gamesJSON.getJSONArray("games").get(gameID);
		/*Object theValue = theGame.get(key);
		if (theValue.getClass() == String.class) {
			theValue = (String)theValue + (String)value;
		} else if (theValue.getClass() == Integer.class) {
			theValue = (Integer)theValue + (Integer)value;
		} else if (theValue.getClass() == Float.class) {
			theValue = (Float)theValue + (Float)value;
		}*/
		theGame.remove(key);
		theGame.put(key, value);
	}
	
	private Object getGameInfoValue(int gameID, String key) {
		JSONObject theGame = (JSONObject) this.gamesJSON.getJSONArray("games").get(gameID);
		return theGame.get(key);
		
	}
	

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		/*this.mdsServerInterpreter.addNewClient(conn);
		clients.put(idcount++, conn);
		this.sendToAll("new connection: " + handshake.getResourceDescriptor() );
		System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
		*/

		this.waitingClients.add(conn);
		conn.send(this.gamesJSON.toString());
		
		System.out.println(handshake.getResourceDescriptor());
		this.printState();
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		if (this.waitingClients.contains(conn)) {
			this.waitingClients.remove(conn);
		
		}
		
		if (this.playingClients.containsKey(conn)) {
			int gameID = this.playingClients.get(conn);
			this.mdsInterpreters.get(gameID).onLostConnection(conn);
			int activeplayers = (Integer) this.getGameInfoValue(gameID, "activeplayers") - 1;
			this.updateGameInfo(gameID, "activeplayers", activeplayers);
			this.notifyLobby();
			this.playingClients.remove(conn);
			if(!this.playingClients.containsValue(gameID)) {
				this.mdsInterpreters.remove(gameID);
			}
			
		}
		
		this.printState();
		
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		try {
			JSONObject mes = new JSONObject(message);
			if(this.playingClients.containsKey(conn)) {
				int gameID = this.playingClients.get(conn);
				List<WhiteboardUpdateObject> wObj = WhiteboardHandler.toObject(message);
				if(wObj.size() == 1) {
					this.mdsInterpreters.get(gameID).onWhiteboardUpdate(conn, wObj.get(0).getKeys(), wObj.get(0).getValue());
					//mdsServerInterpreter.onWhiteboardUpdate(conn, wObj.get(0).getKeys(), wObj.get(0).getValue());
						
				} else {
					this.mdsInterpreters.get(gameID).onFullWhiteboardUpdate(conn, wObj);
					//mdsServerInterpreter.onFullWhiteboardUpdate(conn, wObj);
				}
			} else if (this.waitingClients.contains(conn) && (mes.get("mode").equals("join") || mes.get("mode").equals("create") )) {
				// Mode join oder create
				assignInterpreter(conn, mes);
			}
			//System.out.println(conn + ": " + message);
			
			
			if (mes.get("mode").equals("games")) {
				conn.send(this.gamesJSON.toString());
			}
			
		} catch (JSONException e) {
			System.out.println("Can't create JSONObject from message: '" + message + "'");
			e.printStackTrace();
		}
		
		this.printState();
		
	}

	private void assignInterpreter(WebSocket conn, JSONObject mes) {
		String mode = mes.getString("mode");
		int id = mes.getInt("id");
		String name = mes.getString("name");
		JSONArray gamesArray = (JSONArray) this.gamesJSON.get("games");
		
		if (mode.equals("join")){
			
			if (this.mdsInterpreters.containsKey(id)) {
				this.mdsInterpreters.get(id).onNewConnection(conn, name);
				this.waitingClients.remove(conn);
				this.playingClients.put(conn, id);
				int activeplayers = (Integer) this.getGameInfoValue(id, "activeplayers") + 1;
				this.updateGameInfo(id, "activeplayers", activeplayers);
			}			
			
		}
		
		if (mode.equals("create") || (mode.equals("join") && !this.mdsInterpreters.containsKey(id))){
			
			String url = (String) this.getGameInfoValue(id, "serverurl");
			MdsServerInterpreter mdsSI = this.createInterpreterFromURL(url);
			mdsSI.onNewConnection(conn, name);
			if(mode.equals("create")) {
				JSONObject interpreterInfo = new JSONObject(((JSONObject) gamesArray.get(id)).toString());
				id = gamesArray.length();
				interpreterInfo.remove("id");
				interpreterInfo.put("id", id);
				interpreterInfo.remove("activeplayers");
				interpreterInfo.put("activeplayers", 1);
				gamesArray.put(interpreterInfo);
				this.gamesJSON.remove("games");
				this.gamesJSON.put("games", gamesArray);
			} else {
				int activePlayers = (Integer) this.getGameInfoValue(id, "activeplayers") + 1;
				this.updateGameInfo(id, "activeplayers", activePlayers);
			}
						
			this.mdsInterpreters.put(id, mdsSI);
			this.waitingClients.remove(conn);
			this.playingClients.put(conn, id);
			
		}
		
		this.notifyLobby();
	}

	private void notifyLobby() {
		for(WebSocket ws : this.waitingClients) {
			ws.send(this.gamesJSON.toString());
		}
		System.out.println("Lobby Update");
		
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
		System.err.println("onWhitboardUpdate auf ComServer");
		
	}
	
	public void sendUpdate(WebSocket conn, List<String> keys, WhiteboardEntry entry) {
		String message = "";
		try {
			message = WhiteboardHandler.toJson(keys, entry);
		} catch (UnknownWhiteboardTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		conn.send(message);
	}
	
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
	
	private File jsonEinlesen(String url) {

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
			System.out.println("JSON Einlesen erfolgreich.");
			System.out.println(json.length());
		} else {
			System.out.println("JSON Einlesen fehlgeschlagen");
		}

		return json;

	}
	
	private void printState() {
		System.out.println("\n---------------- STATS ----------------");
		System.out.println(this.playingClients.size() + " Player(s) playing");
		System.out.println(this.waitingClients.size() + " Player(s) in the Lobby");
		System.out.println(this.mdsInterpreters.size() + " Interpreter(s) currently running\n");
		
	}


}