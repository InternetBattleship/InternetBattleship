package battleship.networking;

import battleship.networking.browsing.NetBrowserMessage;
import battleship.networking.browsing.NetHostInfo;

public class NetTest {
	
	public static void main(String args[]) {
//		new NetTestFrame();
		NetBrowserMessage nbm1, nbm2;
		try {
			nbm1 = NetBrowserMessage.Factory.hostInfo(NetHostInfo.Factory.local((int)(Math.random()*65535), new NetUser()));
			byte[] bytes = nbm1.getBytes();
			nbm2 = NetBrowserMessage.fromBytes(bytes);
			System.out.println(nbm1.equals(nbm2));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		new NetBrowserFrame();
	}
}
