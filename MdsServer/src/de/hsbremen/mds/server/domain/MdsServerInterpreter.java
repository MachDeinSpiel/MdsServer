package de.hsbremen.mds.server.domain;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
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
import de.hsbremen.mds.server.parser.ParserServerNew;

public class MdsServerInterpreter implements ServerInterpreterInterface, ComServerInterface {
	//Test Whiteboard
	Whiteboard whiteboard = new Whiteboard();
	//---------------------------------
	private MdsComServer comServer;

	//Websockets Hashmap...
	private HashMap<String,WebSocket> clients = new HashMap<String, WebSocket>();

	
	public MdsServerInterpreter (MdsComServer mdsComServer, File file) {
		this.comServer = mdsComServer;
		ParserServerNew parServ = new ParserServerNew(file);
		this.whiteboard = parServ.getWB();
		
		this.displayWhiteboard(this.whiteboard, new Vector<String>());
		
		// Muss fuer Test hinzugefuegt werden:
		try {
			this.whiteboard.setAttribute(new WhiteboardEntry(new Whiteboard(), "all"), "Players");
		} catch (InvalidWhiteboardEntryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		
		for (Entry<String, WhiteboardEntry> mapEntry : wb.entrySet()) {
			if (mapEntry.getValue().value instanceof Whiteboard) {
				keys.add(mapEntry.getKey());
				this.onFullWhiteboardUpdate(conn, (Whiteboard) mapEntry.getValue().value, keys);
			} else {
				this.comServer.sendUpdate(conn, keys, mapEntry.getValue());
				keys = new Vector<String>();
			}
		}
		
	}
	
	private void displayWhiteboard(Whiteboard wb, List<String> keys) {
		for (Entry<String, WhiteboardEntry> mapEntry : wb.entrySet()) {
			if (mapEntry.getValue().value instanceof Whiteboard) {
				System.out.println("Whiteboard");
				keys.add(mapEntry.getKey());
				this.displayWhiteboard((Whiteboard) mapEntry.getValue().value, keys);
			} else {
				
				String path = "";
				Iterator<String> it = keys.iterator();
				if(it.hasNext()){
					path = it.next();
					while (it.hasNext()) {
						path = path + "." + it.next();
					}
				}
				System.out.println("PATH: " + path + " VALUE:" + mapEntry.getValue().value);
				keys = new Vector<String>();
			}
		}
	}


	@Override
	public boolean onNewConnection(WebSocket conn, ClientHandshake handshake) {
		
		String playerName = handshake.getResourceDescriptor();
		
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
			// this.onFullWhiteboardUpdate(conn, this.whiteboard, new Vector<String>());
			this.onWhiteboardUpdate(conn, keys, player);
			
			return true;
		}
		
		return false;
	}
	
}
