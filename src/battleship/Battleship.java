package battleship;

import java.awt.Color;

public class Battleship extends Ship {

	public Battleship(int newX, int newY, int newOrientation){
		super(newX, newY, newOrientation);
		length = 4;
		type = 3;
		shipColor = new Color(90, 90, 90);
	}
}
