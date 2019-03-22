package app.service;

import app.Repository.Repository;
import app.models.Account;
import app.models.AccountC;
import app.models.Constants;
import app.server.ServerHandler;
import app.utils.Utils;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.Iterator;
import java.util.TreeSet;

import static app.Repository.Repository.currentTimeStamp2;

/**
 * Created by Alikin E.A. on 2019-03-17.
 */
public class RecomendedService {
    public static DefaultFullHttpResponse handleRecomended(FullHttpRequest req) {
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
                String[] params = Utils.tokenize(req.uri().substring(req.uri().indexOf(Constants.URI_RECOMENDED) + 12), '&');
                int limit;
                String country;
                String city;
                for (String param : params) {
                    if (param.startsWith(Constants.LIMIT)) {
                        try {
                            limit = Integer.parseInt(Utils.getValue(param));
                            if (limit <= 0) {
                                return ServerHandler.BAD_REQUEST_R;
                            }
                        } catch (Exception e) {
                            return ServerHandler.BAD_REQUEST_R;
                        }
                    }
                    if (param.startsWith(Constants.COUNTRY)) {
                        country = Utils.getValue(param);
                        if (country.isEmpty()) {
                            return ServerHandler.BAD_REQUEST_R;
                        }
                    }
                    if (param.startsWith(Constants.CITY)) {
                        city = Utils.getValue(param);
                        if (city.isEmpty()) {
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

    private static void calcCompat(Account accountData, String country, String city, TreeSet<AccountC> compat, Iterator<Account> iterator) {
        while (iterator.hasNext()) {
            Account account1 = iterator.next();
            if (account1.getId() != accountData.getId()) {
                if (city.isEmpty() || city.equals(account1.getCity())) {
                    if (country.isEmpty() || country.equals(account1.getCountry())) {
                        if (!accountData.getSex().equals(account1.getSex())) {
                            int c = getCompatibility(accountData, account1);
                            if (c > 0) {
                                AccountC accountC = new AccountC();
                                accountC.setAccount(account1);
                                accountC.setC(c);
                                while (!compat.add(accountC)) {
                                    c = c + 1;
                                    accountC.setC(c);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static int getCompatibility(Account accountData, Account account1) {
        int compt = 0;
        if (account1.getStatus().equals(Constants.STATUS1)) {
            compt = compt + 50_000_0;
        } else if (account1.getStatus().equals(Constants.STATUS2)){
            compt = compt + 20_000_0;
        } else {
            compt = compt + 1_000_0;
        }
        boolean notComp = true;
        if (accountData.getInterests() != null) {
            for (String interest : accountData.getInterests()) {
                if (account1.getInterests() != null) {
                    if (Utils.contains(account1.getInterests(),interest)) {
                        notComp = false;
                        compt = compt + 1_000_0;
                    }
                } else {
                    return 0;
                }
            }
        }
        if (notComp) {
            return 0;
        }
        if (account1.getStart() != 0) {
            if (currentTimeStamp2 < account1.getFinish()
                    && currentTimeStamp2 > account1.getStart()) {
                compt = compt + 60_000_0;
            }
        }
        int daysAcc1 = (int) (((currentTimeStamp2 - account1.getBirth())) / (60*60*24));
        int daysAccData = (int) (((currentTimeStamp2 - accountData.getBirth())) / (60*60*24));

        int days = 36500 - Math.abs(daysAccData - daysAcc1);
        compt = compt + days;
        return compt * 100;
    }
}
