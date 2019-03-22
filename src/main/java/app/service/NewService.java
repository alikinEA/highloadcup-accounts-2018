package app.service;

import app.Repository.Repository;
import app.models.Account;
import app.models.Constants;
import app.server.ServerHandler;
import app.utils.Utils;
import com.jsoniter.JsonIterator;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

import java.nio.charset.StandardCharsets;

/**
 * Created by Alikin E.A. on 2019-03-17.
 */
public class NewService {

    public static DefaultFullHttpResponse handleNew(FullHttpRequest req) {
        Account account = Utils.anyToAccount(JsonIterator.deserialize(req.content().toString(StandardCharsets.UTF_8)));
        if (account == null || account.getId() == -1) {
            return ServerHandler.BAD_REQUEST_R;
        }

        if (account.getSex() != null) {
            if (!account.getSex().equals(Constants.F)
                    && !account.getSex().equals(Constants.M)) {
                return ServerHandler.BAD_REQUEST_R;
            }
        } else {
            return ServerHandler.BAD_REQUEST_R;
        }
        if (account.getStatus() != null) {
            if (!account.getStatus().equals(Constants.STATUS1)
                    && !account.getStatus().equals(Constants.STATUS2)
                    && !account.getStatus().equals(Constants.STATUS3)) {
                return ServerHandler.BAD_REQUEST_R;
            }
        } else {
            return ServerHandler.BAD_REQUEST_R;
        }
        if (account.getEmail() != null) {
            if (!account.getEmail().contains("@")) {
                return ServerHandler.BAD_REQUEST_R;
            }

            LocalPoolService.lock.readLock().lock();
            try {
                if (Repository.ids[account.getId()] != null) {
                    return ServerHandler.BAD_REQUEST_R;
                }
                if (Repository.emails.contains(account.getEmail())) {
                    return ServerHandler.BAD_REQUEST_R;
                }
            } finally {
                LocalPoolService.lock.readLock().unlock();
            }

            LocalPoolService.lock.writeLock().lock();
            try {
                Repository.list[Repository.index.incrementAndGet()] = account;
                Repository.ids[account.getId()] = account;
                Repository.emails.add(account.getEmail());
                Repository.insertToIndex(account);
                return ServerHandler.CREATED_R;
            } finally {
                LocalPoolService.lock.writeLock().unlock();
            }
        }
        return ServerHandler.CREATED_R;
    }
}
