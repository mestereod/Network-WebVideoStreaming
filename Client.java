import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import java.util.UUID;


public class Client extends Thread {

	private DataInputStream in; //armazena a stream da imagem recebida
	private JFrame frame; //janela onde aparecerá imagem
	private GUI gui; //interface gráfica geral do cliente

	//Cliente inicializa com o hostname, port de stream e port de chat
	public Client(String serverName, int portStreaming, int portChat ) {
		frame = new JFrame("Streaming");
		gui = new GUI();
		frame.setContentPane(gui.root);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		ImageIO.setUseCache(false); //cache false para não salvar as imagens no disco

		try {
			System.out.println("Connecting to " + serverName + " on port " + portStreaming);
			Socket client = new Socket(serverName, portStreaming); //criação do Socket para stream
			InputStream inFromServer = client.getInputStream(); //getInputStream() captura o que o servidor está enviando
			this.in = new DataInputStream(inFromServer);
			System.out.println("Just connected to " + client.getRemoteSocketAddress());

			Thread chat = new ChatListener(serverName, portChat, gui, frame); //aloca uma thread para o chat
			chat.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void run() {

		while (true) {
			try {

				int len = in.readInt(); // recebe o tamanho da imagem

				byte[] byteImg = new byte[len];
				in.readFully(byteImg, 0, len); // le n bytes correspondentes à imagem recebida

				BufferedImage img = ImageIO.read(new ByteArrayInputStream(byteImg)); // converte a cadeia de bytes em uma imagem
				BufferedImage image = Scalr.resize(img,Scalr.Mode.FIT_TO_HEIGHT,gui.root.getWidth(), (gui.root.getHeight() - gui.chatPanel.getHeight())); // ajusta a escala da imagem de acordo com o tamanho da janela do cliente

				//mostra a imagem na janela do cliente
				if (img != null)
					gui.label.setIcon(new ImageIcon(image)); // reference: https://github.com/Imran92/Java-UDP-Video-Stream-Server/blob/master/src/java_video_stream/JavaClient.java#L149
				//atualiza a imagem
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

	private DataOutputStream outChat; //variável para armazenar a saída de texto, cliente -> servidor
	private DataInputStream inChat; //variável para armazenar a entrada de texto, servidor -> cliente
	private JFrame frame;
	private GUI gui;

	//construtor utilizando o hostname e o port do chat para se conectar 
	public ChatListener(String serverName, int port, GUI gui, JFrame frame) throws IOException {
		Socket chat = new Socket(serverName, port); //criação do socket do chat
		this.outChat = new DataOutputStream(chat.getOutputStream());
		this.inChat = new DataInputStream(chat.getInputStream());
		this.gui = gui;
		this.gui.setUsername(UUID.randomUUID().toString()); //utilização do UUID para criação de identificadores no chat sem expor o IP.
		this.gui.setDataOutputStream(outChat); //escreve o texto que o cliente enviou no campo de chat
		this.frame = frame;
	}

	public void run() {
		while(true) {
			String chatMessage = null;
			try {
				chatMessage = inChat.readUTF(); //faz a leitura do texto recebido
				if (chatMessage != null) {
					gui.view.append(chatMessage); //adiciona o texto recebido  no chat
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
