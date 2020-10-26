import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class Server extends Thread { //classe principal do servidor, onde é feito a criacao de socket e o envio das cadeias de bytes das imagens para os clientes
	private static ServerSocket serverSocket; //socket do stream de imagens
	private static ServerSocket serverSocketChat; //socket do chat
	private static ArrayList<DataOutputStream> clientsOut; // arranjo contendo os dados que serão transmitidos para todos os clientes
//	private static ArrayList<DataInputStream> clientsIn;
	public static Queue<byte[]> imgQueue; //fila contendo as imagens que sao adicionadas conforme o Robot executa a tarefa

	public void run() {

		while (true) {
			byte[] byteImg = null;
			synchronized (imgQueue) { //como tem mais de 1 instancia do Robot rodando, e necessario sincronizar o input nessa fila
				byteImg = imgQueue.poll();
			}
			synchronized (clientsOut) {
				if (byteImg != null) {
					for (Iterator<DataOutputStream> it = clientsOut.iterator(); it.hasNext(); ) {
						DataOutputStream out = it.next();
						try {
							out.writeInt(byteImg.length); // envia o tamanho da imagem
							out.write(byteImg); // envia a imagem // reference: https://stackoverflow.com/questions/25086868/how-to-send-images-through-sockets-in-java
							out.flush(); // força a escrever no socket tudo que esta no DataOutputStream buffer
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
			serverSocket = new ServerSocket(port); //inicializa o socket para stream de imagens
			serverSocketChat = new ServerSocket(portChat); //inicializa o socket para stream do chat
			clientsOut = new ArrayList<DataOutputStream>(); //inicializar o arranjo de saida de dados para os clientes
			imgQueue = new LinkedList<byte[]>(); //inicializar a fila de imagens em sequencia, geradas pelo Robot
			Thread serverThread = new Server(); //inicializa a Thread do servidor
			serverThread.start();
			//utiliza o metodo estatico do ScreenPrinter, o qual ira usar o Robot para gerar as screenshots.
			//e passado o numero de threads, para que se tenha mais de uma instancia funcionando do Robot
			//tambem passa o numero representante do delay em que essas instancias irao ficar esperando apos cada screenshot.
			ScreenPrinter.startScreenshots(2,30, imgQueue); 

			while(true) { // reference: https://stackoverflow.com/questions/10131377/socket-programming-multiple-client-to-one-server
				System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
				Socket server = serverSocket.accept();  //aguarda ate o momento em que o cliente faz uma requisiçao de conexao do stream de imagens
				System.out.println("Just connected to " + server.getRemoteSocketAddress());
				synchronized (clientsOut) {
					clientsOut.add(new DataOutputStream(server.getOutputStream())); //adiciona mais um elemento no arranjo de dados de saida para os clientes
				}

				// chat
				Socket chatSocket = serverSocketChat.accept(); //a requisicao de conexao do chat por parte do cliente vem logo em seguida
				Thread chat = new ServerChatListener(chatSocket); //aloca uma Thread de chat para recebimento de textos desse cliente
				chat.start();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class ServerChatListener extends Thread { //classe focada em sincronizar os textos do chat entre os clientes

	private static ArrayList<DataOutputStream> clientsOut; // arranjo contendo os dados de saida para os clientes
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