package de.hsbremen.mds.server.test;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class MdsWebSocketClient extends WebSocketClient {

	public MdsWebSocketClient(URI serverURI) {
		super(serverURI);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onClose(int arg0, String reason, boolean arg2) {
		System.out.println(reason);

	}

	@Override
	public void onError(Exception e) {
		e.printStackTrace();

	}

	@Override
	public void onMessage(String message) {
		System.out.println(message);

	}

	@Override
	public void onOpen(ServerHandshake arg0) {
//		System.out.println();

	}

}
