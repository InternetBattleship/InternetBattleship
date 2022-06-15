package battleship.networking;

import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class NetClient {

	// UI
	private JFrame frame = new JFrame(), pFrame = null;
	private JTextField ipField = new JTextField(16), portField = new JTextField(7);
	private JButton connectBtn = new JButton("Connect");
	
	// Misc
	private NetUser self = NetUser.Factory.random();
	
	public NetClient(JFrame parentFrame) {
		this.pFrame = parentFrame;
		frame.setTitle("IBServer " + self.toString());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				pFrame.setVisible(true);
			}
		});
		frame.setLocationRelativeTo(pFrame);
		
		JPanel pane = new JPanel();
		final int PDG = 10;
		pane.setLayout(new BoxLayout(pane, BoxLayout.LINE_AXIS));
		pane.add(ipField);
		pane.add(Box.createHorizontalStrut(PDG));
		pane.add(new JLabel(":"));
		pane.add(Box.createHorizontalStrut(PDG));
		pane.add(portField);
		pane.add(Box.createHorizontalGlue());
		pane.add(Box.createHorizontalStrut(PDG));
		pane.add(connectBtn);
		pane.setBorder(BorderFactory.createEmptyBorder(PDG,PDG,PDG,PDG));
		
		connectBtn.addActionListener(connect);
		ipField.addActionListener(connect);
		portField.addActionListener(connect);
		
		frame.add(pane);
		frame.pack();
		frame.setVisible(true);
	}
	
	private ActionListener connect = (e) -> {
		String portVal = portField.getText();
		String ipVal = ipField.getText();
		int port = -1;
		try {
			port = Integer.valueOf(portVal);
			if (port < 0 || port > 65535) throw new Exception();
		} catch (Exception ex) {
			displayError("Invalid port number: \"" + portVal + "\"");
		}
		try {
			Socket s = new Socket();
			InetSocketAddress isa = new InetSocketAddress(ipVal, port);
			s.connect(isa);
			new NetConnection(s, true, frame);
		} catch (IOException ex) {
			ex.printStackTrace();
			displayError(ex.getMessage());
		}
	};
	
	private void displayError(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "Error",
			    JOptionPane.ERROR_MESSAGE);
	}
	
}
