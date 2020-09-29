import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
	public static void main(String [] args) {
		Scanner scan = new Scanner(System.in);
		String serverName = "localhost";
		int port = 12345;
		try {
			System.out.println("Connecting to " + serverName + " on port " + port);
        	Socket client = new Socket(serverName, port);

        	System.out.println("Just connected to " + client.getRemoteSocketAddress());
        	OutputStream outToServer = client.getOutputStream();
        	DataOutputStream out = new DataOutputStream(outToServer);

        	InputStream inFromServer = client.getInputStream();
         	DataInputStream in = new DataInputStream(inFromServer);

        	out.writeUTF("Hello from " + client.getLocalSocketAddress());

        	String msg = "";
        	while(!"Quit".equalsIgnoreCase(msg) && msg != null) {
        		msg = scan.nextLine();
        		out.writeUTF(msg);

         		//System.out.println("Server says " + in.readUTF());
   			}
   			client.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}