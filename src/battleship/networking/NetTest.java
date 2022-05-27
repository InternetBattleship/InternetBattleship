package battleship.networking;

import java.io.IOException;

import battleship.networking.browsing.NetBrowserFrame;
import battleship.networking.browsing.NetBrowserMessage;
import battleship.networking.browsing.NetHostInfo;

public class NetTest {
	
	public static void main(String args[]) {
//		new NetTestFrame();
		NetBrowserMessage s1, s2;
		s1 = NetBrowserMessage.Factory.hostInfo(NetHostInfo.Factory.local(720, NetUser.Factory.make("Derek", 1234)));
		s2 = NetBrowserMessage.Factory.hostInfo(NetHostInfo.Factory.local(1280, NetUser.Factory.make("Thomas", 9999)));
		try {
			test(s1);
			System.out.println();
			test(s2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		new NetBrowserFrame();
	}
	public static void test(NetBrowserMessage s) throws ClassNotFoundException, IOException {
		System.out.println(s);
		
		byte[] b = s.getBytes();
		NetBrowserMessage f = NetBrowserMessage.fromBytes(b);
		
		System.out.println(f);
		System.out.println("S: " + s.hashCode());
		System.out.println("F: " + f.hashCode());
		System.out.println("==: " + (s == f));
		System.out.println(".equals(): " + s.equals(f));
	}
}
