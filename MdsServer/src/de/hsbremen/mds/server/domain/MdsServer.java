package de.hsbremen.mds.server.domain;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import org.java_websocket.WebSocket;

import sun.net.util.URLUtil;

/**
 * 
 */
public class MdsServer {
	
	private static MdsComServer wsServer;
	private static boolean userauth = true;
	
	public static void main(String[] args) throws Exception {
		

		
		Runtime.getRuntime().addShutdownHook(new Thread(){
		    @Override
		    public void run(){
		    	if (wsServer != null) {
		    		wsServer.shutdown();
		    	}
		        
		    }
		});
		
		if (args.length > 0) {
			try {
				
				new URL(args[0]).openStream();
				
			} catch (MalformedURLException e) {
				System.out.println("Unable to read from URL: " + args[0] );
				System.exit(0);
			}
				
		        System.out.println("Configuration URL: 			" + args[0]);
		        
		        if (args.length == 2 && args[1].equals("-debug")) {
		        	WebSocket.DEBUG = true;
		        	System.out.println("Debugging mode: 			enabled");
		        }
		        if (args.length == 3 && args[2].equals("-noauth")) {
		        	userauth = false;
		        }
		
		        
				
				int port = 8887; // 843 flash policy port
				try {
					port = Integer.parseInt(args[ 0 ]);
				} catch (Exception ex) {
					
				}
				
				wsServer = new MdsComServer(port, args[0], userauth);
		
				wsServer.start();
				
				System.out.println("MdsServer WebSocket started on port:	" + wsServer.getPort());
				System.out.println("Stop Server with CTRL+C\n");

		
		} else {
			System.out.println("Please specify a configuration URL.");
			System.out.println("Usage: \n	mdsserver.jar <configuration-URL> [-debug | -debug -noauth]");
		}
		
	
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