import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class Server extends Thread {
	private static ServerSocket serverSocket;
	private static ArrayList<DataOutputStream> clientsOut; // used for sending messages to all clients
	private static ArrayList<DataInputStream> clientsIn;
	public static Queue<byte[]> imgQueue;

	synchronized public void run() {

		while (true) {

			byte[] byteImg = imgQueue.poll();
			if (byteImg != null) {
				for (Iterator<DataOutputStream> it = clientsOut.iterator(); it.hasNext(); ) {
					DataOutputStream out = it.next();
					try {
						out.writeInt(byteImg.length); // sending the size of the image
						out.write(byteImg); // sending the image // reference: https://stackoverflow.com/questions/25086868/how-to-send-images-through-sockets-in-java
						out.flush(); // forcing to write in the socket everything on the DataOutputStream buffer
					} catch (IOException ioException) {
						it.remove();
						ioException.printStackTrace();
					}
				}
			}

			try {
				Thread.sleep(15);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
   
	synchronized public static void main(String [] args) {
		int port = 12345;
		try {
			serverSocket = new ServerSocket(port);
			clientsOut = new ArrayList<DataOutputStream>();
			imgQueue = new LinkedList<byte[]>();
			ScreenPrinter.startScreenshots(3,15,imgQueue);
			Thread t = new Server();
			t.start();

			while(true) { // reference: https://stackoverflow.com/questions/10131377/socket-programming-multiple-client-to-one-server
				System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
				Socket server = serverSocket.accept();
				System.out.println("Just connected to " + server.getRemoteSocketAddress());
				clientsOut.add(new DataOutputStream(server.getOutputStream()));
			}

		} catch (Exception e) {
        	e.printStackTrace();
    	}
	}
}