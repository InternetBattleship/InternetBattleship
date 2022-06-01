package battleship.networking.ui;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import battleship.networking.NetworkController;
import battleship.networking.browsing.NetBrowserListModel;
import battleship.networking.browsing.NetFinder;
import battleship.networking.browsing.NetHostInfo;

public class NetBrowserPanel extends JPanel {
	
	private JList<NetHostInfo> hostList;
	private NetBrowserListModel model;
	private JButton connectBtn = new JButton("Connect"), 
			clearBtn = new JButton("Clear"), 
			queryBtn = new JButton("Query");
	
	// Underlying logic
	private NetFinder finder;
	
	public NetBrowserPanel(NetworkController controller) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		this.add(makeHostPanel());
		this.add(makeBtnPanel());
		setBrowser(new NetFinder(controller));
	}
	private MouseAdapter connectionAttempter = new MouseAdapter() {
		@Override
	    public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() > 1) attemptConnectionToSelected();
	    }
	};
	private void attemptConnectionToSelected() {
		NetHostInfo sel = hostList.getSelectedValue();
		if (sel == null) return;
		finder.attemptConnection(sel);
	}
	
	private JPanel makeHostPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JLabel title = new JLabel("Servers:");
		title.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(0, 0, 5, 0),
				BorderFactory.createLineBorder(Color.RED)));
		panel.add(title);
		
		hostList = new JList<NetHostInfo>();
		hostList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		hostList.setFixedCellWidth(300);
		panel.add(new JScrollPane(hostList));
		
		return panel;
	}
	
	private ActionListener connectAction = (e) -> attemptConnectionToSelected(), 
			queryAction = (e) -> {
			new Thread(() -> {
				try {
					finder.query();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}).start();
		}, clearAction = (e) -> {
			finder.clearCache();
		};
	
	private JPanel makeBtnPanel() {
		JPanel btnPanel = new JPanel();
		BoxLayout layout = new BoxLayout(btnPanel, BoxLayout.LINE_AXIS);
		btnPanel.setLayout(layout);
		
		btnPanel.add(connectBtn);
		btnPanel.add(Box.createHorizontalGlue());
		btnPanel.add(clearBtn);
		btnPanel.add(Box.createHorizontalStrut(5));
		btnPanel.add(queryBtn);
		btnPanel.setBorder(BorderFactory.createEmptyBorder(0,5,5,5));
		
		return btnPanel;
	}
	// ASSIGN LOGIC
	public void setBrowser(NetFinder m) { // Fill structure with content from underlying logic
		finder = m;
		if (finder == null) throw new IllegalArgumentException("Browser is null!");
		model = new NetBrowserListModel(finder);
		hostList.setModel(model);
		hostList.addMouseListener(connectionAttempter);
		hostList.addListSelectionListener((e) -> updateConnectBtnState());
		connectBtn.addActionListener(connectAction);
		clearBtn.addActionListener(clearAction);
		queryBtn.addActionListener(queryAction);
		
		updateConnectBtnState();
	}
	
	private void updateConnectBtnState() {
		connectBtn.setEnabled(hostList.getSelectedValue() != null);
	}
	
	public static String getLANIPAddress() throws UnknownHostException {
		return Inet4Address.getLocalHost().getHostAddress();
	}
	
}
