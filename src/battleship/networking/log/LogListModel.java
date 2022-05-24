package battleship.networking.log;

import java.util.ArrayList;

import javax.swing.AbstractListModel;

// Simple class utilized to add and store LogMessages, can also clear history
public class LogListModel extends AbstractListModel<LogMessage> {
	private ArrayList<LogMessage> messages = new ArrayList<LogMessage>(); // List that stores all messages
	public void add(LogMessage lm) { // Appends to top of list
		messages.add(0,lm);
		fireIntervalAdded(this, 0,0);
	}
	public void clear() { // Clear the history of messages
		int last = messages.size()-1;
		messages.clear();
		fireContentsChanged(this, 0, last);
	}
	@Override public int getSize() { return messages.size(); }
	@Override public LogMessage getElementAt(int index) { return messages.get(index); }
}
