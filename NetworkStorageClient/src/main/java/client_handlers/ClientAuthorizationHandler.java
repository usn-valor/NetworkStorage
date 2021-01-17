package client_handlers;

import client.ClientEventListener;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientAuthorizationHandler extends ChannelInboundHandlerAdapter {

    private ClientEventListener clientEventListener;

    public ClientAuthorizationHandler(ClientEventListener clientEventListener) {
        this.clientEventListener = clientEventListener;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        int i = buf.readInt();
        byte[] user = new byte[i];
        buf.readBytes(user);
        String nickname = new String(user);
        clientEventListener.setNameOfAuthorizedUser(nickname);
        System.out.println("Ты авторизован под ником " + nickname);
    }
}
