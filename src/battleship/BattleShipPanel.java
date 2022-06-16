package battleship;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JPanel;

public class BattleShipPanel extends JPanel {
	//constants
	final char[] BOARD_LABELS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
	
	//orientations
	final int NORTH = 0;
	final int EAST = 1;
	final int SOUTH = 2;
	final int WEST = 3;
		
	public static final boolean CHEATER_MODE = true;
	
	//board variables
	int boardSize = 400;
	
	//left
	int leftBoardX = 100;
	int leftBoardY = 100;
	
	//right
	int rightBoardX = 650;
	int rightBoardY = 100;
	
	//shipyard
	int homeShipCount = 0;
	Ship[] homeShipyard = new Ship[5], awayShipyard = null;
	ArrayList<GameMove> homeMoves = new ArrayList<GameMove>(),
			awayMoves = new ArrayList<GameMove>();
	
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
		for (int i = 0; i < homeShipCount; i++) 
		{
			homeShipyard[i].drawMe(rightBoardX, rightBoardY, boardSize, g);
		}
		if (CHEATER_MODE && awayShipyard != null) {
			for (int i = 0; i < awayShipyard.length; i++) 
			{
				Ship s = awayShipyard[i];
				if (s != null) s.drawMe(leftBoardX, leftBoardY, boardSize, g);
			}
		}
		
		//draw moves
		for (GameMove awayMove : awayMoves) {
			int i = awayMove.x, j = awayMove.y;
			if(awayMove.getHit()) //draw a hit
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
		for (GameMove homeMove : homeMoves) {
			int i = homeMove.x, j = homeMove.y;
			if(homeMove.getHit())//draw a hit
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
		g.setColor(Color.black);
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
					homeShipyard[homeShipCount] = newShip;
					homeShipCount++;
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
	public GameMove takeShot(int x, int y) //true means the shot was successfully fired
	{
		int boardX = ((x - leftBoardX)/(boardSize/10));
		int boardY = ((y - leftBoardY)/(boardSize/10));

		GameMove move = new GameMove(boardX, boardY);
		//note, currently this function checks the players own board for testing, this will not be the case in final version
		if(boardX < 10 && boardX >= 0 && boardY < 10 && boardY >= 0 && moveAvailable(awayMoves, move))
		{
			//make gamemove
			
			
			//**************************************
			//this line makes the player play against them self
			move.setResponse(recieveShot(boardX, boardY));
			//************************************
			
			
			//places move on board
			homeMoves.add(move);
			if(move.getWin())
			{
				System.out.println("Game Over");
			}
			return move;
		}
		else
		{
			return null;
		}
	}
	
	//Receive shot
	public String recieveShot(int x, int y)
	{
		//output string
		String out = "";
		Ship shipHit = checkForShip(x +1, y +1);
		
		//if a ship was hit
		if(shipHit != null)
		{
			//hit
			shipHit.addHit();
			
			//if ship has sunk
			if(shipHit.getHasSunk())
			{
				//sunk
				out = out + "1" + "1" +  shipHit.getType();
			}
			else
			{
				//not sunk
				out = out + "1" + "0" +  shipHit.getType();
			}
		}
		else
		{
			//miss
			out = out + "000";
		}
		
		if(checkLoss())
		{
			out = out + "1";
		}
		else
		{
			out = out + "0";
		}
		
		//adds move to gameboard
		GameMove move = new GameMove(x, y);
		if (moveExists(awayMoves, move)) throw new IllegalStateException("Opponent already made move: " + x +", " + y);
		move.setResponse(out);
		homeMoves.add(move);
		
		//returns response
		return out;
	}
	
	public boolean isIntersecting(Ship shipOne, int x, int y, int orientation)//checks if a ship at given location will intersect with any other ship
	{
		for(int a = 0; a < homeShipCount; a++)//runs through all ships on the board
		{	
			Ship shipTwo = homeShipyard[a];
			
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
		for(int i = 0; i < homeShipCount; i++)
		{
			Ship s = homeShipyard[i];
			ArrayList<int[]> shipCoords = getShipCoords(s);
			
			//goes through every space any ship occupies and sees if any match the given coords
			for(int j = 0; j < s.getLength(); j++)
			{
				if(shipCoords.get(0)[j] == x && shipCoords.get(1)[j] == y)
				{
					return s;
				}
			}
		}
		return null;
	}
	
	//checks if the player has lost
	public boolean checkLoss()
	{
		for(int i = 0; i < 5; i++)
		{
			if(!homeShipyard[i].hasSunk)
			{
				//if any of the ships haven't sunk returns false
				return false;
			}
		}
		return true;
	}
	
	//resets the boards
	public void resetBoard()
	{
		homeShipyard = new Ship[5];
		homeShipCount = 0;
		awayShipyard = null;
		
		awayMoves = new ArrayList<GameMove>();
		homeMoves = new ArrayList<GameMove>();
	}
	
	public boolean moveAvailable(ArrayList<GameMove> arr, GameMove m) {
		return !moveExists(arr, m);
	}
	
	public boolean moveExists(ArrayList<GameMove> arr, GameMove m) {
		for (GameMove a : arr) {
			if (a.x == m.x && a.y == m.y) return true;
		}
		return false;
	}
	
}
