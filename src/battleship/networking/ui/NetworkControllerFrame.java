package battleship.networking.ui;

import java.awt.Color;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import battleship.networking.NetConnection;
import battleship.networking.NetServer;
import battleship.networking.NetworkController;
import layout.SpringUtilities;

public class NetworkControllerFrame implements NetworkController.Listener, NetServer.Listener {

	private NetworkController controller;
	
	// UI components
	private JFrame frame = new JFrame();
		// IP/port/connect client
	public static final int PORT_UNRESTRICTED_MIN = 1024;
	public static final int PORT_MAX = 65536;
	public JTextField ipField = new JTextField("localhost", 25);
	public SpinnerNumberModel portsnm = new SpinnerNumberModel(
			PORT_UNRESTRICTED_MIN, PORT_UNRESTRICTED_MIN, PORT_MAX, 1);
	public JSpinner portField = new JSpinner(portsnm);
	public static final String CONNECT = "Connect", DISCONNECT = "Disconnect";
	public JButton connectionBtn = new JButton("Not initialized");

		// Server
	public JLabel serverStatusLabel = new JLabel("Not initialized");
	public static final String LISTEN = "Listen", CLOSE = "Close";
	public JButton serverBtn = new JButton("Not initialized");
	
	public NetworkControllerFrame(NetworkController ctrl) {
		this.controller = ctrl;
		controller.addListener(this);
		controller.getServer().addListener(this);
		
		frame.setTitle("IB - " + controller.getSelf());
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
		frame.add(makeClientPane());
		frame.add(makeServerPane());
		connectionBtn.addActionListener(connectDisconnect);
		
		updateState();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	// General State
	private static final Color LISTENING_COL = new Color(0,127,0),
			INACTIVE_COL = new Color(127,0,0);
	private void updateState() {
		SwingUtilities.invokeLater(() -> {
			setClientEnabled(!controller.isConnected());
			connectionBtn.setText(controller.isConnected()?DISCONNECT:CONNECT);
			NetServer s = controller.getServer();
			setServerStatus(s.getStatus(), s.isListening() ? LISTENING_COL : INACTIVE_COL);
			serverBtn.setEnabled(s.canListen());
			serverBtn.setText(s.isListening()?CLOSE:LISTEN);
		});
	}
	private void setServerStatus(String str, Color c) {
		serverStatusLabel.setForeground(c);
		serverStatusLabel.setText(str);
	}
	
	// Client
	private JPanel makeClientPane() {
		JPanel pane = new JPanel();
		pane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		pane.add(makeClientAddrPane());
		pane.add(makeClientBtnPane());
		return pane;
	}
	private JPanel makeClientAddrPane() {
		JPanel addr = new JPanel(new SpringLayout());
		JLabel ipLab = new JLabel("IP:");
		ipLab.setLabelFor(ipField);
		addr.add(ipLab);
		addr.add(ipField);
		JLabel portLab = new JLabel("Port:");
		portLab.setLabelFor(portField);
		addr.add(portLab);
		addr.add(portField);
		SpringUtilities.makeCompactGrid(addr,2,2,0,0,5,5); // Taken from https://docs.oracle.com/javase/tutorial/uiswing/layout/spring.html
		return addr;
	}
	private JPanel makeClientBtnPane() {
		JPanel btns = new JPanel();
		btns.setLayout(new BoxLayout(btns,BoxLayout.LINE_AXIS));
		btns.add(Box.createHorizontalGlue());
		btns.add(connectionBtn);
		connectionBtn.addActionListener(connectDisconnect);
		return btns;
	}
	public void setClientEnabled(boolean enabled) {
		ipField.setEnabled(enabled);
		portField.setEnabled(enabled);
	}
	private ActionListener connectDisconnect = (e) -> {
				if (controller.isConnected()) {
					controller.attemptDisconnect(true);
				} else {
					controller.attemptConnection(ipField.getText(), (int) portField.getValue());
				}
			};
			
	// Server
	private JPanel makeServerPane() {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.LINE_AXIS));
		pane.add(serverBtn);
		serverBtn.addActionListener(listenClose);
		pane.add(Box.createHorizontalGlue());
		pane.add(serverStatusLabel);
		pane.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
		return pane;
	}
	private ActionListener listenClose = (e) -> {
		NetServer s = controller.getServer();
		if (s.isListening()) {
			System.out.println("[listenClose] CLOSE!");
			s.stopListening();	
		} else {
			System.out.println("[listenClose] LISTEN!");
			s.listenConcurrently();
		}
	};
	
	// NetworkController.Listener
	@Override
	public void connectionAttained(NetConnection c) {
		updateState();
		System.out.println("[NetworkControllerFrame.connectionAttained]");
	}

	@Override
	public void connectionClosed(NetConnection c) {
		updateState();
		System.out.println("[NetworkControllerFrame.connectionClosed]");
	}

	@Override
	public void connectionException(Exception e) {
		updateState();
		System.out.println("[NetworkControllerFrame.connectionException] " + e.getMessage());
		e.printStackTrace();
	}
	
	// NetServer Listener
	@Override
	public void connectionReceived(NetConnection c) {
		updateState();
		System.out.println("[NetworkControllerFrame.connectionReceived]");
	}
	@Override
	public void beganListening() {
		updateState();
		System.out.println("[NetworkControllerFrame.beganListening]");
	}
	@Override
	public void stoppedListening() {
		updateState();
		System.out.println("[NetworkControllerFrame.stoppedListening]");
	}
	
}
