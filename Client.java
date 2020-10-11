import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client extends Thread {

	private DataOutputStream out;
	private DataInputStream in;
	private int byteLength;
	private byte[] image;


	public Client(String serverName, int port ) {
		try {
			System.out.println("Connecting to " + serverName + " on port " + port);
			Socket client = new Socket(serverName, port);

			System.out.println("Just connected to " + client.getRemoteSocketAddress());
			OutputStream outToServer = client.getOutputStream();
			this.out = new DataOutputStream(outToServer);

			InputStream inFromServer = client.getInputStream();
			this.in = new DataInputStream(inFromServer);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {

		JFrame frame = new JFrame("test");
		GUI gui = new GUI();
		frame.setContentPane(gui.panel1);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

		while (true) {
			try {
				byteLength = in.readInt();
				image = new byte[byteLength];
				in.read(image);
				BufferedImage img = ImageIO.read(new ByteArrayInputStream(image));

				if (img != null) {
					gui.jl.setIcon(new ImageIcon(img));
					frame.repaint();
					Thread.sleep(15);
				}


			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static void main(String [] args) {
		Scanner scan = new Scanner(System.in);
		String serverName = "localhost";
		int port = 12345;
		Thread clt = new Client(serverName, port);
		clt.start();

//		try {
//			System.out.println("Connecting to " + serverName + " on port " + port);
//        	Socket client = new Socket(serverName, port);
//
//        	System.out.println("Just connected to " + client.getRemoteSocketAddress());
//        	OutputStream outToServer = client.getOutputStream();
//        	DataOutputStream out = new DataOutputStream(outToServer);
//
//        	InputStream inFromServer = client.getInputStream();
//         	DataInputStream in = new DataInputStream(inFromServer);
//			int byteLength = in.readInt();
//			byte[] image = new byte[byteLength];
//			in.read(image);
//			BufferedImage img = ImageIO.read(new ByteArrayInputStream(image));
//			ImageIO.write(img, "jpg", new File("screen.jpg"));
//
//        	out.writeUTF("Hello from " + client.getLocalSocketAddress());
//
//        	String msg = "";
//        	while(!"Quit".equalsIgnoreCase(msg) && msg != null) {
//        		msg = scan.nextLine();
//        		out.writeUTF(msg);
//
//         		//System.out.println("Server says " + in.readUTF());
//   			}
//   			client.close();
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//		}
	}
}