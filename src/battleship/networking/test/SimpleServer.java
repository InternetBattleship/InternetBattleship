package battleship.networking.test;

import java.util.Scanner;

import battleship.networking.NetConnection;
import battleship.networking.NetServer;
import battleship.networking.NetworkController;

public class SimpleServer implements NetServer.Listener {
	public static void main(String args[]) {
		NetworkController ctrl = new NetworkController();
		
		new SimpleServer(ctrl);
	}
	
	private NetServer server;
	private Scanner console;
	
	public SimpleServer(NetworkController c) {
		server = new NetServer(c);
		server.listenConcurrently();
		server.addListener(this);
		this.console = new Scanner(System.in);
	}
	
	@Override
	public void connectionReceived(NetConnection c) {
		System.out.println("[SimpleServer] connectionReceived");
		new SimpleConnection(c, console);
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
