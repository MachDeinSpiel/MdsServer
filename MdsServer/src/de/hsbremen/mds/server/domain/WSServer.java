package de.hsbremen.mds.server.domain;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

/**
 * 
 */
public class WSServer extends WebSocketServer {
	

	public WSServer(int port) throws UnknownHostException {
		super(new InetSocketAddress(port) );
	}

	public WSServer(InetSocketAddress address) {
		super(address);
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
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
		//this.sendToAll(message);
		System.out.println(conn + ": " + message);
	}

	public void onFragment(WebSocket conn, Framedata fragment) {
		System.out.println("received fragment: " + fragment);
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