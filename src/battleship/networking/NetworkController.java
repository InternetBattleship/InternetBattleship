package battleship.networking;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import battleship.networking.messaging.NetMessage;
import battleship.networking.messaging.NetUser;

// This class should facilitate all network connection making and destroying to/from opponents.
public class NetworkController implements NetServer.Listener, NetConnection.Listener {

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
		public void connectionAttained(NetConnection c); // Connection received or initiated
		public void connectionClosed(NetConnection c); // Connection closed, by user-input or network error
		// Errors: (sent in addition to connectionClosed, or before connectionAttained would occur)
		public void connectionException(Exception e);
	}
	
	// Net Users
	private NetUser self = null;
	public NetUser getSelf() { return self; }
	
	// Services
	private NetServer server = null;
	public NetServer getServer() { return server; }
	
	// Connection
	private NetConnection connection = null;
	public NetConnection getConnection() { return connection; }
	
	public NetworkController() {
		this(NetUser.Factory.random());
	}
	public NetworkController(NetUser self) {
		if (self == null) throw new IllegalArgumentException("User is null!");
		this.self = self;
		server = new NetServer(this);
		server.addListener(this);
	}

	public void attemptConnection(String target, int port) { // Attempt a connection, subsequently create object I/O streams.
		System.out.println("[attemptConnection] " + target + ":" + port);
		try {
			InetAddress addr = InetAddress.getByName(target);
			InetSocketAddress saddr = new InetSocketAddress(addr, port);
			attemptConnection(saddr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void attemptConnection(InetSocketAddress givenSaddr) { // Attempt a connection, subsequently create object I/O streams.
		System.out.println("[attemptConnection] Connect!");
		if (isConnected()) throw new IllegalStateException("Cannot attempt new connection: connection already established!");
		final InetSocketAddress saddr;
		if (givenSaddr.isUnresolved()) {
			saddr = new InetSocketAddress(givenSaddr.getAddress(), givenSaddr.getPort());
			if (saddr.isUnresolved()) {
				invokeListeners((l) -> l.connectionException(new Exception(saddr.toString())));
				return;
			}
		} else {
			saddr = givenSaddr;
		}
		try (Socket s = new Socket(saddr.getAddress(), saddr.getPort())) {
			acquireConnection(new NetConnection(this, s, false));
			invokeListeners((l) -> l.connectionAttained(connection));
		} catch (ConnectException ex) {
			ex.printStackTrace();
			invokeListeners((l) -> l.connectionException(ex));
		} catch (SocketTimeoutException ex) {
			ex.printStackTrace();
			invokeListeners((l) -> l.connectionException(ex));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			invokeListeners((l) -> l.connectionException(e));
		} catch (IOException e) {
			e.printStackTrace();
			invokeListeners((l) -> l.connectionException(e));
		}
	}
	private void acquireConnection(NetConnection c) {
		System.out.println("[NetworkController.acquireConnection]");
		connection = c;
		connection.addListener(this);
		if (isListening()) server.stopListening();
	}
	
	public boolean attemptDisconnect(boolean locallyInitiated) { // Destroy socket and streams of connection
		System.out.println("[attemptDisconnect] Disconnect!");
		/*
		if (connection == null) throw new IllegalStateException("Connection is null!");
		if (!connection.isConnected()) throw new IllegalStateException("Connection isn't connected!");
		
		boolean closedSuccessfully = connection.disconnect(locallyInitiated);
		invokeListeners((l) -> l.connectionClosed(connection));

		connection.removeListener(this);
		connection = null;
		return closedSuccessfully; */ return false;
	}
	public String getStatus() { // Displayed on UI
		String str = "Inactive";
		if (isListening()) str = server.getStatus();
		if (isConnected()) str = connection.getStatus();
		return str;
	}
	
	public boolean isConnected() {
		return connection != null && connection.isConnected();
	}
	public boolean isListening() {
		return server != null && server.isListening();
	}
	
	// Connection
	@Override
	public void netMessageReceived(NetMessage nm) {
		System.out.println("[NetworkController.netMessageReceived] " + nm);
	}
	@Override
	public void connectionBegan() {
		System.out.println("[NetworkController.connectionBegan]");
	}
	@Override
	public void connectionStopped() {
		System.out.println("[NetworkController.connectionStopped]");
	}
	
	// Server
	@Override
	public void connectionReceived(NetConnection c) {
		System.out.println("[NetworkController.connectionReceived]");
		acquireConnection(c);
	}
	@Override
	public void beganListening() {
		System.out.println("[NetworkController.beganListening]");
	}
	@Override
	public void stoppedListening() {
		System.out.println("[NetworkController.stoppedListening]");
	}
}
