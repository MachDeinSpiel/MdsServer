package de.hsbremen.mds.server.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.java_websocket.WebSocketImpl;
import org.json.JSONObject;

import de.hsbremen.mds.common.communication.EntryHandler;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;
import de.hsbremen.mds.common.whiteboard.WhiterboardUpdateObject;

/**
 * 
 */
public class MdsServer {
	
	public static void main(String[] args) throws Exception {
			  
		// Websocket-Server
		WebSocketImpl.DEBUG = true;
		int port = 8000; // 843 flash policy port
		try {
			port = Integer.parseInt(args[ 0 ]);
		} catch (Exception ex) {
			
		}
				
		MdsComServer wsServer = new MdsComServer(port);

		wsServer.start();
		
		System.out.println("MdsServer WebSocket started on port: " + wsServer.getPort());
		
		List<String> keys = new Vector<String>();
		keys.add("MDS");
		keys.add("Players");
		WhiteboardEntry value = new WhiteboardEntry((Boolean)false, "all");
		
		//WhiterboardUpdateObject wObj = new WhiterboardUpdateObject(keys, value);
		WhiterboardUpdateObject wObj = null;
		
		String message = null;
		String path = "MDS,Player,Object";
		String[] parts = path.split(",");
		System.out.println(Arrays.toString(parts));
		
		message = EntryHandler.toJson(keys, value);
		
		System.out.println(message);
		
		wObj = EntryHandler.toObject(message);
		
		System.out.println(wObj);
		
		message = EntryHandler.toJson(wObj.getKeys(), wObj.getValue());
		
		System.out.println(message);
		

		
		
		
		/*
		BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
		
		while (true) {
			String in = sysin.readLine();
			wsServer.sendToAll(in);
			if( in.equals("exit") ) {
				wsServer.stop();
				break;
			} else if( in.equals("restart")) {
				wsServer.stop();
				wsServer.start();
				break;
			}
		}*/
	}

}