import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class Server extends Thread {
	private static ServerSocket serverSocket;
	private static ServerSocket serverSocketChat;
	private static ArrayList<DataOutputStream> clientsOut; // used for sending the frames to all clients
	private static ArrayList<DataInputStream> clientsIn;
	public static Queue<byte[]> imgQueue;

	public void run() {

		while (true) {
			byte[] byteImg = null;
			synchronized (imgQueue) {
				byteImg = imgQueue.poll();
			}
			synchronized (clientsOut) {
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
			}

			try {
				Thread.sleep(45);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	public static void main(String [] args) {

		// GUI
		JFrame frame = new JFrame("Server");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		int port = 12345;
		int portChat = 12346;
		try {
			serverSocket = new ServerSocket(port);
			serverSocketChat = new ServerSocket(portChat);
			clientsOut = new ArrayList<DataOutputStream>();
			imgQueue = new LinkedList<byte[]>();
			Thread serverThread = new Server();
			serverThread.start();
			ScreenPrinter.startScreenshots(2,30, imgQueue);

			while(true) { // reference: https://stackoverflow.com/questions/10131377/socket-programming-multiple-client-to-one-server
				System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
				Socket server = serverSocket.accept();
				System.out.println("Just connected to " + server.getRemoteSocketAddress());
				synchronized (clientsOut) {
					clientsOut.add(new DataOutputStream(server.getOutputStream()));
				}

				// chat
				Socket chatSocket = serverSocketChat.accept();
				Thread chat = new ServerChatListener(chatSocket);
				chat.start();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class ServerChatListener extends Thread {

	private static ArrayList<DataOutputStream> clientsOut; // used for sending messages to all clients
	private DataInputStream in;

	public ServerChatListener(Socket socket) throws IOException {
		in = new DataInputStream(socket.getInputStream());
		if (clientsOut == null)
			clientsOut = new ArrayList<DataOutputStream>();
		synchronized (clientsOut) {
			clientsOut.add(new DataOutputStream(socket.getOutputStream()));
		}
	}

	public void run() {
		while(true) {
			String chatMessage = null;
			try {
				chatMessage = in.readUTF();
				if (chatMessage != null) {
					synchronized (clientsOut) {
						for (Iterator<DataOutputStream> it = clientsOut.iterator(); it.hasNext(); ) {
							DataOutputStream out = it.next();
							try {
								out.writeUTF(chatMessage);
							} catch (IOException ioException) {
								it.remove();
								ioException.printStackTrace();
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}
}