import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;

public class Client extends Thread {

	private DataOutputStream out;
	private DataInputStream in;

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

		JFrame frame = new JFrame("Streaming");
		GUI gui = new GUI();
		frame.setContentPane(gui.panel1); // reference: https://www.youtube.com/watch?v=5vSyylPPEko
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

		while (true) {
			try {

				int len = in.readInt(); // receiving the screenshot's length

				byte[] byteImg = new byte[len];
				in.readFully(byteImg, 0, len); // reading the screenshot

				Image img = ImageIO.read(new ByteArrayInputStream(byteImg)).getScaledInstance(frame.getWidth(),frame.getHeight(), Image.SCALE_DEFAULT); // converting the bytes into an image

				// showing the image on the GUI
				if (img != null)
					gui.jl.setIcon(new ImageIcon(img)); // reference: https://github.com/Imran92/Java-UDP-Video-Stream-Server/blob/master/src/java_video_stream/JavaClient.java#L149
				frame.getContentPane().repaint();

				Thread.sleep(15);

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
	}
}