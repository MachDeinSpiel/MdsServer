package de.hsbremen.mds.server.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.java_websocket.WebSocketImpl;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * 
 */
public class MdsServer {
	
	private static File jsonEinlesen() {

		InputStream is = null;
		
		try {
			is = new URL("https://raw.githubusercontent.com/MachDeinSpiel/MdsJsons/master/BombDefuser_Server.json").openStream();
		} catch (MalformedURLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		// InputStream is =
		// getInputStreamFromUrl("http://195.37.176.178:1388/MDSS-0.1/api/appinfo/2.xml");
		// InputStream is =
		// getInputStreamFromUrl("http://195.37.176.178:1388/MDSS-0.1/api/appinfo/3");

		// Temporäre Datei anlegen
		File json = null;
		try {
			json = File.createTempFile("App", ".json");
		} catch (IOException e1) {
			e1.printStackTrace();
		}


		try {
			// Inputstream zum einlesen der Json
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			// Json wird zeilenweise eingelesn uns in das File json geschrieben
			FileWriter writer = new FileWriter(json, true);

			String t = "";

			while ((t = br.readLine()) != null) {
				//System.out.println(t);
				writer.write(t);
			}

			writer.flush();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Überprüfung, ob es geklappt hat
		if (json.exists()) {
			System.out.println("JSON Einlesen erfolgreich.");
			System.out.println(json.length());
		} else {
			System.out.println("JSON Einlesen fehlgeschlagen");
		}

		return json;

	}
	
	public static void main(String[] args) throws Exception {
			  
		// Websocket-Server
		WebSocketImpl.DEBUG = true;
		int port = 8000; // 843 flash policy port
		try {
			port = Integer.parseInt(args[ 0 ]);
		} catch (Exception ex) {
			
		}
		
		File file = jsonEinlesen();
		

				
		MdsComServer wsServer = new MdsComServer(port, file);

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