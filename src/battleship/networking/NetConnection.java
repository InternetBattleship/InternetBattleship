package battleship.networking;

import java.net.Socket;

import javax.swing.JFrame;

import battleship.Game;

public class NetConnection {

	private JFrame parentFrame;
	
	
	private SocketStreams sock;
	private boolean isHost;
	public boolean isHost() { return isHost; }
	
	public NetConnection(SocketStreams s, boolean isHost, JFrame pFrame) {
		parentFrame = pFrame;
		this.isHost = isHost;
		parentFrame.setVisible(false);
		sock = s;
		new Game(this);
	}
	
	public boolean isActive() {
		return sock.isActive();
	}
	
	public NetMessage receiveMessage() {
		return sock.receiveMessage();
	}
	public void sendMessage(NetMessage nm) {
		sock.sendObject(nm);
	}
	
	public void close() {
		intl_close();
	}
	
	private void intl_close() {
		sock.close();
		parentFrame.setVisible(true);
	}
	
	private String getString(Socket s) {
		return "Connected to " + s.getInetAddress().getHostAddress() + ":" + s.getPort();
	}
	
}
