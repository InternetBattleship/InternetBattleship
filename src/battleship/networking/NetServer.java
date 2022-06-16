package battleship.networking;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import battleship.networking.NetMessage.Category;

public class NetServer {

	// UI
	private JFrame frame = new JFrame(), pFrame = null;
	private JLabel statusLabel = new JLabel("statusLabel");
	private JButton listenBtn = new JButton("Listen"),
			closeBtn = new JButton("Close");
	
	// Networking
	private ServerSocket server;
	private boolean listening = false;
	private Thread listenerThread = null;
	
	// Misc
	private NetUser self = NetUser.Factory.random();
	
	public NetServer(JFrame parentFrame) {
		pFrame = parentFrame;
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
		pane.add(statusLabel);
		pane.add(Box.createHorizontalStrut(PDG));
		pane.add(Box.createHorizontalGlue());
		pane.add(listenBtn);
		pane.add(Box.createHorizontalStrut(PDG));
		pane.add(closeBtn);
		pane.setBorder(BorderFactory.createEmptyBorder(PDG,PDG,PDG,PDG));
		
		listenBtn.addActionListener(listen);
		closeBtn.addActionListener(close);
		
		frame.add(pane);
		frame.pack();
		frame.setVisible(true);
		updateState();
	}
	
	private static Color INACTIVE = new Color(127,0,0), ACTIVE = new Color(0,127,0);
	
	private void updateState() {
		SwingUtilities.invokeLater(() -> {
			statusLabel.setText(listening ? getServerStatus() : "Not listening");
			statusLabel.setForeground(listening ? ACTIVE : INACTIVE);
			
			closeBtn.setEnabled(listening);
			listenBtn.setEnabled(!listening);
			frame.pack();
		});
	}
	private String getServerStatus() {
		if (server != null) {
			if (server.isBound()) {
				if (listening) {
					return "Server listening at port " + server.getLocalPort();
				} else {
					return "Server bound to port " + server.getLocalPort();
				}
			} else {
				return "Server not bound";
			}
		} else {
			return "Server is null";
		}
	}
	
	private ActionListener listen = (e) -> {
		if (listenerThread == null) {
			listenerThread = makeServerListenThread();
			listenerThread.start();
		} else {
			System.err.println("[(ActionListener)listen] listenerThread isn't null!");
		}
	}, close = (e) -> {
		try {
			server.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			listenerThread.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		server = null;
		listenerThread = null;
		listening = false;
		updateState();
	};
	
	
	private Thread makeServerListenThread() { return new Thread(serverListen, "ServerListener"); }
	private Runnable serverListen = () -> {
		try {
			server = new ServerSocket();
			server.setReuseAddress(true);
			server.bind(null);
			listening = true;
			updateState();
			Socket s = server.accept();
			listening = false;
			promptApproval(s);
		} catch (SocketException e) {
			if (e.getMessage().equals("Socket closed")) {
				System.out.println("Socket closed normally");
			} else {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		server = null;
		updateState();
	};
	
	private static String[] acceptDeny = new String[] { "Accept", "Deny" };
	
	private void promptApproval(Socket s) {
		SocketStreams sock = new SocketStreams(s, self);
		final NetMessage nm = sock.receiveMessage();
		if (nm.getCategory() != Category.GREETING) {
			JOptionPane.showMessageDialog(frame, "Protocol error: Client didn't begin with greeting message!", "Error", JOptionPane.ERROR_MESSAGE);
			sock.close();
			return;
		}
		int n = JOptionPane.showOptionDialog(frame,
			"Accept request from " + nm.getGreeting(),
			"Client request?",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE,
			null,
			acceptDeny,
			acceptDeny[0]);
		if (n==0) {
			sock.sendObject(NetMessage.Factory.greeting(self));
			new NetConnection(sock, true, frame);
		} else {
			sock.close();
		}
	}
	
}
