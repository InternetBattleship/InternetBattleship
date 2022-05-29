package battleship.networking;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import layout.SpringUtilities;

// This class currently serves as a basic connection client/server, also allows to chat with the opponent in its current state.
public class NetTestFrame extends JPanel implements NetworkManager.Listener {

	public JLabel statusLabel = new JLabel("Not initialized");
	
	// IP/port
	public static final int PORT_UNRESTRICTED_MIN = 1024;
	public static final int PORT_MAX = 65536;
	public JTextField ipField = new JTextField("localhost");
	public SpinnerNumberModel portsnm = new SpinnerNumberModel(
			PORT_UNRESTRICTED_MIN, PORT_UNRESTRICTED_MIN, PORT_MAX, 1);
	public JSpinner portField = new JSpinner(portsnm);
	
	// Connection/listening
	public static final String CONNECT = "Connect", DISCONNECT = "Disconnect",
			LISTEN = "Listen", CLOSE = "Close";
	public JButton connectionBtn = new JButton("Not initialized"),
			serverBtn = new JButton("Not initialized");
	
	// Underlying logic
	private NetworkManager manager;
	
	// UI/COMPONENT STRUCTURE
	public NetTestFrame() { 
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(statusLabel);
		add(makeAddrPane());
		add(makeBtnPane());
		statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		statusLabel.setAlignmentX(0.5f);
		setClientEnabled(false);
		setManager(new NetworkManager());
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
	
	// ASSIGN LOGIC
	public void setManager(NetworkManager m) { // Fill structure with content from underlying logic
		manager = m;
		if (manager == null) throw new IllegalArgumentException("Manager is null!");
		connectionBtn.addActionListener(connectDisconnect);
		serverBtn.addActionListener(listenClose);
		manager.addListener(this);
		updateState();
	}
	
	// UPDATING STATE
	public void setClientEnabled(boolean enabled) {
		ipField.setEnabled(enabled);
		portField.setEnabled(enabled);
	}
	private static final Color CONNECTED_COL = new Color(0,127,0),
			LISTENING_COL = new Color(0,0,0),
			INACTIVE_COL = new Color(127,0,0);
	public void updateState() {
		SwingUtilities.invokeLater(() -> {
			setStatus(manager.getStatusString(), manager.isConnected() ? CONNECTED_COL : manager.isListening()?LISTENING_COL:INACTIVE_COL);
			setClientEnabled(!manager.isConnected());
			connectionBtn.setText(manager.isConnected()?DISCONNECT:CONNECT);
			serverBtn.setEnabled(!manager.isConnected());
			serverBtn.setText(manager.isListening()?CLOSE:LISTEN);
		});
	}
	public void setStatus(String str, Color c) {
		statusLabel.setForeground(c);
		statusLabel.setText(str);
	}

	// RESPONDING TO UPDATES IN LOGIC
	@Override
	public void netMessageReceived(NetMessage nm) {
		System.err.println("[NetTestFrame.netMessageReceived] Message not handled: " + nm.getCategory());
	}
	
	// LOGIC LISTENER
	@Override
	public void connectionAttained(Socket s) {
		System.out.println("[Listener] connectionAttained");
		new NetConnectionFrame(manager, null);
	}
	@Override
	public void connectionClosed(Socket s) {
		System.out.println("[Listener] connectionClosed");
//		ncf.dispose();
	}
	@Override
	public void beganListening() { 
		System.out.println("[Listener] beganListening");
		updateState();
//		lm.add(LogMessage.networkLog("Listening...", true));
	}
	@Override
	public void stoppedListening() { 
		System.out.println("[Listener] stoppedListening");
		updateState();
//		lm.add(LogMessage.networkLog("Stopped listening.", false));
	}
	@Override
	public void refusedConnection(InetSocketAddress addr) { 
		System.out.println("[Listener] refusedConnection");
//		lm.add(LogMessage.networkLog("Refused connection: " + addr.getHostString() + ":" + addr.getPort(), false));
	}
	@Override
	public void unresolvedAddress(InetSocketAddress addr) { 
		System.out.println("[Listener] unresolvedAddress");
//		lm.add(LogMessage.networkLog("Unresolved Address: " + addr.getHostString() + ":" + addr.getPort(), false));
	}
	public void connectionTimeout(InetSocketAddress addr, int toMs) {
		System.out.println("[Listener] connectionTimeout " + toMs);
//		lm.add(LogMessage.networkLog("Connection timed out: " + addr.getHostString() + ":" + addr.getPort() + " after " + toMs + "ms", false));
	}
	
	// INTERACT WITH LOGIC LAYER
	private ActionListener connectDisconnect = (e) -> {
				if (manager.isConnected()) {
					manager.attemptDisconnect(true);
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
			};
	@Override
	public void handshakeCompleted(Socket s) { }
	@Override
	public void handshakeFailed(Socket s, NetHandshakeException e) { }
	
}
