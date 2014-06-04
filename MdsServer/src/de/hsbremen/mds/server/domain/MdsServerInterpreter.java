package de.hsbremen.mds.server.domain;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import org.java_websocket.WebSocket;
import de.hsbremen.mds.common.interfaces.ComServerInterface;
import de.hsbremen.mds.common.whiteboard.InvalidWhiteboardEntryException;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.common.whiteboard.WhiteboardUpdateObject;
import de.hsbremen.mds.server.parser.ParserServerNew;



public class MdsServerInterpreter implements ComServerInterface {
	private Whiteboard whiteboard         = new Whiteboard();
	private Whiteboard playerTemplate     = new Whiteboard();
	private Map<String,WebSocket> clients = new ConcurrentHashMap<String, WebSocket>();	//Websockets Hashmap...
	private Vector<WhiteboardUpdateObject> whiteboardUpdateObjects = new Vector<WhiteboardUpdateObject>();
	private MdsComServer comServer;
	
	public MdsServerInterpreter (MdsComServer mdsComServer, File file) {
		this.comServer = mdsComServer;
		ParserServerNew parServ = new ParserServerNew(file);
		this.whiteboard = parServ.getWB();
		this.savePlayerTemplate();
	}

	@Override
	/**
	 * 
	 */
	public void onWhiteboardUpdate(WebSocket conn, List<String> keys, WhiteboardEntry entry) {
		// Lokales WB aktualisieren
		this.onWhiteboardUpdate(keys, entry);
//		//Whiteboard loeschen
//		if(entry.getValue().equals("delete")){
//			this.removeWhiteboard(conn, keys);
//		}
//		// Allen anderen Clients das Update schicken
		for (Entry<String, WebSocket> mapEntry : this.clients.entrySet()) {
		//	if (!mapEntry.getValue().equals(conn)) {
				this.comServer.sendUpdate(mapEntry.getValue(), keys, entry);
		//	}
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
				Whiteboard playerAtt = new Whiteboard();
				for(Entry<String, WhiteboardEntry> entry : this.playerTemplate.entrySet()) {
					playerAtt.put(entry.getKey(), 
							new WhiteboardEntry(this.playerTemplate.get(entry.getKey()).getValue(), this.playerTemplate.get(entry.getKey()).getVisibility()));
				}
				player = new WhiteboardEntry(playerAtt, "all");

			} catch (InvalidWhiteboardEntryException e) {
				e.printStackTrace();
			}
		
			
			this.clients.put("Players," + playerName, conn);
			
			List<String> keys = new Vector<String>();
			keys.add("Players");
			keys.add(playerName);

			this.onFullWhiteboardUpdate(conn, this.whiteboard, new Vector<String>());
			this.onWhiteboardUpdate(conn, keys, player);
			
			return true;
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
	 * Player Templat speichern und das Template aus den globalen Whiteboard loeschen.
	 */
	private void savePlayerTemplate(){
		this.playerTemplate = (Whiteboard) this.whiteboard.getAttribute("Players", "0").value;
		this.whiteboard.deleteAttribute("Players", "0");
	}
	
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
