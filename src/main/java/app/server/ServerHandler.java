package app.server;

import app.Repository.Repository;
import app.service.Service;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


/**
 * Created by Alikin E.A. on 18.05.18.
 */
public class ServerHandler  extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static DefaultFullHttpResponse OK_EMPTY_R;
    public static DefaultFullHttpResponse OK_EMPTY_GR_R;
    public static DefaultFullHttpResponse NOT_FOUND_R;
    public static DefaultFullHttpResponse BAD_REQUEST_R;
    public static DefaultFullHttpResponse ACCEPTED_R;
    public static DefaultFullHttpResponse CREATED_R;
    public static DefaultFullHttpResponse INTERNAL_ERROR_R;

    private static final byte[] EMPTY = "{}".getBytes();
    private static final byte[] EMPTY_ACCOUNTS = "{\"accounts\":[]}".getBytes();
    private static final byte[] EMPTY_GROUPS = "{\"groups\":[]}".getBytes();


    static  {
        OK_EMPTY_R = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK,copiedBuffer(EMPTY_ACCOUNTS));
        OK_EMPTY_R.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        OK_EMPTY_R.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, OK_EMPTY_R.content().readableBytes());
        OK_EMPTY_R.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");

        OK_EMPTY_GR_R = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK,copiedBuffer(EMPTY_GROUPS));
        OK_EMPTY_GR_R.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        OK_EMPTY_GR_R.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, OK_EMPTY_GR_R.content().readableBytes());
        OK_EMPTY_GR_R.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");

        BAD_REQUEST_R = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
        BAD_REQUEST_R.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        BAD_REQUEST_R.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, BAD_REQUEST_R.content().readableBytes());
        BAD_REQUEST_R.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");

        NOT_FOUND_R = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.NOT_FOUND,copiedBuffer(EMPTY));
        NOT_FOUND_R.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        NOT_FOUND_R.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, NOT_FOUND_R.content().readableBytes());
        NOT_FOUND_R.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");

        ACCEPTED_R = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.ACCEPTED,copiedBuffer(EMPTY));
        ACCEPTED_R.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        ACCEPTED_R.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, ACCEPTED_R.content().readableBytes());
        ACCEPTED_R.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");

        CREATED_R = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.CREATED,copiedBuffer(EMPTY));
        CREATED_R.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        CREATED_R.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, CREATED_R.content().readableBytes());
        CREATED_R.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");

        INTERNAL_ERROR_R = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR,copiedBuffer(EMPTY));
        INTERNAL_ERROR_R.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        INTERNAL_ERROR_R.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, INTERNAL_ERROR_R.content().readableBytes());
        INTERNAL_ERROR_R.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");

    }

    public static DefaultFullHttpResponse createOK(byte[] bytes) {
        ByteBuf bytesC = copiedBuffer(bytes);
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK,bytesC);
        response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, bytesC.readableBytes());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");
        return response;
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        try {
            FullHttpResponse response = Service.handle(request);
            response = response.duplicate().retain();
            ctx.writeAndFlush(response);
            Repository.resortIndexForStage();
        } catch (Exception e) {
            e.printStackTrace();
            ctx.writeAndFlush(INTERNAL_ERROR_R);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
        ctx.writeAndFlush(response);
    }

}
