package server_handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;

public class AuthorizationHandler extends ChannelInboundHandlerAdapter {

    private static final String NON_AUTH = "non&auth";
    private static final byte MANAGER_BYTE = 2;

    private ChannelGroup channels;
    private boolean isAdded;

    public AuthorizationHandler(ChannelGroup channels) {
        this.channels = channels;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        isAdded = channels.add(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("You die, if you try...");
        ByteBuf buf = ((ByteBuf) msg);
        byte b = buf.readByte();
        int i = buf.readInt();
        byte[] user = new byte[i];
        buf.readBytes(user);
        String loginAndPassword = new String(user);
        if (loginAndPassword.equals(NON_AUTH) && b != MANAGER_BYTE)
            ctx.writeAndFlush("Ты не авторизован");
        else if (b == MANAGER_BYTE)
            ctx.fireChannelRead(loginAndPassword);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
