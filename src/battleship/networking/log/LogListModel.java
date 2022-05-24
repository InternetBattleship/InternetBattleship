package battleship.networking.log;

import java.util.ArrayList;

import javax.swing.AbstractListModel;

public class LogListModel extends AbstractListModel<LogMessage> {

	private ArrayList<LogMessage> messages = new ArrayList<LogMessage>();
	
	public void log(LogMessage lm) {
		messages.add(0,lm);
		fireIntervalAdded(this, 0,0);
	}
	
	public void clear() {
		int last = messages.size()-1;
		messages.clear();
		fireContentsChanged(this, 0, last);
	}
	
	@Override
	public int getSize() {
		return messages.size();
	}

	@Override
	public LogMessage getElementAt(int index) {
		return messages.get(index);
	}

}
