package de.hsbremen.mds.server.domain;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.regex.Pattern;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import de.hsbremen.mds.common.interfaces.ComServerInterface;
import de.hsbremen.mds.common.interfaces.ServerInterpreterInterface;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.server.parser.ParserServer;

public class MdsServerInterpreter implements ServerInterpreterInterface, ComServerInterface {
	//Test Whiteboard
	Whiteboard whiteboard = new Whiteboard();
	//---------------------------------
	private MdsComServer comServer;

	//Websockets Hashmap...
	private HashMap<String,WebSocket> clients = new HashMap<String, WebSocket>();

	
	public MdsServerInterpreter (MdsComServer mdsComServer, File file) {
		this.comServer = mdsComServer;
		ParserServer parServ = new ParserServer(file);
		
	}

	@Override
	/**
	 */
	public void onWhiteboardUpdate(WebSocket conn, List<String> keys, WhiteboardEntry entry) {
		
		// Lokales WB aktualisieren
		this.onWhiteboardUpdate(keys, entry);
		
		// Allen anderen Clients das Update schicken
		for (Entry<String, WebSocket> mapEntry : this.clients.entrySet()) {
			if (mapEntry.getValue() != conn) {
				this.comServer.sendUpdate(mapEntry.getValue(), keys, entry);
			}
		}
		
	}


	@Override
	public void onWhiteboardUpdate(List<String> keys, WhiteboardEntry value) {
		String[] key = new String[keys.size()];
		key = keys.toArray(key);
		this.whiteboard.setAttribute(value, key);
	}


	@Override
	public boolean onNewConnection(WebSocket conn, ClientHandshake handshake) {
		
		String playerName = handshake.getResourceDescriptor();
		
		WhiteboardEntry player = this.whiteboard.getAttribute("Players", playerName);
		
		if (player == null) {
			player = new WhiteboardEntry(playerName, "all");
			
			
			this.clients.put("Players," + playerName, conn);
			
			List<String> keys = new Vector<String>();
			keys.add("Players");
			keys.add(playerName);
			
			
			this.onWhiteboardUpdate(conn, keys, player);
			
			return true;
		}
		
		return false;
	}
}
