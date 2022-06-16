package battleship.networking;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import battleship.networking.NetMessage.Category;

// Wrapping for reading/sending objects in/out of a socket
public class SocketStreams {
	
	private NetUser self, away = null;
	public NetUser self() { return self; }
	public NetUser away() { return away; }
	
	// Socket
	private Socket sock;
	public Socket getSock() { return sock; }
	public SocketStreams(Socket s, NetUser self) {
		System.out.println("[SocketStreams] Constructor: " + s);
		sock = s;
		if (sock == null) throw new IllegalArgumentException("Socket is null!");
		if (!isActive()) throw new IllegalStateException("Socket not active!");
		try {
			// Setup streams
			oos = new ObjectOutputStream(sock.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(sock.getInputStream());
		} catch (EOFException e) {
			System.err.println("[SocketStreams] EOFException!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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
	
	public NetMessage receiveMessage() throws IllegalStateException {
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
		if (o instanceof NetMessage) {
			final NetMessage nm = (NetMessage) o;
			if (nm.getCategory()==Category.GREETING) {
				if (away != null) throw new IllegalStateException("Opponent greeted twice!");
				away = nm.getGreeting();
			}
			System.out.println("[SocketStreams] Received obj: " + nm);
			return (NetMessage) o;
		} else {
			throw new IllegalStateException("Not instance of NetMessage");
		}
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
	}
}