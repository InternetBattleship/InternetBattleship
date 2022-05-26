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
	
	private static final int HANDSHAKE_TARGET = 3;
	
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
		public void handshakeCompleted(Socket s);
		public void handshakeFailed(Socket s, NetHandshakeException e);
		// Messaging
		public void netMessageReceived(NetMessage nm);
	}

	public String getStatusString() { // Displayed on UI
		String str = "Inactive";
		if (isListening()) str = "Listening at " + getLocalIPString() + ":" + getListenPort();
		if (isConnected()) str =  "Connected to " + opponent;
		System.out.println(str);
		return str;
	}
	
	// MESSAGING: TODO: Separate message handling into its own separate class
	private Thread inputThread;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	public void sendNetMessage(NetMessage nm) { // Send a message through object stream to the opponent
		System.out.println("Sending NetMessage: " + nm.getCategory());
		if (!isConnected()) throw new IllegalStateException("Not connected!");
		try {
			oos.writeObject(nm);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void makeStreams(boolean handshakeStarter) { // Construct streams from general connection socket
		if (!isConnected()) throw new IllegalStateException("Not connected!");
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());
			inputThread = new Thread(() -> {
				int handshakeLevel = handshakeStarter ? 0 : -1;
				while (isConnected()) {
					try {
						Object o = readObject(ois);
						NetMessage nm = parseObjectToNetMessage(o);
							switch (processHandshake(handshakeLevel, nm)) {
							case INCREMENT:
								handshakeLevel++;
								break;
							case COMPLETED:
//								sendNetMessage(NetMessage.Factory.connection(self));
								break;
							case NON_HANDSHAKE:
								handleNetMessage(nm);
								break;
							default:
								throw new NetHandshakeException("Unknown HandshakeState returned!");
							}
					} catch (NetHandshakeException e) {
						System.err.println("Handshake failed: " + e.getMessage());
						invokeListeners((l) -> l.handshakeFailed(socket, e));
						break;
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
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
			if (handshakeStarter) sendNetMessage(NetMessage.Factory.handshake(0));
		} catch (IOException e) {
			e.printStackTrace();
			attemptDisconnect(true);
		}
	}
	private Object readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		Object o = null;
		try {
			o = ois.readObject();
		} catch (SocketException e) {
			System.err.println("[NetworkManager.readObject] SOCKET EXCEPTION");
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
			if (nm == null) throw new IllegalArgumentException("Object is null!");
			nm.flipRemote();
			return nm;
	}
	// Returns true if local level should be incremented, throws exception handshake has failed, returns false if hands are shook
	private boolean localCompletedHandshake = false;
	private boolean remoteCompletedHandshake = false;
	private void resetHandshakeStatus() {
		localCompletedHandshake = false;
		remoteCompletedHandshake = false;
	}
	private enum HandshakeState { INCREMENT, COMPLETED, NON_HANDSHAKE; }
	private HandshakeState processHandshake(int local, NetMessage nm) throws NetHandshakeException {
		if ((!nm.isHandshake()) && (!localCompletedHandshake)) throw new NetHandshakeException("Non-handshake messages can't be sent until handshake completed: " + nm.getCategory());
		if (nm.isHandshake() && remoteCompletedHandshake && localCompletedHandshake) throw new NetHandshakeException("Excessive handshaking, already completed!");
		if ((!nm.isHandshake()) && localCompletedHandshake) return HandshakeState.NON_HANDSHAKE;
		if (local > HANDSHAKE_TARGET) throw new NetHandshakeException("Local handshake stage passed local target!");
		int remote = nm.getHandshakeStage();
		if (remote == HANDSHAKE_TARGET) remoteCompletedHandshake = true;
		if (remote > HANDSHAKE_TARGET) throw new NetHandshakeException("Remote handshake stage passed local target!");
		if (local+1 != remote && local != remote) throw new NetHandshakeException("Handshake out of sync!");
		if (local < HANDSHAKE_TARGET) {
			local++;
			handshakeCheck(local, remoteCompletedHandshake);
			return HandshakeState.INCREMENT;
		}
		if (localCompletedHandshake) throw new NetHandshakeException("Handshake cannot be completed twice!");
		handshakeCheck(local, remoteCompletedHandshake);
		return HandshakeState.COMPLETED;
	}
	private void handshakeCheck(int local, boolean remoteComplete) {
		if (local == HANDSHAKE_TARGET) localCompletedHandshake = true;
		if (!remoteComplete) sendNetMessage(NetMessage.Factory.handshake(local));
		if (localCompletedHandshake && remoteCompletedHandshake) sendNetMessage(NetMessage.Factory.connection(self));
	}

	private void handleNetMessage(NetMessage nm) {
		switch (nm.getCategory()) {
		case CONNECTION:
			opponent = nm.getGreeting();
			break;
		case DISCONNECT:
			attemptDisconnect(false);
			break;
		default:
			invokeListeners((l) -> l.netMessageReceived(nm));
			break;
		}
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
	public boolean attemptDisconnect(boolean locallyInitiated) { // Destroy socket and streams of connection
		System.out.println("[attemptDisconnect] Disconnect!");
		if (socket == null) throw new IllegalStateException("Socket is null");
		if (!socket.isConnected()) throw new IllegalStateException("Socket was never connected");
		if (socket.isClosed()) throw new IllegalStateException("Socket is already closed");
		if (locallyInitiated) sendNetMessage(NetMessage.Factory.disconnect());
		boolean closedSuccessfully = false;
		try {
			socket.close();
			closedSuccessfully = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		socket = null;
		ois = null;
		oos = null;
		resetHandshakeStatus();
		invokeListeners((l) -> l.connectionClosed(socket));
		return closedSuccessfully;
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
					invokeListeners((l) -> l.stoppedListening());
					invokeListeners((l) -> l.connectionAttained(socket));
				} catch (SocketException e) {
					System.err.println("Server socket closed successfully!");
				}
				server.close();
				server = null;
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
