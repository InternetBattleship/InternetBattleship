package battleship.networking.ui;

import java.awt.event.ActionListener;

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

import battleship.networking.NetConnection;
import battleship.networking.NetworkController;
import layout.SpringUtilities;

// This class currently serves as a basic connection client/server, also allows to chat with the opponent in its current state.
public class NetDirectPanel extends JPanel implements NetworkController.Listener {

	// IP/port
	public static final int PORT_UNRESTRICTED_MIN = 1024;
	public static final int PORT_MAX = 65536;
	public JTextField ipField = new JTextField("localhost");
	public SpinnerNumberModel portsnm = new SpinnerNumberModel(
			PORT_UNRESTRICTED_MIN, PORT_UNRESTRICTED_MIN, PORT_MAX, 1);
	public JSpinner portField = new JSpinner(portsnm);
	
	// Connection
	public static final String CONNECT = "Connect", DISCONNECT = "Disconnect";
	public JButton connectionBtn = new JButton("Not initialized");
	
	// Underlying logic
	private NetworkController controller;
	
	// UI/COMPONENT STRUCTURE
	public NetDirectPanel(NetworkController c) { 
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(makeAddrPane());
		add(makeBtnPane());
		setClientEnabled(false);
		setManager(c);
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
		btns.setBorder(BorderFactory.createEmptyBorder(0,5,5,5));
		return btns;
	}
	
	// ASSIGN LOGIC
	public void setManager(NetworkController m) { // Fill structure with content from underlying logic
		controller = m;
		if (controller == null) throw new IllegalArgumentException("Manager is null!");
		connectionBtn.addActionListener(connectDisconnect);
		controller.addListener(this);
		updateState();
	}
	
	// UPDATING STATE
	public void setClientEnabled(boolean enabled) {
		ipField.setEnabled(enabled);
		portField.setEnabled(enabled);
	}
	public void updateState() {
		SwingUtilities.invokeLater(() -> {
			setClientEnabled(!controller.isConnected());
			connectionBtn.setText(controller.isConnected()?DISCONNECT:CONNECT);
		});
	}

	// INTERACT WITH LOGIC LAYER
	private ActionListener connectDisconnect = (e) -> {
				if (controller.isConnected()) {
					controller.attemptDisconnect(true);
				} else {
					controller.attemptConnection(ipField.getText(), (int) portField.getValue());
				}
			};
			
	// LOGIC LISTENER
	@Override
	public void connectionAttained(NetConnection c) {
		updateState();
	}
	@Override
	public void connectionClosed(NetConnection  s) {
		updateState();
	}
	@Override
	public void connectionException(Exception e) {
		updateState();
	}
	
}
