package battleship.networking.test;

import java.util.Scanner;

import battleship.networking.NetConnection;
import battleship.networking.NetworkController;
import battleship.networking.messaging.NetMessage;

public class SimpleConnection implements NetConnection.Listener {

	public static Scanner console;
	
	public static void main(String args[]) {
		NetworkController ctrl = new NetworkController();
		ctrl.addListener(new NetworkController.Listener() {
			public void connectionAttained(NetConnection c) {
				System.out.println("[SimpleConnection] connectionAttained");
				new SimpleConnection(c, console);
			}
			public void connectionClosed(NetConnection c) {
				System.out.println("[SimpleConnection] connectionClosed");
				
			}
			public void connectionException(Exception e) {
				System.err.println("[SimpleConnection] connectionException: " + e.getMessage());
				
			}
		});
		console = new Scanner(System.in);
		String addr = null;
		int port = -1;
		boolean looking = true;
		while (looking) {
			System.out.println("[ Connect ]");
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
	public SimpleConnection(NetConnection c) {
		this(c, new Scanner(System.in));
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
		console.close();
	}

	// Connection listener
	@Override
	public void netMessageReceived(NetMessage nm) {
		System.out.println("[SimpleConnection] netMessageReceieved: " + nm.toString());
		switch (nm.getCategory()) {
		case CHAT:
			System.out.println("Received: " + nm.getMessage());
			break;
		default:
			break;
		}
	}
	
}
