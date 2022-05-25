package battleship.networking;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

// This class should facilitate all network connection making and destroying to/from opponents.
public class NetworkManager {
	
	private static final int HANDSHAKE_TARGET = 2;
	
	// Net Users
	private NetUser self = new NetUser(), opponent = null;
	public NetUser getMyNetUser() { return self; }
	public NetUser getOpponentNetUser() { return opponent; }
	
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
		public void connectionAttained(Socket s); // Connection received or initiated
		public void connectionClosed(Socket s);
		// Server/port listening
		public void beganListening(); // Server listening for incoming
		public void stoppedListening();
		// Errors:
		public void refusedConnection(InetSocketAddress a);
		public void unresolvedAddress(InetSocketAddress a);
		public void connectionTimeout(InetSocketAddress a, int toMs);
		// Handshake:
		public void handshakeFailed(Socket s, int toMs);
		public void handshakeCompleted(Socket s);
		// Messaging
		public void netMessageReceived(NetMessage nm);
	}

	public String getStatusString() { // Displayed on UI
		if (isListening()) return "Listening at " + getLocalIPString() + ":" + getListenPort();
		if (isConnected()) return "Connected to " + opponent;
		return "Inactive";
	}
	
	// MESSAGING: TODO: Separate message handling into its own separate class
	private Thread inputThread;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	public void sendNetMessage(NetMessage nm) { // Send a message through object stream to the opponent
		if (!isConnected()) throw new IllegalStateException("Not connected!");
		try {
			oos.writeObject(nm);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void makeStreams(boolean handshakeStarter) { // Construct streams from general connection socket
		int startingStage = handshakeStarter? 0 : -1;
		if (!isConnected()) throw new IllegalStateException("Not connected!");
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());
			inputThread = new Thread(() -> {
				int handshakeLevel = startingStage;
				while (isConnected()) {
					try {
						Object o = readObject(ois);
						NetMessage nm = parseObjectToNetMessage(o);
							if (validateHandshake(handshakeLevel, nm)) {
								handshakeLevel++;
							} else {
								handleNetMessage(nm);
							}
					} catch (NetHandshakeException e) {
						System.err.println("Handshake failed: " + e.getMessage());
						break;
					} catch (ClassNotFoundException e) {
						System.err.println("[NetorkManager] Foreign class recieved!");
						break;
					} catch (IOException e) {
						e.printStackTrace();
						break;
					}
				}
				ois = null;
				oos = null;
				opponent = null;
			}, "NetworkInputStreamReader");
			inputThread.start();
			if (handshakeStarter) oos.writeObject(NetMessage.Factory.handshake(startingStage, handshakeStarter));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private Object readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		Object o = null;
		try {
			o = ois.readObject();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (EOFException e) {
			System.err.println("EOFException: " + e.getMessage());
		} catch (Exception e) {
			throw e;
		}
		return o;
	}
	private NetMessage parseObjectToNetMessage(Object o) {
			final NetMessage nm = NetMessage.class.cast(o); 
			nm.flipRemote();
			return nm;
	}
	// Returns true if local level should be incremented, throws exception handshake has failed, returns false if hands are shook
	private boolean validateHandshake(int local, NetMessage nm) throws NetHandshakeException {
		if (local == HANDSHAKE_TARGET) return false;
		if (local > HANDSHAKE_TARGET) throw new NetHandshakeException("Handshake passed target!");
		if (!nm.isHandshake()) throw new NetHandshakeException("Handshake failed!");
		int remote = nm.getHandshakeStage();
		System.out.println("[validateHandshake] Local" + local + " Remote" + remote);
		if (local+1!=remote) throw new NetHandshakeException("Handshake out of sync!");
		return true;
	}
	private void handleNetMessage(NetMessage nm) {
		if (nm.getCategory() == NetMessage.Category.CONNECTION) {
			opponent = nm.getGreeting();
		}
		invokeListeners((l) -> l.netMessageReceived(nm));
	}
	
	// GENERAL CONNECTION MANAGEMENT: TODO: Separate general connection into a new class
	public boolean isConnected() {
		return socket != null && socket.isConnected() && (!socket.isClosed());
	}
	private Socket socket;
	public void attemptConnection(String target, int port) { // Attempt a connection, subsequently create object I/O streams.
		System.out.println("[attemptConnection] Connect!");
		if (isConnected()) throw new IllegalStateException("Cannot attempt new connection: connection already established!");
		Socket s = new Socket();
		try {
			InetAddress addr = InetAddress.getByName(target);
			InetSocketAddress saddr = new InetSocketAddress(addr, port);
			if (saddr.isUnresolved()) {
				invokeListeners((l) -> l.unresolvedAddress(saddr));
				return;
			}
			final int TIMEOUT = 3000; // TODO: Allow for user variable in timeout
			try {
				s.connect(saddr, TIMEOUT); 
			} catch (ConnectException ex) {
				invokeListeners((l) -> l.refusedConnection(saddr));
				s = null;
			} catch (SocketTimeoutException ex) {
				invokeListeners((l) -> l.connectionTimeout(saddr, TIMEOUT));
				s = null;
			}
			socket = s;
			if (socket != null) {
				if (isListening()) stopListening();
				makeStreams(false);
				invokeListeners((l) -> l.connectionAttained(socket));
			} else {
				ois = null;
				oos = null;
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public boolean attemptDisconnect() { // Destroy socket and streams of connection
		System.out.println("[attemptDisconnect] Disconnect!");
		if (socket == null) throw new IllegalStateException("Socket is null");
		if (!socket.isConnected()) throw new IllegalStateException("Socket was never connected");
		if (socket.isClosed()) throw new IllegalStateException("Socket is already closed");
		try {
			socket.close();
			socket = null;
			ois = null;
			oos = null;
			invokeListeners((l) -> l.connectionClosed(socket));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	// SERVER/PORT LISTENING: TODO: Seperate listening into a seperate class
	private boolean listening = false; 
	public boolean isListening() { // returns true if currently accepting incoming connections
		return (!isConnected()) && server != null && listening;
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
		System.out.println("[listenConcurrently] Listening...");
		if (isConnected()) throw new IllegalStateException("Can't listen: already established connection!");
		listenerThread = new Thread(() -> {
			listening = true;
			try {
				server = new ServerSocket(0);
				invokeListeners((l) -> l.beganListening());
				try {
					socket = server.accept();
					makeStreams(true);
					invokeListeners((l) -> l.connectionAttained(socket));
				} catch (SocketException e) {
					System.err.println("Server socket closed successfully!");
				}
				server.close();
				server = null;
				invokeListeners((l) -> l.stoppedListening());
			} catch (IOException e) {
				e.printStackTrace();
			}
			server = null;
			listening = false;
		}, "ServerListenerThread");
		listenerThread.start();
	}
	public void stopListening() { // Halt the thread listening for new connections and destroy server socket
		System.out.println("[stopListening] Stopped listening!");
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
