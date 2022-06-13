package battleship.networking.messaging;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
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
	private static final SocketOption<?>[] opts = new SocketOption[] {
			StandardSocketOptions.IP_MULTICAST_IF,
			StandardSocketOptions.IP_MULTICAST_LOOP,
			StandardSocketOptions.IP_MULTICAST_TTL,
			StandardSocketOptions.IP_TOS,
			StandardSocketOptions.SO_BROADCAST,
			StandardSocketOptions.SO_KEEPALIVE,
			StandardSocketOptions.SO_LINGER,
			StandardSocketOptions.SO_RCVBUF,
			StandardSocketOptions.SO_SNDBUF,
			StandardSocketOptions.SO_REUSEADDR,
			StandardSocketOptions.SO_REUSEPORT,
			StandardSocketOptions.TCP_NODELAY,
	};
	public static final void printSocketOptions(Socket s) {
		for (SocketOption<?> so : opts) {
			try {
				System.out.println(so.name() + ": " + s.getOption(so));
			} catch (Exception e) {
				System.err.println(so.name());
			}
		}
	}
	
	public SocketStreams(Socket s) {
		System.out.println("[SocketStreams] Constructor: " + s);
		sock = s;
		if (sock == null) throw new IllegalArgumentException("Socket is null!");
		if (!isActive()) throw new IllegalStateException("Socket not active!");
		try {
			// Setup streams
			oos = new ObjectOutputStream(sock.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(sock.getInputStream());
			
			// Listen on input stream
			inputThread = makeInputThread();
			inputThread.start();
		} catch (EOFException e) {
			System.err.println("[SocketStreams] EOFException!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Thread inputThread;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	
	public void sendObject(Object o) { // Send a message through object stream to the opponent
		System.out.println("[SocketStreams] Connected: " + sock.isConnected() +", Sending obj: " + o);
		if (!isActive()) throw new IllegalStateException("Not connected!");
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
		return sock.isConnected();
	}
	public boolean isActive() { // Connected and open
		return sock.isConnected() && (!sock.isClosed());
	}
	public boolean isClosed() {
		return sock.isClosed();
	}
	
	private Thread makeInputThread() {
		return new Thread(() -> {
			while (isActive()) {
				try {
				final Object o = receiveObject();
					invokeListeners((l) -> l.objectReceived(o));
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			}
			invokeListeners((l) -> l.streamsClosed(sock));
		}, "NetworkInputStreamReader");
		
	}
	
	private Object receiveObject() throws IllegalStateException{
		Object o = null;
		try {
			o = ois.readObject();
		} catch (SocketException e) {
			System.err.println("[SocketStreams] Socket closed!");
			intl_close();
			throw new IllegalStateException(e);
		} catch(EOFException e) {
			System.err.println("[SocketStreams.receiveObject()] EOFException");
			intl_close();
			throw new IllegalStateException(e);
		} catch (ClassNotFoundException e) {
			System.err.println("[SocketStreams.receiveObject()] ClassNotFoundException");
			intl_close();
			throw new IllegalStateException(e);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return o;
	}
	
	public void close() {
		if (!isClosed()) intl_close();
	}
	
	private void intl_close() {
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
