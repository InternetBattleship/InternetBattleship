package battleship.networking;

import java.net.Socket;
import java.util.ArrayList;

import battleship.networking.messaging.NetMessage;
import battleship.networking.messaging.NetUser;
import battleship.networking.messaging.SocketStreams;

public class NetConnection implements SocketStreams.Listener {

	// LISTENERS
	private ArrayList<Listener> listeners = new ArrayList<Listener>(); // List
	// Add/remove
	public void addListener(Listener l) { 
		if (listeners.contains(l)) {
			System.err.println("[NetConnection.addListener] Redundant add!");
		} else {
			listeners.add(0, l);
		}
	} 
	public boolean removeListener(Listener l) { return listeners.remove(l); }
	// Invoking
	public void invokeListeners(ListenerInvoker li) {
		for (int i=listeners.size()-1;i>=0;i--) li.invoke(listeners.get(i));
	}
	private interface ListenerInvoker {
		public void invoke(Listener l);
	}
	public interface Listener { // Listener outline
		// State
		public void connectionBegan(); // Sent after greeting received, from NetworkInputThread
		public void connectionStopped();
		
		// Messaging
		public void netMessageReceived(NetMessage nm); // from NetworkInputThread
	}
	
	// Initiating client-side connections
	public static NetConnection connectTo(NetworkController ctrl, String addr, int port) throws Exception {
		try (Socket s = new Socket(addr, port)) {
			NetConnection c = new NetConnection(ctrl, s, false);
			return c;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
	
	// Network identities
	private boolean greeted = false; // Opponent has sent their identity, must be first message
	private NetUser self = null, opponent = null;
	public NetUser getSelf() { return self; }
	public NetUser getOpponent() { return opponent; }

	private SocketStreams sockStreams; // All socket usage contained within this property
	
	public NetConnection(NetworkController ctrl, Socket socket, boolean isHost) {
		self = ctrl.getSelf();
		this.addListener(ctrl);
		sockStreams = new SocketStreams(socket);
		sockStreams.addListener(this);
		sendNetMessage(NetMessage.Factory.connection(self));
	}
	
	// Connection state reporting/management
	public String getStatus() {
		if (isConnected()) {
			return "Connected to " + getOpponent();
		} else {
			return "Connection is inactive";
		}
	}
	public boolean isConnected() {
		return sockStreams.isActive();
	}
	public boolean disconnect(boolean localOrigin) {
//		for (StackTraceElement e : Thread.currentThread().getStackTrace()) System.out.println(e);
		System.out.println("[NetConnection] Disconnect, local: " + localOrigin);
		sockStreams.close();
		invokeListeners((l) -> l.connectionStopped());
		return false;
	}
	@Override
	public void streamsClosed(Socket s) {
		disconnect(false);
	}
	
	// Message sending/receiving
	public void sendNetMessage(NetMessage nm) { // Send a message through object stream to the opponent
		sockStreams.sendObject(nm);
	}
	
	@Override
	public void objectReceived(Object o) {
		if (!(o instanceof NetMessage)) {
			System.err.println("[NetConnection.objectReceived] Foreign object not handled: " + o);
			return;
		}
		final NetMessage nm = (NetMessage) o;
		handleNetMessage(nm);
	}
	private void handleNetMessage(NetMessage nm) {
		switch (nm.getCategory()) {
		case CONNECTION:
			opponent = nm.getGreeting();
			if (opponent == null) throw new IllegalStateException("Greeted with null!");
			greeted = true;
			invokeListeners((l)->l.connectionBegan());
			break;
		default:
			if (!greeted) throw new IllegalStateException(nm.getCategory() + " type receieved before greeting occured!");
			break;
		}
		invokeListeners((l) -> l.netMessageReceived(nm));
	}
}
