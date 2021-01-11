package client;

import common.FileHandler;
import common.ManagerByte;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class UserTools implements ChannelFutureListener {

    private final ClientConnectUtil network;

    private boolean isAuthorized = true;
    private CountDownLatch networkStarter;

    private ByteBuf buf;
    private Channel channel;
    private byte managerByte;

    public UserTools() {
        this.network = new ClientConnectUtil();
        networkStarter = new CountDownLatch(1);
    }

    public void createNewUser(String newUser, byte b) {
        managerByte = b;
        System.out.println("Управляющий байт = " + b);
    }

    public void authorize(String auth, byte b) {
        managerByte = b;
        connectToServer();
        writeManagerByte();

        byte[] user = auth.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(user.length);
        channel.writeAndFlush(buf);
        buf = ByteBufAllocator.DEFAULT.directBuffer(user.length);
        buf.writeBytes(user);
        channel.writeAndFlush(buf);
        System.out.println("Управляющий байт = " + b);
    }

    public void downloadFile(String downloadedFilename, byte b) {
        managerByte = b;
        connectToServer();
        writeManagerByte();
    }

    public void uploadFile(String uploadedFilename, byte b) {
        managerByte = b;
        connectToServer();
        Path path = null;
        path = Paths.get("NetworkStorageClient/src/main/resources/text.txt");

        FileRegion region = null;
        try {
            region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        writeManagerByte();

        byte[] filenameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(filenameBytes.length);
        channel.writeAndFlush(buf);
        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.writeAndFlush(buf);

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

    private void writeManagerByte() {
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte(managerByte);
        channel.writeAndFlush(buf);
    }

    public void synchronize() {

    }

    public List<Path> getFilesAndDirectoriesList() {
        return new ArrayList<>();
    }

    public void shareFilesAndDirectoriesList() {

    }

    public void authorize() {
        System.out.println("Необходимо авторизоваться");
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
            network.stop();
        }
        if (future.isSuccess()) {
            System.out.println("Файл успешно передан");
            network.stop();
        }
    }
}
