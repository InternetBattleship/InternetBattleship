package battleship.networking.messaging;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class SocketStreams {

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
		// Closed:
		public void streamsClosed(Socket s);
		// Receiving:
		public void objectReceived(Object o);
	}
	
	// Socket
	private Socket sock;
	
	public SocketStreams(Socket s) {
		sock = s;
		makeStreams();
	}

	private Thread inputThread;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	public void sendObject(Object o) { // Send a message through object stream to the opponent
		System.out.println("[SocketStreams] Connected: " + sock.isConnected() +", Sending obj: " + o);
		if (!sock.isConnected()) throw new IllegalStateException("Not connected!");
		try {
			oos.writeObject(o);
		} catch (SocketException e) {
			System.err.println("Socket likely closed by machine");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public boolean isConnected() {
		return sock.isConnected() && (!sock.isClosed());
	}
	private void makeStreams() { // Construct streams from general connection socket
		if (!isConnected()) throw new IllegalStateException("Not connected!");
		try {
			// Setup streams
			oos = new ObjectOutputStream(sock.getOutputStream());
//			oos.flush();
			ois = new ObjectInputStream(sock.getInputStream());
			
			// Listen on input stream
			inputThread = makeInputThread();
			inputThread.start();
		} catch (EOFException e) {
			System.err.println("EOFException!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private Thread makeInputThread() {
		return new Thread(() -> {
			while (isConnected()) {
				final Object o = receiveObject();
				invokeListeners((l) -> l.objectReceived(o));
			}
			invokeListeners((l) -> l.streamsClosed(sock));
		}, "NetworkInputStreamReader");
		
	}
	private Object receiveObject() {
		Object o = null;
		try {
			o = ois.readObject();
		} catch(EOFException e) {
			System.err.println("[SocketStreams.receiveObject()] EOFException");
			close();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		return o;
	}
	private void close() {
		System.out.println("[SocketStreams.close()]");
		try {
			ois.close();
			oos.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		invokeListeners((l) -> l.streamsClosed(sock));
	}
}
