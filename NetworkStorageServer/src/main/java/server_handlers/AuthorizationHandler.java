package server_handlers;

import common.State;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import server.SqlClient;

public class AuthorizationHandler extends ChannelInboundHandlerAdapter {

    private static final String NON_AUTH = "non&auth";
    private static final byte MANAGER_BYTE = 2;

    private ChannelGroup channels;
    private boolean auth;
    private ByteBuf buf;
    private int bufLength = 12;

    public AuthorizationHandler(ChannelGroup channels) {
        this.channels = channels;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channels.add(ctx.channel());
        System.out.println("Client connected...");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channels.remove(ctx.channel());
        System.out.println("Client disconnected...");
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

        if (buf.readableBytes() >= bufLength) {
            if (auth) {
                System.out.println("Идем дальше под ником");
                ctx.fireChannelRead(buf);
                buf = ctx.alloc().buffer(32);
            }
            else {
                byte b = buf.readByte();
                int i = buf.readInt();
                byte[] user = new byte[i];
                buf.readBytes(user);
                String loginAndPassword = new String(user);

                if (loginAndPassword.equals(NON_AUTH) && b != MANAGER_BYTE) {
                    buf = ctx.alloc().buffer(32);
                    System.out.println("Ты не авторизован");
                    ctx.writeAndFlush("Ты не авторизован");
                }
                else {
                    System.out.println("Идет авторизация");
                    ctx.writeAndFlush(getNicknameFromDB(loginAndPassword));
                    buf = ctx.alloc().buffer(32);
                    auth = true;
                    bufLength = 3;
                }
            }
        }
    }

    private String getNicknameFromDB(String client) {
        String[] logPass = client.split(",");
        SqlClient.connect();
        String nick = SqlClient.getNickname(logPass[0], logPass[1]);
        SqlClient.disconnect();
        return nick;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
