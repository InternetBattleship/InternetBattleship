package battleship.networking.test;

import battleship.networking.NetworkController;
import battleship.networking.ui.NetworkControllerFrame;

public class NetTest {
	
	public static void main(String args[]) {
		NetworkController controller = new NetworkController();
		new NetworkControllerFrame(controller);
	}
}
