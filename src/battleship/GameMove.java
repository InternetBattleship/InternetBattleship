package battleship;

public class GameMove {
	
	/*
	 * This class is just skeleton so I can figure out how to render the moves on the board
	 */
	
	boolean hit;
	
	public GameMove(boolean newHit, int x, int y)
	{
		hit = newHit;
	}
	
	public boolean getHit()
	{
		return hit;
	}
}
