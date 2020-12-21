package client;

import network.SocketThread;
import network.SocketThreadListener;
import common.Common;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Client extends JFrame implements Thread.UncaughtExceptionHandler, SocketThreadListener {

    private boolean shownIoErrors = false;
    private SocketThread socketThread;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss: ");

    public static void main(String[] args) {
        new Client().startClient();
    }

    private void startClient() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            String s;
            while (true) {
                s = br.readLine();
                if (s.equals("exit"))
                    break;
                else if (s.equals("download")) {
                    System.out.println("Enter the name of the downloaded file");
                    downLoadFile(br.readLine());
                }
                else if (s.equals("upload")) {
                    System.out.println("Enter the name of the uploaded file");
                    uploadFile(br.readLine());
                }
            }
            br.close();
        } catch (IOException e) {
            showException(Thread.currentThread(), e);
        }
    }

    private void connect(String str) {
        try {
            Socket socket = new Socket("127.0.0.1", 8189);
            socketThread = new SocketThread(this, "Client", socket, str);
        } catch (IOException exception) {
            showException(Thread.currentThread(), exception);
        }
    }

    private void downLoadFile(String fileName) {
       connect(fileName);
    }

    private void uploadFile(String fileName) {
        socketThread.sendFile(fileName);
    }

    private void showException(Thread t, Throwable e) {
        String msg;
        StackTraceElement[] ste = e.getStackTrace();
        if (ste.length == 0)
            msg = "Empty Stacktrace";
        else {
            msg = String.format("Exception in \"%s\" %s: %s\n\tat %s",
                    t.getName(), e.getClass().getCanonicalName(), e.getMessage(), ste[0]);
            JOptionPane.showMessageDialog(this, msg, "Exception", JOptionPane.ERROR_MESSAGE);
        }
        JOptionPane.showMessageDialog(null, msg, "Exception", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        showException(t, e);
        System.exit(1);
    }

    @Override
    public void onSocketStart(SocketThread thread, Socket socket) {

    }

    @Override
    public void onSocketStop(SocketThread thread) {

    }

    @Override
    public void onSocketReady(SocketThread thread, Socket socket) {

    }

    @Override
    public void onReceiveFile(SocketThread thread, Socket socket, String msg) {

    }

    @Override
    public void onSocketException(SocketThread thread, Exception exception) {
        showException(thread, exception);
    }
}
