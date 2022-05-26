package battleship.networking;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import battleship.networking.log.LogListCellRenderer;
import battleship.networking.log.LogListModel;
import battleship.networking.log.LogMessage;

public class NetConnectionFrame implements NetworkManager.Listener {
	
	public JFrame frame = new JFrame("IBConnection"); // JFrame
	public JLabel statusLabel = new JLabel("Not initialized"); // Status label
	// Log/chat
	private LogListModel lm = new LogListModel();
	private JList<LogMessage> logList = new JList<LogMessage>(lm); // TODO: Fix weird bug with all items disappearing sometimes	
	public JTextField chatField = new JTextField();
	
	private NetworkManager manager; // Logic
	
	public NetConnectionFrame(NetworkManager m, JFrame parentFrame) {
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
		    public void windowClosing(WindowEvent e) {
				System.out.println("Closing");
				dispose();
			}
			@Override
		    public void windowClosed(WindowEvent e) {
				System.out.println("Closed");
			}
		});
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
		setManager(m);
	}
	public void dispose() {
		frame.dispose();
	}
	private static final Color CONNECTED_COL = new Color(0,127,0), ERROR_COL = new Color(255,0,0);
	private void updateStatus() {
		setStatus(manager.getStatusString(), manager.isConnected() ? CONNECTED_COL : ERROR_COL);
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
	public void setManager(NetworkManager m) {
		manager = m;
		if (manager == null) throw new IllegalArgumentException("Manager is null!");
		frame.setTitle("IB - " + manager.getMyNetUser().toString());
		manager.addListener(this);
		chatField.addActionListener(sendChat);
		updateStatus();
	}
	
	private ActionListener sendChat = (e) -> {
				String msg = chatField.getText().trim();
				if (msg.length() > 0) sendNetMessage(NetMessage.Factory.chat(msg));
				chatField.setText("");
//				if (e.getSource() instanceof JTextField)
//					((JTextField)e.getSource()).setText("");
			};

	public void sendNetMessage(NetMessage nm) {
		logNetMessage(nm);
		manager.sendNetMessage(nm);
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
	public void connectionAttained(Socket s) { 
		lm.add(LogMessage.networkLog("Connection attained!", true));
	}
	@Override
	public void connectionClosed(Socket s) { 
		lm.add(LogMessage.networkLog("Connection closed!", false));
	}
	@Override
	public void beganListening() { 
		lm.add(LogMessage.networkLog("Listening...", true));
	}
	@Override
	public void stoppedListening() { 
		lm.add(LogMessage.networkLog("Stopped listening.", false));
	}
	@Override
	public void refusedConnection(InetSocketAddress addr) { 
		lm.add(LogMessage.networkLog("Refused connection: " + addr.getHostString() + ":" + addr.getPort(), false));
	}
	@Override
	public void unresolvedAddress(InetSocketAddress addr) { 
		lm.add(LogMessage.networkLog("Unresolved Address: " + addr.getHostString() + ":" + addr.getPort(), false));
	}
	@Override
	public void connectionTimeout(InetSocketAddress addr, int toMs) {
		lm.add(LogMessage.networkLog("Connection timed out: " + addr.getHostString() + ":" + addr.getPort() + " after " + toMs + "ms", false));
	}
	@Override
	public void handshakeFailed(Socket s, NetHandshakeException e) {
		lm.add(LogMessage.networkLog("Handshake failed: " + e.getMessage(), false));
	}
	@Override
	public void handshakeCompleted(Socket s) {
		lm.add(LogMessage.networkLog("Handshake completed!", true));
	}

	@Override
	public void netMessageReceived(NetMessage nm) {
		logNetMessage(nm);
		switch (nm.getCategory()) {
//		case CONNECTION:
//		case DISCONNECT:
//			updateStatus();
//			break;
		default:
			updateStatus();
			System.out.println("Received NetMessage");
			break;
		}
	}

}
