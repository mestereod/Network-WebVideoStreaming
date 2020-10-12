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
			Robot rb = new Robot(); // reference: https://github.com/Imran92/Java-UDP-Video-Stream-Server/blob/master/src/java_video_stream/JavaServer.java#L151
			while (true) {
				BufferedImage screencap = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(screencap,"jpeg",baos);
				byte[] baos_array  = baos.toByteArray();
				byte[] baos_fixed_array = new byte[1000000];
				for(int i = 4; i < baos_array.length + 4; i++){
					baos_fixed_array[i] = baos_array[i-4];
				}

				for(int i = 0; i < 4; i++){
					baos_fixed_array[i] = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(baos_array.length).array()[i];
				}

				System.out.println(baos_array.length);
				out.write(baos_fixed_array); // reference: https://stackoverflow.com/questions/25086868/how-to-send-images-through-sockets-in-java

				//out.writeInt(baos.size());

				out.flush();

				Thread.sleep(7);
			}

//			String msg = in.readUTF();
//        	System.out.println(msg);
//        	while(!"Quit".equalsIgnoreCase(msg) && msg != null) {
//   				msg = in.readUTF();
//   				System.out.println(msg);
//   			}
//   			System.out.println("Closing connection with " + server.getRemoteSocketAddress());
//   			server.close();
   		}
   		catch(Exception e) {
   			e.printStackTrace();
   		}
	}
   
	public static void main(String [] args) {
		int port = 12345;
		try {
			serverSocket = new ServerSocket(port);
			clientsOut = new ArrayList<DataOutputStream>();

			while(true) { // reference: https://stackoverflow.com/questions/10131377/socket-programming-multiple-client-to-one-server
				System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
				Socket server = serverSocket.accept();
				System.out.println("Just connected to " + server.getRemoteSocketAddress());
				Thread t = new Server(server);
        		t.start();
			}

		} catch (IOException e) {
        	e.printStackTrace();
    	}
	}
}