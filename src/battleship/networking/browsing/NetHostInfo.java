package battleship.networking.browsing;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import battleship.networking.NetUser;

public class NetHostInfo implements Serializable {
	public InetSocketAddress address;
	public NetUser user;
	
	public NetHostInfo(InetSocketAddress addr, NetUser host) {
		this.address = addr;
		this.user = host;
	}
	public static class Factory {
		public static NetHostInfo local(int port, NetUser user) throws UnknownHostException {
			return new NetHostInfo(new InetSocketAddress(InetAddress.getLocalHost(), port), user);
		}
	}
	
	@Override
	public String toString() {
		return user.toString() + ", " + getAddressAsString();
	}
	public String getAddressAsString() {
		return address.getAddress().getHostAddress() + ":" + address.getPort();
	}
	
	@Override
	public int hashCode() {
		return (user.hashCode()*43) ^ address.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof NetHostInfo)) return false;
		NetHostInfo nhi = (NetHostInfo) o;
		return nhi.user.equals(this.user) && nhi.address.equals(this.address);
	}
}
