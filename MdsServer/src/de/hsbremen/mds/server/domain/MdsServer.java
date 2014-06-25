package de.hsbremen.mds.server.domain;

import org.java_websocket.WebSocket;

/**
 * 
 */
public class MdsServer {
	
	public static void main(String[] args) throws Exception {
			  
		// Websocket-Server
		WebSocket.DEBUG = true;
		int port = 8887; // 843 flash policy port
		try {
			port = Integer.parseInt(args[ 0 ]);
		} catch (Exception ex) {
			
		}
		
		MdsComServer wsServer = new MdsComServer(port, "https://raw.githubusercontent.com/MachDeinSpiel/MdsJsons/master/config.json");

		wsServer.start();
		
		System.out.println("MdsServer WebSocket started on port: " + wsServer.getPort());
		
	
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