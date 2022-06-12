package battleship.networking.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import battleship.networking.NetConnection;
import battleship.networking.log.LogListModel;
import battleship.networking.log.LogMessage;
import battleship.networking.messaging.NetMessage;

public class NetConnectionFrame implements NetConnection.Listener {
	
	public JFrame frame = new JFrame("IBConnection"); // JFrame
	public JLabel statusLabel = new JLabel("Not initialized"); // Status label
	// Log/chat
	private LogListModel lm = new LogListModel();
	private JList<LogMessage> logList = new JList<LogMessage>(lm); // TODO: Fix weird bug with all items disappearing sometimes	
	public JTextField chatField = new JTextField();
	
	private NetConnection connection; // Logic
	
	public NetConnectionFrame(NetConnection con, JFrame parentFrame) {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
		    public void windowClosing(WindowEvent e) {
				System.out.println("Closing");
				try {
					dispose();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			@Override
		    public void windowClosed(WindowEvent e) {
				System.out.println("Closed");
			}
		});

		setConnection(con);
		Container c = frame.getContentPane();
		c.setLayout(new BoxLayout(c, BoxLayout.PAGE_AXIS));
		statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		statusLabel.setAlignmentX(0.5f);
		frame.add(statusLabel);
		c.add(makeLog());
		c.add(chatField);
		frame.setLocationRelativeTo(parentFrame);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void dispose() {
		connection.disconnect(true);
		frame.dispose();
	}
	
	private static final Color CONNECTED_COL = new Color(0,127,0), ERROR_COL = new Color(255,0,0);
	private void updateStatus() {
		setStatus(connection.getStatus(), connection.isConnected() ? CONNECTED_COL : ERROR_COL);
	}
	
	public void setStatus(String str, Color c) {
		SwingUtilities.invokeLater(() -> {
			statusLabel.setForeground(c);
			statusLabel.setText(str);
		});
	}
	
	private JComponent makeLog() {
		JScrollPane jsp = new JScrollPane(logList);
		logList.setCellRenderer(new LogListCellRenderer());
		jsp.setAutoscrolls(true);
		return jsp;
	}
	public void setConnection(NetConnection c) {
		if (c == null) throw new IllegalArgumentException("Connection is null!");
		connection = c;
		frame.setTitle("IB - " + connection.getSelf());
		connection.addListener(this);
		chatField.addActionListener(sendChat);
		updateStatus();
	}
	
	private ActionListener sendChat = (e) -> {
				String msg = chatField.getText().trim();
				if (msg.length() > 0) sendNetMessage(NetMessage.Factory.chat(msg));
				chatField.setText("");
			};

	public void sendNetMessage(NetMessage nm) {
		logNetMessage(nm);
		connection.sendNetMessage(nm);
	}
	public void logNetMessage(NetMessage nm) {
		switch (nm.getCategory()) {
		case CHAT:
			lm.add(LogMessage.chatLog(nm.getMessage(), nm.isRemote()));
			break;
		case CONNECTION:
			lm.add(LogMessage.chatLog(nm.getGreeting().toString(), nm.isRemote()));
			break;
		case DISCONNECT:
			lm.add(LogMessage.chatLog("Disconnect notification", nm.isRemote()));
			break;
		default:
			throw new IllegalArgumentException("Unhandled NetMessage category: " + nm.getCategory());
		}
	}

	@Override
	public void netMessageReceived(NetMessage nm) {
		logNetMessage(nm);
	}

}
