package battleship.networking.browsing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.ArrayList;

public class NetMulticaster {
	// Listeners
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
	public interface Listener {
		public void startedMulticastListening();
		public void multicastMessageReceived(NetMulticastMessage nbm);
		public void stoppedMulticastListening();
	}
	public class ListenerAdapter implements Listener {
		public void startedMulticastListening() { }
		public void multicastMessageReceived(NetMulticastMessage nbm) { }
		public void stoppedMulticastListening() { }
	}
	
	// Multicast
	private MulticastSocket socket;
	private InetAddress mcastaddr;
	private InetSocketAddress group;
	private NetworkInterface netIf;
	
	public NetMulticaster(String groupIP, int localPort) {
		establish(groupIP, localPort);
		listen();
	}
	
	private void establish(String groupIP, int localPort) {
		try {
			// Establish
			final int port = localPort;
			mcastaddr = InetAddress.getByName(groupIP);
			group = new InetSocketAddress(mcastaddr, port);
			netIf = NetworkInterface.getNetworkInterfaces().nextElement();
			socket = new MulticastSocket(port);
			// Listen
			socket.joinGroup(new InetSocketAddress(mcastaddr, 0), netIf); // Port doesn't matter?
		} catch (IOException e) {
			e.printStackTrace();	
		}
	}
	
	private void cleanup() {
		if (socket != null && socket.isConnected() && !socket.isClosed()) socket.close();
		mcastaddr = null;
		group = null;
		netIf = null;
		socket = null;
	}
	
	private boolean listening = false;
	public boolean isListening() { return listening; }
	public void listen() {
		if (listening) throw new IllegalStateException("Browser already listening!");
		new Thread(() -> {
			listening = true;
			byte[] rec = new byte[2048];
			invokeListeners((l) -> l.startedMulticastListening());
			while (true) {
				DatagramPacket r = new DatagramPacket(rec, rec.length);
				try {
					socket.receive(r);
					NetMulticastMessage nbm = NetMulticastMessage.fromBytes(r.getData());
//					System.out.println("[listen()]R: " + nbm);
					invokeListeners((l) -> l.multicastMessageReceived(nbm));
				} catch (IOException e) {
					e.printStackTrace();
					break;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					break;
				}
			}
			// Leave
			try {
				socket.leaveGroup(group, netIf);
			} catch (IOException e) {
				e.printStackTrace();
			}
			listening = false;
			invokeListeners((l) -> l.stoppedMulticastListening());
			cleanup();
		}, "MulticastListenerThread").start();
	}

	public void sendMessage(NetMulticastMessage msg) throws IOException {
//		System.out.println("S: " + msg);
		byte[] data = msg.getBytes();
		DatagramPacket i = new DatagramPacket(data, data.length, group);
		socket.send(i);
	}
}
