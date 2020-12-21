package network;

import java.io.File;
import java.net.Socket;

public interface SocketThreadListener {

    void onSocketStart(SocketThread thread, Socket socket);
    void onSocketStop(SocketThread thread);

    void onSocketReady(SocketThread thread, Socket socket);
    void onReceiveFile(SocketThread thread, Socket socket, String msg);

    void onSocketException(SocketThread thread, Exception exception);
}
