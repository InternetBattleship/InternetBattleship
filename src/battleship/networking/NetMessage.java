package battleship.networking;

import java.io.Serializable;

public class NetMessage implements Serializable {

	enum Category {
		CONNECTION, CHAT, DISCONNECT;
	}
	
	private boolean remote = false;
	public boolean isRemote() { return remote; }
	public void flipRemote() { remote = !remote; }
	private Category category;
	public Category getCategory() { return category; }
	private Object content;
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
