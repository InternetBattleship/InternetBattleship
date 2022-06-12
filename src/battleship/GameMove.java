package battleship;

public class GameMove {
	
	/*
	 * This class is just skeleton so I can figure out how to render the moves on the board
	 */
	
	boolean hit = false;
	int x;
	int y;
	//first digit is hit or miss(0 or 1) second digit is sunk(0 or 1) third digit is the type of ship(0-4 in order of length) and last digit is win (0 or 1)
	String response = "0000";
	
	public GameMove(int newX, int newY)
	{
		//hit = newHit;
		x = newX;
		y = newY;
	}
	
	//gets whether or not a ship was hit
	public boolean getHit()
	{
		return response.charAt(0) == '1';
	}
	
	//gets whether or not a ship was sunk
	public boolean getSunk()
	{
		return response.charAt(1) == '1';
	}
	
	//gets whether or not a player has won
	public boolean getWin()
	{
		return response.charAt(3) == '1';
	}
	
	//gets what type of ship was hit
	public String getShip()
	{
		char shipType = response.charAt(2);
		String shipReturn = "";
		
		if(getSunk())//only returns the type of ship if a ship was sunk
		{
			switch(shipType)
			{
				case '0':
					shipReturn = "Patrol Boat";
					break;
				case '1':
					shipReturn = "Destroyer";
					break;
				case '2':
					shipReturn = "Submarine";
					break;
				case '3':
					shipReturn = "Battleship";
					break;
				case '4':
					shipReturn = "Carrier";
					break;
			}
		}
		
		return shipReturn;
	}
	
	//gets response from other player
	public void getResponse()
	{
		//send x and y get back a string
		response = "1110";
	}
	
	
}
