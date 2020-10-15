import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Queue;

public class ScreenPrinter extends Thread {

    public static Queue<byte[]> imgQueue;
    public int sleepTime;
    private Robot rb;
    private Rectangle screen;

    public ScreenPrinter(Queue<byte[]> imgQueue, int sleepTime) throws Exception {
        this.imgQueue = imgQueue;
        this.sleepTime = sleepTime;
        this.rb = new Robot(); // reference: https://github.com/Imran92/Java-UDP-Video-Stream-Server/blob/master/src/java_video_stream/JavaServer.java#L151
        this.screen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    }

    public static void startScreenshots(int nThreads, int sleepTime, Queue<byte[]> queue) throws Exception{
        Thread[] printers = new ScreenPrinter[nThreads];
        for (int i = 0; i < nThreads; i++) {
            printers[i] = new ScreenPrinter(queue, sleepTime);
        }

        for (Thread printer : printers) {
            printer.start();
            Thread.sleep(sleepTime);
        }
    }

    public void run() {
        while(true) {
            try {

                // reference: https://stackoverflow.com/questions/19839172/how-to-read-all-of-inputstream-in-server-socket-java
                BufferedImage screenshot = rb.createScreenCapture(screen); // getting the screenshot

                // converting the screenshot into bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(screenshot, "jpeg", baos);

                if (baos != null) {
                    byte[] byteImg = baos.toByteArray();
                    imgQueue.add(byteImg);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
