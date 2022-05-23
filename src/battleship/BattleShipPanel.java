package battleship;

import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JPanel;

public class BattleShipPanel extends JPanel{
	//constants
	final char[] BOARD_LABELS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
	
	//orientations
	final int NORTH = 0;
	final int EAST = 1;
	final int SOUTH = 2;
	final int WEST = 3;
		
	
	//board variables
	int boardSize = 400;
	
	//left
	int leftBoardX = 100;
	int leftBoardY = 100;
	
	//right
	int rightBoardX = 650;
	int rightBoardY = 100;
	
	//shipyard
	ArrayList<Ship> shipyard = new ArrayList<Ship>();
	
	public BattleShipPanel()
	{
		super();
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		
		//draw boards
		drawBoard(leftBoardX, leftBoardY, g);
		drawBoard(rightBoardX, rightBoardY, g);
		
		//draw ships
		for (int i = 0; i < shipyard.size(); i++) 
		{
			shipyard.get(i).drawMe(rightBoardX, rightBoardY, boardSize, g);
		}
	}
	
	//draws a playing board
	public void drawBoard(int x, int y, Graphics g)
	{
		
		for(int i = 0; i < 11; i++)
		{
			int spacing = (boardSize * i)/10;
			g.drawLine(x + spacing, y, x + spacing, y+boardSize);
			g.drawLine(x, y + spacing, x + 400, y + spacing);
			if(i != 0)
			{
				g.drawString(i+"", x + spacing - (boardSize/20), y -2);
				g.drawString(BOARD_LABELS[i -1] + "", x -10, y + spacing - (boardSize/20));
			}
		}
	}
	
	//add ship to shipyard and board, returns false if ship was not added, true if ship was added
	public boolean addShip(int x, int y, String orientation, Ship newShip)
	{
		//checks if the given x and y are on the board
		if(x > rightBoardX && x < rightBoardX + boardSize && y > rightBoardY && y < rightBoardY + boardSize)
		{
			//converts x and y into positions on the board
			int boardX = ((x - rightBoardX)/(boardSize/10)) + 1;
			int boardY = ((y - rightBoardY)/(boardSize/10)) + 1;
			
			//gets length of ship being added
			int shipLength = newShip.getLength();
			
			//checks if in the given orientation the ship would fit on the board
			if((orientation.toLowerCase().equals("north") &&  boardY < shipLength) ||
					(orientation.toLowerCase().equals("south") &&  boardY > (11-shipLength)) ||
					(orientation.toLowerCase().equals("east") &&  boardX > (11-shipLength)) ||
					(orientation.toLowerCase().equals("west") &&  boardX < shipLength) )
			{
				System.out.println("Ship does not fit");
				return false;
			}
			else
			{
				//converts orientation from string to int
				int newOrientation = NORTH;
				switch(orientation.toLowerCase())
				{
					case "north":
						newOrientation = NORTH;
						break;
					case "east":
						newOrientation = EAST;
						break;
					case "west":
						newOrientation = WEST;
						break;
					case "south":
						newOrientation = SOUTH;
						break;
				}
				
				//adds new ship to board
				newShip.setX(boardX);
				newShip.setY(boardY);
				newShip.setOrientation(newOrientation);
				shipyard.add(newShip);
				return true;
			}
		}
		else
		{//did not click on the board
			return false;
		}
		
	}
}
