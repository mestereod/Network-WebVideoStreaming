import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class GUI {
    public JPanel root;
    public JTextArea view;
    private JButton sendButton;
    public JLabel label;
    public JPanel chatPanel;
    private JTextField chat;
    private DataOutputStream messageToServer;
    private String username;

    public GUI() {
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { // reference: https://github.com/Imran92/Java-UDP-Video-Stream-Server/blob/master/src/java_video_stream/JavaClient.java#L185
                String sentence = chat.getText();
                if (sentence == null || sentence.equals("")) return;
                try {
                    messageToServer.writeUTF(username + ": " + sentence + '\n');
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                chat.setText(null);
            }
        });
        chat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sentence = chat.getText();
                if (sentence == null || sentence.equals("")) return;
                try {
                    messageToServer.writeUTF(username + ": " + sentence + '\n');
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                chat.setText(null);
            }
        });
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() { return username; }

    public void setDataOutputStream(DataOutputStream messageToServer) {
        this.messageToServer = messageToServer;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Test");
        GUI gui = new GUI();
        frame.setContentPane(gui.root); // reference: https://www.youtube.com/watch?v=5vSyylPPEko
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
