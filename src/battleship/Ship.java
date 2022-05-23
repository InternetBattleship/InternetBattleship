package battleship;

import java.awt.Color;
import java.awt.Graphics;

public class Ship {
	
	final int NORTH = 0;
	final int EAST = 1;
	final int SOUTH = 2;
	final int WEST = 3;
	
	
	int length;
	int orientation;
	int x;
	int y;
	
	
	public int getLength() {
		return length;
	}


	public int getOrientation() {
		return orientation;
	}



	public void setOrientation(int orientation) {
		orientation = orientation;
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

	
	public Ship(int newX, int newY){
		x = newX;
		y = newY;
	}
	
	public void drawMe(int boardX, int boardY, int boardSize, Graphics g)
	{
		g.setColor(Color.blue);
		g.fillRect(boardX, boardY, (boardSize/10), (boardSize/10) * length);
		
	}
}










