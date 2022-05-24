package battleship.networking.log;

// Messages are stored in the log in this format, with categories determining the formatting
public class LogMessage {
	
	// Remote/local origin
	private boolean remoteOrigin;
	public boolean originatedFromRemote() { return remoteOrigin; }
	
	// Categories
	private Category category;
	public Category getCategory() { return category; }
	public enum Category {
		CHAT, NETWORK_POSITIVE, NETWORK_NEGATIVE
	}
	
	// Content
	private String str;
	public String getContent() { return str; }
	
	// Constructors
	private LogMessage(Category c, String content, boolean remote) {
		this.category = c;
		this.str = content;
		this.remoteOrigin = remote;
	}
	public static LogMessage networkLog(String msg, boolean sign) {
		return new LogMessage(sign?Category.NETWORK_POSITIVE:Category.NETWORK_NEGATIVE, msg, false);
	}
	public static LogMessage chatLog(String msg, boolean remote) {
		return new LogMessage(Category.CHAT, msg, remote);
	}
	
}
