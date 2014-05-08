package de.hsbremen.mds.server.domain;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.management.timer.Timer;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import de.hsbremen.mds.common.interfaces.ComServerInterface;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;

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
		
		System.out.println(conn + ": " + message);
		
		// TODO: mdsServerInterpreter.receiveMessage(converFromJson(message), conn);
		
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
	public void onWhiteboardUpdate(List<String> keys, WhiteboardEntry value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFullWhiteboardUpdate(Whiteboard newWhiteboard) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWhiteboardUpdate(WebSocket conn, String value, String visibillity, String... keys) {
		
		JSONObject json = new JSONObject();
		
		json.put("visibillity", visibillity);
		json.put("value", value);		
		json.put("keys", arrayToString(keys, "."));
		
		conn.send(json.toString());
		
	}
	
	// Convert an array of strings to one string.
	// Put the 'separator' string between each element
	public static String arrayToString(String[] a, String separator) {
	    String result = "";
	    if (a.length > 0) {
	        result = a[0];    // start with the first element
	        for (int i=1; i<a.length; i++) {
	            result = result + separator + a[i];
	        }
	    }
	    return result;
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