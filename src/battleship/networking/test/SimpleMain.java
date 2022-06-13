package battleship.networking.test;

import java.util.Scanner;

public class SimpleMain {

	private static Scanner console;
	
	public static void main(String args[]) {
		console = new Scanner(System.in);
		boolean picked = false;
		boolean server = false;
		while (!picked) {
			System.out.println("Server or client? (s/c)");
			String choice = console.nextLine().trim().toLowerCase();
			if (choice.equals("c")) {
				server = false;
				picked = true;
			} else if (choice.equals("s")) {
				server = true;
				picked = true;
			} else {
				System.err.println("Invalid input! Try again.");
			}
		}
		
		if (server) {
			SimpleServer.main(null);
		} else {
			SimpleConnection.main(null);
		}
	}
	
}
