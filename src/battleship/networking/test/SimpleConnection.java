package battleship.networking.test;

import java.net.Socket;
import java.util.Scanner;

import battleship.networking.NetConnection;
import battleship.networking.NetworkController;
import battleship.networking.messaging.NetHandshakeException;
import battleship.networking.messaging.NetMessage;

public class SimpleConnection implements NetworkController.Listener, NetConnection.Listener {

	public static Scanner console;
	
	public static void main(String args[]) {
		NetworkController ctrl = new NetworkController();
		console = new Scanner(System.in);
		String addr = null;
		int port = -1;
		boolean looking = true;
		while (looking) {
			System.out.println("\n    Connect: ");
			System.out.print("IP/hostname: ");
			addr = console.nextLine();
			System.out.print("Port: ");
			try {
				port = console.nextInt();
				if (port < 0 || port > 65535) throw new IllegalArgumentException("Out of range 0-65535: " + port);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}

			if (ctrl.attemptConnection(addr, port)) looking = false;
		}
	}
	
	private NetConnection con;
	public SimpleConnection(NetConnection c, Scanner console) {
		con = c;
		con.addListener(this);
		while (con.isConnected()) {
			String msg = console.nextLine();
			System.out.println("[SimpleConnection] Sending: " + msg);
			c.sendNetMessage(NetMessage.Factory.chat(msg));
		}
	}

	// Connection listener
	@Override
	public void handshakeCompleted(Socket s) {
		System.out.println("[SimpleConnection] handshakeCompleted");
		
	}

	@Override
	public void handshakeFailed(Socket s, NetHandshakeException e) {
		System.out.println("[SimpleConnection] handshakeFailed: " + e.getMessage());
		
	}

	@Override
	public void netMessageReceived(NetMessage nm) {
		System.out.println("[SimpleConnection] netMessageReceieved: " + nm.toString());
	}

	
	// Controller listener
	@Override
	public void connectionAttained(NetConnection c) {
		System.out.println("[SimpleConnection] connectionAttained");
		new SimpleConnection(c, console);
	}

	@Override
	public void connectionClosed(NetConnection c) {
		System.out.println("[SimpleConnection] connectionClosed");
		
	}

	@Override
	public void connectionException(Exception e) {
		System.err.println("[SimpleConnection] connectionException: " + e.getMessage());
		
	}
	
}
