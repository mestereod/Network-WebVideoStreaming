import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class GUI {
    public JPanel main;
    public JLabel jl;
    public JPanel video;
    public JScrollPane text;
    private JTextArea textArea1;
    private JPanel chat;
    private JButton sendButton;
    private JTextArea textArea2;

    public GUI() {
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Streaming");
        GUI gui = new GUI();
        frame.setContentPane(gui.main); // reference: https://www.youtube.com/watch?v=5vSyylPPEko
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
