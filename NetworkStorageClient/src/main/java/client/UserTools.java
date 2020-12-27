package client;

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

    private boolean isAuthorized = true;
    private final NetworkUtil network;
    private BufferedReader br;
    private CountDownLatch networkStarter;

    public UserTools() {
        this.network = new NetworkUtil();
        networkStarter = new CountDownLatch(1);
        br = new BufferedReader(new InputStreamReader(System.in));
    }

    public void commandListener(String s) {
        if (isAuthorized) {
            switch (s) {
                case "download":
                    downloadFile(this);
                    break;
                case "upload":
                    uploadFile(this);
                    break;
            }
        }
        else
            authorize();
    }

    private void downloadFile(ChannelFutureListener finishListener) {
        System.out.println("Enter the name of the downloaded file");

    }

    public void uploadFile(ChannelFutureListener finishListener) {
        //System.out.println("Enter the name of the uploaded file");
        connectToServer();
        Path path = null;
        path = Paths.get("NetworkStorageClient/src/main/resources/text.txt");

        Channel channel = network.getCurrentChannel();

        FileRegion region = null;
        try {
            region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteBuf buf;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 25);
        channel.writeAndFlush(buf);

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
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
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
