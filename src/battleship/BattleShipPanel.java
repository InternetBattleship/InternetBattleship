package battleship;

import java.awt.Color;
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
	ArrayList<ArrayList<GameMove>> gameBoard = new ArrayList<ArrayList<GameMove>>();
	ArrayList<ArrayList<GameMove>> opposingBoard = new ArrayList<ArrayList<GameMove>>();
	
	public BattleShipPanel()
	{
		super();
		
		//creates empty boards of the correct size
		for(int i = 0; i < 10; i++)
		{
			ArrayList<GameMove> blankArray = new ArrayList<GameMove>();
			for(int j = 0; j < 10; j++)
			{
				blankArray.add(null);
			}
			gameBoard.add((ArrayList<GameMove>) blankArray.clone());
			opposingBoard.add(blankArray);
		}
		
		
		
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
		
		//draw moves
		for(int i = 0; i < 10; i++)
		{
			for(int j = 0; j < 10; j++)
			{
				//opposing player's board
				if(opposingBoard.get(i).get(j) != null)
				{
					if(opposingBoard.get(i).get(j).getHit())//draw a hit
					{
						//draws an x
						g.setColor(Color.RED);
						g.drawLine(leftBoardX + ((boardSize/10) * i), leftBoardY + ((boardSize/10) * j), (leftBoardX + ((boardSize/10) * (i +1))), leftBoardY + ((boardSize/10) * (j+1)));
						g.drawLine(leftBoardX + ((boardSize/10) * (i + 1)), leftBoardY + ((boardSize/10) * j), (leftBoardX + ((boardSize/10) * i)), leftBoardY + ((boardSize/10) * (j+1)));
					}
					else//draw a miss
					{
						//draws a circle
						g.setColor(Color.black);
						g.drawOval(leftBoardX + ((boardSize/10) * i), leftBoardY + ((boardSize/10) * j), (boardSize/10), (boardSize/10));
					}
				}
				
				//player's board
				if(gameBoard.get(i).get(j) != null)
				{
					if(gameBoard.get(i).get(j).getHit())//draw a hit
					{
						//draws an x
						g.setColor(Color.RED);
						g.drawLine(rightBoardX + ((boardSize/10) * i), rightBoardY + ((boardSize/10) * j), (rightBoardX + ((boardSize/10) * (i +1))), rightBoardY + ((boardSize/10) * (j+1)));
						g.drawLine(rightBoardX + ((boardSize/10) * (i + 1)), rightBoardY + ((boardSize/10) * j), (rightBoardX + ((boardSize/10) * i)), rightBoardY + ((boardSize/10) * (j+1)));
					}
					else//draw a miss
					{
						//draws a circle
						g.setColor(Color.black);
						g.drawOval(rightBoardX + ((boardSize/10) * i), rightBoardY + ((boardSize/10) * j), (boardSize/10), (boardSize/10));
					}
				}
			}
		}
		g.setColor(Color.black);
	}
	
	//draws a move
	
	
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
				
				//makes sure ships aren't intersecting
				
				if(isIntersecting(newShip, boardX, boardY, newOrientation))
				{
					System.out.println("intersecting");
					return false;
				}
				else
				{
					//adds new ship to board
					newShip.setX(boardX);
					newShip.setY(boardY);
					newShip.setOrientation(newOrientation);
					shipyard.add(newShip);
					return true;
				}
				
				
			}
		}
		else
		{//did not click on the board
			return false;
		}
		
	}
	
	//takes a shot
	public boolean takeShot(int x, int y) //true means the shot was successfully fired
	{
		int boardX = ((x - leftBoardX)/(boardSize/10));
		int boardY = ((y - leftBoardY)/(boardSize/10));
		
		//note, currently this function checks the players own board for testing, this will not be the case in final version
		if(opposingBoard.get(boardX).get(boardY) == null)
		{
			if(checkForShip(boardX + 1 , boardY + 1) != null)//this line needs to be changed to check opponents board
			{
				opposingBoard.get(boardX).set(boardY, new GameMove(true, boardX, boardY));
				checkForShip(boardX + 1 , boardY + 1).addHit();
				if(checkLoss())
				{
					System.out.println("Game Over");
				}
			}
			else
			{
				opposingBoard.get(boardX).set(boardY, new GameMove(false, boardX, boardY));
			}
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isIntersecting(Ship shipOne, int x, int y, int orientation)//checks if a ship at given location will intersect with any other ship
	{
		for(int a = 0; a < shipyard.size(); a++)//runs through all ships on the board
		{	
			Ship shipTwo = shipyard.get(a);
			
			//arrays for holding all the coordinates the ships are on
			int[] shipOneX = new int[shipOne.getLength()];
			int[] shipOneY = new int[shipOne.getLength()];
			
			ArrayList<int[]> shipTwoCoords = getShipCoords(shipTwo);
			int[] shipTwoX = shipTwoCoords.get(0);
			int[] shipTwoY = shipTwoCoords.get(1);
			
			
			for(int i = 0; i < shipOne.getLength(); i++)//generates x and y coord arrays for ship one
			{
				switch(orientation)
				{
				case NORTH:
					shipOneX[i] = x;
					shipOneY[i] = y - i;
					break;
				case SOUTH:
					shipOneX[i] = x;
					shipOneY[i] = y + i;
					break;
				case WEST:
					shipOneX[i] = x - i;
					shipOneY[i] = y;
					break;
				case EAST:
					shipOneX[i] = x + i;
					shipOneY[i] = y;
					break;
				}
			}
			
			
			
			//Checks if any point on ship one is in the same spot as any point on ship two
			for(int i = 0; i < shipTwo.getLength(); i++)
			{
				for(int j = 0; j < shipOne.getLength(); j++)
				{
					if(shipOneX[j] == shipTwoX[i] && shipOneY[j] == shipTwoY[i])
					{
						return true;
					}
				}
			}
			
			
		}
		return false;
	}
	
	//returns array of all x and y coordinates a given ship is on
	public ArrayList<int[]> getShipCoords(Ship ship)
	{
		ArrayList<int[]> returnArray = new ArrayList<int[]>();
		int[] shipX = new int[ship.getLength()];
		int[] shipY = new int[ship.getLength()];
		returnArray.add(shipX);
		returnArray.add(shipY);
		
		for(int i = 0; i < ship.getLength(); i++)//generates x and y coord arrays for ship
		{
			switch(ship.getOrientation())
			{
			case NORTH:
				shipX[i] = ship.getX();
				shipY[i] = ship.getY() - i;
				break;
			case SOUTH:
				shipX[i] = ship.getX();
				shipY[i] = ship.getY() + i;
				break;
			case WEST:
				shipX[i] = ship.getX() - i;
				shipY[i] = ship.getY();
				break;
			case EAST:
				shipX[i] = ship.getX() + i;
				shipY[i] = ship.getY();
				break;
			}
		}
		return returnArray;
	}
	
	//checks if there is a ship at the given x and y coords
	public Ship checkForShip(int x, int y)
	{
		for(int i = 0; i < shipyard.size(); i++)
		{
			ArrayList<int[]> shipCoords = getShipCoords(shipyard.get(i));
			
			//goes through every space any ship occupies and sees if any match the given coords
			for(int j = 0; j < shipyard.get(i).getLength(); j++)
			{
				if(shipCoords.get(0)[j] == x && shipCoords.get(1)[j] == y)
				{
					return(shipyard.get(i));
				}
			}
		}
		return null;
	}
	
	//checks if the player has lost
	public boolean checkLoss()
	{
		for(int i = 0; i < shipyard.size(); i++)
		{
			if(!shipyard.get(i).hasSunk)
			{
				//if any of the ships haven't sunk returns false
				return false;
			}
		}
		return true;
	}
	
	
	
}
