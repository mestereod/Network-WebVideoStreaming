import javax.imageio.ImageIO;
import javax.sound.midi.SysexMessage;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;

public class Client extends Thread {

	private DataOutputStream out;
	private DataInputStream in;
	ListerClient lister;

	public Client(String serverName, int port ) {
		try {
			System.out.println("Connecting to " + serverName + " on port " + port);
			Socket client = new Socket(serverName, port);

			System.out.println("Just connected to " + client.getRemoteSocketAddress());
			OutputStream outToServer = client.getOutputStream();
			this.out = new DataOutputStream(outToServer);

			InputStream inFromServer = client.getInputStream();
			this.in = new DataInputStream(inFromServer);
			Thread l1 = new ListerClient(in);
			Thread l2 = new ListerClient(in);
			l1.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {

		JFrame frame = new JFrame("test");
		GUI gui = new GUI();
		frame.setContentPane(gui.panel1); // reference: https://www.youtube.com/watch?v=5vSyylPPEko
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

		while (true) {
			try {
				long start = System.currentTimeMillis();
				// showing the image on the GUI
				if(lister.b_images.isEmpty()) {
					Thread.sleep(5);
					continue;
				}
				gui.jl.setIcon(new ImageIcon(lister.b_images.remove())); // reference: https://github.com/Imran92/Java-UDP-Video-Stream-Server/blob/master/src/java_video_stream/JavaClient.java#L149
				frame.repaint();
				//System.out.println(System.currentTimeMillis() - start);
				Thread.sleep(15);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static void main(String [] args) {
		Scanner scan = new Scanner(System.in);
		//String serverName = "179.247.249.24";
		String serverName = "localhost";
		int port = 12345;
		Thread clt = new Client(serverName, port);
		clt.start();
	}
}