package battleship.networking;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import battleship.networking.log.LogListCellRenderer;
import battleship.networking.log.LogListModel;
import battleship.networking.log.LogMessage;
import layout.SpringUtilities;

public class NetTestFrame implements NetworkManager.Listener {

	public static final int PORT_UNRESTRICTED_MIN = 1024;
	public static final int PORT_MAX = 65536;
	
	public JFrame frame = new JFrame("IB");
	public JLabel statusLabel = new JLabel("Not initialized");
	public JTextField ipField = new JTextField(20);
	public SpinnerNumberModel portsnm = new SpinnerNumberModel(
			PORT_UNRESTRICTED_MIN, PORT_UNRESTRICTED_MIN, PORT_MAX, 1);
	public JSpinner portField = new JSpinner(portsnm);
	
	public static final String CONNECT = "Connect", DISCONNECT = "Disconnect",
			LISTEN = "Listen", CLOSE = "Close";
	
	public JButton connectionBtn = new JButton("Not initialized"),
			serverBtn = new JButton("Not initialized");
	
	private NetworkManager manager;
	
	private LogListModel lm = new LogListModel();
	private JList<LogMessage> logList = new JList<LogMessage>(lm);	
	
	public JTextField chatField = new JTextField();
	
	public NetTestFrame() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container c = frame.getContentPane();
		c.setLayout(new BoxLayout(c, BoxLayout.PAGE_AXIS));
		frame.add(statusLabel);
		frame.add(makeAddrPane());
		frame.add(makeBtnPane());
		frame.add(makeLog());
		frame.add(chatField);
		statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		statusLabel.setAlignmentX(0.5f);
		setClientEnabled(false);
		frame.pack();
		frame.setVisible(true);
		setManager(new NetworkManager());
	}
	
	public void setManager(NetworkManager m) {
		manager = m;
		if (manager == null) throw new IllegalArgumentException("Manager is null!");
		connectionBtn.addActionListener(connectDisconnect);
		serverBtn.addActionListener(listenClose);
		chatField.addActionListener(sendChat);
		frame.setTitle(manager.getMyNetUser().toString());
		System.out.println(frame.getTitle());
		manager.addListener(this);
		updateState();
	}
	
	private JComponent makeLog() {
		JScrollPane jsp = new JScrollPane(logList);
		logList.setCellRenderer(new LogListCellRenderer());
		jsp.setAutoscrolls(true);
		return jsp;
	}
	
	private JPanel makeAddrPane() {
		JPanel addr = new JPanel(new SpringLayout());
		JLabel ipLab = new JLabel("IP:");
		ipLab.setLabelFor(ipField);
		addr.add(ipLab);
		addr.add(ipField);
		JLabel portLab = new JLabel("Port:");
		portLab.setLabelFor(portField);
		addr.add(portLab);
		addr.add(portField);
		SpringUtilities.makeCompactGrid(addr,2,2,5,0,5,5); // Taken from https://docs.oracle.com/javase/tutorial/uiswing/layout/spring.html
		return addr;
	}
	
	public JPanel makeBtnPane() {
		JPanel btns = new JPanel();
		btns.setLayout(new BoxLayout(btns,BoxLayout.LINE_AXIS));
		btns.add(Box.createHorizontalGlue());
		btns.add(connectionBtn);
		btns.add(Box.createHorizontalStrut(5));
		btns.add(serverBtn);
		btns.setBorder(BorderFactory.createEmptyBorder(0,5,5,5));
		return btns;
	}
	
	public void setStatus(String str, Color c) {
		SwingUtilities.invokeLater(() -> {
			statusLabel.setForeground(c);
			statusLabel.setText(str);
		});
	}
	
	public void setClientEnabled(boolean enabled) {
		ipField.setEnabled(enabled);
		portField.setEnabled(enabled);
	}

	@Override
	public void connectionAttained(Socket s) { 
		lm.log(LogMessage.networkLog("Connection attained!", true));
		updateState(); 
		sendNetMessage(NetMessage.connection(manager.getMyNetUser()));
	}
	@Override
	public void connectionClosed(Socket s) { 
		lm.log(LogMessage.networkLog("Connection closed!", false));
		updateState(); 
		manager.sendNetMessage(NetMessage.disconnect());
	}
	@Override
	public void beganListening() { 
		lm.log(LogMessage.networkLog("Listening...", true));
		updateState(); 
	}
	@Override
	public void stoppedListening() { 
		lm.log(LogMessage.networkLog("Stopped listening.", false));
		updateState(); 
	}
	@Override
	public void refusedConnection(InetSocketAddress addr) { 
		lm.log(LogMessage.networkLog("Refused connection: " + addr.getHostString() + ":" + addr.getPort(), false));
		updateState(); 
	}
	@Override
	public void unresolvedAddress(InetSocketAddress addr) { 
		lm.log(LogMessage.networkLog("Unresolved Address: " + addr.getHostString() + ":" + addr.getPort(), false));
		updateState(); 
	}
	public void connectionTimeout(InetSocketAddress addr, int toMs) {
		lm.log(LogMessage.networkLog("Connection timed out: " + addr.getHostString() + ":" + addr.getPort() + " after " + toMs + "ms", false));
		updateState(); 
	}
	
	private ActionListener connectDisconnect = (e) -> {
				if (manager.isConnected()) {
					sendNetMessage(NetMessage.disconnect());
					manager.attemptDisconnect();
				} else {
					manager.attemptConnection(ipField.getText(), (int) portField.getValue());
				}
			},
			listenClose = (e) -> {
				if (manager.isListening()) {
					manager.stopListening();	
				} else {
					manager.listenConcurrently();
				}
			},
			sendChat = (e) -> {
				String msg = chatField.getText().trim();
				if (msg.length() > 0) sendNetMessage(NetMessage.chat(msg));
				chatField.setText("");
			};
	
	private Color CONNECTED_COL = new Color(0,127,0),
			LISTENING_COL = new Color(0,0,0),
			INACTIVE_COL = new Color(127,0,0);
			
	public void updateState() {
		SwingUtilities.invokeLater(() -> {
			setStatus(manager.getStatusString(), manager.isConnected() ? CONNECTED_COL : manager.isListening()?LISTENING_COL:INACTIVE_COL);
			setClientEnabled(!manager.isConnected());
			connectionBtn.setText(manager.isConnected()?DISCONNECT:CONNECT);
			serverBtn.setEnabled(!manager.isConnected());
			serverBtn.setText(manager.isListening()?CLOSE:LISTEN);
			chatField.setEnabled(manager.isConnected());
		});
	}
	
	@Override
	public void netMessageReceived(NetMessage nm) {
		logNetMessage(nm);
		switch (nm.getCategory()) {
		case CONNECTION:
			updateState();
			break;
		case CHAT:
			updateState();
			break;
		case DISCONNECT:
			manager.attemptDisconnect();
			break;
		default:
			System.out.println("Received NetMessage");
			break;
		}
	}
	
	public void sendNetMessage(NetMessage nm) {
		logNetMessage(nm);
		manager.sendNetMessage(nm);
	}
	public void logNetMessage(NetMessage nm) {
		switch (nm.getCategory()) {
		case CHAT:
			lm.log(LogMessage.chatLog(nm.getMessage(), nm.isRemote()));
			break;
		case CONNECTION:
			lm.log(LogMessage.chatLog(nm.getGreeting().toString(), nm.isRemote()));
			break;
		case DISCONNECT:
			lm.log(LogMessage.chatLog("Disconnect notification", nm.isRemote()));
			break;
		default:
			throw new IllegalArgumentException("Unhandled NetMessage category!");
		}
	}
	
}