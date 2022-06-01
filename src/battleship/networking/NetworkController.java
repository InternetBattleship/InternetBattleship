package battleship.networking;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import battleship.networking.browsing.NetFinder;
import battleship.networking.browsing.NetHostInfo;
import battleship.networking.browsing.NetMulticaster;
import battleship.networking.messaging.NetHandshakeException;
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
		public void connectionClosed(NetConnection c);
		// Errors:
		public void refusedConnection(InetSocketAddress a);
		public void unresolvedAddress(InetSocketAddress a);
		public void connectionTimeout(InetSocketAddress a, int toMs);
	}
	public class ListenerAdapter implements Listener { // Listener outline
		@Override public void connectionAttained(NetConnection c) { } // Connection received or initiated
		@Override public void connectionClosed(NetConnection c) { }
		@Override public void refusedConnection(InetSocketAddress a) { }
		@Override public void unresolvedAddress(InetSocketAddress a) { }
		@Override public void connectionTimeout(InetSocketAddress a, int toMs) { }
	}
	
	// Net Users
	private NetUser self = null;
	public NetUser getSelf() { return self; }
	
	// Services
	private NetServer server = null;
	public NetServer getServer() { return server; }
	private NetMulticaster multicaster = null;
	public NetMulticaster getMulticaster() { return multicaster; }
	private NetFinder netFinder = null;
	public NetFinder getNetFinder() { return netFinder; }
	
	// Connection
	private NetConnection connection = null;
	public NetConnection getConnection() { return connection; }
	
	public NetworkController() {
		this(NetUser.Factory.random());
	}
	public NetworkController(NetUser self) {
		if (self == null) throw new IllegalArgumentException("User is null!");
		this.self = self;
		multicaster = new NetMulticaster("227.145.62.176", 34837);
		netFinder = new NetFinder(this);
		server = new NetServer(this);
		
		server.addListener(this);
	}

	public boolean attemptConnection(String target, int port) { // Attempt a connection, subsequently create object I/O streams.
		System.out.println("[attemptConnection] " + target + ":" + port);
		try {
			InetAddress addr = InetAddress.getByName(target);
			InetSocketAddress saddr = new InetSocketAddress(addr, port);
			return attemptConnection(saddr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean attemptConnection(NetHostInfo nhi) {
		System.out.println("[attemptConnection] " + nhi);
		return attemptConnection(nhi.socketAddress);
	}
	public boolean attemptConnection(InetSocketAddress givenSaddr) { // Attempt a connection, subsequently create object I/O streams.
		System.out.println("[attemptConnection] Connect!");
		if (isConnected()) throw new IllegalStateException("Cannot attempt new connection: connection already established!");
		final InetSocketAddress saddr;
		if (givenSaddr.isUnresolved()) {
			saddr = new InetSocketAddress(givenSaddr.getAddress(), givenSaddr.getPort());
			if (saddr.isUnresolved()) {
				invokeListeners((l) -> l.unresolvedAddress(saddr));
				return false;
			}
		} else {
			saddr = givenSaddr;
		}
		final Socket s = new Socket();
		try {
			final int TIMEOUT = 3000; // TODO: Allow for user variable in timeout
			try (s) {
				s.connect(saddr, TIMEOUT); 
				acquireConnection(new NetConnection(self, s, false));
				invokeListeners((l) -> l.connectionAttained(connection));
				return true;
			} catch (ConnectException ex) {
				invokeListeners((l) -> l.refusedConnection(saddr));
			} catch (SocketTimeoutException ex) {
				invokeListeners((l) -> l.connectionTimeout(saddr, TIMEOUT));
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	private void acquireConnection(NetConnection c) {
		connection = c;
		connection.addListener(this);
		if (isListening()) server.stopListening();
	}
	
	public boolean attemptDisconnect(boolean locallyInitiated) { // Destroy socket and streams of connection
		System.out.println("[attemptDisconnect] Disconnect!");
		if (connection == null) throw new IllegalStateException("Connection is null!");
		if (!connection.isConnected()) throw new IllegalStateException("Connection isn't connected!");
		
		boolean closedSuccessfully = connection.disconnect(locallyInitiated);
		invokeListeners((l) -> l.connectionClosed(connection));

		connection.removeListener(this);
		connection = null;
		return closedSuccessfully;
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
	public void handshakeCompleted(Socket s) { }
	@Override
	public void handshakeFailed(Socket s, NetHandshakeException e) { }
	@Override
	public void netMessageReceived(NetMessage nm) {
		System.out.println("NetworkController.NMR: " + nm);
	}
	// Server
	@Override
	public void connectionReceived(NetConnection c) {
		acquireConnection(c);
	}
	@Override
	public void beganListening() {
		System.out.println("NetworkController.beganListening");
		try {
			netFinder.inform();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void stoppedListening() {
		System.out.println("NetworkController.stoppedListening");
		try {
			netFinder.dispose();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
