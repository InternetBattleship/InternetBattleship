package battleship.networking.browsing;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import battleship.networking.NetUser;

public class NetHostInfo implements Externalizable {
	public InetSocketAddress socketAddress = null;
	public NetUser user = null;
	
	// Should only be used when deserializing
	public NetHostInfo() { }
	
	public NetHostInfo(InetSocketAddress addr, NetUser host) {
		this.socketAddress = addr;
		this.user = host;
	}
	public static class Factory {
		public static NetHostInfo local(int port, NetUser user) {
			try {
				return new NetHostInfo(new InetSocketAddress(InetAddress.getLocalHost(), port), user);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				throw new IllegalStateException("Cannot find local host!");
			}
		}
	}
	
	@Override
	public String toString() {
		return user.toString() + ", " + getIPAndPort();
	}
	public NetUser user() { return user; }
	public String ip() { // Not sure if this is entirely reliable
		return socketAddress.isUnresolved() ? 
				socketAddress.getHostString() : 
				socketAddress.getAddress().getHostAddress();
	}
	public int port() {
		return socketAddress.getPort();
	}
 	public String getIPAndPort() {
		return ip() + ":" + port();
	}
	
	@Override
	public int hashCode() {
		return (user.hashCode()*43) + 
				(36*ip().hashCode()) + // This is bad i should fix it
				port();
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof NetHostInfo)) return false;
		NetHostInfo nhi = (NetHostInfo) o;
		return nhi.user.equals(this.user) && 
				nhi.ip().equals(this.ip()) &&  // This is bad i should fix it
				nhi.port() == this.port();
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		String ip = socketAddress.getAddress().getHostAddress();
		out.writeInt(ip.length());
		out.writeChars(ip);
		out.writeInt(socketAddress.getPort());
		out.writeObject(user);
	}
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int ipLen = in.readInt();
		String ip = "";
		for (int i=0;i<ipLen;i++) ip += in.readChar();
		int port = in.readInt();
		socketAddress = InetSocketAddress.createUnresolved(ip, port);
		user = (NetUser) in.readObject();
	}
}
