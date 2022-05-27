package battleship;

import java.awt.Color;

public class PatrolBoat extends Ship {
	public PatrolBoat(int newX, int newY, int newOrientation){
		super(newX, newY, newOrientation);
		length = 2;
		shipColor = new Color(200, 200, 200);
	}
}
