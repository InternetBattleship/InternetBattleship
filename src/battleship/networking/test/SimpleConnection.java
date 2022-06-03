package battleship.networking.test;

import java.net.Socket;

import battleship.networking.NetConnection;
import battleship.networking.messaging.NetHandshakeException;
import battleship.networking.messaging.NetMessage;

public class SimpleConnection implements NetConnection.Listener {

	private NetConnection con;
	
	public SimpleConnection(NetConnection c) {
		con = c;
		con.addListener(this);
	}

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
	
}
