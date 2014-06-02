package de.hsbremen.mds.server.domain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.java_websocket.WebSocket;
import de.hsbremen.mds.common.interfaces.ComServerInterface;
import de.hsbremen.mds.common.whiteboard.InvalidWhiteboardEntryException;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.common.whiteboard.WhiteboardUpdateObject;
import de.hsbremen.mds.server.parser.ParserServerNew;

public class MdsServerInterpreter implements ComServerInterface {
	private Whiteboard whiteboard = new Whiteboard();
	private MdsComServer comServer;
	private Vector<WhiteboardUpdateObject> whiteboardUpdateObjects = new Vector<WhiteboardUpdateObject>();
	//Websockets Hashmap...
	private Map<String,WebSocket> clients = new ConcurrentHashMap<String, WebSocket>();

	
	public MdsServerInterpreter (MdsComServer mdsComServer, File file) {
		this.comServer = mdsComServer;
		ParserServerNew parServ = new ParserServerNew(file);
		this.whiteboard = parServ.getWB();
//		String a = null;
//		this.printWhiteboard(a, this.whiteboard);
	}

	@Override
	/**
	 * 
	 */
	public void onWhiteboardUpdate(WebSocket conn, List<String> keys, WhiteboardEntry entry) {
		// Lokales WB aktualisieren
		this.onWhiteboardUpdate(keys, entry);
		//Whiteboard loeschen
		if(entry.getValue().equals("delete")){
			this.removeWhiteboard(conn, keys);
		}
		// Allen anderen Clients das Update schicken
		for (Entry<String, WebSocket> mapEntry : this.clients.entrySet()) {
			if (!mapEntry.getValue().equals(conn)) {
				this.comServer.sendUpdate(mapEntry.getValue(), keys, entry);
			}
		}
		
	}

	
	/**
	 * Client kompletten WB senden
	 * 
	 * 
	 * @param conn - der Client
	 * @param wb - das WB
	 * @param keys - Key zum WB
	 */
	public void onFullWhiteboardUpdate(WebSocket conn, Whiteboard wb, List<String> keys) {
		try {
			makeWhiteboardList(wb, keys);
		} catch (InvalidWhiteboardEntryException e) {
			e.printStackTrace();
		}
		this.comServer.onFullWhiteboardUpdate(conn, whiteboardUpdateObjects);
		//Vectoren leeren
		whiteboardUpdateObjects.clear();	
	}

	/**
	 * 
	 * 
	 * @param wb Whiteboard
	 * @param keys Key List
	 * @throws InvalidWhiteboardEntryException
	 */

	@Override
	public boolean onNewConnection(WebSocket conn, String name) {
		System.out.println("Neuer Player angemeldet");
		String playerName = name;
		
		WhiteboardEntry player = this.whiteboard.getAttribute("Players", playerName);
		
		if (player == null) {

			try {
				player = new WhiteboardEntry(playerName, "all");

				/* ---- parsen des player templates ---- */
				JSONParser parser = new JSONParser();

				try {
					Object obj = parser.parse(new FileReader("https://github.com/MachDeinSpiel/MdsJsons/blob/master/BombDefuser_Server.json"));

					JSONObject jsonObject = (JSONObject) obj;
					
					/* ---- aus der JSON datei lesen ---- */
					int health; 
					String latitude, longitude;
					
					JSONArray players_array = (JSONArray) jsonObject.get("Players"); //get all Players
					
					// DONE: Hier mal die Struktur fest gecoded, muss noch dynamisch aus der JSON ausgelesen werden
					for(int i = 0; i < players_array.size(); i++) {
						JSONObject the_player = (JSONObject) players_array.get(i);

						// attribute werden aus dem JSONObject gelesen
						health = Integer.parseInt(the_player.get("health").toString());
						latitude = (String) the_player.get("latitude");
						longitude = (String) the_player.get("longitude");
						
						// das eingelesenen Attribute werden ins whiteboard gepackt
						Whiteboard playerAtt = new Whiteboard();
						playerAtt.put("health", new WhiteboardEntry(health, "none"));
						playerAtt.put("latitude", new WhiteboardEntry(latitude, "none"));
						playerAtt.put("longitude", new WhiteboardEntry(longitude, "none"));	
						
						// das Attribut-Whiteboard wird zum Player-Whiteboard hinzugefügt
						player = new WhiteboardEntry(playerAtt, "all");
					}
				//try{} end
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
		
			
			this.clients.put("Players," + playerName, conn);
			
			List<String> keys = new Vector<String>();
			keys.add("Players");
			keys.add(playerName);

			this.onFullWhiteboardUpdate(conn, this.whiteboard, new Vector<String>());
			this.onWhiteboardUpdate(conn, keys, player);
			
			return true;
			
		}catch(Exception e){
			
		}
		
		}
		return false;
		
	}	
	/**
	 * 
	 * 
	 * @param conn WebSocket
	 */
	public void onLostConnection(WebSocket conn) {
		System.out.println("HALLLOOO");
		String name         = null;
		WhiteboardEntry wbe = null;
		List<String> keys   = new Vector<String>();
		keys.add("Players");
		
		for(Map.Entry<String, WebSocket> entry : clients.entrySet()) {
			if(entry.getValue().equals(conn)){
				name = entry.getKey();
				keys.add(name);
				clients.remove(entry.getKey());
				
				try {
					wbe = new WhiteboardEntry("delete", "");
				} catch (InvalidWhiteboardEntryException e) {
					e.printStackTrace();
				}
				this.onWhiteboardUpdate(conn, keys, wbe);
			}
		}	
	}
	
	/**
	 * 
	 * Test Methoden
	 * 
	 */
	public void printWhiteboard(String keyPath, Whiteboard wb){
		List<String> a = new Vector<String>();
		a.add("null");
		a.add("Players");
		a.add("0");
		for(String key : wb.keySet()){
			if(!(wb.getAttribute(key).value instanceof String)){
				printWhiteboard(keyPath+","+key, (Whiteboard) wb.getAttribute(key).value);
			}else{
				System.out.println(keyPath+","+key+ ":"+ wb.getAttribute(key).value.toString());
			}
		}
		System.out.println("DELEEEEETTTEEEEEEEEEEEEEEEEEEE");
		this.removeWhiteboard(clients.get("1"), a);
		printWhiteboard(keyPath, wb);
	}


	
	/**
	 * 
	 * Private Methoden
	 * 
	 */

	/**
	 * Locales Whiteboard Update
	 * 
	 * @param keys
	 * @param value
	 */
	private void onWhiteboardUpdate(List<String> keys, WhiteboardEntry value) {
		String [] key = this.getStringArrayPath(keys);
		this.whiteboard.setAttribute(value, key);
	}
	
	/**
	 * 
	 * 
	 * @param wb Whiteboard
	 * @param keys List<String>
	 * @throws InvalidWhiteboardEntryException
	 */
	private void makeWhiteboardList(Whiteboard wb, List<String> keys) throws InvalidWhiteboardEntryException{
		for(String key : wb.keySet()){
			if(!(wb.getAttribute(key).value instanceof String)){
				Vector<String> keyList = new Vector<String>(keys);
				keyList.add(key);
				makeWhiteboardList((Whiteboard) wb.getAttribute(key).value, keyList);
			}else{
				Vector<String> keyList = new Vector<String>(keys);
				keyList.add(key);
				WhiteboardEntry wbe = new WhiteboardEntry(wb.getAttribute(key).value, wb.getAttribute(key).visibility);
				WhiteboardUpdateObject wbuo = new WhiteboardUpdateObject(keyList, wbe);
				whiteboardUpdateObjects.add(wbuo);
			}
		}
	}

	
	/**
	 * Loescht einen gewueschtes Whiteboard.
	 * Z.b Player hat das Spiel verlassen, sein Player Whiteboard wird
	 * aus den Players Whiteboard geloescht.
	 * 
	 * @param keys List<String>
	 */
	private void removeWhiteboard(WebSocket conn, List<String> keys){
		String key = keys.get(keys.size() - 1);
		keys.remove(keys.size() - 1);
		String [] path  = this.getStringArrayPath(keys);
		//remove whiteboard
		WhiteboardEntry ent = whiteboard.getAttribute(path);
		Whiteboard wb   = (Whiteboard) ent.getValue();
		wb.remove(key);	
//		//update 
//		keys.add(key);
//		WhiteboardEntry entry = null;
//		try {
//			entry = new WhiteboardEntry("delete", "");
//		} catch (InvalidWhiteboardEntryException e) {
//			e.printStackTrace();
//		}
//		this.onWhiteboardUpdate(conn, keys, entry);
	}	

	/**
	* Gibt einen String Array zurueck.
	* 
	* Im String[] ist der Path fuer das gewueschte Whiteboard.
	* 
	* 
	* @param keys List<String>
	* @return String[]
	*/
	private String[] getStringArrayPath(List<String> keys){
		String[] key = new String[keys.size()];
		return key = keys.toArray(key);
	}
	
	/**
	 * 
	 * Methoden werden nicht benutzt 
	 * 
	 */

	@Override
	public void onFullWhiteboardUpdate(WebSocket conn, List<WhiteboardUpdateObject> wb) {
		// TODO Auto-generated method stub
		
	}	
}
