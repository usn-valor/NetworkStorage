package common.common_handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OutputHandler extends ChannelOutboundHandlerAdapter {

    private static final byte MANAGER_BYTE = 4;
    private static final byte OUTPUT_BYTE = 3;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ByteBuf buffer = ((ByteBuf) msg);
        byte commandByte = buffer.readByte();

        if (commandByte == MANAGER_BYTE) {
            Path path = null;
            path = Paths.get("/Users/Arsen/IdeaProjects/NetworkStorage/NetworkStorageServer/src/main/resources/UserFiles/_text.txt");

            FileRegion region = null;
            try {
                region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteBuf buf;
            buf = ByteBufAllocator.DEFAULT.directBuffer(1);
            buf.writeByte(OUTPUT_BYTE);
            ctx.writeAndFlush(buf);

            byte[] filenameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
            buf = ByteBufAllocator.DEFAULT.directBuffer(4);
            buf.writeInt(filenameBytes.length);
            ctx.writeAndFlush(buf);
            buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
            buf.writeBytes(filenameBytes);
            ctx.writeAndFlush(buf);

            buf = ByteBufAllocator.DEFAULT.directBuffer(8);
            try {
                buf.writeLong(Files.size(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ctx.writeAndFlush(buf);

            ctx.writeAndFlush(region); // собственно, сама передача файла
        }
    }
}
