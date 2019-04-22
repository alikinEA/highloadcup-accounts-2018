package app.service;

import app.Repository.Repository;
import app.models.Account;
import app.models.AccountC;
import app.models.Constants;
import app.server.ServerHandler;
import app.utils.Utils;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

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
                int limit = 0;
                String country = null;
                String city = null;
                String queryId = null;

                String paramUrl = req.uri().substring(req.uri().indexOf(Constants.URI_RECOMENDED) + 12);
                int i = 0;
                int j = 0;
                do {
                    j = paramUrl.indexOf('&', i);
                    String param;
                    if (j != -1) {
                        param = paramUrl.substring(i, j);
                    } else {
                        param = paramUrl.substring(i);
                    }
                    i = j + 1;

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
                    if (param.charAt(0) == 'q' && param.charAt(1) == 'u') {
                        queryId = Utils.getValue(param).intern();
                        DefaultFullHttpResponse cachedQuery = Repository.queryCacheRec.get(queryId);
                        if (cachedQuery != null) {
                            return cachedQuery;
                        }
                    }
                }  while (j >= 0);

                Account[] data = Repository.premium_1_m;
                if (accountData.getSex() == Constants.M) {
                    data = Repository.premium_1_f;
                }

                TreeSet<AccountC> result = LocalPoolService.recommendedResult.get();
                calcRec(accountData, country, city, data, result);
                if (result.size() < limit) {
                    if (country != null) {
                        if (accountData.getSex() == Constants.M) {
                            data = Repository.country_by_name_status_1_not_premium.get(country + Constants.F);
                        } else {
                            data = Repository.country_by_name_status_1_not_premium.get(country + Constants.M);
                        }
                        calcRec(accountData, country, city, data, result);
                    } else {
                        if (accountData.getSex() == Constants.M) {
                            data = Repository.status_1_f_not_premium;
                        } else {
                            data = Repository.status_1_m_not_premium;
                        }
                        calcRec(accountData, country, city, data, result);
                    }
                }
                if (result.size() == 0) {
                    return ServerHandler.OK_EMPTY_R;
                }
                byte[] body = Utils.accountCToString(result,limit);
                DefaultFullHttpResponse cacheQuery = ServerHandler.createOK(body);
                Repository.queryCacheRec.put(queryId, cacheQuery);
                return cacheQuery;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ServerHandler.BAD_REQUEST_R;
        } finally {
            LocalPoolService.lock.readLock().unlock();
        }
    }

    private static void calcRec(Account accountData, String country, String city, Account[] data, TreeSet<AccountC> result) {
        for (Account account1 : data) {
            if (account1 == null) {
                break;
            }
            if (account1.getId() != accountData.getId()) {
                if (city == null || city.equals(account1.getCity())) {
                    if (country == null || country.equals(account1.getCountry())) {
                        if (!accountData.getSex().equals(account1.getSex())) {
                            int c = getCompatibility(accountData, account1);
                            if (c > 0) {
                                AccountC accountC = new AccountC();
                                accountC.setAccount(account1);
                                accountC.setC(c);
                                result.add(accountC);
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
