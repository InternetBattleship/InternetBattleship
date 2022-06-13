package battleship.networking.test;

import java.util.Scanner;

import battleship.Game;
import battleship.networking.NetConnection;
import battleship.networking.NetworkController;
import battleship.networking.messaging.NetMessage;

public class SimpleConnection implements NetConnection.Listener {

	private static Scanner console;
	
	public static void main(String args[]) {
		console = new Scanner(System.in);
		run(console);
	}
	public static void run(Scanner console) {

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
		String addr = null;
		int port = -1;
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
		ctrl.attemptConnection(addr, port);	
	}
	public SimpleConnection(NetConnection c) {
		this(c, new Scanner(System.in));
	}
	
	private NetConnection con;
	public SimpleConnection(NetConnection c, Scanner console) {
		con = c;
		con.addListener(this);
//		while (con.isConnected()) {
//			String msg = console.nextLine().trim();
//			if (msg.length() > 0) {
//				System.out.println("[SimpleConnection] S: " + msg);
//				c.sendNetMessage(NetMessage.Factory.chat(msg));
//			}
//		}
		console.close();
		new Game(c);
	}

	// Connection listener
	@Override
	public void netMessageReceived(NetMessage nm) {
		System.out.println("[SimpleConnection.netMessageReceieved] " + nm.toString());
		switch (nm.getCategory()) {
		case CHAT:
			System.out.println("    R: " + nm.getMessage());
			break;
		default:
			break;
		}
	}
	@Override
	public void connectionBegan() {
		System.out.println("[SimpleConnection.connectionBegan]");
	}
	@Override
	public void connectionStopped() {
		System.out.println("[SimpleConnection.connectionStopped]");
		
	}
	
}
