package battleship.networking;

import java.io.Serializable;

import battleship.GameMove;
import battleship.Ship;

// This class is used to communicate on the Object streams from the NetworkManager, 
// it can wrap other data types within the "content" field
public class NetMessage implements Serializable {
	// Properties/content
	private boolean remote = false;
	public boolean isRemote() { return remote; }
	public void flipRemote() { remote = !remote; } // Used when object is received to indicate that it didn't originate on local machine
	private Category category;
	public Category getCategory() { return category; }
	private Object[] content;
	
	// Content types
	public enum Category {
		GREETING, SHIPYARD, MOVE;
	}
	public GameMove getMove() { 
		switch (category) {
		case MOVE:
			return (GameMove) content[0]; 
		default:
			throw new IllegalStateException("Cannot get GameMove of non-move NetMessage!");
		}
	}
	public NetUser getGreeting() { 
		switch (category) {
		case GREETING:
			return (NetUser) content[0]; 
		default:
			throw new IllegalStateException("Cannot get greeting of non-connection NetMessage!");
		}
	}
	public Ship[] getShipyard() { 
		switch (category) {
		case SHIPYARD:
			return (Ship[]) content[0]; 
		default:
			throw new IllegalStateException("Cannot get Shipyard of non-shipyard NetMessage!");
		}
	}
	// Constructors/factory
	private NetMessage(Category c, Object[] content) {
		this.category = c;
		this.content = content;
	}
	public static class Factory {
		public static NetMessage greeting(NetUser greet) {
			return new NetMessage(Category.GREETING, new Object[] { greet });
		}
		public static NetMessage move(GameMove move) {
			return new NetMessage(Category.MOVE, new Object[] { move });
		}
		public static NetMessage shipyard(Ship[] yard) {
			if (yard.length != 5) throw new IllegalArgumentException("Array length not 5");
			return new NetMessage(Category.SHIPYARD, new Object[] { yard });
		}
	}
	
	@Override
	public String toString() {
		return "NetMessage: " + category.toString();
	}
	
}