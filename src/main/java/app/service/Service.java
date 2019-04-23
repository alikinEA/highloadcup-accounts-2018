package app.service;

import app.models.Constants;
import app.server.ServerHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Alikin E.A. on 15.12.18.
 */
public class Service {

    public static DefaultFullHttpResponse handle(FullHttpRequest req) {
        String uri = req.uri();
        if (uri.charAt(10) == 'f' && uri.charAt(11) == 'i' && uri.charAt(12) == 'l') {
            if (uri.charAt(17) == '?') {
                return FilterService.handleFilterv2(uri);
            }
            return ServerHandler.NOT_FOUND_R;
        } else if (uri.charAt(10) == 'n' && uri.charAt(11) == 'e') {
            if (uri.charAt(14) == '?') {
                return NewService.handleNew(req);
            }
            return ServerHandler.NOT_FOUND_R;
        } else if (uri.charAt(10) == 'l' && uri.charAt(11) == 'i') {
            if (uri.charAt(16) == '?') {
                return LikesService.handleLikes(req);
            }
            return ServerHandler.NOT_FOUND_R;
        } else if (uri.charAt(10) == 'g' && uri.charAt(11) == 'r') {
            if (uri.charAt(16) == '?') {
                return GroupService.handleGroup(req);
            }
            return ServerHandler.NOT_FOUND_R;
        } else if (uri.contains(Constants.URI_SUGGEST)) {
            int index = uri.indexOf(Constants.URI_SUGGEST) + 9;
            if (uri.charAt(index) == '?' && Character.isDigit(uri.charAt(10))) {
                return SuggestService.handleSuggest(req);
            }
            return ServerHandler.NOT_FOUND_R;
        } else if (uri.contains(Constants.URI_RECOMENDED)) {
            if (Character.isDigit(uri.charAt(10))) {
                return RecomendedService.handleRecomended(req);
            }
            return ServerHandler.NOT_FOUND_R;
        } else {
            if (Character.isDigit(uri.charAt(10))) {
                return UpdateService.handleUpdate(req);
            }
            return ServerHandler.NOT_FOUND_R;
        }
    }

}
