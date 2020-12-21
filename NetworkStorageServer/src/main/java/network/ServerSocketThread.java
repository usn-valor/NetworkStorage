package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Данный класс не предназначен ни для чего иного, кроме как создания соединения (сервер-сокета). Он ничего не знает про
 * чат, сокет.
 * */

public class ServerSocketThread extends Thread {

    private final int port;
    private final int timeout;

    private final ServerSocketThreadListener listener;

    public ServerSocketThread(ServerSocketThreadListener listener, String name, int port, int timeout) {
        super(name);
        this.port = port;
        this.timeout = timeout;
        this.listener = listener;
        start();
    }

    @Override
    public void run() {
        listener.onServerStart(this);
        try (ServerSocket server = new ServerSocket(port)) { // объект создаётся один раз
            server.setSoTimeout(timeout);
            listener.onServerSocketCreated(this, server);
            while (!isInterrupted()) { /* пока не прервали (не нажали кнопку stop в gui) "слушает" заданный порт */
                Socket client;
                try { /* этот блок внутри цикла необходим для проверки флага interrupted. Логика такова: метод accept
                 всегда вызывает подвисание потока на какое-то время, пока клиент коннектится. Но в это время его могли
                 бы прервать. Благодаря заданному тайм-ауту вываливается исключение, и происходит возврат к проверке
                 условия продолжения цикла */
                    client = server.accept();
                } catch (SocketTimeoutException e) {
                    listener.onServerTimeout(this, server);
                    continue;
                }
                listener.onSocketAccepted(this, server, client); /* если всё хорошо (не было прерываний), данный
                метод создаёт свой отдельный поток для серверной половинки сокета */
            }
        } catch (IOException e) {
            listener.onServerException(this, e);
        } finally {
            listener.onServerStop(this);
        }
    }
}
