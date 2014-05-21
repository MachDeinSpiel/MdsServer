package de.hsbremen.mds.server.domain;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import de.hsbremen.mds.common.communication.EntryHandler;
import de.hsbremen.mds.common.exception.UnknownWhiteboardTypeException;
import de.hsbremen.mds.common.interfaces.ComServerInterface;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.common.whiteboard.WhiteboardUpdateObject;

/**
 * 
 */
public class MdsComServer extends WebSocketServer implements ComServerInterface {
	
	private MdsServerInterpreter mdsServerInterpreter;
		
	
	public MdsComServer(int port, File file) throws UnknownHostException {
		super(new InetSocketAddress(port) );
		this.mdsServerInterpreter = new MdsServerInterpreter(this, file);
	}

	public MdsComServer(InetSocketAddress address) {
		super(address);
	}
	

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		/*this.mdsServerInterpreter.addNewClient(conn);
		clients.put(idcount++, conn);
		this.sendToAll("new connection: " + handshake.getResourceDescriptor() );
		System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
		*/
	
		boolean isConnected = this.onNewConnection(conn, handshake);
		
		if(!isConnected) {
			conn.close(0);
		}
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		
		List<WhiteboardUpdateObject> wObj = EntryHandler.toObject(message);
		if(wObj.size() == 1) {
			mdsServerInterpreter.onWhiteboardUpdate(conn, wObj.get(0).getKeys(), wObj.get(0).getValue());
				
		} else {
			mdsServerInterpreter.onFullWhiteboardUpdate(conn, wObj);
		}
		System.out.println(conn + ": " + message);
		
		
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
	public boolean onNewConnection(WebSocket conn, ClientHandshake handshake) {
		return mdsServerInterpreter.onNewConnection(conn, handshake);
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


}