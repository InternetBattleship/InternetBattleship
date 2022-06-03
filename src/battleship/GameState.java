package battleship;

import java.util.ArrayList;

public class GameState {
	ArrayList<GameMove> history = new ArrayList<GameMove>();
	GamePlayer playerOne;
	GamePlayer playerTwo;
	GamePlayer whoseTurn;
	
	public GamePlayer getPlayerOne() {
		return playerOne;
	}

	public GamePlayer getPlayerTwo() {
		return playerTwo;
	}

	public GamePlayer getWhoseTurn() {
		return whoseTurn;
	}

	//constructor
	public GameState(GamePlayer a, GamePlayer b)
	{
		playerOne = a;
		playerTwo = b;
		whoseTurn = playerOne;
	}
}
