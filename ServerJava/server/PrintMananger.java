package ServerJava.server;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PrintMananger extends Thread {
    public static Queue<byte[]> images = new LinkedList<byte[]>();

    public void run(){
        try {
            Robot rb = new Robot();
            while (true) {
                BufferedImage screenshot = rb.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize())); // getting the screenshot
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(screenshot,"jpeg",baos);
                byte[] byteImg  = baos.toByteArray();
                images.add(byteImg);
                Thread.sleep(1);
            }
        }
            catch (Exception e){

        }
    }
}
