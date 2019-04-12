package app.service;

import app.Repository.Repository;
import app.models.Account;
import app.models.AccountC;
import app.models.AccountRec;
import app.models.Constants;
import app.server.ServerHandler;
import app.utils.Utils;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

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
                if (accountData.getLikes() == null) {
                    return ServerHandler.OK_EMPTY_R;
                }
                int limit = 0;
                String country = null;
                String city = null;
                String queryId = null;

                String[] params = Utils.tokenize(req.uri().substring(req.uri().indexOf(Constants.URI_SUGGEST) + 10), '&');
                for (String param : params) {
                    if (param.charAt(0) == 'l' && param.charAt(1) == 'i') {
                        try {
                            limit = Integer.parseInt(Utils.getValue(param));
                            if (limit <= 0) {
                                return ServerHandler.BAD_REQUEST_R;
                            }
                        } catch (Exception e) {
                            return ServerHandler.BAD_REQUEST_R;
                        }
                    }
                    if (param.charAt(0) == 'c' && param.charAt(1) == 'o') {
                        country = Utils.getValue(param).intern();
                        if (country.isEmpty()) {
                            return ServerHandler.BAD_REQUEST_R;
                        }
                    }
                    if (param.charAt(0) == 'c' && param.charAt(1) == 'i') {
                        city = Utils.getValue(param).intern();
                        if (city.isEmpty()) {
                            return ServerHandler.BAD_REQUEST_R;
                        }
                    }
                    /*if (param.charAt(0) == 'q' && param.charAt(1) == 'u') {
                        queryId = Utils.getValue(param).intern();
                        if (Repository.queryCount.get() > 117_000) {
                            byte[] cachedQuery = Repository.queryCacheSug.get(queryId);
                            if (cachedQuery != null) {
                                return ServerHandler.createOK(cachedQuery);
                            }
                        }
                    }*/
                }

                /*TreeSet<AccountRec> result = LocalPoolService.suggestResult.get();
                for (int likeData : accountData.getLikes()) {
                    Account[] whoLikes = Repository.likeInvert.get(likeData);
                    for (Account account : whoLikes) {
                        if (account.getLikes() != null
                                && account.getSex().equals(accountData.getSex())
                                && account.getId() != accountData.getId()
                                && (city == null || city.equals(account.getCity()))
                                && (country == null || country.equals(account.getCountry()))) {
                            double s = getSimilarity(accountData, account);
                            AccountRec accountRec = new AccountRec();
                            accountRec.setAccount(account);
                            accountRec.setS(s);
                            result.add(accountRec);
                        }
                    }
                }

                if (result.size() == 0) {
                    return ServerHandler.OK_EMPTY_R;
                }
                Set<Account> resultAcc = LocalPoolService.threadLocalAccounts.get();
                int count = 0;
                for (AccountRec accountRec : result) {
                    int[] likesRec = accountRec.getAccount().getLikes();
                    for (int i = likesRec.length - 1; i >= 0; i--) {
                        if (count == limit) {
                            break;
                        }
                        int likeValue = likesRec[i];
                        for (int likeData : accountData.getLikes()) {
                            if (likeValue != likeData) {
                                count++;
                                resultAcc.add(Repository.ids[likeValue]);
                                break;
                            }
                        }
                    }
                }

                byte[] body = Utils.accountRecToString(resultAcc);
                if (Repository.queryCount.get() > 117_000) {
                    Repository.queryCacheSug.put(queryId, body);
                }
                return ServerHandler.createOK(body);*/
                return ServerHandler.OK_EMPTY_R;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ServerHandler.BAD_REQUEST_R;
        } finally {
            LocalPoolService.lock.readLock().unlock();
        }
    }

    /*private static double getSimilarity(Account accountData, Account account) {
        double sim = 0;
        for (int i = 0; i < accountData.getLikes().length; i++) {
            int likeData = accountData.getLikes()[i];
            int likeDataTs = accountData.getLikesTs()[i];
            for (int j = 0; j < account.getLikes().length; j++) {
                if (likeData == account.getLikes()[j]) {
                    int value =  likeDataTs - account.getLikesTs()[j];
                    if (value == 0) {
                        sim = sim + 1;
                    } else {
                        sim = sim + 1d / Math.abs(value);
                    }
                    break;
                }
            }
        }
        return sim;
    }*/
}
