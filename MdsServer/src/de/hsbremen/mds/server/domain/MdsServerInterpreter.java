package de.hsbremen.mds.server.domain;

import java.util.HashMap;
import java.util.List;

import org.java_websocket.WebSocket;

import de.hsbremen.mds.common.interfaces.ServerInterpreterInterface;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;

public class MdsServerInterpreter implements ServerInterpreterInterface {
	//Test Whiteboard
	Whiteboard whiteboard = new Whiteboard();
	//---------------------------------
	private MdsComServer comServer;
	private HashMap<Integer,WebSocket> clients = new HashMap<Integer, WebSocket>();
	private int idcount = 0;
	
	public MdsServerInterpreter (MdsComServer mdsComServer) {
		this.comServer = mdsComServer;
		
	}
	
	
	
	//String pfad = game->players->playerOne; String value = "xyz;"
	public void onWhiteboardUpdate(List<String> keys, String value) {

		int i = 0;
		boolean b = true;
		String[] key = new String[keys.size()];
		key = keys.toArray(key);
		if(whiteboard.getAttribute(key) != null){
			whiteboard.setAttributeValue(value, key);
		}else{
			do{
				if(whiteboard.getAttribute(keys.get(i)) == null){
				//	whiteboard.put(keys, value);
					b = false;
				}else{
					i++;
				}
			}while(b);
		}
		
		//this.comServer.onWhiteboardUpdate(keys, value, conn);
	}

	public void receiveMessage(Object ob, WebSocket ws){
	
	}
	//Client merken
	public void addNewClient(WebSocket conn) {
		if (!this.clients.containsValue(conn)) {
			this.clients.put(this.idcount++, conn);
		}
	}

	//Client entfernen
	public void removeClient(WebSocket conn, int code, String reason, boolean remote) {

		// TODO: this.clients.remove(key);
		
	}
	
	//TODO: Key Pfad erstellen. 
	/*private String[] getKeys(Object value, Whiteboard w){
		String[] keys = new String[w.size()];
		
		if(w.get(value) != null) {
			
		}
		
		return keys;
	}*/
	/*
	//TEST WHITEBOARD
	private void whiteboardErstellen(){
		//Whiteboards
		Whiteboard whiteboard  = new Whiteboard();
		Whiteboard playerboard = new Whiteboard();
		//Players
		MdsPlayer playerOne = new MdsPlayer("Detlef", 8.8934326171875, 53.053321150329076);
		MdsPlayer playerTwo = new MdsPlayer("Mascha", 8.8934326171875, 53.053321150329076);
		//WhiteboardEnty
		WhiteboardEntry playerBoardEnty = new WhiteboardEntry(playerboard, "board");
		WhiteboardEntry playerEntyOne   = new WhiteboardEntry(playerOne, "player1");
		WhiteboardEntry playerEntyTwo   = new WhiteboardEntry(playerTwo, "player2");
		//PUT
		whiteboard.put("players", playerBoardEnty);
		playerboard.put("detlef", playerEntyOne);
		playerboard.put("Mascha", playerEntyTwo);
	}
	*/



	@Override
	/**
	 * Wenn vom ComServer eine neue Nachricht empfangen wird, wird diese Methode aufgerufen.
	 * 
	 * @param WebSocket conn - das WebSocket welches die Nachricht gesendet hat
	 * @param List<String> keys - Der Pfad als List
	 * @param WhiteboardEntry entry - der Entry mit Value und Visibility
	 */
	public void onWhiteboardUpdate(WebSocket conn, List<String> keys, WhiteboardEntry entry) {
		// TODO Auto-generated method stub
		
	}
}
