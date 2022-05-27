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

public class Game implements ActionListener, MouseListener{
	
	BattleShipPanel panel = new BattleShipPanel();
	JFrame frame = new JFrame();
	
	//east container
	JButton placeCarrierB = new JButton("Carrier(5)");
	JButton placeBattleShipB = new JButton("Battle Ship(4)");
	JButton placeDestroyerB = new JButton("Destroyer(3)");
	JButton placeSubmarineB = new JButton("Submarine(3)");
	JButton placePatrolB = new JButton("Patrol Boat(2)");
	JTextField shipOrientation = new JTextField("North");
	Container east = new Container();
	
	final int NONE = 0;
	final int PLACING_CARRIER = 1;
	final int PLACING_BATTLESHIP = 2;
	final int PLACING_DESTROYER = 3;
	final int PLACING_SUBMARINE = 4;
	final int PLACING_PATROL_BOAT = 5;
	int state = NONE;
	
	public Game() {
		frame.setSize(1200, 700);
		frame.setLayout(new BorderLayout());
		frame.add(panel, BorderLayout.CENTER);
		
		east.setLayout(new GridLayout(6, 1));
		placeCarrierB.addActionListener(this);
		placeBattleShipB.addActionListener(this);
		placeDestroyerB.addActionListener(this);
		placeSubmarineB.addActionListener(this);
		placePatrolB.addActionListener(this);
		east.add(placeCarrierB);
		east.add(placeBattleShipB);
		east.add(placeDestroyerB);
		east.add(placeSubmarineB);
		east.add(placePatrolB);
		east.add(shipOrientation);
		frame.add(east, BorderLayout.EAST);
		
		placeCarrierB.setBackground(Color.LIGHT_GRAY);
		placeBattleShipB.setBackground(Color.LIGHT_GRAY);
		placeDestroyerB.setBackground(Color.LIGHT_GRAY);
		placeSubmarineB.setBackground(Color.LIGHT_GRAY);
		placePatrolB.setBackground(Color.LIGHT_GRAY);
		
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
			placeBattleShipB.setBackground(Color.LIGHT_GRAY);
			placeDestroyerB.setBackground(Color.LIGHT_GRAY);
			placeSubmarineB.setBackground(Color.LIGHT_GRAY);
			placePatrolB.setBackground(Color.LIGHT_GRAY);
		}
		if(e.getSource().equals(placeBattleShipB))//place Destroyer button
		{
			state = PLACING_BATTLESHIP;
			placeCarrierB.setBackground(Color.LIGHT_GRAY);
			placeBattleShipB.setBackground(Color.GREEN);
			placeDestroyerB.setBackground(Color.LIGHT_GRAY);
			placeSubmarineB.setBackground(Color.LIGHT_GRAY);
			placePatrolB.setBackground(Color.LIGHT_GRAY);
		}
		if(e.getSource().equals(placeDestroyerB))//place Destroyer button
		{
			state = PLACING_DESTROYER;
			placeCarrierB.setBackground(Color.LIGHT_GRAY);
			placeBattleShipB.setBackground(Color.LIGHT_GRAY);
			placeDestroyerB.setBackground(Color.GREEN);
			placeSubmarineB.setBackground(Color.LIGHT_GRAY);
			placePatrolB.setBackground(Color.LIGHT_GRAY);
		}
		if(e.getSource().equals(placeSubmarineB))//place Destroyer button
		{
			state = PLACING_SUBMARINE;
			placeCarrierB.setBackground(Color.LIGHT_GRAY);
			placeBattleShipB.setBackground(Color.LIGHT_GRAY);
			placeDestroyerB.setBackground(Color.LIGHT_GRAY);
			placeSubmarineB.setBackground(Color.GREEN);
			placePatrolB.setBackground(Color.LIGHT_GRAY);
		}
		if(e.getSource().equals(placePatrolB))//place Destroyer button
		{
			state = PLACING_PATROL_BOAT;
			placeCarrierB.setBackground(Color.LIGHT_GRAY);
			placeBattleShipB.setBackground(Color.LIGHT_GRAY);
			placeDestroyerB.setBackground(Color.LIGHT_GRAY);
			placeSubmarineB.setBackground(Color.LIGHT_GRAY);
			placePatrolB.setBackground(Color.GREEN);
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
			placeShip(e.getX(), e.getY(), new Carrier(1,1, 0), placeCarrierB);
		}
		else if(state == PLACING_BATTLESHIP)
		{
			placeShip(e.getX(), e.getY(), new Battleship(1,1, 0), placeBattleShipB);
		}
		else if(state == PLACING_DESTROYER)
		{
			placeShip(e.getX(), e.getY(), new Destroyer(1,1, 0), placeDestroyerB);
		}
		else if(state == PLACING_SUBMARINE)
		{
			placeShip(e.getX(), e.getY(), new Submarine(1,1, 0), placeSubmarineB);
		}
		else if(state == PLACING_PATROL_BOAT)
		{
			placeShip(e.getX(), e.getY(), new PatrolBoat(1,1, 0), placePatrolB);
		}
		
	}

	public void placeShip(int mouseX, int mouseY, Ship newShip, JButton button)
	{
		
		String orientation = shipOrientation.getText();
		if(orientation.toLowerCase().equals("north") || 
				orientation.toLowerCase().equals("east") ||
				orientation.toLowerCase().equals("south") ||
				orientation.toLowerCase().equals("west"))
		{//if orientation is valid
			if(panel.addShip(mouseX, mouseY, orientation, newShip))
			{//ship placed
				frame.repaint();
				button.setEnabled(false);
				state = NONE;
				button.setBackground(Color.LIGHT_GRAY);
			}
			else
			{//ship not placed
				JOptionPane.showMessageDialog(frame, "Ship does not fit there");
			}
		}
		else
		{//invalid orientation
			JOptionPane.showMessageDialog(frame, "Invalid Orientation");
		}
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
