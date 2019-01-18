package app.server;

import app.Service;
import app.models.Result;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by Alikin E.A. on 18.05.18.
 */
public class ServerHandler  extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        try {
            boolean keepAlive = HttpUtil.isKeepAlive(request);
            Result result = Service.handle(request);
            ByteBuf bytesC = copiedBuffer(result.getContent());
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, result.getStatus(),bytesC);
            if (keepAlive) {
                response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            } else {
                response.headers().set(CONNECTION, HttpHeaderValues.CLOSE);
            }
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, bytesC.readableBytes());
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");
            ctx.writeAndFlush(response);
        } catch (Exception e) {
            e.printStackTrace();
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");
            ctx.writeAndFlush(response);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
        ctx.writeAndFlush(response);
    }

}
