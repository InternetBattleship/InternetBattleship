package battleship.networking;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import battleship.networking.browsing.NetBrowserFrame;

public class NetTest {
	
	public static void main(String args[]) {
		JFrame frame = new JFrame("IB");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Browser", new NetBrowserFrame());
		tabs.addTab("Advanced", new NetTestFrame());
		frame.add(tabs);
		
		frame.pack();
		frame.setVisible(true);
	}
}
