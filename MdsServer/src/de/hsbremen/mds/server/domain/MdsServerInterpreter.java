package de.hsbremen.mds.server.domain;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import de.hsbremen.mds.common.interfaces.ComServerInterface;
import de.hsbremen.mds.common.interfaces.ServerInterpreterInterface;
import de.hsbremen.mds.common.whiteboard.InvalidWhiteboardEntryException;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.common.whiteboard.WhiteboardUpdateObject;
import de.hsbremen.mds.server.parser.ParserServerNew;

public class MdsServerInterpreter implements ServerInterpreterInterface, ComServerInterface {
	//Test Whiteboard
	Whiteboard whiteboard = new Whiteboard();
	//---------------------------------
	private MdsComServer comServer;
	private Vector<WhiteboardUpdateObject> whiteboardUpdateObjects = new Vector<WhiteboardUpdateObject>();
	//Websockets Hashmap...
	private HashMap<String,WebSocket> clients = new HashMap<String, WebSocket>();

	
	public MdsServerInterpreter (MdsComServer mdsComServer, File file) {
		this.comServer = mdsComServer;
		ParserServerNew parServ = new ParserServerNew(file);
		this.whiteboard = parServ.getWB();
		String keyPath = null;
		//this.printWhiteboard(keyPath, this.whiteboard);
		/*
		// Muss fuer Test hinzugefuegt werden:
		try {
			this.whiteboard.setAttribute(new WhiteboardEntry(new Whiteboard(), "all"), "Players");
		} catch (InvalidWhiteboardEntryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

	@Override
	/**
	 */
	public void onWhiteboardUpdate(WebSocket conn, List<String> keys, WhiteboardEntry entry) {
		// Lokales WB aktualisieren
		this.onWhiteboardUpdate(keys, entry);
		
		// Allen anderen Clients das Update schicken
		for (Entry<String, WebSocket> mapEntry : this.clients.entrySet()) {
			if (!mapEntry.getValue().equals(conn)) {
				this.comServer.sendUpdate(mapEntry.getValue(), keys, entry);
			}
		}
		
	}

	/**
	 * Lokales WB aktualisieren
	 */
	@Override
	public void onWhiteboardUpdate(List<String> keys, WhiteboardEntry value) {
		String[] key = new String[keys.size()];
		key = keys.toArray(key);
		this.whiteboard.setAttribute(value, key);
	}
	
	/**
	 * Client kompletten WB senden
	 * 
	 * TODO: Rekursion pruefen
	 * 
	 * @param conn - der Client
	 * @param wb - das WB
	 * @param keys - Key zum WB
	 */
	public void onFullWhiteboardUpdate(WebSocket conn, Whiteboard wb, List<String> keys) {

		try {
			makeWhiteboardList(wb, keys);
		} catch (InvalidWhiteboardEntryException e) {
			// TODO Auto-generated catch block
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
	 * @param keys List<String>
	 * @throws InvalidWhiteboardEntryException
	 */
	public void makeWhiteboardList(Whiteboard wb, List<String> keys) throws InvalidWhiteboardEntryException{
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
			} catch (InvalidWhiteboardEntryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			this.clients.put("Players," + playerName, conn);
			
			List<String> keys = new Vector<String>();
			keys.add("Players");
			keys.add(playerName);
			
			// TODO: laeuft noch nicht :-(
			this.onFullWhiteboardUpdate(conn, this.whiteboard, new Vector<String>());
			this.onWhiteboardUpdate(conn, keys, player);
			
			return true;
		}
		
		return false;
	}

	@Override
	public void onFullWhiteboardUpdate(WebSocket conn, List<WhiteboardUpdateObject> wb) {
		// TODO Auto-generated method stub
		
	}
	public void printWhiteboard(String keyPath, Whiteboard wb){
		for(String key : wb.keySet()){
			if(!(wb.getAttribute(key).value instanceof String)){
				printWhiteboard(keyPath+","+key, (Whiteboard) wb.getAttribute(key).value);
			}else{
				System.out.println(keyPath+","+key+ ":"+ wb.getAttribute(key).value.toString());
			}
		}
	}
}
