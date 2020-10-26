import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/*
Classe principal do servidor, uma instancia desse ca classe Server tem a função de enviar os frames(screenshots) para
os clientes.
Manipula a fila de frames imgQueue, retirando frames dela e enviando aos clientes. O envio é feito iterando por um array
de DataOutputStream chamado clientsOut, nele estão alocados os DataOutputStream de cada cliente que se conectou ao servidor.
A inclusão desses DataOutputStreams no array é feito no método main dessa classe após o aceite da conexão por parte do servidor.
 */
public class Server extends Thread {
	private static ServerSocket serverSocket; // socket do stream de imagens
	private static ServerSocket serverSocketChat; // socket do chat
	private static ArrayList<DataOutputStream> clientsOut; // array contendo o DataOutputStream de cada cliente conectado ao servidor
	public static Queue<byte[]> imgQueue; // fila contendo os frames(screenshots) obtidos via threads da classe ScreenPrinter

	public void run() {

		while (true) {
			/*
			Obtenção dos frames para enviar aos clientes. Synchronized usado para evitar o uso concorrente dessa fila,
			que é utilizada por essa thread Server e por K threads  da classe ScreenPrinter, que a alimentam.
			 */
			byte[] byteImg = null;
			synchronized (imgQueue) {
				byteImg = imgQueue.poll();
			}

			/*
			Envio dos frames para todos os clientes conectados ao servidor. Itera por cada DataOutputStream, envia o
			tamanho do frame e em seguida o frame. O envio do tamanho da imagem antes é necessário para que o consumidor
			não tente transformar o array de bytes em uma imagem antes de a ter por inteiro.
			Referência utilizada: https://stackoverflow.com/questions/25086868/how-to-send-images-through-sockets-in-java
			 */
			synchronized (clientsOut) {
				if (byteImg != null) {
					for (Iterator<DataOutputStream> it = clientsOut.iterator(); it.hasNext(); ) {
						DataOutputStream out = it.next();
						try {
							out.writeInt(byteImg.length); // envia o tamanho da imagem
							out.write(byteImg); // envia a imagem
							out.flush(); // força a escrever no socket tudo que esta no DataOutputStream buffer
						} catch (IOException ioException) {
							/* A exceção indica erro na comunicação com o cliente, nesse caso ele é removido do array e deve se conectar novamente */
							it.remove();
							ioException.printStackTrace();
						}
					}
				}
			}

			/* Grosseiramente podemos considerar o envio de um frame a cada X milisegundos, sendo X o número indicado no sleep */
			try {
				Thread.sleep(45);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	/*
	O método main tem a função de aceitar as conexões ao servidor gerenciar as estruturas de dados utilizadas pela
	thread Server e pelas threads ServerChatListener como por exemplo adicionar os DataOutputStreams de cada cliente
	do socket de streaming no array clientsOut.
	 */
	public static void main(String [] args) {

		// GUI - Uma interface gráfica vazia apenas para facilitar a utilização e encerramento via executável
		JFrame frame = new JFrame("Server");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		// portas utilizadas para respectivamente streaming e chat
		int port = 12345;
		int portChat = 12346;

		try {
			serverSocket = new ServerSocket(port); // inicializa o socket para streaming de tela
			serverSocketChat = new ServerSocket(portChat); // inicializa o socket para o chat

			clientsOut = new ArrayList<DataOutputStream>(); // inicializa o array de DataOutputStream de clientes conectados
			imgQueue = new LinkedList<byte[]>(); // inicializar a fila de frames gerados pelas threads da classe ScreenPrinter

			// inicializa e começa a rodar a thread Server, que envia frames da fila imgQueue para todos os clientes conectados
			Thread serverThread = new Server(); // inicializa a Thread do servidor
			serverThread.start();

			/*
			Aciona o método estático da classe ScreenPrinter que tem a função de começar a rodar threads ScreenPrinter
			para tirarem screenshots da tela e converterem as imagens para arrays de bytes.
			O uso de tal função via método estático tem o objetivo de centralizar a parametrização das threads de captura de tela
			Durante os testes a suavidade(smoothness) do vídeo se mostrou diferente dependendo da máquina. Dito isto, o
			ajuste desses parâmetros pode melhorar a experiência de vídeo.
			Entre os parâmetros temos o número de threads da classe ScreenPrinter que ficaram ativamente tirando capturas
			de tela, e o tempo de sleep dessas threads. É importante ressaltar que elas são "startadas" também de acordo
			com o sleepTime, assim de maneira grosseira podemos esperar que cada screenshot terá um espaçamento de
			no mínimo 30 milisegundos.
			 */
			ScreenPrinter.startScreenshots(2,30, imgQueue);

			/*
			O while abaixo aceita as conexões, inclui o DataOuputStream de streaming do novo cliente no array utilizado no envio dos frames e
			começa a rodar uma Thread ServerChatListener pra esse cliente, que escuta as mensagens desse cliente para propragar aos outros.
			Referência utilizada: https://stackoverflow.com/questions/10131377/socket-programming-multiple-client-to-one-server
			 */
			while(true) {
				System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
				Socket server = serverSocket.accept();  // aguarda ate o momento em que o cliente faz uma requisiçao de conexao do stream de frames
				System.out.println("Just connected to " + server.getRemoteSocketAddress());

				synchronized (clientsOut) { // inclui o DataOutputStream desse cliente no array utilizado para o envio dos frames
					clientsOut.add(new DataOutputStream(server.getOutputStream()));
				}

				// chat
				Socket chatSocket = serverSocketChat.accept(); // a requisicao de conexao do chat por parte do cliente vem logo em seguida da de streaming
				Thread chat = new ServerChatListener(chatSocket); // instancia e inicia uma Thread de chat para recebimento das mensagens desse cliente
				chat.start();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

/*
Essa classe tem a função de sincronizar os textos do chat entre os clientes.
Se N clientes estão conectados ao servidor então N instâncias dessa thread ficam rodando simultaneamente
Ela aguarda um texto escrito pelo cliente e então propraga esse texto entre os clientes.
Tem uma estratégia similiar à thread Server pois se utiliza de um array de DataOuputStream estático, contendo o
DataOutputStream de todos os clientes conectados ao servidor, assim, se alguma das N threads escutar uma mensagem do
seu respectivo cliente, ela iterará pelo array de DataOutputStreams e enviará essa mensagem para todos os clientes conectados
 */
class ServerChatListener extends Thread {

	private static ArrayList<DataOutputStream> clientsOut; // array contendo o DataOutputStream de cada cliente conectado ao servidor
	private DataInputStream in; // DataInputStream do cliente, age como o listener das mensagens enviadas por este cliente

	/*
	Construtor para capturar o DataOutputStream e DataInputStream dos clientes conectados
	 */
	public ServerChatListener(Socket socket) throws IOException {
		in = new DataInputStream(socket.getInputStream());
		if (clientsOut == null) // caso seja o primeiro cliente a se conectar, inicializa o array de DataOutputStreams dos clientes
			clientsOut = new ArrayList<DataOutputStream>();
		synchronized (clientsOut) {
			clientsOut.add(new DataOutputStream(socket.getOutputStream())); // Adiciona o DataOutputStream desse cliente no array de propragação
		}
	}

	public void run() {
		while(true) {
			String chatMessage = null;
			try {
				chatMessage = in.readUTF(); // Fica aguardando até receber uma mensagem do cliente
				if (chatMessage != null) { // Caso receba, inicia a iteração para propagação dessa mensagem
					synchronized (clientsOut) {
						for (Iterator<DataOutputStream> it = clientsOut.iterator(); it.hasNext(); ) {
							DataOutputStream out = it.next();
							try {
								out.writeUTF(chatMessage); // envio da mensagem para cada um dos clientes conectados
							} catch (IOException ioException) {
								it.remove(); // caso ocorra um problema de conexão, remove o cliente do array de propagação
								ioException.printStackTrace();
							}
						}
					}
				}
			} catch (IOException e) { // caso ocorra um problema de conexão durante a escuta, encerra a thread de escuta
				e.printStackTrace();
				break;
			}
		}
	}
}