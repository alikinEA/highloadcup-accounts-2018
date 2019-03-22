package app.service;

import app.Repository.Repository;
import app.models.Account;
import app.models.Constants;
import app.server.ServerHandler;
import app.utils.Utils;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Created by Alikin E.A. on 2019-03-17.
 */
public class SuggestService {

    public static DefaultFullHttpResponse handleSuggest(FullHttpRequest req) {
        LocalPoolService.lock.readLock().lock();
        try {
            String replAcc = req.uri().substring(10);
            String idStr = replAcc.substring(0, replAcc.indexOf("/"));

            int id = Integer.parseInt(idStr);
            if (id > Repository.MAX_ID) {
                return ServerHandler.NOT_FOUND_R;
            }
            Account accountData = Repository.ids[id];
            if (accountData == null) {
                return ServerHandler.NOT_FOUND_R;
            } else {
                String[] params = Utils.tokenize(req.uri().substring(req.uri().indexOf(Constants.URI_SUGGEST) + 10), '&');
                for (String param : params) {
                    if (param.startsWith(Constants.LIMIT)) {
                        try {
                            Integer limit = Integer.parseInt(Utils.getValue(param));
                            if (limit <= 0) {
                                return ServerHandler.BAD_REQUEST_R;
                            }
                        } catch (Exception e) {
                            return ServerHandler.BAD_REQUEST_R;
                        }
                    }
                    if (param.startsWith(Constants.COUNTRY)) {
                        if (Utils.getValue(param).isEmpty()) {
                            return ServerHandler.BAD_REQUEST_R;
                        }
                    }
                    if (param.startsWith(Constants.CITY)) {
                        if (Utils.getValue(param).isEmpty()) {
                            return ServerHandler.BAD_REQUEST_R;
                        }
                    }
                }

                return ServerHandler.OK_EMPTY_R;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ServerHandler.BAD_REQUEST_R;
        } finally {
            LocalPoolService.lock.readLock().unlock();
        }
    }
}
