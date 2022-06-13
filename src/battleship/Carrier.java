package battleship;

import java.awt.Color;
import java.awt.Graphics;

public class Carrier extends Ship {
	
	public Carrier(int newX, int newY, int newOrientation){
		super(newX, newY, newOrientation);
		length = 5;
		type = 4;
		shipColor = new Color(50, 50, 50);
	}
	
	
}
