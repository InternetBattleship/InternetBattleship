package battleship.networking.browsing;

import java.io.IOException;
import java.util.ArrayList;

import battleship.networking.NetServer;
import battleship.networking.NetworkController;
import battleship.networking.messaging.NetUser;

public class NetFinder implements NetMulticaster.Listener {

	// Listeners
	private ArrayList<Listener> listeners = new ArrayList<Listener>(); // List
	// Add/remove
	public void addListener(Listener l) {
		listeners.add(0, l); 
	} 
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
		if (i.user().equals(controller.getSelf())) return;
		if (cache.contains(i)) return;
		int idx = cache.size();
		cache.add(i);
		invokeListeners((l) -> l.addedToCache(idx, i));
	}
	private void disposeUser(NetUser u) {
		for (int i=cache.size()-1;i>=0;i--) {
			NetHostInfo nhi = cache.get(i);
			if (nhi.user.equals(u)) {
				final int idx = i;
				cache.remove(idx);
				invokeListeners((l)->l.removedFromCache(idx, nhi));
			}
		}
	}
	public void clearCache() {
		int prevSize = cache.size();
		cache.clear();
		invokeListeners((l) -> l.clearedCache(prevSize));
	}
	public void refreshListeners() {
		invokeListeners((l) -> l.cacheContentChanged(0, cache.size()));
	}
	
	private NetworkController controller;
	
	public NetFinder(NetworkController controller) {
		this.controller = controller;
		controller.getMulticaster().addListener(this);
	}
	
	// Send info
	public void inform() throws IOException { 
		NetServer serv = controller.getServer();
		if (serv.isListening()) {
			controller.getMulticaster().sendMessage(
					NetMulticastMessage.Factory.hostInfo(
							controller.getServer().getInfo()));
		} else {
			System.err.println("[NetFinder] Attempted informant but server wasn't listening!");
		}
	}
	// Dispose from list
	public void dispose() throws IOException { 
		controller.getMulticaster().sendMessage(
				NetMulticastMessage.Factory.dispose(controller.getSelf()));
	}

	// Send scan
	public void query() throws IOException { 
		controller.getMulticaster().sendMessage(NetMulticastMessage.Factory.query());
	}
	
	public void attemptConnection(NetHostInfo i) {
		controller.attemptConnection(i);
	}
	@Override
	public void startedMulticastListening() { }
	@Override
	public void stoppedMulticastListening() { }
	@Override
	public void multicastMessageReceived(NetMulticastMessage nbm) {
		switch (nbm.getType()) {
		case INFORM:
			attemptCache(nbm.getHostInfo());
			break;
		case QUERY:
			try {
				inform();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case DISPOSE:
			disposeUser(nbm.getUser());
			break;
		default:
			throw new IllegalArgumentException("Unhandled NetBrowserMessage.Type: " + nbm.getType().toString());
		}
	}
	
}
