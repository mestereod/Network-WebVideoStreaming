import ServerJava.server.PrintMananger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class Server extends Thread {
	private static ServerSocket serverSocket;
	private static ArrayList<DataOutputStream> clientsOut; // used for sending messages to all clients
	private Socket server;
	private DataInputStream in;
	private static PrintMananger printM;

	public Server(Socket server) {
   		this.server = server;
   		try {
        	this.in = new DataInputStream(server.getInputStream());
		} catch (IOException e) {
        	e.printStackTrace();
		}
	}

	public void run() {
    	try {

			DataOutputStream out = new DataOutputStream(server.getOutputStream());
			clientsOut.add(out);
			// reference: https://github.com/Imran92/Java-UDP-Video-Stream-Server/blob/master/src/java_video_stream/JavaServer.java#L151
			while (true) {
				long start = System.currentTimeMillis();
				// reference: https://stackoverflow.com/questions/19839172/how-to-read-all-of-inputstream-in-server-socket-java
				byte[] byteImg = ServerUpdateImage.img_bytes;

				if(byteImg.length == 1) {
					Thread.sleep(15);
					continue; //coxambre
				}

				out.writeInt(byteImg.length); // sending the size of the image
				out.write(byteImg); // sending the image // reference: https://stackoverflow.com/questions/25086868/how-to-send-images-through-sockets-in-java
				out.flush(); // forcing to write in the socket everything on the DataOutputStream buffer
				//System.out.println(System.currentTimeMillis() - start);
				Thread.sleep(15);
			}
   		}
   		catch(Exception e) {
   			e.printStackTrace();
   		}
	}
   
	public static void main(String [] args) {
		int port = 12345;
		boolean isPrinting = false;

		try {
			serverSocket = new ServerSocket(port);
			clientsOut = new ArrayList<DataOutputStream>();
			printM = new PrintMananger();
			Thread p1 = new PrintMananger();
			Thread p2 = new PrintMananger();
			Thread p3 = new PrintMananger();
			Thread sui = new ServerUpdateImage();

			while(true) { // reference: https://stackoverflow.com/questions/10131377/socket-programming-multiple-client-to-one-server
				System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
				Socket server = serverSocket.accept();
				if(!isPrinting){
					p1.start();
					p2.start();
					sui.start();
					isPrinting = true;
				}
				System.out.println("Just connected to " + server.getRemoteSocketAddress());
				Thread t = new Server(server);
        		t.start();
			}

		} catch (IOException e) {
        	e.printStackTrace();
    	}
	}
}