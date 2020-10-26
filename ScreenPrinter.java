import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Queue;

/*
Classe cuja função é tirar capturas de tela, converter as imagens para array de bytes e adicionar esses array na fila de envio
 */
public class ScreenPrinter extends Thread {

    public static Queue<byte[]> imgQueue; // fila de envio de imagens, passada por referência pela classe Server
    public int sleepTime; // delay para uma thread gerar um novo screenshot
    private Robot rb; // a instância de Robot é utilizada para tirar as capturas de tela
    private Rectangle screen; // delimitador das dimensões para a captura de tela

    public ScreenPrinter(Queue<byte[]> imgQueue, int sleepTime) throws Exception {
        this.imgQueue = imgQueue;
        this.sleepTime = sleepTime;
        this.rb = new Robot(); // Referência utilizada: https://github.com/Imran92/Java-UDP-Video-Stream-Server/blob/master/src/java_video_stream/JavaServer.java#L151
        this.screen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        /*
        A utilização do Robot se mostrou muito devagar para realizar streaming, como uma das estratégias para melhorar
        esse cenário desativamos a função de cache, que gravava os screenshots em disco antes da manipulação.
         */
        ImageIO.setUseCache(false);
    }

    /*
    Método estático para inicializar as threads dessa mesma classe de acordo com os parâmetros.
    Em uma abordagem para melhorar o smoothness do streaming, escalona as threads para iniciarem com uma distância
    de sleepTime milisegundos da sua anterior
     */
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
                // Referência utilizada: https://stackoverflow.com/questions/19839172/how-to-read-all-of-inputstream-in-server-socket-java
                BufferedImage screenshot = rb.createScreenCapture(screen); // Robot gera a screenshot

                // converte a imagem em bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageOutputStream stream = new MemoryCacheImageOutputStream(baos); 
                ImageIO.write(screenshot, "jpeg", stream);
                stream.close();

                synchronized (imgQueue) {
                    imgQueue.add(baos.toByteArray()); // adiciona a imagem convertida em bytes na fila de imagens para envio
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
