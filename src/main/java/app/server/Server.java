package app.server;

import app.Repository.Repository;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

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
        try {
            Process p = Runtime.getRuntime().exec("uname -r");

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = stdInput.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Repository.initData();
        printCurrentMemoryUsage();
        Thread thread = new Thread(Server::warmUp);

        thread.start();
        new Server().run();

    }

    private static void warmUp() {
        try {
            Thread.sleep(5_000);
            System.out.println("start warm up"+ new Date().getTime());
            HttpClient client = new DefaultHttpClient();
            int warpUpCount = 500;
            if (Repository.isRait) {
                warpUpCount = 5_000;
            }
            for (int i = 0; i < warpUpCount; i++) {
                HttpGet request = new HttpGet("http://localhost:" + PORT + "/accounts/filter/?country_eq=Румция&sname_starts=Кис&limit=38&sex_eq=f&query_id=1");
                HttpResponse response = client.execute(request);
                EntityUtils.consumeQuietly(response.getEntity());
                /*try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                    String line = "";
                    while ((line = rd.readLine()) != null) {
                        System.out.println(line);
                    }
                }*/
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Repository.queryCount.set(1);
        System.gc();
        printCurrentMemoryUsage();
        System.out.println("end warm up" + new Date().getTime());
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
                    //.option(ChannelOption.SO_BACKLOG, 512)
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
