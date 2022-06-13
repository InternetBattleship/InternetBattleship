package battleship;

import java.awt.Color;
import java.awt.Graphics;

public class Ship {
	
	//orientations
	final int NORTH = 0;
	final int EAST = 1;
	final int SOUTH = 2;
	final int WEST = 3;
	
	//variables
	int length;
	int type;
	int orientation = SOUTH;
	int x;
	int y;
	int timesHit = 0;
	boolean hasSunk = false;
	Color shipColor = Color.blue;
	
	//getters and setters
	public int getLength() {
		return length;
	}

	public int getType() {
		return type;
	}

	public int getOrientation() {
		return orientation;
	}

	public boolean getHasSunk()
	{
		return hasSunk;
	}

	public void setOrientation(int newOrientation) {
		orientation = newOrientation;
	}



	public int getX() {
		return x;
	}



	public void setX(int x) {
		this.x = x;
	}



	public int getY() {
		return y;
	}



	public void setY(int y) {
		this.y = y;
	}

	//constructor
	public Ship(int newX, int newY, int newOrientation){
		x = newX;
		y = newY;
		orientation = newOrientation;
	}
	
	//draw ship function
	public void drawMe(int boardX, int boardY, int boardSize, Graphics g)
	{
		//color of ship
		g.setColor(shipColor);
		int squareSize = (boardSize/10);
		
		//different orientations
		if(orientation == NORTH)
		{
			g.fillRect(boardX + (squareSize *(x - 1)), boardY + (squareSize *(y - length)), squareSize, squareSize * length);
			g.setColor(Color.BLACK);
			g.drawRect(boardX + (squareSize *(x - 1)), boardY + (squareSize *(y - length)), squareSize, squareSize * length);
			
		}
		if(orientation == EAST)
		{
			g.fillRect(boardX + (squareSize *(x - 1)), boardY + (squareSize *(y - 1)), squareSize * length, squareSize);
			g.setColor(Color.BLACK);
			g.drawRect(boardX + (squareSize *(x - 1)), boardY + (squareSize *(y - 1)), squareSize * length, squareSize);

		}
		if(orientation == WEST)
		{
			g.fillRect(boardX + (squareSize *(x - length)), boardY + (squareSize *(y - 1)), squareSize * length, squareSize);
			g.setColor(Color.BLACK);
			g.drawRect(boardX + (squareSize *(x - length)), boardY + (squareSize *(y - 1)), squareSize * length, squareSize);
		}
		if(orientation == SOUTH)
		{
			g.fillRect(boardX + (squareSize *(x - 1)) , boardY + (squareSize *(y - 1)), squareSize, squareSize * length);
			g.setColor(Color.BLACK);
			g.drawRect(boardX + (squareSize *(x - 1)) , boardY + (squareSize *(y - 1)), squareSize, squareSize * length);

		}
		
	}
	
	//adds a hit to the ship and checks if the ship has sunk
	public void addHit() {
		timesHit++;
		if(timesHit == length)
		{
			hasSunk = true;
		}
	}
}










