package battleship.networking;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import battleship.networking.messaging.NetUser;

public class NetServer {

	// LISTENERS
	private ArrayList<Listener> listeners = new ArrayList<Listener>(); // List
	// Add/remove
	public void addListener(Listener l) { listeners.add(0, l); } 
	public boolean removeListener(Listener l) { return listeners.remove(l); }
	// Invoking
	public void invokeListeners(ListenerInvoker li) {
		for (int i=listeners.size()-1;i>=0;i--) li.invoke(listeners.get(i));
	}
	private interface ListenerInvoker {
		public void invoke(Listener l);
	}
	public interface Listener { // Listener outline
		// Connection
		public void connectionReceived(NetConnection c); // Connection received
		// Server/port listening
		public void beganListening(); // Server listening for incoming
		public void stoppedListening();
	}
	
	private NetworkController controller;
	
	private NetUser self;
	public NetUser getSelf() { return self; }
	
	public NetServer(NetworkController c) {
		if (c == null) throw new IllegalArgumentException("Controller is null!");
		this.controller = c;
		this.self = c.getSelf();
	}
	
	public String getStatus() {
		return isListening() ? "Listening at " + getLocalIPString() + ":" + getListenPort() : "Inactive";
	}
	
	public boolean canListen() {
		return !controller.isConnected();
	}
	
	private boolean listening = false; 
	public boolean isListening() { // returns true if currently accepting incoming connections
		return server != null && listening;
	}
	private ServerSocket server; // Server socket to listen for incoming connections
	private Thread listenerThread; // Thread which listens for connections
	public int getListenPort() { return server.getLocalPort(); }
	public String getLocalIPString() { // Returns host ip as a string
		try {
			return Inet4Address.getLocalHost().getHostAddress().toString();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return "ERR.ORF.OUN.Djm";
	}
	public void listenConcurrently() { // Listen for new connections on another thread
		listenerThread = new Thread(() -> {
			listening = true;
			try {
				server = new ServerSocket(0);
				invokeListeners((l) -> l.beganListening());
				try {
					System.out.println("[NetServer.listenConcurrently] Accepting...");
					Socket s = server.accept();
					listening = false;
					NetConnection c = new NetConnection(controller, s, true);
					invokeListeners((l) -> l.connectionReceived(c));
				} catch (SocketException e) {
					System.err.println("Server socket closed successfully!");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			listening = false;
			invokeListeners((l) -> l.stoppedListening());
		}, "ServerListenerThread");
		listenerThread.start();
	}
	
	public void stopListening() { // Halt the thread listening for new connections and destroy server socket
		if (!isListening()) throw new IllegalStateException("Can't stop listening: was never listening.");
		listening = false;
		try {
			if (server != null && (!server.isClosed()))
				server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (listenerThread != null) {
			try {
				listenerThread.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		listenerThread = null;
	}
}
