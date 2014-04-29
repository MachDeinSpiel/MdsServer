package de.hsbremen.mds.server.domain;

import java.util.HashMap;
import java.util.List;

import org.java_websocket.WebSocket;

import de.hsbremen.mds.common.interfaces.ServerInterpreterInterface;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;

public class MdsServerInterpreter implements ServerInterpreterInterface {
	
	private MdsComServer comServer;
	private HashMap<Integer,WebSocket> clients = new HashMap<Integer, WebSocket>();
	private int idcount = 0;
	
	public MdsServerInterpreter (MdsComServer mdsComServer) {
		this.comServer = mdsComServer;
		
	}
	

	@Override
	public void onWhiteboardUpdate(List<String> keys, WhiteboardEntry value) {
		//this.comServer.onWhiteboardUpdate(keys, value, conn);
		
		// TODO Auto-generated method stub
		
	}


	public void addNewClient(WebSocket conn) {
		if (!this.clients.containsValue(conn)) {
			this.clients.put(this.idcount++, conn);
		}
	}


	public void removeClient(WebSocket conn, int code, String reason, boolean remote) {

		// TODO: this.clients.remove(key);
		
	}

}
