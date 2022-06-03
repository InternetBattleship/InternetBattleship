package battleship;

public class GamePlayer {
	String name;
	int id;
	
	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	
	//constructor
	public GamePlayer(String newName, int newId)
	{
		name = newName;
		id = newId;
	}
}
