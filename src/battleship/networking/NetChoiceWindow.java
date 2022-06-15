package battleship.networking;

import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class NetChoiceWindow {

	public static void main(String args[]) {
		new NetChoiceWindow();
	}
	
	private JFrame frame = new JFrame("Internet Battleship");
	
	private JPanel main = new JPanel(), btns = new JPanel();
	private JButton server = new JButton("Server"), client = new JButton("Client");
	private JLabel aboutLabel = new JLabel("Created by Lucas Gomes and Jeff McMillan");
	
	public NetChoiceWindow() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final int PDG = 5;
		main.setLayout(new BoxLayout(main, BoxLayout.PAGE_AXIS));
		btns.setLayout(new BoxLayout(btns, BoxLayout.LINE_AXIS));
		btns.add(server);
		btns.add(Box.createHorizontalStrut(PDG));
		btns.add(client);
		
		main.add(btns);
		main.add(Box.createVerticalStrut(PDG));
		aboutLabel.setAlignmentX(0.5f);
		main.add(aboutLabel);
		main.setBorder(BorderFactory.createEmptyBorder(PDG, PDG, PDG, PDG));
		
		server.addActionListener(run);
		client.addActionListener(run);
		
		frame.add(main);
		frame.pack();
		frame.setVisible(true);
	}
	
	private ActionListener run = (e) -> {
		if (e.getSource() == server) {
			new NetServer(frame);
		} else if (e.getSource() == client) {
			new NetClient(frame);
		} else {
			throw new IllegalArgumentException("INVALID EVENT SOURCE");
		}
		frame.setVisible(false);
	};
	
}
