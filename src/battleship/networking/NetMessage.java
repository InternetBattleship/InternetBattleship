package battleship.networking;

import java.io.Serializable;

// This class is used to communicate on the Object streams from the NetworkManager, 
// it can wrap other data types within the "content" field

public class NetMessage implements Serializable {
	
	// Properties/content
	private boolean remote = false;
	public boolean isRemote() { return remote; }
	public void flipRemote() { remote = !remote; } // Used when object is received to indicate that it didn't originate on local machine
	private Category category;
	public Category getCategory() { return category; }
	private Object content;
	
	// Content types
	enum Category {
		CONNECTION, CHAT, DISCONNECT; // TODO: Add a strike/missile option that contains a gameMove as content.
	}
	public String getMessage() { 
		switch (category) {
		case CHAT:
			return (String) content; 
		default:
			throw new IllegalStateException("Cannot get message of non-chat NetMessage!");
		}
	}
	public NetUser getGreeting() { 
		switch (category) {
		case CONNECTION:
			return (NetUser) content; 
		default:
			throw new IllegalStateException("Cannot get greeting of non-connection NetMessage!");
		}
	}
	
	// Constructors/factory
	private NetMessage(Category c, Object content) {
		this.category = c;
		this.content = content;
	}
	public static NetMessage chat(String msg) {
		return new NetMessage(Category.CHAT, msg);
	}
	public static NetMessage connection(NetUser greet) {
		return new NetMessage(Category.CONNECTION, greet);
	}
	public static NetMessage disconnect() {
		return new NetMessage(Category.DISCONNECT, null);
	}
	
}
