package battleship;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class Battleship implements ActionListener, MouseListener{
	
	BattleShipPanel panel = new BattleShipPanel();
	JFrame frame = new JFrame();
	
	//east container
	JButton placeCarrierB = new JButton("Carrier");
	JTextField shipOrientation = new JTextField("North");
	Container east = new Container();
	
	
	final int PLACING_CARRIER = 1;
	int state = PLACING_CARRIER;
	
	public Battleship() {
		frame.setSize(1200, 700);
		frame.setLayout(new BorderLayout());
		frame.add(panel, BorderLayout.CENTER);
		
		east.setLayout(new GridLayout(5, 1));
		placeCarrierB.addActionListener(this);
		east.add(placeCarrierB);
		east.add(shipOrientation);
		frame.add(east, BorderLayout.EAST);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.repaint();
		panel.addMouseListener(this);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(placeCarrierB))//place carrier button
		{
			state = PLACING_CARRIER;
			placeCarrierB.setBackground(Color.GREEN);
			System.out.println("buttonpressed");
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(state == PLACING_CARRIER){
			int mouseX = e.getX();
			int mouseY = e.getY();
			String orientation = shipOrientation.getText();
			if(orientation.toLowerCase().equals("north") || 
					orientation.toLowerCase().equals("east") ||
					orientation.toLowerCase().equals("south") ||
					orientation.toLowerCase().equals("west"))
			{//if orientation is valid
				Carrier newCarrier = new Carrier(1,1, 0);
				if(panel.addShip(mouseX, mouseY, orientation, newCarrier))
				{//ship placed
					frame.repaint();
				}
				else
				{//ship not placed
					JOptionPane.showMessageDialog(frame, "Ship cannot be placed in that orientation");
				}
			}
			else
			{//invalid orientation
				JOptionPane.showMessageDialog(frame, "Invalid Orientation");
			}
			
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
