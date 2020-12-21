package network;

import java.io.*;
import java.net.Socket;

/**
 * Данный класс не предназначен ни для чего иного, кроме как создания половинок сокета на двух сторонах
 * (которые объединяются в сокет). Здесь выполняется функционал нашей программы: приём, отправка сообщений.
 * */

public class SocketThread extends Thread {

    private final SocketThreadListener listener;
    private final Socket socket;
    private OutputStream out;
    private static final String COMPLETED_DOWNLOADING = "File downloaded";
    private final String upFile;

    public SocketThread(SocketThreadListener listener, String name, Socket socket, String upFile) {
        super(name);
        this.socket = socket;
        this.listener = listener;
        this.upFile = upFile;
        start();
    }

    @Override
    public void run() {
        try {
            listener.onSocketStart(this, socket);
            InputStream in = socket.getInputStream();
            out = socket.getOutputStream();
            listener.onSocketReady(this, socket);
            int i;
            while (!isInterrupted()) {
                FileOutputStream fos = new FileOutputStream("out/" + upFile);
                byte[] buf = new byte[1024];
                while ((i = in.read(buf)) != -1)
                    fos.write(buf, 0, i);
                listener.onReceiveFile(this, socket, COMPLETED_DOWNLOADING);
            }
        } catch (IOException e) {
            listener.onSocketException(this, e);
        } finally {
            close();
            listener.onSocketStop(this);
        }
    }

    public synchronized boolean sendFile(String fileName) {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            int i;
            byte[] buf = new byte[1024];
            while ((i = fis.read(buf)) != -1)
                out.write(buf, 0, i);
            out.flush();
            return true;
        } catch (IOException e) {
            listener.onSocketException(this, e);
            close();
            return false;
        }
    }

    public synchronized void close() {
        interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            listener.onSocketException(this, e);
        }
    }
}
