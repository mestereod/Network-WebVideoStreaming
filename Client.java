import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import java.util.UUID;


public class Client extends Thread {

	private DataInputStream in;
	private JFrame frame;
	private GUI gui;

	public Client(String serverName, int portStreaming, int portChat ) {
		frame = new JFrame("Streaming");
		gui = new GUI();
		frame.setContentPane(gui.root); // reference: https://www.youtube.com/watch?v=5vSyylPPEko
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

		try {
			System.out.println("Connecting to " + serverName + " on port " + portStreaming);
			Socket client = new Socket(serverName, portStreaming);
			InputStream inFromServer = client.getInputStream();
			this.in = new DataInputStream(inFromServer);
			System.out.println("Just connected to " + client.getRemoteSocketAddress());

			Thread chat = new ChatListener(serverName, portChat, gui, frame);
			chat.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void run() {

		while (true) {
			try {

				int len = in.readInt(); // receiving the screenshot's length

				byte[] byteImg = new byte[len];
				in.readFully(byteImg, 0, len); // reading the screenshot

				BufferedImage img = ImageIO.read(new ByteArrayInputStream(byteImg)); // converting the bytes into an image
				BufferedImage image = Scalr.resize(img,Scalr.Mode.FIT_TO_HEIGHT,gui.root.getWidth(), (gui.root.getHeight() - gui.chatPanel.getHeight()));

				// showing the image on the GUI
				if (img != null)
					gui.label.setIcon(new ImageIcon(image)); // reference: https://github.com/Imran92/Java-UDP-Video-Stream-Server/blob/master/src/java_video_stream/JavaClient.java#L149

				frame.getContentPane().repaint();

				Thread.sleep(15);

			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame,
						"Server does not respond.",
						"ERROR",
						JOptionPane.WARNING_MESSAGE);
				break;

			}
		}

	}

	public static void main(String [] args) {
		String serverName = "localhost";
		int portStreaming = 12345;
		int portChat = 12346;
		Thread streaming = new Client(serverName, portStreaming, portChat);
		streaming.start();
	}
}

class ChatListener extends Thread {

	private DataOutputStream outChat;
	private DataInputStream inChat;
	private JFrame frame;
	private GUI gui;

	public ChatListener(String serverName, int port, GUI gui, JFrame frame) throws IOException {
		Socket chat = new Socket(serverName, port);
		this.outChat = new DataOutputStream(chat.getOutputStream());
		this.inChat = new DataInputStream(chat.getInputStream());
		this.gui = gui;
		this.gui.setUsername(UUID.randomUUID().toString());
		this.gui.setDataOutputStream(outChat);
		this.frame = frame;
	}

	public void run() {
		while(true) {
			String chatMessage = null;
			try {
				chatMessage = inChat.readUTF();
				if (chatMessage != null) {
					gui.view.append(chatMessage);
					gui.view.setCaretPosition(gui.view.getDocument().getLength());
				}

				frame.getContentPane().repaint();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}
}
