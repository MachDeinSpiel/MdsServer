package de.hsbremen.mds.server.test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Vector;

import org.json.JSONObject;


public class MdsServerTest {


	public static void main(String[] args) {
		
		JSONObject message = new JSONObject("{\"mode\":\"login\", \"username\":\"Alice\", \"password\":\"md5-Passwort\"}");
		int i = 0;
		
		List<MdsWebSocketClient> clients = new Vector<MdsWebSocketClient>();
		
		System.out.println("Creating Clients ...");
		
		while (i < 100) {
			
			
		
			System.out.println(i);
			
			MdsWebSocketClient wsC = null;
			try {
				wsC = new MdsWebSocketClient(new URI("ws://localhost:8887"));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			clients.add(wsC);
			
			i++;
			
		}
		
		i=0;
		
		System.out.println("Conntecting Clients ...");
		
		for (MdsWebSocketClient wsC : clients) {
			
			System.out.println(i);
			
			wsC.connect();
			wsC.send(message.toString());
			
			i++;
						
		}
		
		while(true) {
			
		}
		
		
		
//		i=0;
//		
//		System.out.println("Logging in Clients ...");
//		
//		for (MdsWebSocketClient wsC : clients) {
//			
//			System.out.println(i);
//			
//			wsC.send(message.toString());
//		
//			i++;
//			
//		}

	}


}
