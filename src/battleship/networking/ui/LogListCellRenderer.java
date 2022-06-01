package battleship.networking.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import battleship.networking.log.LogMessage;
import battleship.networking.log.LogMessage.Category;

// This class draws the LogMessages in a list
public class LogListCellRenderer implements ListCellRenderer<LogMessage> {

	private JLabel l = new JLabel();
	
	private static final Color HOME_CHAT = new Color(127,127,255), AWAY_CHAT = new Color(255,180,115), CHAT_BACK = new Color(25,25,25),
			NET_BACK = Color.BLACK, NET_POS = Color.GREEN, NET_NEG = Color.RED,
			MISC_BACK = Color.LIGHT_GRAY, MISC_FORE = Color.YELLOW;
	
	@Override
	public Component getListCellRendererComponent(JList<? extends LogMessage> list, LogMessage msg, int index,
			boolean isSelected, boolean cellHasFocus) {
		// https://stackoverflow.com/questions/2420742/make-a-jlabel-wrap-its-text-by-setting-a-max-width
		l.setText(String.format("<html><div WIDTH=%d style=\"text-align:%s;\">%s</div></html>", // TODO: Fix width adding a horizontal scroll when vertical scroll occurs
				list.getWidth(), 
				msg.getCategory() == Category.CHAT ? (msg.originatedFromRemote() ?"left":"right") : "center",
				msg.getContent()));
		l.setOpaque(true);
		l.setHorizontalAlignment(SwingConstants.CENTER);
		l.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		switch (msg.getCategory()) {
		case CHAT:
			l.setBackground(CHAT_BACK);
			l.setForeground(msg.originatedFromRemote() ? AWAY_CHAT:HOME_CHAT);
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
