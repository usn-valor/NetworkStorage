package client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class UserTools implements ChannelFutureListener, ClientEventListener {

    private static final String NON_AUTH = "non&auth";

    private final ClientConnectUtil network;

    private boolean isAuthorized;
    private CountDownLatch networkStarter;

    private ByteBuf buf;
    private Channel channel;
    private byte managerByte;
    private String nameOfAuthorizedUser;

    public UserTools() {
        this.network = new ClientConnectUtil(this);
        networkStarter = new CountDownLatch(1);
        nameOfAuthorizedUser = NON_AUTH;
        connectToServer();
    }

    public void setNameOfAuthorizedUser(String nameOfAuthorizedUser) {
        this.nameOfAuthorizedUser = nameOfAuthorizedUser;
        isAuthorized = true;
    }

    public void createNewUser(String newUser, byte b) {
        managerByte = b;
        System.out.println("Управляющий байт = " + b);
    }

    public void authorize(String auth, byte b) {
        managerByte = b;
        writeManagerByteAndCommand(auth);
    }

    private boolean sendHelloToServer() {
        if (!isAuthorized) {
            writeManagerByteAndCommand(NON_AUTH);
            return true;
        }
        return false;
    }

    public void downloadFile(String downloadedFilename, byte b) {
        managerByte = b;
        writeManagerByteAndCommand(nameOfAuthorizedUser);
    }

    public void uploadFile(String uploadedFilename, byte b) {
        managerByte = b;

        if (sendHelloToServer()) {
            return;
        }

        Path path = null;
        path = Paths.get("NetworkStorageClient/src/main/resources/text.txt");

        FileRegion region = null;
        try {
            region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(nameOfAuthorizedUser);
        writeManagerByteAndCommand(nameOfAuthorizedUser);
        writeData(path.getFileName().toString());

        buf = ByteBufAllocator.DEFAULT.directBuffer(8);
        try {
            buf.writeLong(Files.size(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        channel.writeAndFlush(buf);

        ChannelFuture transferOperationFuture = channel.writeAndFlush(region); // собственно, сама передача файла
        transferOperationFuture.addListener(this);
    }

    public void delete() {

    }

    public void synchronize() {

    }

    private void writeManagerByteAndCommand(String s) {
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte(managerByte);
        channel.writeAndFlush(buf);
        writeData(s);
    }

    private void writeData(String data) {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(bytes.length);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(bytes.length);
        buf.writeBytes(bytes);
        channel.writeAndFlush(buf);
    }

    public List<Path> getFilesAndDirectoriesList() {
        return new ArrayList<>();
    }

    public void shareFilesAndDirectoriesList() {

    }

    public void createNewUser() {

    }

    private boolean connectToServer() {
        new Thread(() -> network.start(networkStarter)).start();
        try {
            networkStarter.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        channel = network.getCurrentChannel();
        return false;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
            future.cause().printStackTrace();
            System.out.println("Облом");
        }

        if (future.isSuccess()) {
            System.out.println("Файл успешно передан");
        }
    }
}
