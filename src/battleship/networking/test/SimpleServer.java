package battleship.networking.test;

import battleship.networking.NetConnection;
import battleship.networking.NetServer;

public class SimpleServer implements NetServer.Listener {

	private NetServer server;
	
	public SimpleServer(NetServer s) {
		server = s;
		server.addListener(this);
	}
	
	@Override
	public void connectionReceived(NetConnection c) {
		new SimpleConnection(c);
	}

	@Override
	public void beganListening() {
		System.out.println("[SimpleServer] beganListening: " + server.getListenPort());
		
	}

	@Override
	public void stoppedListening() {
		System.out.println("[SimpleServer] stoppedListening");
	}

}
