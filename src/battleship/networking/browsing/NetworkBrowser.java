package battleship.networking.browsing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.ArrayList;

import battleship.networking.NetUser;

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
	public interface Listener { }
	
	// Net Users
	private NetUser self = NetUser.Factory.random();
	public NetUser getMyNetUser() { return self; }
	
	// Multicast
	private MulticastSocket socket;
	
	public NetworkBrowser() {
		try {
			// Establish
			final int port = 6789;
			InetAddress mcastaddr = InetAddress.getByName("227.145.62.176");
			InetSocketAddress group = new InetSocketAddress(mcastaddr, port);
			NetworkInterface netIf = NetworkInterface.getNetworkInterfaces().nextElement();
			socket = new MulticastSocket(port);
			
			// Send info
			NetBrowserMessage info = NetBrowserMessage.Factory.hostInfo(NetHostInfo.Factory.local(2048, self));
			byte[] data = info.getBytes();
			DatagramPacket i = new DatagramPacket(data, data.length, group);
			socket.send(i);
			
			// Listen
			socket.joinGroup(new InetSocketAddress(mcastaddr, 0), netIf);
			new Thread(() -> {
				byte[] rec = new byte[2048];
				while (true) {
					DatagramPacket r = new DatagramPacket(rec, rec.length);
					try {
						socket.receive(r);
						NetBrowserMessage nbm = NetBrowserMessage.fromBytes(r.getData());
						switch (nbm.getType()) {
						case INFO:
							System.out.println("Rec: " + nbm.getHostInfo().toString());
							break;
						case SCAN:
							System.out.println("Rec: Scan req!");
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
				System.out.println("LEFT GROUP!");
			}, "MulticastListenerThread").start();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// Send scan
			NetBrowserMessage scan = NetBrowserMessage.Factory.scan();
			data = scan.getBytes();
			i = new DatagramPacket(data, data.length, group);
			socket.send(i);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
