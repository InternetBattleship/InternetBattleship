package battleship.networking.ui;

import java.net.InetSocketAddress;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import battleship.networking.NetConnection;
import battleship.networking.NetworkController;

public class NetworkControllerFrame implements NetworkController.Listener {

	private JFrame frame;
	private NetworkController controller;
	
	public NetworkControllerFrame(NetworkController c) {
		if (c == null) throw new IllegalArgumentException("Controller is null!");
		controller = c;
		
		frame = new JFrame("IB " + controller.getSelf());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
	
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Browser", new NetBrowserPanel(controller));
		tabs.addTab("Advanced", new NetDirectPanel(controller));
		frame.add(tabs);
		frame.add(new NetServerPanel(controller.getServer()));
		
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void connectionAttained(NetConnection c) {
		System.err.println("[NetworkControllerFrame] connectionAttained");
		new NetConnectionFrame(c, frame);
	}
	@Override
	public void connectionClosed(NetConnection c) {
		System.err.println("[NetworkControllerFrame] connectionClosed");
	}
	@Override
	public void refusedConnection(InetSocketAddress a) {
		System.err.println("[NetworkControllerFrame] refusedConnection");
	}
	@Override
	public void unresolvedAddress(InetSocketAddress a) {
		System.err.println("[NetworkControllerFrame] unresolvedAddress");
	}
	@Override
	public void connectionTimeout(InetSocketAddress a, int toMs) {
		System.err.println("[NetworkControllerFrame] connectionTimeout");
	}
	
}
