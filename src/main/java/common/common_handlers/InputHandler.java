package common.common_handlers;

import common.FileHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class InputHandler extends ChannelInboundHandlerAdapter {

    private static final byte MANAGER_BYTE = 3;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Client connected...");
        ByteBuf buf = ((ByteBuf) msg);
        byte commandByte = buf.getByte(0);

        if (commandByte == MANAGER_BYTE)
            ctx.fireChannelRead(FileHandler.acceptFile(msg));
        else {
            System.out.println(commandByte);
            ctx.writeAndFlush(buf);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}