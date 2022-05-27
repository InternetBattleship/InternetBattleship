package battleship.networking.browsing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class NetBrowserMessage implements Serializable {
	private static byte[] serializeToBytes(NetBrowserMessage nbm) throws IOException {
		System.out.println("Writing: " + nbm.toString());
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos = new ObjectOutputStream(bos);
		oos.writeObject(nbm);
		byte[] data = bos.toByteArray();
		System.out.println("  Wrote bytes: " + bytesToHexStr(data));
		return data;
	}
	private static NetBrowserMessage deserializeFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
		byte[] data = bytes.clone();
		System.out.println("Reading bytes: " + bytesToHexStr(data));
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		bis.
		ObjectInputStream ois = new ObjectInputStream(bis);
		NetBrowserMessage nbm = (NetBrowserMessage) ois.readObject();
		return nbm;
	}
	private static String bytesToHexStr(byte[] bytes) {
	    String str = ""; 
		for (byte b : bytes) {
	        str += String.format("%02X", b);
	    }	
		return str;
	}
	public byte[] getBytes() throws IOException {
		return NetBrowserMessage.serializeToBytes(this);
	}
	public static NetBrowserMessage fromBytes(byte[] bytes) throws ClassNotFoundException, IOException {
		return deserializeFromBytes(bytes);
	}
	public enum Type { SCAN, INFO; }
	private Type type;
	public Type getType() { return type; }
	private Object[] content;
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
}
