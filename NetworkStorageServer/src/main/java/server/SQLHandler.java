package server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;

public class SQLHandler extends ChannelInboundHandlerAdapter {

    private static final byte MANAGER_BYTE = 2;
    private ChannelGroup channels;
    private boolean isAdded;

    public SQLHandler(ChannelGroup channels) {
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
        byte commandByte = buf.getByte(0);

        if (commandByte == MANAGER_BYTE) {
            buf.readByte();
            int i = buf.readInt();
            byte[] user = new byte[i];
            buf.readBytes(user);
            String loginAndPassword = new String(user);
            String[] logPass = loginAndPassword.split(",");
            String nickname = getNicknameFromDB(logPass);
        }
        else {
            System.out.println(commandByte);
            ctx.fireChannelRead(buf);
        }
    }

    private String getNicknameFromDB(String[] client) {
        SqlClient.connect();
        String nick = SqlClient.getNickname(client[0], client[1]);
        SqlClient.disconnect();
        return nick;
    }

    private void changeNickNameToDB() {

    }

    private void createNewUser(String[] client) {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
