package de.hsbremen.mds.server.domain;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

/**
 * 
 */
public class MdsComServer extends WebSocketServer {
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
		clients.put(idcount++, conn);
		this.sendToAll("new connection: " + handshake.getResourceDescriptor() );
		System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		this.sendToAll(conn + " has left the room!");
		System.out.println( conn + " has left the room!");
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		JSONObject json = new JSONObject(message);
		
		
		for(Entry<Integer, WebSocket> entry: this.clients.entrySet()){
			  if (entry.getValue().equals(conn)) {
				  System.out.println(entry.getKey());
				  this.locat.put(entry.getKey(), json);
				  json.put("ClientID", entry.getKey());
				  
				  System.out.println("Der Client mit der ID: " +entry.getKey() +" Latitude " +this.locat.get(entry.getKey()).get("Latitude"));
				  System.out.println("Der Client mit der ID: " +entry.getKey() +" Longitude " +this.locat.get(entry.getKey()).get("Longitude"));
			  }
			
		}
		
		for(Entry<Integer, WebSocket> entry: this.clients.entrySet()){
			  if (!entry.getValue().equals(conn)) {
				  entry.getValue().send(json.toString());
			  }
			
		}
		
		//this.sendToAll(message);
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
}