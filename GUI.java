import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;

/*
Classe utilizada para configurar e gerenciar a apresentação e função dos objetos de interface com o usuário
 */
public class GUI {
    public JPanel root;
    public JTextArea view;
    private JButton sendButton;
    public JLabel label;
    public JPanel chatPanel;
    private JTextField chat;
    private DataOutputStream messageToServer;
    private String username;

    /*
    O construtor se encarrega apenas de configurar as funções do botão de envio de mensagens e da tecla enter durante
    a escrita de mensagens, ambos realizam a mesma coisa: o envio do texto digitado para o servidor via o DataOutputStream
    passado pela instância ChatListener do cliente
     */
    public GUI() {
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { // Referência utilizada: https://github.com/Imran92/Java-UDP-Video-Stream-Server/blob/master/src/java_video_stream/JavaClient.java#L185
                String sentence = chat.getText(); // captura a mensagem digitada no objeto UI
                if (sentence == null || sentence.equals("")) return; // caso não exista mensagem não envia
                try {
                    messageToServer.writeUTF(username + ": " + sentence + '\n'); // envia a mensagem ao servidor
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                chat.setText(null); // limpa o campo de escrita
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

    /*
    Getters e setters para configuração da GUI externamente
     */
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() { return username; }

    public void setDataOutputStream(DataOutputStream messageToServer) {
        this.messageToServer = messageToServer;
    }

    /*
    O método main é encarregado de criar a janela e capturar todas as configurações de forms como tamanho de cada janela,
    organização na tela, etc, do arquivo GUI.form
    Isso é realizado pelo método frame.setContentPane()
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Test");
        GUI gui = new GUI();
        frame.setContentPane(gui.root); // Referência utilizada: https://www.youtube.com/watch?v=5vSyylPPEko
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
