package battleship;

import java.awt.Color;

public class Submarine extends Ship {
	public Submarine(int newX, int newY, int newOrientation){
		super(newX, newY, newOrientation);
		length = 3;
		shipColor = new Color(170, 170, 170);
	}
}
