package battleship.networking;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import battleship.networking.messaging.NetHandshakeException;
import battleship.networking.messaging.NetMessage;
import battleship.networking.messaging.NetUser;
import battleship.networking.messaging.SocketStreams;

public class NetConnection implements SocketStreams.Listener {

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
	
	private static final int HANDSHAKE_TARGET = 3;
	
	private NetUser self = null, opponent = null;
	public NetUser getSelf() { return self; }
	public NetUser getOpponent() { return opponent; }

	private Socket socket;	
	private SocketStreams sockStreams;
	private int handshakeLevel;
	
	public NetConnection(NetUser self, Socket socket, boolean isHost) {
		if (socket == null) throw new IllegalArgumentException("Socket is null!");
		System.out.println("[NetConnection] Constructor: " + socket);
		if (self == null) throw new IllegalArgumentException("User is null!");
		if (!socket.isConnected()) throw new IllegalArgumentException("Socket isn't connected!");
		if (socket.isClosed()) throw new IllegalArgumentException("Socket is closed!");
		
		this.self = self;
		this.socket = socket;
		sockStreams = new SocketStreams(socket);
		sockStreams.addListener(this);
		
		// Start handshake
		handshakeLevel = isHost ? 0 : -1;
		if (isHost) sendNetMessage(NetMessage.Factory.handshake(handshakeLevel));
		
		System.out.println("inited handshake");
		if (!socket.isConnected()) throw new IllegalArgumentException("Socket isn't connected!");
		if (socket.isClosed()) throw new IllegalArgumentException("Socket is closed!");
	}
	public String getStatus() {
		return "Connected to " + getOpponent();
	}
	public boolean isConnected() {
		return socket != null && socket.isConnected() && (!socket.isClosed());
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
		sockStreams = null;
		resetHandshakeStatus();
		return closedSuccessfully;
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
		// Exceptions
		if ((!nm.isHandshake()) && (!localCompletedHandshake)) throw new NetHandshakeException("Non-handshake messages can't be sent until handshake completed: " + nm.getCategory());
		if (nm.isHandshake() && remoteCompletedHandshake && localCompletedHandshake) throw new NetHandshakeException("Excessive handshaking, already completed!");
		if (local > HANDSHAKE_TARGET) throw new NetHandshakeException("Local handshake stage passed local target!");
		
		// Pass through
		if ((!nm.isHandshake()) && localCompletedHandshake) return HandshakeState.NON_HANDSHAKE;
		
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
	public void sendNetMessage(NetMessage nm) { // Send a message through object stream to the opponent
		sockStreams.sendObject(nm);
	}
	@Override
	public void streamsClosed(Socket s) {
		disconnect(false);
	}
	@Override
	public void objectReceived(Object o) {
		if (!(o instanceof NetMessage)) {
			System.err.println("[NetConnection.objectReceived] Foreign object not handled: " + o);
			return;
		}
		final NetMessage nm = (NetMessage) o;
		try {
			HandshakeState state = processHandshake(handshakeLevel, nm);
			switch (state) {
			case INCREMENT:
				handshakeLevel++;
				break;
			case COMPLETED:
				break;
			case NON_HANDSHAKE:
				handleNetMessage(nm);
				break;
			default:
				throw new NetHandshakeException("Unknown HandshakeState returned: " + state);
			}
		} catch (NetHandshakeException e) {
			System.err.println("Handshake failed: " + e.getMessage());
			invokeListeners((l) -> l.handshakeFailed(socket, e));
		}
	}
}
