package app.server;

import app.Service;
import app.models.Result;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by Alikin E.A. on 18.05.18.
 */
public class ServerHandler  extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Charset CHARSET = Charset.forName("UTF-8");

    private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
    private static final AsciiString CONTENT_LENGTH =  new AsciiString("Content-Length");
    private static final AsciiString SERVER =  new AsciiString("Server");
    private static final AsciiString SERVER_VALUE =  new AsciiString("WebSphere");
    private static final AsciiString CONTENT_TYPE_VALUE =  new AsciiString("application/json; charset=UTF-8");


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        try {
            Result result = Service.handle(request);
            ByteBuf bytesC = copiedBuffer(result.getContent());
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, result.getStatus(),bytesC);
            response.headers().setInt(CONTENT_LENGTH, bytesC.readableBytes());
            response.headers().set(CONTENT_TYPE, CONTENT_TYPE_VALUE);
            response.headers().set(SERVER, SERVER_VALUE);
            ctx.writeAndFlush(response);
        } catch (Exception e) {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(CONTENT_TYPE, CONTENT_TYPE_VALUE);
            response.headers().set(SERVER, SERVER_VALUE);
            ctx.writeAndFlush(response);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
        response.headers().setInt(CONTENT_LENGTH, copiedBuffer(cause.getMessage(),CHARSET).readableBytes());
        response.headers().set(CONTENT_TYPE, TEXT_PLAIN);
        response.headers().set(SERVER, SERVER_VALUE);
        ctx.writeAndFlush(response);
    }

}
