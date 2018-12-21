package app.server;

import app.Repository;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;

/**
 * Created by Alikin E.A. on 15.12.18.
 */
public class Server {
    static final int PORT = Integer.parseInt(System.getProperty("port", "80"));

    public Server() {

    }

    public static void  printCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used memory is bytes: " + memory);
        System.out.println("Used memory is mb: " + bytesToMegabytes(memory));

    }

    private static final long MEGABYTE = 1024L * 1024L;

    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    public static void main(String[] args) throws Exception {
        printCurrentMemoryUsage();
        Repository.initData();
        printCurrentMemoryUsage();
        new Server().run();
    }

    public void run() throws Exception {

        EpollEventLoopGroup bossGroup = new EpollEventLoopGroup(1);
        EpollEventLoopGroup workerGroup = new EpollEventLoopGroup(8);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(EpollServerSocketChannel.class)
                    //.handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 512)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000);

            ChannelFuture f = b.bind(PORT).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
