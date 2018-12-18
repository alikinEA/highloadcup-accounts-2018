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

    public static void main(String[] args) throws Exception {
        Repository.initData();
        long heapSize = Runtime.getRuntime().totalMemory();
        //version 4
        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
        long heapMaxSize = Runtime.getRuntime().maxMemory();

        // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
        long heapFreeSize = Runtime.getRuntime().freeMemory();

        System.out.println("heapsize"+formatSize(heapSize));
        System.out.println("heapmaxsize"+formatSize(heapMaxSize));
        System.out.println("heapFreesize"+formatSize(heapFreeSize));
        new Server().run();
    }


    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
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
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3_000);

            ChannelFuture f = b.bind(PORT).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
