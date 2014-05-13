package de.hsbremen.mds.server.domain;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import de.hsbremen.mds.common.communication.EntryHandler;
import de.hsbremen.mds.common.exception.UnknownWhiteboardTypeException;
import de.hsbremen.mds.common.interfaces.ComServerInterface;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.common.whiteboard.WhiterboardUpdateObject;

/**
 * 
 */
public class MdsComServer extends WebSocketServer implements ComServerInterface {
	
	private MdsServerInterpreter mdsServerInterpreter = new MdsServerInterpreter(this);
	
	private HashMap<Integer,JSONObject> locat = new HashMap<Integer, JSONObject>();
	private HashMap<Integer,WebSocket> clients = new HashMap<Integer, WebSocket>();
	private int idcount;
	

	
	
	public MdsComServer(int port) throws UnknownHostException {
		super(new InetSocketAddress(port) );
	}

	public MdsComServer(InetSocketAddress address) {
		super(address);
	}
	

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		this.mdsServerInterpreter.addNewClient(conn);
		clients.put(idcount++, conn);
		this.sendToAll("new connection: " + handshake.getResourceDescriptor() );
		System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		this.mdsServerInterpreter.removeClient(conn, code, reason, remote);
		this.sendToAll(conn + " has left the room!");
		System.out.println( conn + " has left the room!");
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		
		WhiterboardUpdateObject wObj = EntryHandler.toObject(message);
		 
		
		/*
		for(Entry<Integer, WebSocket> entry: this.clients.entrySet()){
			  if (!entry.getValue().equals(conn)) {
				  entry.getValue().send(message);
			  }
			
		}
		*/
			
		mdsServerInterpreter.onWhiteboardUpdate(conn, wObj.getKeys(), wObj.getValue());
		
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

	/**
	 * Sends <var>text</var> to all currently connected WebSocket clients.
	 * 
	 * @param text
	 *            The String to send across the network.
	 * @throws InterruptedException
	 *             When socket related I/O errors occur.
	 */
	public void sendToAll(String text) {
		Collection<WebSocket> con = connections();
		synchronized (con) {
			for(WebSocket c : con) {
				c.send(text);
			}
		}
	}
	
	public void notifyWSClients(String object, int id, String status) {
		JSONObject json = new JSONObject();
		json.put(object, id);
		json.put("status", status);
		this.sendToAll(json.toString());
	}


	@Override
	/**
	 * Wird vom Interpreter aufgerufen wenn es ein WhiteboardUpdate gibt
	 * @prama WebSocket conn - Client dem das Update mitgeteilt werden soll
	 * @prama List<String> keys - Pfad zum WhiteboardEntry
	 * @prama WhiteboardEntry entry - der WhiteboardEntry der dem Client mitgeteilt werden soll
	 */
	public void onWhiteboardUpdate(WebSocket conn, List<String> keys, WhiteboardEntry entry) {
		
		String message = null;
		try {
			message = EntryHandler.toJson(keys, entry);
		} catch (UnknownWhiteboardTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		conn.send(message);
		
	}

	
	
	// Bot zum Senden zufälliger Koordninaten über WS
	public void bot(){
		double botLongitude, botLatitude;
		while(true){
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			botLatitude = myRandom(52.9,53.9);
			botLongitude = myRandom(8.2,8.9);
			System.out.println("Latitude: " +botLatitude+ " und Longitude: " +botLongitude);
		}
	}
	// Zufallsgenerator
	public static double myRandom(double low, double high) {
		return Math.random() * (high - low) + low;
	}


}