package battleship.networking;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import battleship.networking.browsing.NetBrowserMessage;

public class Serialization {

	public static byte[] serializeToBytes(NetBrowserMessage nbm) throws IOException {
		try (
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		) {
			oos.writeObject(nbm);
			return bos.toByteArray();
		}
	}
	public static NetBrowserMessage deserializeFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
		return deserializeFromBytes(bytes, 0, bytes.length);
	}
	public static NetBrowserMessage deserializeFromBytes(byte[] bytes, int offset, int length) throws IOException, ClassNotFoundException {
		byte[] data = bytes.clone();
		try (
			ByteArrayInputStream bis = new ByteArrayInputStream(data, offset, length);
			ObjectInputStream ois = new ObjectInputStream(bis);
		) {
			NetBrowserMessage nbm = (NetBrowserMessage) ois.readObject();
			return nbm;
		}
	}
	public static String bytesToHexStr(byte[] bytes) {
	    String str = ""; 
		for (byte b : bytes) {
	        str += String.format("%02X", b);
	    }	
		return str;
	}
}
