package core;

import network.ServerSocketThread;
import network.ServerSocketThreadListener;
import network.SocketThread;
import network.SocketThreadListener;
import common.Common;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListener, SocketThreadListener {

    private ServerSocketThread server;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss: ");
    private Vector<SocketThread> clients = new Vector<>();
    private ChatServerListener listener; /* этим листнером иожет стать любой графический интерфейс к серверной части задел
    на будущее (можно было бы просто передать сюда ServerGUI) */

    public ChatServer(ChatServerListener listener) {
        this.listener = listener;
    }

    public void start(int port) {
        if (server != null && server.isAlive()) {
            putLog("Server already stared");
        } else {
            server = new ServerSocketThread(this, "Chat server", port, 2000);
        }
    }

    public void stop() {
        if (server == null || !server.isAlive()) {
            putLog("Server is not running");
        } else {
            server.interrupt();
        }
    }

    private void putLog(String msg) {
        msg = DATE_FORMAT.format(System.currentTimeMillis()) + Thread.currentThread().getName() + ": " + msg;
        listener.onChatServerMessage(msg);
    }

    /**
     * Server Socket Thread Listener methods
     * */
    @Override
    public void onServerStart(ServerSocketThread thread) {
        putLog("Server started");
        SqlClient.connect();
    }

    @Override
    public void onServerStop(ServerSocketThread thread) {
        putLog("Server stopped");
        SqlClient.disconnect();
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).close();
        }
    }

    @Override
    public void onServerSocketCreated(ServerSocketThread thread, ServerSocket server) {
        putLog("Listening to port");
    }

    @Override
    public void onServerTimeout(ServerSocketThread thread, ServerSocket server) {
//        putLog("Ping? Pong!");
    }

    @Override
    public void onSocketAccepted(ServerSocketThread thread, ServerSocket server, Socket socket) {
        // client connected
        String name = "Client " + socket.getInetAddress() + ":" + socket.getPort();
        new ClientThread(this, name, socket);
    }

    @Override
    public void onServerException(ServerSocketThread thread, Throwable exception) {
        exception.printStackTrace();
    }

    /**
     * Socket Thread Listener methods
     * */
    @Override
    public void onSocketStart(SocketThread thread, Socket socket) {
        putLog("Client thread started");
    }

    @Override
    public void onSocketStop(SocketThread thread) {
        ClientThread client = (ClientThread) thread;
        clients.remove(thread);
        if (client.isAuthorized() && !client.isReconnecting()) {
            sendToAllAuthorizedClients(Common.getTypeBroadcast("Server", client.getNickname() + " disconnected"));
        }
        sendToAllAuthorizedClients(Common.getUserList(getUsers()));
    }

    @Override
    public void onSocketReady(SocketThread thread, Socket socket) {
        putLog("Client is ready to chat");
        clients.add(thread);
    }

    @Override
    public void onReceiveFile(SocketThread thread, Socket socket, String msg) {
        ClientThread client = (ClientThread) thread;
        if (client.isAuthorized()) {
            handleAuthMessage(client, msg);
        } else {
            handleNonAuthMessage(client, msg);
        }
    }

    private void handleNonAuthMessage(ClientThread client, String msg) {
        String[] arr = msg.split(Common.DELIMITER);
        if (arr.length != 3 || !arr[0].equals(Common.AUTH_REQUEST)) {
            client.msgFormatError(msg);
            return;
        }
        String login = arr[1];
        String password = arr[2];
        String nickname = SqlClient.getNickname(login, password);
        if (nickname == null) {
            putLog("Invalid login attempt: " + login);
            client.authFail();
            return;
        }
        else {
            ClientThread oldClient = findClientByNickname(nickname);
            client.authAccept(nickname);
            if (oldClient == null) {
                sendToAllAuthorizedClients(Common.getTypeBroadcast("Server", nickname + " connected\n" +
                        "You can change your nickname by typing change_nickname old nickname"));
                sendPrivateMessage(Common.getTypeClientPrivate("Server", nickname, "Your Nickname " + nickname), nickname);
            } else {
                oldClient.reconnect();
                clients.remove(oldClient);
            }
        }
        sendToAllAuthorizedClients(Common.getUserList(getUsers()));
    }

    private void handleAuthMessage(ClientThread client, String msg) {
        String[] arr = msg.split(Common.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Common.TYPE_BCAST_CLIENT:
                sendToAllAuthorizedClients(Common.getTypeBroadcast(client.getNickname(), arr[1]));
                break;
            case Common.TYPE_CLIENT_PRIVATE:
                sendPrivateMessage(Common.getTypeClientPrivate(client.getNickname(), arr[2], arr[3]), arr[2]);
                break;
            case Common.TYPE_CLIENT_CHANGE_NICKNAME:
                changeNickname(arr[1], arr[2], arr[3]);
                break;
            default:
                client.msgFormatError(msg);
        }
    }

    private void changeNickname(String login, String oldName, String newName) {
        boolean b = SqlClient.setNewNickName(login, newName);
        if (b) {
            sendToAllAuthorizedClients(Common.getTypeFromServerChangeNickname(oldName, newName));
            updateUserList(oldName, newName);
            sendToAllAuthorizedClients(Common.getUserList(getUsers()));
        }
    }

    private void updateUserList(String oldName, String newName) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (client.getNickname().equals(oldName)) {
                client.setNickname(newName);
                break;
            }
        }
    }

    private void sendPrivateMessage(String msg, String rec) {
        ClientThread client = findClientByNickname(rec);
        if (client != null)
            client.sendFile(msg);
    }

    private void sendToAllAuthorizedClients(String msg) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized())
                continue;
            client.sendFile(msg);
        }
    }

    @Override
    public void onSocketException(SocketThread thread, Exception exception) {
        System.out.println("Connection lost");
        /*for (StackTraceElement x: exception.getStackTrace())
            System.out.println(x);*/
    }

    private String getUsers() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized())
                continue;
            sb.append(client.getNickname()).append(Common.DELIMITER);
        }
        return sb.toString();
    }

    private synchronized ClientThread findClientByNickname(String nickname) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized())
                continue;
            if (client.getNickname().equals(nickname))
                return client;
        }
        return null;
    }
}
