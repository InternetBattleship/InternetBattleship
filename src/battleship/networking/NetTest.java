package battleship.networking;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class NetTest {
	
	public static void main(String args[]) {
		new NetTest();
	}
	
	public NetTest() {
		establish();
		promptConcurrently();
		listenOnServer();
		printSocket();
	}
	
	public ServerSocket server;
	public Socket socket;
	public Thread promptThread;
	public Scanner in;
	
	private void establish() {
		try {
			server = new ServerSocket(0);
			System.out.println("Listening at " + Inet4Address.getLocalHost().getHostAddress() + ":" + server.getLocalPort());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private boolean attemptedConnection = false;
	private void promptConcurrently() {
		promptThread = new Thread(() -> {
			in = new Scanner(System.in);
			System.out.print("Enter the server address: ");
			String ip = in.nextLine();
			if (Thread.interrupted()) {
				System.out.println("Prompt interrupted!");
				in.close();
				return;
			}
			int port = 0;
			boolean valid = false;
			while (!valid) {
				try {
					System.out.print("Enter the server port: ");
					port = in.nextInt();
					valid = true;
				} catch (InputMismatchException e) {
					System.out.println("Invalid port! \n");
				} finally {
					if (Thread.interrupted()) {
						System.out.println("Prompt interrupted!");
						in.close();
						return;
					}
				}
			}
			in.close();
			attemptedConnection = true;
			try {
				Socket s = new Socket(ip, port);
				server.close();
				socket = s;
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		promptThread.start();
	}
	private void listenOnServer() {
		try {
			socket = server.accept();
			promptThread.interrupt();
			try {
				promptThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch(SocketException e) {
			if (!attemptedConnection) {
				e.printStackTrace();
			} else {
				System.out.println("Server closed because connection was initiated!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void printSocket() {
		System.out.println();
		if (socket != null) {
			System.out.println("Connected from " + socket.getLocalPort() + " to " + socket.getInetAddress() + ":" + socket.getPort());
		} else {
			System.out.println("No connection made.");
		}
		System.out.println(promptThread.isAlive());
	}
}
