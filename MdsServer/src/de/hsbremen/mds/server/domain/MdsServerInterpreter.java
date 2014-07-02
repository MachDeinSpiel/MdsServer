package de.hsbremen.mds.server.domain;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
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
	private int maxPlayer;
	
	public MdsServerInterpreter (MdsComServer mdsComServer, File file, int maxPlayer_) {
		this.comServer  = mdsComServer;
		ParserServerNew parServ = new ParserServerNew(file);
		this.whiteboard = parServ.getWB();
		this.savePlayerTemplate();
		this.maxPlayer = maxPlayer_;
	}

	@Override
	public void onWhiteboardUpdate(WebSocket conn, List<String> keys, WhiteboardEntry entry) {
		if(entry.getValue().equals("delete")){//Whiteboard loeschen
			this.removeWhiteboard(conn, keys);
			this.sendUpdate(conn, keys, entry);
		}else{
			this.onWhiteboardUpdate(keys, entry);// Lokales WB aktualisieren
			if(entry.getValue() instanceof Whiteboard){
				try {
					makeWhiteboardList((Whiteboard) entry.getValue(), keys);
					// Allen anderen Clients das Update schicken
					for (Entry<String, WebSocket> mapEntry : this.clients.entrySet()) {
						if (!mapEntry.getValue().equals(conn)) {
							for(Iterator<WhiteboardUpdateObject> iter = this.whiteboardUpdateObjects.iterator(); iter.hasNext();){
								WhiteboardUpdateObject wbupdateObj = iter.next();
								WhiteboardEntry wbentry = wbupdateObj.getValue();
								List<String> path = wbupdateObj.getKeys();
								this.comServer.sendUpdate(mapEntry.getValue(), path, wbentry);
							}
						}	
					}
					this.whiteboardUpdateObjects.clear();
				} catch (InvalidWhiteboardEntryException e) {
					e.printStackTrace();
				}
			}else{
				//Verschickt nur ein WhiteboardEntry. In dem z.b die Positionen gaendert wurden.
				try {
					whiteboard.setAttributeValue(entry.getValue(), this.getStringArrayPath(keys));
					this.sendUpdate(conn, keys, entry);
				} catch (InvalidWhiteboardEntryException e) {
					e.printStackTrace();
				}
			}
		}
	}

	
	/**
	 * Das Komplette Whiteboard wird gesendet. 
	 * 
	 * 
	 * @param conn WebSocket
	 * @param wb   Whiteboard
	 * @param keys List<String> 
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
	 * @param conn WebSocket
	 * @param playerName String 
	 * @param teamName String
	 */
	public boolean onNewConnection(WebSocket conn, String name, String teamName){
		
		WhiteboardEntry player;
		List<String> keys = new Vector<String>();
		String playerName = name;
		
		if(teamName != null){
			System.out.println("Neuer Player fuer das Team: "+ teamName +" angemeldet");
			player = this.whiteboard.getAttribute("Teams", teamName, playerName);
		}else{
			System.out.println("Neuer Player angemeldet");
			player = this.whiteboard.getAttribute("Players", playerName);
		}
		
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
		
			if(teamName != null){
				this.clients.put(teamName + playerName, conn);
			
				keys.add("Teams");
				keys.add(teamName);
				keys.add(playerName);
			}else{
				this.clients.put("Players," + playerName, conn);
		
				keys.add("Players");
				keys.add(playerName);
			}
			
			this.onWhiteboardUpdate(conn, keys, player);
			if(maxPlayer == this.clients.size()){
				this.onFullWhiteboardUpdate(conn, this.whiteboard, new Vector<String>());
			}
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
		String name         = null;
		WhiteboardEntry wbe = null;
		List<String> keys   = new Vector<String>();
		
		for(Map.Entry<String, WebSocket> entry : clients.entrySet()) {
			if(entry.getValue().equals(conn)){
				name = entry.getKey();
				keys.add(name);
				clients.remove(entry.getKey());
				try {
					wbe = new WhiteboardEntry("delete", "none");
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
//	public void printWhiteboard(String keyPath, Whiteboard wb){
//		List<String> a = new Vector<String>();
//		a.add("null");
//		a.add("Players");
//		a.add("0");
//		for(String key : wb.keySet()){
//			if(!(wb.getAttribute(key).value instanceof String)){
//				printWhiteboard(keyPath+","+key, (Whiteboard) wb.getAttribute(key).value);
//			}else{
//				System.out.println(keyPath+","+key+ ":"+ wb.getAttribute(key).value.toString());
//			}
//		}
////		System.out.println("DELEEEEETTTEEEEEEEEEEEEEEEEEEE");
//		this.removeWhiteboard(clients.get("1"), a);
//		printWhiteboard(keyPath, wb);
//	}


	
	/**
	 * ##########################################################################################################################################
	 * 
	 * Private Methoden
	 * 
	 * ##########################################################################################################################################
	 */

	/**
	 * Hilfs Methode.
	 * 
	 * Sendet Updates an alle Clients, ausser an den In­i­ti­a­tor des Updates.
	 * 
	 * 
	 * @param conn Websocket
	 * @param keys List<String>
	 * @param entry WhiteboardEntry
	 */
	private void sendUpdate(WebSocket conn, List<String> keys, WhiteboardEntry entry){
		for (Entry<String, WebSocket> mapEntry : this.clients.entrySet()) {
			if (!mapEntry.getValue().equals(conn)) {
				this.comServer.sendUpdate(mapEntry.getValue(), keys, entry);
			}	
		}
	}
	
	/**
	 * Player Templat speichern und das Template aus den globalen Whiteboard loeschen.
	 */
	private void savePlayerTemplate(){
		this.playerTemplate = (Whiteboard) this.whiteboard.getAttribute("Players", "template").value;
		this.whiteboard.deleteAttribute("Players", "template");
	}
	
	/**
	 * Lokales Whiteboard Update.
	 * 
	 * @param keys  String<List>
	 * @param value WhiteboardEntry
	 */
	private void onWhiteboardUpdate(List<String> keys, WhiteboardEntry value) {
		String [] key = this.getStringArrayPath(keys);
		this.whiteboard.setAttribute(value, key);
	}
	
	/**
	 * Erstellt, aus dem Globalen Whiteboard, eine Liste.
	 * 
	 * @param  wb   Whiteboard
	 * @param  keys List<String>
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
	 * Loescht das gewueschtes Whiteboard.
	 * 
	 * Z.b Player hat das Spiel verlassen, sein Player Whiteboard wird
	 * aus den Players Whiteboard geloescht.
	 * 
	 * @param keys List<String>
	 */
	private void removeWhiteboard(WebSocket conn, List<String> keys){
		String path = keys.get(0);
		String[] segs = path.split( Pattern.quote( "," ) );
		this.whiteboard.deleteAttribute(segs);
	}	

	/**
	* List<String> keys to String Array
	* 
	* @param keys List<String>
	* @return String[]
	*/
	private String[] getStringArrayPath(List<String> keys){
		String[] key = new String[keys.size()];
		return key = keys.toArray(key);
	}
	
	/* ##############################################################################
	* 
	*   Monitoring method
	* 
	*  #############################################################################
	*/
	public void attachMonitor(String name, WebSocket conn) {
		this.clients.put(name, conn);
		this.onFullWhiteboardUpdate(conn, this.whiteboard, new Vector<String>());
		
	}

	public void detachMonitor(WebSocket conn) {
		this.clients.remove(conn);
		
	}	
	
	/**
	 * #########################################################################################################################################
	 * 
	 * Methoden werden nicht benutzt 
	 * 
	 * #########################################################################################################################################
	 */

	@Override
	public void onFullWhiteboardUpdate(WebSocket conn, List<WhiteboardUpdateObject> wb) {
		// TODO Auto-generated method stub
		
	}

}
