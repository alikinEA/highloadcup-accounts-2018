package app.service;

import app.Repository.GroupRepository;
import app.Repository.Repository;
import app.models.Account;
import app.models.Constants;
import app.server.ServerHandler;
import app.utils.Utils;
import com.jsoniter.JsonIterator;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

import java.nio.charset.StandardCharsets;

/**
 * Created by Alikin E.A. on 2019-03-17.
 */
public class UpdateService {
    public static DefaultFullHttpResponse handleUpdate(FullHttpRequest req) {
        String curId = req.uri().substring(10, req.uri().lastIndexOf("/?"));

        Account accountData;
        Account account;
        LocalPoolService.lock.readLock().lock();
        try {
            int id = Integer.parseInt(curId);
            if (id > Repository.MAX_ID) {
                return ServerHandler.NOT_FOUND_R;
            }
            accountData = Repository.ids[id];
            if (accountData == null) {
                return ServerHandler.NOT_FOUND_R;
            }

            ByteBuf buf = req.content();
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            account = Utils.anyToAccount(JsonIterator.deserialize(bytes));
            if (account == null) {
                return ServerHandler.BAD_REQUEST_R;
            }
            if (account.getSex() != null) {
                if (!account.getSex().equals(Constants.F)
                        && !account.getSex().equals(Constants.M)) {
                    return ServerHandler.BAD_REQUEST_R;
                }
            }
            if (account.getStatus() != null) {
                if (!account.getStatus().equals(Constants.STATUS1)
                        && !account.getStatus().equals(Constants.STATUS2)
                        && !account.getStatus().equals(Constants.STATUS3)) {
                    return ServerHandler.BAD_REQUEST_R;
                }
            }
            if (account.getEmail() != null) {
                if (!account.getEmail().contains("@")) {
                    return ServerHandler.BAD_REQUEST_R;
                }
                if (Repository.emails.contains(account.getEmail())) {
                    return ServerHandler.BAD_REQUEST_R;
                }
            }
        } finally {
            LocalPoolService.lock.readLock().unlock();
        }


        LocalPoolService.lock.writeLock().lock();
        try {
            if (account.getEmail() != null) {
                Repository.emails.remove(accountData.getEmail());
                Repository.emails.add(account.getEmail());
                accountData.setEmail(account.getEmail());
                Repository.updateEmailIndex(accountData);
            }
            if (account.getSex() != null) {
                Utils.updateStrValue(account.getSex(),accountData.getSex(), GroupRepository.sex_gr_n);
                accountData.setSex(account.getSex());
                Repository.updateSexIndex(accountData);
            }
            if (account.getFname() != null) {
                accountData.setFname(account.getFname());
                Repository.updateFnameIndex(accountData);
            }
            if (account.getInterests() != null) {
                accountData.setInterests(account.getInterests());
                Repository.updateInterestIndex(accountData);
            }
            if (account.getStatus() != null) {
                Utils.updateStrValue(account.getStatus(),accountData.getStatus(), GroupRepository.status_gr_n);
                accountData.setStatus(account.getStatus());
                Repository.updateStatusIndex(accountData);
            }
            if (account.getStart() != 0) {
                accountData.setStart(account.getStart());
                accountData.setFinish(account.getFinish());
                Repository.updatePremiumIndex(accountData);
            }
            if (account.getPhone() != null) {
                accountData.setPhone(account.getPhone());
                Repository.updatePhoneIndex(accountData);
            }
            if (account.getBirth() != 0) {
                accountData.setBirth(account.getBirth());
                Repository.updateYearIndex(accountData);
            }
            if (account.getCity() != null) {
                Utils.updateStrValue(account.getCity(),accountData.getCity(), GroupRepository.city_gr_n);
                accountData.setCity(account.getCity());
                Repository.updateCityIndex(accountData);
            }
            if (account.getCountry() != null) {
                Utils.updateStrValue(account.getCountry(),accountData.getCountry(),GroupRepository.country_gr_n);
                accountData.setCountry(account.getCountry());
                Repository.updateCountryIndex(accountData);
            }
            if (account.getSname() != null) {
                accountData.setSname(account.getSname());
                Repository.updateSnameIndex(accountData);
            }
            if (account.getLikes() != null) {
                Repository.updateLikesInvertIndex(account);
            }
        } finally {
            LocalPoolService.lock.writeLock().unlock();
        }

        return ServerHandler.ACCEPTED_R;

    }
}
