package battleship.networking.log;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

public class LogListCellRenderer implements ListCellRenderer<LogMessage> {

	private JLabel l = new JLabel();
	
	private static final Color HOME_CHAT = new Color(127,127,255), AWAY_CHAT = new Color(255,180,115), CHAT_BACK = new Color(25,25,25),
			NET_BACK = Color.BLACK, NET_POS = Color.GREEN, NET_NEG = Color.RED,
			MISC_BACK = Color.LIGHT_GRAY, MISC_FORE = Color.YELLOW;
	
	@Override
	public Component getListCellRendererComponent(JList<? extends LogMessage> list, LogMessage msg, int index,
			boolean isSelected, boolean cellHasFocus) {
		l.setText(msg.getContent());
		l.setOpaque(true);
		l.setHorizontalAlignment(SwingConstants.CENTER);
		switch (msg.getCategory()) {
		case CHAT:
			l.setBackground(CHAT_BACK);
			l.setForeground(msg.originatedFromRemote() ? AWAY_CHAT:HOME_CHAT);
			l.setHorizontalAlignment(msg.originatedFromRemote() ? SwingConstants.LEFT : SwingConstants.RIGHT);
			break;
		case NETWORK_POSITIVE:
			l.setBackground(NET_BACK);
			l.setForeground(NET_POS);
			break;
		case NETWORK_NEGATIVE:
			l.setBackground(NET_BACK);
			l.setForeground(NET_NEG);
			break;
		default:
			l.setBackground(MISC_BACK);
			l.setForeground(MISC_FORE);
			break;
		}
		
		return l;
	}

}
