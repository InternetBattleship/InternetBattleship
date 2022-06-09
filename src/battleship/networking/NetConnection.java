package battleship.networking;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

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
		// Messaging
		public void netMessageReceived(NetMessage nm);
	}
	
	private NetworkController controller = null;
	private NetUser opponent = null;
	public NetUser getSelf() { return controller.getSelf(); }
	public NetUser getOpponent() { return opponent; }

	private Socket socket;	
	private SocketStreams sockStreams;
	
	public NetConnection(NetworkController ctrl, Socket socket, boolean isHost) {
		if (socket == null) throw new IllegalArgumentException("Socket is null!");
		System.out.println("[NetConnection] Constructor: " + socket);
		if (ctrl == null) throw new IllegalArgumentException("Controller is null!");
		if (!socket.isConnected()) throw new IllegalArgumentException("Socket isn't connected!");
		if (socket.isClosed()) throw new IllegalArgumentException("Socket is closed!");
		
		this.controller = ctrl;
		this.socket = socket;
		sockStreams = new SocketStreams(this.socket);
		sockStreams.addListener(this);
		
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
		System.out.println("[NetConnection] Disconnect, local: " + localOrigin);
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
		return closedSuccessfully;
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
		handleNetMessage(nm);
	}
}
