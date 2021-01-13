package server_handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import server.SqlClient;

public class SQLHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String user = (String) msg;
        System.out.println(user);
        ctx.writeAndFlush(getNicknameFromDB(user));
    }

    private String getNicknameFromDB(String client) {
        String[] logPass = client.split(",");
        SqlClient.connect();
        String nick = SqlClient.getNickname(logPass[0], logPass[1]);
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