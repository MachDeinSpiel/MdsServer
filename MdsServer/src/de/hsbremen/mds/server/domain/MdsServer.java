package de.hsbremen.mds.server.domain;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.java_websocket.WebSocketImpl;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * 
 */
public class MdsServer extends ServerResource {
	
	
	@Get  
	public String toString() {  
		
	    // Print the requested URI path  
	    return "Resource URI  : " + getReference() + '\n' + "Root URI      : "  
	            + getRootRef() + '\n' + "Routed part   : "  
	            + getReference().getBaseRef() + '\n' + "Remaining part: "  
	            + getReference().getRemainingPart();  
	}
	


	
	public static void main(String[] args) throws Exception {
		
		AppInfoManager aiM = new AppInfoManager();
		GameManager gM = new GameManager();
		PlayerManager pM = new PlayerManager();
		ItemManager iM = new ItemManager();
		
		// REST Service
//		new Server(Protocol.HTTP, 8182, MdsServer.class).start();
	    // Create a new Component.
	    Component component = new Component();

	    // Add a new HTTP server listening on port 8182.
	    component.getServers().add(Protocol.HTTP, 8080);

	  
		// Websocket-Server
		WebSocketImpl.DEBUG = true;
		int port = 8887; // 843 flash policy port
		try {
			port = Integer.parseInt(args[ 0 ]);
		} catch (Exception ex) {
		}
		WSServer wsServer = new WSServer(port);
		
		// Attach the sample application.
	    component.getDefaultHost().attach("/mds", new MDSRouteService(wsServer, aiM, gM, pM, iM));

	    // Start the component.
	    component.start();
		
		wsServer.start();
		System.out.println("MdsServer WebSocket started on port: " + wsServer.getPort());

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
		}
	}






}