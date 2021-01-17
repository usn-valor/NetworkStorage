package server;

import common.common_handlers.InputHandler;
import common.common_handlers.OutputHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import server_handlers.AuthorizationHandler;
import server_handlers.SQLHandler;
import server_handlers.ServerAnswersHandler;
import server_handlers.ServerHandler;

public class ServerConnectUtil {

    private static final int PORT_NUMBER = 8189;
    private ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline()
                            .addLast(new ServerAnswersHandler())
                            .addLast(new AuthorizationHandler(channels))
                            .addLast(new SQLHandler())
                            .addLast(new OutputHandler())
                            .addLast(new InputHandler())
                            .addLast(new ServerHandler());
                        }
                    });
            ChannelFuture f = b.bind(PORT_NUMBER).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new ServerConnectUtil().run();
    }
}