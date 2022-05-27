package battleship.networking;

import java.util.ArrayList;

public class ListenerSource<T> {

	private ArrayList<Listener<T>> listeners = new ArrayList<Listener<T>>(); // List
	// Add/remove
	public void addListener(Listener<T> l) { listeners.add(0, l); } 
	public boolean removeListener(Listener<T> l) { return listeners.remove(l); }
	// Invoking
	public void invokeListeners(ListenerInvoker<T> li) {
		for (int i=listeners.size()-1;i>=0;i--) li.invoke(listeners.get(i));
	}
	private interface ListenerInvoker<E> {
		public void invoke(Listener<E> l);
	}
	public interface Listener<D> { }
}
