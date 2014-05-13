package de.hsbremen.mds.server.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.java_websocket.WebSocket;
import de.hsbremen.mds.common.interfaces.ServerInterpreterInterface;
import de.hsbremen.mds.common.valueobjects.MdsImage;
import de.hsbremen.mds.common.valueobjects.MdsItem;
import de.hsbremen.mds.common.valueobjects.MdsMap;
import de.hsbremen.mds.common.valueobjects.MdsText;
import de.hsbremen.mds.common.valueobjects.MdsVideo;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;

public class MdsServerInterpreter implements ServerInterpreterInterface {
	//Test Whiteboard
	Whiteboard whiteboard = new Whiteboard();
	//---------------------------------
	private MdsComServer comServer;
	//Websockets Hashmap...
	private HashMap<Integer,WebSocket> clients = new HashMap<Integer, WebSocket>();
	private int idcount = 0;
	
	public MdsServerInterpreter (MdsComServer mdsComServer) {
		this.comServer = mdsComServer;
		
	}
	

//	@Override
//	public void onWhiteboardUpdate(List<String> keys, WhiteboardEntry value) {
//		// TODO Auto-generated method stub
//		
//	}
	
	public void onWhiteboardUpdate(String path, String value) {
		String[] keys = getStringWhiteboardPath(path);
		//Object obValue = getWhiteboardValue(value);
		if(whiteboard.getAttribute(keys) != null){
			whiteboard.setAttributeValue(value, keys);
		}else if(whiteboard.getAttribute(keys) == null){
			whiteboard.setAttributeValue(value, keys);
		}
		
		
		//TODO: nach erfolgreichen update, MdsComServer die Strings uebergebene 
	}
	/*
	 * Zerlegt den String Path
	 * @return: String Array 
	 */
	private String[] getStringWhiteboardPath(String path){
		String[] pathArray = path.split(Pattern.quote("+"));
		return pathArray;
	}
	
	//
	public void receiveMessage(Object ob, WebSocket ws){
		
	}
	//Client merken
	public void addNewClient(WebSocket conn) {
		if (!this.clients.containsValue(conn)) {
			this.clients.put(this.idcount++, conn);
		}
		//TODO: whiteboard update?!
	}

	//Client entfernen
	public void removeClient(WebSocket conn, int code, String reason, boolean remote) {
		if(this.clients.containsValue(conn)){
			for (Map.Entry<Integer, WebSocket> entry : clients.entrySet()){
	    		if(entry.getValue().equals(conn)){
	    			this.clients.remove(entry.getKey());
	    		}
	    	}
		}
	}

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
