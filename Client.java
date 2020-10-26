import org.imgscalr.Scalr;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import java.util.UUID;

/*
Classe responsável por escutar as imagens e mensagens enviadas pelo servidor, manipular esses dados e apresentar
na interface para o cliente
 */
public class Client extends Thread {

	private DataInputStream in; // DataInputStream que recebe os frames(screenshots) do servidor
	private JFrame frame; // janela da interface com o usuário
	private GUI gui; // interface gráfica geral do cliente, gerada via forms utilizando java Swing UI. É alimentada pelo arquivo GUI.form

	/*
	O construtor inicializa a GUI e inicia a conexão de streaming com o servidor de acordo com o hostname e porta de streaming
	Também inicia a thread de ChatListener que inicia a conexão de chat utilizando o hostname e a porta do chat
	 */
	public Client(String serverName, int portStreaming, int portChat ) {

		// inicialização da GUI
		frame = new JFrame("Streaming");
		gui = new GUI();
		frame.setContentPane(gui.root);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

		/*
        A utilização do Robot se mostrou muito devagar para realizar streaming, como uma das estratégias para melhorar
        esse cenário desativamos a função de cache, que gravava os screenshots em disco antes da manipulação.
         */
		ImageIO.setUseCache(false);

		try {
			System.out.println("Connecting to " + serverName + " on port " + portStreaming);
			Socket client = new Socket(serverName, portStreaming); // inicia a conexão de streaming
			this.in = new DataInputStream(client.getInputStream()); // inicializa o DataInputStream para receber as imagens do servidor
			System.out.println("Just connected to " + client.getRemoteSocketAddress());

			// instancia e começa a thread para o chat
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

				int len = in.readInt(); // recebe o tamanho da imagem

				byte[] byteImg = new byte[len]; // aloca um array com o tamanho da imagem
				in.readFully(byteImg, 0, len); // le todos os dados de entrada até completarem o array byteImg, tendo assim a imagem completa

				BufferedImage img = ImageIO.read(new ByteArrayInputStream(byteImg)); // converte o array de bytes em uma imagem
				BufferedImage image = Scalr.resize(img,Scalr.Mode.FIT_TO_HEIGHT,gui.root.getWidth(), (gui.root.getHeight() - gui.chatPanel.getHeight())); // ajusta a escala da imagem de acordo com o tamanho da janela do cliente

				// mostra a imagem na janela do cliente
				if (img != null)
					gui.label.setIcon(new ImageIcon(image)); // Referência utilizada: https://github.com/Imran92/Java-UDP-Video-Stream-Server/blob/master/src/java_video_stream/JavaClient.java#L149
				// atualiza a janela para mostrar a nova imagem
				frame.getContentPane().repaint();

				Thread.sleep(15);

			} catch (Exception e) { // caso a conexão falhe, gera um aviso para o cliente e encerra a thread
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame,
						"Server does not respond. Please try again later.",
						"ERROR",
						JOptionPane.WARNING_MESSAGE);
				break;

			}
		}

	}

	/*
	O main tem a função de centralizar os parâmetros de conexão com o servidor como hostname e portas e inicia a thread Client,
	que gera a GUI, inicia a thread ChatListener e mantém a atualização dos screenshots tirados no servidor.
	 */
	public static void main(String [] args) {
		String serverName = "localhost";
		int portStreaming = 12345;
		int portChat = 12346;
		Thread streaming = new Client(serverName, portStreaming, portChat);
		streaming.start();
	}
}

/*
Essa classe tem a função de estabelecer a conexão do chat, enviar o DataOuputStream desse cliente para a classe GUI
(utilizado ao clicar no botão enviar para então enviar a mensagem digitada ao servidor), e escutar a propragação de
mensagens do servidor e apresentá-las atualizando a GUI.
 */
class ChatListener extends Thread {

	private DataOutputStream outChat; // utilizado para enviar mensagens ao servidor
	private DataInputStream inChat; // utilizado escutar as mensagens enviadas pelo servidor
	private JFrame frame;
	private GUI gui;

	/*
	O construtor inicia a conexão com o servidor, aloca as variáveis, configura um username random para o cliente
	e envia a referência do DataOuputStream do cliente para a classe GUI que utilizará ele para enviar mensagens digitadas
	pelo usuário, a utilização pode ser vista no método sendButton.addActionListener da classe GUI
	 */
	public ChatListener(String serverName, int port, GUI gui, JFrame frame) throws IOException {
		Socket chat = new Socket(serverName, port); // inicia a conexão
		this.outChat = new DataOutputStream(chat.getOutputStream());
		this.inChat = new DataInputStream(chat.getInputStream());
		this.gui = gui;
		this.gui.setUsername("guest:"+UUID.randomUUID().toString());
		this.gui.setDataOutputStream(outChat);
		this.frame = frame;
	}

	/*
	O run da thread se encarrega de ficar escutando a propragação de mensagens do servidor para então apresentar na GUI
	 */
	public void run() {
		while(true) {
			String chatMessage = null;
			try {
				chatMessage = inChat.readUTF(); // fica aguardando o recebimento de uma mensagem
				if (chatMessage != null) { // apresenta a mensagem na GUI do cliente
					gui.view.append(chatMessage);
					gui.view.setCaretPosition(gui.view.getDocument().getLength());
				}
				frame.getContentPane().repaint(); // atualiza a janela para apresentar as atualizações
			} catch (IOException e) { // caso a conexão seja quebrada, a thread de escuta de mensagens é encerrada
				e.printStackTrace();
				break;
			}
		}
	}
}
