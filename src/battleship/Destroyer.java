package battleship;

import java.awt.Color;

public class Destroyer extends Ship {
	public Destroyer(int newX, int newY, int newOrientation){
		super(newX, newY, newOrientation);
		length = 3;
		type = 1;
		shipColor = new Color(130, 130, 130);
	}
}
