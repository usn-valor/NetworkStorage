package client_handlers;

import client.ClientEventListener;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientCommandHandler extends ChannelInboundHandlerAdapter {

    private static final String NON_AUTH = "Ты не авторизован";

    private ClientEventListener clientEventListener;
    private ByteBuf buf;

    public ClientCommandHandler(ClientEventListener clientEventListener) {
        this.clientEventListener = clientEventListener;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        buf = ctx.alloc().buffer(32);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        buf.release();
        buf = null;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = ((ByteBuf) msg);
        buf.writeBytes(byteBuf);
        byteBuf.release();

        String nickname = null;

        if (buf.readableBytes() >= 8) {
            int i = buf.readInt();
            byte[] user = new byte[i];
            buf.readBytes(user);
            nickname = new String(user);
        }

        if (nickname != null) {
            if (nickname.equals(NON_AUTH))
                System.out.println(NON_AUTH);
            else {
                clientEventListener.setNameOfAuthorizedUser(nickname);
                System.out.println("Ты авторизован под ником " + nickname);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("?????????????");
        cause.printStackTrace();
        ctx.close();
    }
}