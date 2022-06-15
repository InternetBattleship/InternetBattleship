package battleship.networking;

import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class NetConnection {

	public NetConnection(Socket s, boolean isHost, JFrame parentFrame) {
		JOptionPane.showMessageDialog(parentFrame, getString(s), "NetConection", JOptionPane.PLAIN_MESSAGE);
	}
	
	private String getString(Socket s) {
		return "Connected to " + s.getInetAddress().getHostAddress() + ":" + s.getPort();
	}
	
}
