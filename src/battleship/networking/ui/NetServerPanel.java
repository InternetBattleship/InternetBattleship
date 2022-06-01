package battleship.networking.ui;

import java.awt.Color;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import battleship.networking.NetConnection;
import battleship.networking.NetServer;

// This class currently serves as a basic connection client/server, also allows to chat with the opponent in its current state.
public class NetServerPanel extends JPanel implements NetServer.Listener {

	public JLabel statusLabel = new JLabel("Not initialized");
	
	// Connection/listening
	public static final String LISTEN = "Listen", CLOSE = "Close";
	public JButton serverBtn = new JButton("Not initialized");
	
	// Underlying logic
	private NetServer server;
	
	// UI/COMPONENT STRUCTURE
	public NetServerPanel(NetServer server) { 
		super();
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		add(serverBtn);
		add(Box.createHorizontalGlue());
		add(statusLabel);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setServer(server);
	}
	
	// ASSIGN LOGIC
	public void setServer(NetServer s) { // Fill structure with content from underlying logic
		if (s == null) throw new IllegalArgumentException("Manager is null!");
		server = s;
		serverBtn.addActionListener(listenClose);
		server.addListener(this);
		updateState();
	}
	
	// UPDATING STATE
	private static final Color LISTENING_COL = new Color(0,127,0),
			INACTIVE_COL = new Color(127,0,0);
	public void updateState() {
		SwingUtilities.invokeLater(() -> {
			setStatus(server.getStatus(), server.isListening() ? LISTENING_COL : INACTIVE_COL);
			serverBtn.setEnabled(server.canListen());
			serverBtn.setText(server.isListening()?CLOSE:LISTEN);
		});
	}
	public void setStatus(String str, Color c) {
		statusLabel.setForeground(c);
		statusLabel.setText(str);
	}
	
	// INTERACT WITH LOGIC LAYER
	private ActionListener listenClose = (e) -> {
				if (server.isListening()) {
					server.stopListening();	
				} else {
					server.listenConcurrently();
				}
			};

	@Override
	public void connectionReceived(NetConnection c) {
		updateState();
	}

	@Override
	public void beganListening() {
		updateState();
	}

	@Override
	public void stoppedListening() {
		updateState();
	}
	
}
