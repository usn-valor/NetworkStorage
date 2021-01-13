package client_handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private static final String SOURCE = "/Users/Arsen/IdeaProjects/NetworkStorage/";
    private static final String DEST = "/Users/Arsen/IdeaProjects/NetworkStorage/NetworkStorageClient/src/main/resources/SharedFiles/";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        File file = (File) msg;
        Path sourcePath = Paths.get(SOURCE + file.getName());
        Path destPath = Paths.get(DEST + file.getName());
        Files.move(sourcePath, destPath);
    }
}
