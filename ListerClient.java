import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.LinkedList;
import java.util.Queue;

public class ListerClient extends Thread{
    DataInputStream in;
    public static Queue<BufferedImage> b_images = new LinkedList<BufferedImage>();
    public static byte byteimg[];

    public ListerClient(DataInputStream in){
        this.in = in;
    }
    public void run(){
        try {
            while (true){
                int len = in.readInt(); // receiving the screenshot's length
                byte[] byteImg = new byte[len];
                in.readFully(byteImg, 0, len); // reading the screenshot
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(byteImg)); // converting the bytes into an image
                b_images.add(img);
                Thread.sleep(15);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
