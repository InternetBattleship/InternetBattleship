package battleship.networking.browsing;

import java.awt.Container;
import java.net.Inet4Address;
import java.net.UnknownHostException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class NetBrowserFrame implements NetworkBrowser.Listener {
	
	// Frame / Components
	public JFrame frame = new JFrame("IBB");
	private JPanel hostPanel;
	private JButton scanBtn = new JButton("Scan");
	
	// Underlying logic
	private NetworkBrowser browser;
	
	public NetBrowserFrame() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container c = frame.getContentPane();
		c.setLayout(new BoxLayout(c, BoxLayout.PAGE_AXIS));
		frame.add(makeHostPanel());
		frame.add(makeBtnPanel());
		frame.pack();
		frame.setVisible(true);
		setBrowser(new NetworkBrowser());
	}
	private JComponent makeHostPanel() {
		hostPanel = new JPanel();
		hostPanel.setLayout(new BoxLayout(hostPanel, BoxLayout.PAGE_AXIS));
		hostPanel.add(new JButton("Test Button!"));
		return new JScrollPane(hostPanel);
	}
	private JPanel makeBtnPanel() {
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.LINE_AXIS));
		btnPanel.add(Box.createHorizontalGlue());
		btnPanel.add(scanBtn);
		return btnPanel;
	}
	// ASSIGN LOGIC
	public void setBrowser(NetworkBrowser m) { // Fill structure with content from underlying logic
		browser = m;
		if (browser == null) throw new IllegalArgumentException("Browser is null!");
		frame.setTitle("IB - " + browser.getMyNetUser().toString());
		System.out.println(frame.getTitle());
		browser.addListener(this);
	}
	
	public static String getLANIPAddress() throws UnknownHostException {
		return Inet4Address.getLocalHost().getHostAddress();
	}
	
}
