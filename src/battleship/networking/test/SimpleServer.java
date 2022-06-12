package battleship.networking.test;

import battleship.networking.NetConnection;
import battleship.networking.NetServer;
import battleship.networking.NetworkController;

public class SimpleServer implements NetServer.Listener {
	public static void main(String args[]) {
		NetworkController ctrl = new NetworkController();
		
		new SimpleServer(ctrl);
	}
	
	private NetServer server;
	
	public SimpleServer(NetworkController c) {
		server = c.getServer();
		server.listenConcurrently();
		server.addListener(this);
	}
	
	@Override
	public void connectionReceived(NetConnection c) {
		System.out.println("[SimpleServer] connectionReceived");
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
