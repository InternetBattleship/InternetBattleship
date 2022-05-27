package battleship.networking.browsing;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

import battleship.networking.Serialization;

public class NetBrowserMessage implements Externalizable {
	
	// Should only be used when deserializing
	public NetBrowserMessage() { }
	
	public byte[] getBytes() throws IOException {
		return Serialization.serializeToBytes(this);
	}
	public static NetBrowserMessage fromBytes(byte[] bytes) throws ClassNotFoundException, IOException {
		return Serialization.deserializeFromBytes(bytes);
	}
	public enum Type { SCAN, INFO; }
	private Type type = null;
	public Type getType() { return type; }
	private Object[] content = null;
	private NetBrowserMessage(Type type, Object[] content) {
		this.type = type;
		this.content = content;
	}
	@Override
	public String toString() {
		String suffix = "default suffix";
		switch (type) {
		case INFO:
			suffix = getHostInfo().toString();
			break;
		case SCAN:
			suffix = "Scan";
			break;
		default:
			throw new IllegalArgumentException("Unknown type" + type.toString());
		}
		return "NetBrowserMessage: " + suffix;
	}
	public NetHostInfo getHostInfo() {
		switch (this.type) {
		case INFO:
			return (NetHostInfo) content[0];
		default:
			throw new IllegalStateException("Unknown NetBrowserMessage.Type: " + type.toString());
		}
	}
	public static class Factory {
		public static NetBrowserMessage scan() {
			return new NetBrowserMessage(Type.SCAN, null);
		}
		public static NetBrowserMessage hostInfo(NetHostInfo info) {
			return new NetBrowserMessage(Type.INFO, new Object[] { info });
		}
	}
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NetBrowserMessage)) return false;
		NetBrowserMessage o = (NetBrowserMessage) obj;
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
