package battleship.networking.browsing;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

import battleship.networking.messaging.NetUser;
import battleship.networking.messaging.Serialization;

public class NetMulticastMessage implements Externalizable {
	
	// Should only be used when deserializing
	public NetMulticastMessage() { }
	
	public byte[] getBytes() throws IOException {
		return Serialization.serializeToBytes(this);
	}
	public static NetMulticastMessage fromBytes(byte[] bytes) throws ClassNotFoundException, IOException {
		return Serialization.deserializeFromBytes(bytes);
	}
	public enum Type { QUERY, INFORM, DISPOSE; }
	private Type type = null;
	public Type getType() { return type; }
	private Object[] content = null;
	private NetMulticastMessage(Type type, Object[] content) {
		this.type = type;
		this.content = content;
	}
	@Override
	public String toString() {
		String suffix = "default suffix";
		switch (type) {
		case INFORM:
			suffix = "Inform, " + getHostInfo();
			break;
		case QUERY:
			suffix = "Query";
			break;
		case DISPOSE:
			suffix = "Dispose, " + getUser();
			break;
		default:
			throw new IllegalArgumentException("Unknown type" + type.toString());
		}
		return "NetBrowserMessage: " + suffix;
	}
	public NetHostInfo getHostInfo() {
		switch (this.type) {
		case INFORM:
			return (NetHostInfo) content[0];
		default:
			throw new IllegalStateException("No NetHostInfo in: " + type.toString());
		}
	}
	public NetUser getUser() {
		switch (this.type) {
		case DISPOSE:
			return (NetUser) content[0];
		default:
			throw new IllegalStateException("No NetUser in: : " + type.toString());
		}
	}
	public static class Factory {
		public static NetMulticastMessage query() {
			return new NetMulticastMessage(Type.QUERY, null);
		}
		public static NetMulticastMessage hostInfo(NetHostInfo info) {
			return new NetMulticastMessage(Type.INFORM, new Object[] { info });
		}
		public static NetMulticastMessage dispose(NetUser user) {
			return new NetMulticastMessage(Type.DISPOSE, new Object[] { user });
		}
	}
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NetMulticastMessage)) return false;
		NetMulticastMessage o = (NetMulticastMessage) obj;
		return Arrays.equals(content, o.content) && this.type.equals(o.type);
	}
	@Override
	public int hashCode() {
		return 31*Arrays.hashCode(content) + type.hashCode();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		String typeStr = type.name();
		out.writeInt(typeStr.length());
		out.writeChars(typeStr);
		out.writeObject(content);
	}
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int typeStrLen = in.readInt();
		String typeStr = "";
		for (int i=0;i<typeStrLen;i++) typeStr += in.readChar();
		this.type = Type.valueOf(typeStr);
		content = (Object[]) in.readObject();
	}
	
}
