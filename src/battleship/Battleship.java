package battleship;

import java.awt.BorderLayout;

import javax.swing.JFrame;

public class Battleship {
	
	BattleShipPanel panel = new BattleShipPanel();
	JFrame frame = new JFrame();
	
	public Battleship() {
		frame.setSize(1200, 700);
		frame.setLayout(new BorderLayout());
		frame.add(panel, BorderLayout.CENTER);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.repaint();
		
	}
}
