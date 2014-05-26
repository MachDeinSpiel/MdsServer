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

import de.hsbremen.mds.common.communication.EntryHandler;
import de.hsbremen.mds.common.exception.UnknownWhiteboardTypeException;
import de.hsbremen.mds.common.interfaces.ComServerInterface;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.common.whiteboard.WhiteboardUpdateObject;

/**
 * 
 */
public class MdsComServer extends WebSocketServer implements ComServerInterface {
	
	private JSONObject gamesJSON;
	private List<MdsServerInterpreter> mdsInterpreters;
	private List<WebSocket> waitingClients;
	private Map<WebSocket, Integer> games;
		
	
	public MdsComServer(int port, File file) throws UnknownHostException {
		super(new InetSocketAddress(port) );
		this.initInterpreters(file);
		this.waitingClients = new Vector<WebSocket>();
		this.games = new HashMap<WebSocket, Integer>();
		//this.mdsServerInterpreter = new MdsServerInterpreter(this, file);
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
		this.mdsInterpreters = new Vector<MdsServerInterpreter>();
		
		for	(int i = 0; i < gamesArray.length(); i++) {
			JSONObject game = (JSONObject) gamesArray.get(i);
			String url = game.get("serverurl").toString();
			System.out.println("\nNew Interpreter with " + url);
			MdsServerInterpreter mdsSI = new MdsServerInterpreter(this, this.jsonEinlesen(url));
			this.mdsInterpreters.add(mdsSI);	
		}
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
	/*
		boolean isConnected = this.onNewConnection(conn, handshake);
		
		if(!isConnected) {
			conn.close(0);
		}
		*/
		this.waitingClients.add(conn);
		conn.send(this.gamesJSON.toString());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
	
		if(this.games.containsKey(conn)) {
			int gameID = this.games.get(conn);
			List<WhiteboardUpdateObject> wObj = EntryHandler.toObject(message);
			if(wObj.size() == 1) {
				this.mdsInterpreters.get(gameID).onWhiteboardUpdate(conn, wObj.get(0).getKeys(), wObj.get(0).getValue());
				//mdsServerInterpreter.onWhiteboardUpdate(conn, wObj.get(0).getKeys(), wObj.get(0).getValue());
					
			} else {
				this.mdsInterpreters.get(gameID).onFullWhiteboardUpdate(conn, wObj);
				//mdsServerInterpreter.onFullWhiteboardUpdate(conn, wObj);
			}
		} else if (this.waitingClients.contains(conn)) {
			JSONObject mes = new JSONObject(message);
			String mode = mes.getString("mode");
			int id = mes.getInt("id");
			String name = mes.getString("name");
			if (mode.equals("join")){
				this.mdsInterpreters.get(id).onNewConnection(conn, name);
				this.waitingClients.remove(conn);
				int activeplayers = (Integer) this.getGameInfoValue(id, "activeplayers") + 1;
				this.updateGameInfo(id, "activeplayers", activeplayers);
				this.notifyLobby();
			}
			
		}
		//System.out.println(conn + ": " + message);
		
		
	}

	private void notifyLobby() {
		for(WebSocket ws : this.waitingClients) {
			ws.send(this.gamesJSON.toString());
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
		System.err.println("onWhitboardUpdate auf ComServer");
		
	}
	
	public void sendUpdate(WebSocket conn, List<String> keys, WhiteboardEntry entry) {
		String message = "";
		try {
			message = EntryHandler.toJson(keys, entry);
		} catch (UnknownWhiteboardTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		conn.send(message);
	}
	
	public void onFullWhiteboardUpdate(WebSocket conn, List<WhiteboardUpdateObject> wObj) {
		String message = "";
		try {
			message = EntryHandler.toJson(wObj);
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


}