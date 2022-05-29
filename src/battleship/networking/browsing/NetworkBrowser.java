package battleship.networking.browsing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;

import battleship.networking.NetConnectionFrame;
import battleship.networking.NetHandshakeException;
import battleship.networking.NetMessage;
import battleship.networking.NetUser;
import battleship.networking.NetworkManager;

public class NetworkBrowser {

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
		public void removedFromCache(int i, NetHostInfo nhi);
		public void addedToCache(int i, NetHostInfo nhi);
		public void clearedCache(int prevLength);
		public void cacheContentChanged(int offset, int length);
	}
	public class ListenerAdapter implements Listener {
		@Override public void removedFromCache(int i, NetHostInfo nhi) { }
		@Override public void addedToCache(int i, NetHostInfo nhi) { }
		@Override public void clearedCache(int prevLength) { }
		@Override public void cacheContentChanged(int offset, int length) { }
	}
	
	private ArrayList<NetHostInfo> cache = new ArrayList<>();
	public int cacheSize() { return cache.size(); }
	public NetHostInfo getHostFromCache(int i) { return cache.get(i); }
	
	private void attemptCache(NetHostInfo i) {
		if (i.user().equals(self)) return;
		if (cache.contains(i)) return;
		int idx = cache.size();
		cache.add(i);
		invokeListeners((l) -> l.addedToCache(idx, i));
	}
	public void clearCache() {
		int prevSize = cache.size();
		cache.clear();
		invokeListeners((l) -> l.clearedCache(prevSize));
	}
	public void refreshListeners() {
		invokeListeners((l) -> l.cacheContentChanged(0, cache.size()));
	}
	
	// Net Users
	private NetUser self = NetUser.Factory.random();
	public NetUser getMyNetUser() { return self; }
	
	// Multicast
	private MulticastSocket socket;
	private InetAddress mcastaddr;
	private InetSocketAddress group;
	private NetworkInterface netIf;
	
	public NetworkBrowser() {
		establishAndJoin();
	}

	private void establishAndJoin() {
		try {
			// Establish
			final int port = 6789;
			mcastaddr = InetAddress.getByName("227.145.62.176");
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
			int packetsReceived = 0;
			while (true) {
				DatagramPacket r = new DatagramPacket(rec, rec.length);
				try {
					if (packetsReceived++ < 1) query();
					socket.receive(r);
					NetBrowserMessage nbm = NetBrowserMessage.fromBytes(r.getData());
//					System.out.println("R: " + nbm);
					switch (nbm.getType()) {
					case INFO:
						attemptCache(nbm.getHostInfo());
						break;
					case QUERY:
						inform();
						break;
					default:
						throw new IllegalArgumentException("Unhandled NetBrowserMessage.Type: " + nbm.getType().toString());
					}
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
			System.out.println("LEFT GROUP!");
		}, "MulticastListenerThread").start();
	}
	
	// Send info
	public void inform() throws IOException {
		sendMessage(NetBrowserMessage.Factory.hostInfo(NetHostInfo.Factory.local(2048, self)));
	}

	// Send scan
	public void query() throws IOException {
		sendMessage(NetBrowserMessage.Factory.query());
	}
	private void sendMessage(NetBrowserMessage msg) throws IOException {
//		System.out.println("S: " + info);
		byte[] data = msg.getBytes();
		DatagramPacket i = new DatagramPacket(data, data.length, group);
		socket.send(i);
	}
	
	public void attemptConnection(NetHostInfo i) {
		NetworkManager m = new NetworkManager(self);
		m.addListener(new NetworkManager.Listener() {
			@Override 
			public void unresolvedAddress(InetSocketAddress a) {
				System.out.println("unresolvedAddress");
			}
			@Override
			public void stoppedListening() {
				System.out.println("stoppedListening");
			}
			@Override
			public void refusedConnection(InetSocketAddress a) {
				System.out.println("refusedConnection");
			}
			@Override
			public void netMessageReceived(NetMessage nm) {
				System.out.println("netMessageReceived");
			}
			@Override
			public void handshakeFailed(Socket s, NetHandshakeException e) {
				System.out.println("handshakeFailed");
			}
			@Override
			public void handshakeCompleted(Socket s) {
				System.out.println("handshakeCompleted");
			}
			@Override
			public void connectionTimeout(InetSocketAddress a, int toMs) {
				System.out.println("connectionTimeout");
			}
			@Override
			public void connectionClosed(Socket s) {
				System.out.println("connectionClosed");
			}
			@Override
			public void connectionAttained(Socket s) {
				System.out.println("beganListening");
			}
			@Override
			public void beganListening() {
				System.out.println("beganListening");
			}
		});
		if (m.attemptConnection(i.socketAddress)) {
			new NetConnectionFrame(m, null);
		} else {
			System.out.println("Failure");
		}
	}
	
}
