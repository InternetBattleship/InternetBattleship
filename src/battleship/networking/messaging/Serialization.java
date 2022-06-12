package battleship.networking.messaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import battleship.networking.browsing.NetMulticastMessage;

public class Serialization {

	public static byte[] serializeToBytes(NetMulticastMessage nbm) throws IOException {
		try (
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		) {
			oos.writeObject(nbm);
			return bos.toByteArray();
		}
	}
	public static NetMulticastMessage deserializeFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
		return deserializeFromBytes(bytes, 0, bytes.length);
	}
	public static NetMulticastMessage deserializeFromBytes(byte[] bytes, int offset, int length) throws IOException, ClassNotFoundException {
		byte[] data = bytes.clone();
		try (
			ByteArrayInputStream bis = new ByteArrayInputStream(data, offset, length);
			ObjectInputStream ois = new ObjectInputStream(bis);
		) {
			NetMulticastMessage nbm = (NetMulticastMessage) ois.readObject();
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
