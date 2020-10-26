import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Queue;

public class ScreenPrinter extends Thread { //classe com foco em gerar screenshots da tela principal do servidor e entao adicionar a imagem na fila

    public static Queue<byte[]> imgQueue; //fila de imagens referencias do Server
    public int sleepTime; //delay para gerar uma nova screenhot
    private Robot rb; //classe Robot responsavel por gerar screenshot
    private Rectangle screen;

    public ScreenPrinter(Queue<byte[]> imgQueue, int sleepTime) throws Exception {
        this.imgQueue = imgQueue;
        this.sleepTime = sleepTime;
        this.rb = new Robot(); // reference: https://github.com/Imran92/Java-UDP-Video-Stream-Server/blob/master/src/java_video_stream/JavaServer.java#L151
        this.screen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        ImageIO.setUseCache(false);//desativa cache para nao gravar em disco as imagens
    }

    //metodo para inicializar as threads dessa mesma classe de acordo com os parametros
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
                BufferedImage screenshot = rb.createScreenCapture(screen); // Robot gera a screenshot

                // converte a imagem em bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageOutputStream stream = new MemoryCacheImageOutputStream(baos); 
                ImageIO.write(screenshot, "jpeg", stream);
                stream.close();

                synchronized (imgQueue) {
                    imgQueue.add(baos.toByteArray()); //adiciona o arranjo de bytes da imagem na fila de imagens
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
