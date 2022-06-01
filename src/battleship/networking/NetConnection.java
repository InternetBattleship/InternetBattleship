package battleship.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import battleship.networking.messaging.NetHandshakeException;
import battleship.networking.messaging.NetMessage;
import battleship.networking.messaging.NetUser;

public class NetConnection {

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
		// Handshake:
		public void handshakeCompleted(Socket s);
		public void handshakeFailed(Socket s, NetHandshakeException e);
		// Messaging
		public void netMessageReceived(NetMessage nm);
	}
	public class ListenerAdapter implements Listener { // Listener outline
		@Override public void handshakeCompleted(Socket s) { }
		@Override public void handshakeFailed(Socket s, NetHandshakeException e) { }
		@Override public void netMessageReceived(NetMessage nm) { }
	}
	
	private static final int HANDSHAKE_TARGET = 3;
	
	private NetUser self = null, opponent = null;
	public NetUser getSelf() { return self; }
	public NetUser getOpponent() { return opponent; }

	private Socket socket;	
	
	public NetConnection(NetUser self, Socket socket, boolean isHost) {
		if (self == null) throw new IllegalArgumentException("User is null!");
		if (socket == null) throw new IllegalArgumentException("Socket is null!");
		this.self = self;
		System.out.println("[NetConnection] constructor: " + socket.isConnected() + ", " + socket.isClosed() + ", " + socket);
		this.socket = socket;
		makeStreams(isHost);
	}
	public String getStatus() {
		return "Connected to " + getOpponent();
	}
	public boolean isConnected() {
		return socket != null && socket.isConnected() && (!socket.isClosed());
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
						NetMessage nm = readNetMessage();
						switch (processHandshake(handshakeLevel, nm)) {
						case INCREMENT:
							handshakeLevel++;
							break;
						case COMPLETED:
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
					}
				}
				ois = null;
				oos = null;
				opponent = null;
			}, "NetworkInputStreamReader");
			inputThread.start();
			System.out.println("[makeStreams] Closed socket: " + socket.isClosed());
			if (handshakeStarter) sendNetMessage(NetMessage.Factory.handshake(0));
		} catch (IOException e) {
			e.printStackTrace();
			disconnect(true);
		}
	}
	public boolean disconnect(boolean localOrigin) {
		System.out.println("[NetConnection] Disconnect");
		if (socket == null) throw new IllegalStateException("Socket is null");
		if (!socket.isConnected()) throw new IllegalStateException("Socket was never connected");
		if (socket.isClosed()) throw new IllegalStateException("Socket is already closed");
		if (localOrigin) sendNetMessage(NetMessage.Factory.disconnect());
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
		return closedSuccessfully;
	}
	
	private NetMessage readNetMessage() {
		try {
			Object o = ois.readObject();
			if (!(o instanceof NetMessage)) throw new IllegalArgumentException("Object isn't a NetMessage instance!");
			final NetMessage nm = (NetMessage) o;
			nm.flipRemote();
			return nm;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
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
			handshakeCheck(local);
			return HandshakeState.INCREMENT;
		}
		if (localCompletedHandshake) throw new NetHandshakeException("Handshake cannot be completed twice!");
		handshakeCheck(local);
		return HandshakeState.COMPLETED;
	}
	private void handshakeCheck(int local) {
		if (local == HANDSHAKE_TARGET) localCompletedHandshake = true;
		if (!remoteCompletedHandshake) sendNetMessage(NetMessage.Factory.handshake(local));
		if (localCompletedHandshake && remoteCompletedHandshake) sendNetMessage(NetMessage.Factory.connection(self));
	}

	private void handleNetMessage(NetMessage nm) {
		switch (nm.getCategory()) {
		case CONNECTION:
			opponent = nm.getGreeting();
			break;
		case DISCONNECT:
			disconnect(false);
			break;
		default:
			invokeListeners((l) -> l.netMessageReceived(nm));
			break;
		}
	}
	private Thread inputThread;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	public void sendNetMessage(NetMessage nm) { // Send a message through object stream to the opponent
		System.out.println("[NetConnection] Connected: " + socket.isConnected() +", Sending NetMessage: " + nm.getCategory());
		if (!isConnected()) throw new IllegalStateException("Not connected!");
		try {
			oos.writeObject(nm);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
