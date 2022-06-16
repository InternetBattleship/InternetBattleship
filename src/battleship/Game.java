package battleship;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import battleship.networking.NetConnection;
import battleship.networking.NetMessage;
import battleship.networking.NetUser;

public class Game implements ActionListener, MouseListener {
	
	BattleShipPanel panel = new BattleShipPanel();
	JFrame frame = new JFrame();
	
	//east container
	JButton placeCarrierB = new JButton("Carrier(5)");
	JButton placeBattleShipB = new JButton("Battle Ship(4)");
	JButton placeDestroyerB = new JButton("Destroyer(3)");
	JButton placeSubmarineB = new JButton("Submarine(3)");
	JButton placePatrolB = new JButton("Patrol Boat(2)");
	private static String[] NESW = new String[] { "North","East","South","West" };
	JComboBox<String> shipOrientation = new JComboBox<>(NESW);
	Container east = new Container();
	
	//west container
	JButton aimShotB = new JButton("Aim Shot");
	Container west = new Container();
	
	final int NONE = 0;
	final int PLACING_CARRIER = 1;
	final int PLACING_BATTLESHIP = 2;
	final int PLACING_DESTROYER = 3;
	final int PLACING_SUBMARINE = 4;
	final int PLACING_PATROL_BOAT = 5;
	final int AIMING_SHOT = 6;
	int state = NONE;
	
	int shipsPlaced = 0;
	
	private NetConnection con;
	private boolean receivedAwayShipyard = false;
	private boolean confirmedHomeShipyard = false;
	private NetUser turn = null;
	private JLabel statusLabel = new JLabel("Not initialized");
	
	public Game(NetConnection c) {
		con = c;
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				con.close();
			}
		});
		frame.setSize(1400, 700);
		frame.setLayout(new BorderLayout());
		frame.add(panel, BorderLayout.CENTER);
		frame.add(statusLabel, BorderLayout.NORTH);
		constructFrame();
		
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		panel.addMouseListener(this);
		updateStatus();
		pollMessages();
	}
	
	private void pollMessages() {
		frame.repaint();
		while (con.isActive()) {
			NetMessage nm = con.receiveMessage();
			switch (nm.getCategory()) {
			case GREETING:
				throw new IllegalStateException("Shouldn't be receiving greeting here!");
			case MOVE:
				GameMove m = nm.getMove();
				panel.recieveShot(m.x, m.y);
				break;
			case SHIPYARD:
				if (receivedAwayShipyard) throw new IllegalStateException("Already received away shipyard!");
				confirmAwayShipyard(nm.getShipyard());
				break;
			default:
				throw new IllegalArgumentException("Unhandled NetMessage.Category: " + nm.getCategory());
			}
			updateStatus();
			frame.repaint();
		}
		System.err.println("Disposed!");
		frame.dispose();
	}
	
	private void updateStatus() {
		SwingUtilities.invokeLater(() -> {
			statusLabel.setText(getStatus());
		});
	}
	private String getStatus() {
		if (receivedAwayShipyard) {
			if (confirmedHomeShipyard) {
				return turn.toString() + "'s turn...";
			} else {
				return "Opponent is waiting for you to confirm your shipyard...";
			}
		} else {
			if (confirmedHomeShipyard) {
				return "Waiting for opponent to confirmed their shipyard...";
			} else {
				return "Both players haven't confirmed their shipyards...";
			}
		}
	}

	private void confirmAwayShipyard(Ship[] yard) {
		receivedAwayShipyard = true;
		panel.awayShipyard = yard;
	}
	
	private void confirmHomeShipyard() {
		confirmedHomeShipyard = true;
		con.sendMessage(NetMessage.Factory.shipyard(panel.homeShipyard));
	}
	
	private void constructFrame() {
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
		
		west.setLayout(new GridLayout(2,1));
		aimShotB.addActionListener(this);
		aimShotB.setEnabled(false);
		west.add(aimShotB);
		frame.add(west, BorderLayout.WEST);
		
		placeCarrierB.setBackground(Color.LIGHT_GRAY);
		placeBattleShipB.setBackground(Color.LIGHT_GRAY);
		placeDestroyerB.setBackground(Color.LIGHT_GRAY);
		placeSubmarineB.setBackground(Color.LIGHT_GRAY);
		placePatrolB.setBackground(Color.LIGHT_GRAY);
		aimShotB.setBackground(Color.LIGHT_GRAY);
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
		if(e.getSource().equals(aimShotB))
		{
			aimShotB.setBackground(Color.GREEN);
			state = AIMING_SHOT;
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {//mouse clicked
		if(state == PLACING_CARRIER){//place carrier
			placeShip(e.getX(), e.getY(), new Carrier(1,1, 0), placeCarrierB);
		}
		else if(state == PLACING_BATTLESHIP)//place battleship
		{
			placeShip(e.getX(), e.getY(), new Battleship(1,1, 0), placeBattleShipB);
		}
		else if(state == PLACING_DESTROYER)//place destroyer
		{
			placeShip(e.getX(), e.getY(), new Destroyer(1,1, 0), placeDestroyerB);
		}
		else if(state == PLACING_SUBMARINE)//place submarine
		{
			placeShip(e.getX(), e.getY(), new Submarine(1,1, 0), placeSubmarineB);
		}
		else if(state == PLACING_PATROL_BOAT)//place patrol boat
		{
			placeShip(e.getX(), e.getY(), new PatrolBoat(1,1, 0), placePatrolB);
		}
		else if (state == AIMING_SHOT)
		{
			GameMove shot = panel.takeShot(e.getX(), e.getY());
			
			con.sendMessage(NetMessage.Factory.move(shot));
			
			if(shot != null)//also check whose turn it is
			{
				frame.repaint();
				state = NONE;
				aimShotB.setBackground(Color.LIGHT_GRAY);
				
				if(shot.getHit() && shot.getSunk())
				{
					JOptionPane.showMessageDialog(frame, "You sunk their " + shot.getShip());
				}
				if(shot.getWin())
				{
					reset(true);
				}
			}
			else
			{
				JOptionPane.showMessageDialog(frame, "You cannot shoot there");
			}
		}
		
	}
	

	public void placeShip(int mouseX, int mouseY, Ship newShip, JButton button)//attempts to place ship on the board
	{
		//gets orientation given by user
		String orientation = (String) shipOrientation.getSelectedItem();
		
		//checks if orientation is valid
		if(orientation.toLowerCase().equals("north") || 
				orientation.toLowerCase().equals("east") ||
				orientation.toLowerCase().equals("south") ||
				orientation.toLowerCase().equals("west"))
		{
			if(panel.addShip(mouseX, mouseY, orientation, newShip))
			{
				//ship placed
				frame.repaint();
				button.setEnabled(false);
				button.setBackground(Color.LIGHT_GRAY);
				shipsPlaced++;
				
				//if all ships have been placed enables aiming shot
				if(shipsPlaced == 5)
				{
					/*
					 * Send signal to other player that this player is ready
					 * check who's going first. Enables the button for whoever is going first
					 */
					
					confirmHomeShipyard();
					aimShotB.setEnabled(true);
					state = NONE;
				}
				else
				{
					state = NONE;
				}
			}
			else
			{
				//ship not placed
				JOptionPane.showMessageDialog(frame, "Ship does not fit there");
			}
		}
		else
		{
			//invalid orientation
			JOptionPane.showMessageDialog(frame, "Invalid Orientation");
		}
	}
	
	
	
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
	
	//resets the game
	public void reset(boolean didWin)
	{
		confirmedHomeShipyard = false;
		receivedAwayShipyard = false;
		//resets the game and gives a win or loss message
		if(didWin)
		{
			JOptionPane.showMessageDialog(frame, "You won!");
		}
		else
		{
			JOptionPane.showMessageDialog(frame, "You Lost");
		}
		
		placeCarrierB.setBackground(Color.LIGHT_GRAY);
		placeBattleShipB.setBackground(Color.LIGHT_GRAY);
		placeDestroyerB.setBackground(Color.LIGHT_GRAY);
		placeSubmarineB.setBackground(Color.LIGHT_GRAY);
		placePatrolB.setBackground(Color.LIGHT_GRAY);
		aimShotB.setBackground(Color.LIGHT_GRAY);
		
		placeCarrierB.setEnabled(true);
		placeBattleShipB.setEnabled(true);
		placeDestroyerB.setEnabled(true);
		placeSubmarineB.setEnabled(true);
		placePatrolB.setEnabled(true);
		aimShotB.setEnabled(false);
		
		state = NONE;
		panel.resetBoard();
		frame.repaint();
		shipsPlaced = 0;
	}
}
