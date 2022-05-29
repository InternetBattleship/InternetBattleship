package battleship.networking.browsing;

import javax.swing.AbstractListModel;

public class NetBrowserListModel extends AbstractListModel<NetHostInfo> implements NetworkBrowser.Listener {

	private NetworkBrowser browser;
	
	public NetBrowserListModel(NetworkBrowser b) {
		browser = b;
		browser.addListener(this);
		browser.refreshListeners();
	}
	
	@Override
	public int getSize() { return browser.cacheSize(); }

	@Override
	public NetHostInfo getElementAt(int i) {
		return browser.getHostFromCache(i);
	}

	@Override
	public void removedFromCache(int i, NetHostInfo nhi) {
		fireIntervalRemoved(this, i, i);
	}

	@Override
	public void addedToCache(int i, NetHostInfo nhi) {
		fireIntervalAdded(this, i, i);
	}

	@Override
	public void clearedCache(int prevLength) {
		fireIntervalRemoved(this, 0, prevLength);
	}
	
	@Override
	public void cacheContentChanged(int offset, int length) {
		fireContentsChanged(this, offset, length);
	}

}
