import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

public class GUI {
    public JPanel panel1;
    public JButton button1;
    public JLabel jl;

    public GUI() {
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("test");
        GUI gui = new GUI();
        frame.setContentPane(gui.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        try {

            BufferedImage img = ImageIO.read(new File("screen.jpg"));;

            gui.jl.setIcon(new ImageIcon(img));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        frame.repaint();


    }
}
