package app.service;

import app.Repository.Repository;
import app.models.Account;
import app.models.Constants;
import app.server.ServerHandler;
import app.utils.Utils;
import com.jsoniter.JsonIterator;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static app.Repository.Repository.currentTimeStamp2;

/**
 * Created by Alikin E.A. on 15.12.18.
 */
public class Service {

    public static ReadWriteLock lock = new ReentrantReadWriteLock();

    public static DefaultFullHttpResponse handle(FullHttpRequest req) throws UnsupportedEncodingException {
        String uri = req.uri();
        Repository.resortIndexForStage();
        if (uri.charAt(10) == 'f' && uri.charAt(11) == 'i' && uri.charAt(12) == 'l') {
            if (uri.charAt(17) == '?') {
                return handleFilterv2(uri);
            }
            return ServerHandler.NOT_FOUND_R;
        } else if (uri.charAt(10) == 'n' && uri.charAt(11) == 'e') {
            if (uri.charAt(14) == '?') {
                return handleNew(req);
            }
            return ServerHandler.NOT_FOUND_R;
        } else if (uri.charAt(10) == 'l' && uri.charAt(11) == 'i') {
            if (uri.charAt(16) == '?') {
                return handleLikes(req);
            }
            return ServerHandler.NOT_FOUND_R;
        } else if (uri.charAt(10) == 'g' && uri.charAt(11) == 'r') {
            if (uri.charAt(16) == '?') {
                return handleGroup(req);
            }
            return ServerHandler.NOT_FOUND_R;
        } else if (uri.contains(Constants.URI_SUGGEST)) {
            int index = uri.indexOf(Constants.URI_SUGGEST) + 9;
            if (uri.charAt(index) == '?' && Character.isDigit(uri.charAt(10))) {
                return handleSuggest(req);
            }
            return ServerHandler.NOT_FOUND_R;
        } else if (uri.contains(Constants.URI_RECOMENDED)) {
            if (Character.isDigit(uri.charAt(10))) {
                return handleRecomended(req);
            }
            return ServerHandler.NOT_FOUND_R;
        } else {
            if (Character.isDigit(uri.charAt(10))) {
                return handleUpdate(req);
            }
            return ServerHandler.NOT_FOUND_R;
        }
    }

    private static DefaultFullHttpResponse handleUpdate(FullHttpRequest req) {
        String curId = req.uri().substring(10, req.uri().lastIndexOf("/?"));

        Account accountData;
        Account account;
        lock.readLock().lock();
        try {
            accountData = Repository.ids[Integer.parseInt(curId)];
            if (accountData == null) {
                return ServerHandler.NOT_FOUND_R;
            }

            account = Utils.anyToAccount(JsonIterator.deserialize(req.content().toString(StandardCharsets.UTF_8)));
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
            lock.readLock().unlock();
        }


        lock.writeLock().lock();
        try {
            if (account.getEmail() != null) {
                Repository.emails.remove(accountData.getEmail());
                Repository.emails.add(account.getEmail());
                accountData.setEmail(account.getEmail());
                Repository.updateEmailIndex(accountData);
            }
            if (account.getSex() != null) {
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
                accountData.setCity(account.getCity());
                Repository.updateCityIndex(accountData);
            }
            if (account.getCountry() != null) {
                accountData.setCountry(account.getCountry());
                Repository.updateCountryIndex(accountData);
            }
            if (account.getSname() != null) {
                accountData.setSname(account.getSname());
                Repository.updateSnameIndex(accountData);
            }
            if (account.getLikes() != null) {
                accountData.setLikes(account.getLikes());
            }
        } finally {
            lock.writeLock().unlock();
        }

        return ServerHandler.ACCEPTED_R;

    }


    private static DefaultFullHttpResponse handleRecomended(FullHttpRequest req) {
        return ServerHandler.BAD_REQUEST_R;
    }

    private static DefaultFullHttpResponse handleSuggest(FullHttpRequest req) {
        return ServerHandler.BAD_REQUEST_R;
    }

    public static DefaultFullHttpResponse handleGroup(FullHttpRequest req) {
        return ServerHandler.NOT_FOUND_R;
    }

    private static DefaultFullHttpResponse handleLikes(FullHttpRequest req) {
        try {
            Any likesRequestAny = JsonIterator.deserialize(req.content().toString(StandardCharsets.UTF_8));
            List<Any>likesListAny = likesRequestAny.get(Constants.LIKES).asList();

            for (Any any : likesListAny) {
                Any value = any.get(Constants.TS);
                if (!ValueType.NUMBER.equals(value.valueType())) {
                    return ServerHandler.BAD_REQUEST_R;
                }
                value = any.get(Constants.LIKEE);
                int likeeId;
                if (!ValueType.NUMBER.equals(value.valueType())) {
                    return ServerHandler.BAD_REQUEST_R;
                } else {
                    likeeId = value.toInt();
                    if (Repository.ids[likeeId] == null) {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                value = any.get(Constants.LIKER);
                if (!ValueType.NUMBER.equals(value.valueType())) {
                    return ServerHandler.BAD_REQUEST_R;
                } else {
                    Account liker = Repository.ids[value.toInt()];
                    if (liker == null) {
                        return ServerHandler.BAD_REQUEST_R;
                    } else {
                        int[] likesOld = liker.getLikes();
                        int[] likesNew;
                        if (likesOld != null) {
                            likesNew = Arrays.copyOf(likesOld, likesOld.length + 1);
                        } else {
                            likesNew = new int[1];
                        }
                        likesNew[likesNew.length - 1] = likeeId;
                        Arrays.sort(likesNew);
                        lock.writeLock().lock();
                        try {
                            liker.setLikes(likesNew);
                        } finally {
                            lock.writeLock().unlock();
                        }
                    }
                }
            }
            return ServerHandler.ACCEPTED_R;
        } catch (Exception e) {
            e.printStackTrace();
            return ServerHandler.BAD_REQUEST_R;
        }
    }

    private static DefaultFullHttpResponse handleNew(FullHttpRequest req) {
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

            lock.readLock().lock();
            try {
                if (Repository.ids[account.getId()] != null) {
                    return ServerHandler.BAD_REQUEST_R;
                }
                if (Repository.emails.contains(account.getEmail())) {
                    return ServerHandler.BAD_REQUEST_R;
                }
            } finally {
                lock.readLock().unlock();
            }

            lock.writeLock().lock();
            try {
                Repository.list[Repository.index.incrementAndGet()] = account;
                Repository.ids[account.getId()] = account;
                Repository.emails.add(account.getEmail());
                Repository.insertToIndex(account);
                return ServerHandler.CREATED_R;
            } finally {
                lock.writeLock().unlock();
            }
        }
        return ServerHandler.CREATED_R;
    }


    private static String getValue(String param) throws UnsupportedEncodingException {
        if (param.startsWith(Constants.COUNTRY)
                || param.startsWith(Constants.CITY)
                || param.startsWith(Constants.INTERESTS)
                || param.startsWith(Constants.FNAME)
                || param.startsWith(Constants.SNAME)
                || param.startsWith(Constants.STATUS)
                || param.startsWith(Constants.LIKES)
                || param.startsWith(Constants.KEYS)
        ) {
            return URLDecoder.decode(param.substring(param.indexOf("=") + 1), Constants.UTF_8);
        } else {
            return param.substring(param.indexOf("=") + 1);
        }
    }

    private static String getPredicate(String param) {
        return param.substring(param.indexOf("_") + 1,param.indexOf("="));
    }

    public static DefaultFullHttpResponse handleFilterv2(String uri) throws UnsupportedEncodingException {
        lock.readLock().lock();
        try {
            String[] params = Utils.tokenize(uri.substring(18), '&');

            if (params.length < 2) {
                return ServerHandler.BAD_REQUEST_R;
            }

            boolean emailPr = false;
            boolean sexPr = false;
            boolean fnamePr = false;
            boolean interestsPr = false;
            boolean statusPr = false;
            boolean premiumPr = false;
            boolean phonePr = false;
            boolean birthPr = false;
            boolean cityPr = false;
            boolean countryPr = false;
            boolean snamePr = false;
            boolean likesPr = false;

            String emailPrV = null;
            String fnamePrV = null;
            String interestsPrV = null;
            String statusPrV = null;
            String premiumPrV = null;
            String phonePrV = null;
            String birthPrV = null;
            String cityPrV = null;
            String countryPrV = null;
            String snamePrV = null;

            String emailV = null;
            String sexV = null;
            String fnameV = null;
            String interestsV = null;
            String statusV = null;
            String premiumV = null;
            String phoneV = null;
            String birthV = null;
            String cityV = null;
            String countryV = null;
            String snameV = null;
            String likesV = null;



            int limit = 0;

            for (String param : params) {
                String valueParam = getValue(param).intern();
                String predicate = getPredicate(param).intern();
                if (param.charAt(0) == 'q' && param.charAt(1) == 'u') {
                    continue;
                }
                if (param.charAt(0) == 's' && param.charAt(1) == 'e') {
                    sexPr = true;
                    sexV = valueParam;
                    if (!predicate.equals(Constants.EQ_PR)) {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'e') {
                    emailPr = true;
                    emailV = valueParam;
                    if (predicate.equals(Constants.DOMAIN_PR)) {
                        emailPrV = Constants.DOMAIN_PR;
                    } else if (predicate.equals(Constants.LT_PR)) {
                        emailPrV =Constants.LT_PR;
                    } else if (predicate.equals(Constants.GT_PR)) {
                        emailPrV = Constants.GT_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 's' && param.charAt(1) == 't') {
                    statusPr = true;
                    statusV = valueParam;
                    if (predicate.equals(Constants.EQ_PR)) {
                        statusPrV = Constants.EQ_PR;
                    } else if(predicate.equals(Constants.NEQ_PR)) {
                        statusPrV = Constants.NEQ_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'f') {
                    fnamePr = true;
                    fnameV = valueParam;
                    if (predicate.equals(Constants.EQ_PR)) {
                        fnamePrV = Constants.EQ_PR;
                    } else if (predicate.equals(Constants.ANY_PR)) {
                        fnamePrV = Constants.ANY_PR;
                    } else if (predicate.equals(Constants.NULL_PR)) {
                        fnamePrV = Constants.NULL_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 's' && param.charAt(1) == 'n') {
                    snamePr = true;
                    snameV = valueParam;
                    if (predicate.equals(Constants.EQ_PR)) {
                        snamePrV = Constants.EQ_PR;
                    } else if (predicate.equals(Constants.STARTS_PR)) {
                        snamePrV = Constants.STARTS_PR;
                    } else if (predicate.equals(Constants.NULL_PR)){
                        snamePrV = Constants.NULL_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'p' && param.charAt(1) == 'h') {
                    phonePr = true;
                    phoneV = valueParam;
                    if (predicate.equals(Constants.NULL_PR)) {
                        phonePrV = Constants.NULL_PR;
                    } else if (predicate.equals(Constants.CODE_PR)) {
                        phonePrV = Constants.CODE_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'c' && param.charAt(1) == 'o') {
                    countryPr = true;
                    countryV = valueParam;
                    if (predicate.equals(Constants.NULL_PR)) {
                        countryPrV = Constants.NULL_PR;
                    } else if (predicate.equals(Constants.EQ_PR)) {
                        countryPrV = Constants.EQ_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'c' && param.charAt(1) == 'i') {
                    cityPr = true;
                    cityV = valueParam;
                    if (predicate.equals(Constants.EQ_PR)) {
                        cityPrV = Constants.EQ_PR;
                    } else if (predicate.equals(Constants.ANY_PR)) {
                        cityPrV = Constants.ANY_PR;
                    } else if (predicate.equals(Constants.NULL_PR)) {
                        cityPrV = Constants.NULL_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'b') {
                    birthPr = true;
                    birthV = valueParam;
                    if (predicate.equals(Constants.YEAR_PR)) {
                        birthPrV = Constants.YEAR_PR;
                    } else if (predicate.equals(Constants.LT_PR)) {
                        birthPrV = Constants.LT_PR;
                    }  else if (predicate.equals(Constants.GT_PR)) {
                        birthPrV = Constants.GT_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'i') {
                    interestsPr = true;
                    interestsV = valueParam;
                    if (predicate.equals(Constants.CONTAINS_PR)) {
                        interestsPrV = Constants.CONTAINS_PR;
                    } else if (predicate.equals(Constants.ANY_PR)) {
                        interestsPrV = Constants.ANY_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'l' && param.charAt(1) == 'i' && param.charAt(2) == 'k') {
                    likesPr = true;
                    if (predicate.equals(Constants.CONTAINS_PR)) {
                        likesV = valueParam;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'p' && param.charAt(1) == 'r') {
                    premiumPr = true;
                    premiumV = valueParam;
                    if (predicate.equals(Constants.NULL_PR)) {
                        premiumPrV = Constants.NULL_PR;
                    } else if (predicate.equals(Constants.NOW_PR)) {
                        premiumPrV = Constants.NOW_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }

                if (param.charAt(0) == 'l' && param.charAt(1) == 'i' && param.charAt(2) == 'm') {
                    if (!Character.isDigit(valueParam.charAt(0))) {
                        return ServerHandler.BAD_REQUEST_R;
                    } else {
                        limit = Integer.parseInt(valueParam);
                    }
                }
            }

            Set<Account> accounts = LocalPool.threadLocalAccounts.get();
            if (params.length == 2) {
                for (Account account : Repository.list) {
                    if (accounts.size() == limit) {
                        return ServerHandler.createOK(Utils.accountToString(accounts
                                ,sexPr
                                ,fnamePr
                                ,statusPr
                                ,premiumPr
                                ,phonePr
                                ,birthPr
                                ,cityPr
                                ,countryPr
                                ,snamePr).getBytes(StandardCharsets.UTF_8));
                    } else {
                        accounts.add(account);
                    }
                }
            }
            Integer year = null;
            String[] cityArr = null;
            String[] fnameArr = null;
            String[] interArr = null;
            int[] likesArr = null;
            if (birthPr) {
                year = Integer.parseInt(birthV);
            }
            if (cityPr) {
                if (cityPrV == Constants.ANY_PR) {
                    cityArr = Utils.tokenize(cityV, Constants.DELIM);
                }
            }
            if (fnamePr) {
                if (fnamePrV == Constants.ANY_PR) {
                    fnameArr = Utils.tokenize(fnameV, Constants.DELIM);
                }
            }
            if (interestsPr) {
                interArr = Utils.tokenize(interestsV, Constants.DELIM);
            }
            if (likesPr) {
                String[] likesArrStr = Utils.tokenize(likesV, Constants.DELIM);
                likesArr = new int[likesArrStr.length];
                int index = 0;
                for (String s : likesArrStr) {
                    likesArr[index] = Integer.valueOf(s);
                }
            }


            Account[] listForSearch = getIndexForFilter(interArr,interestsPrV
                    ,phonePr,phonePrV,phoneV
                    ,snamePr,snamePrV,snameV
                    ,cityPr,cityPrV,cityV,sexV
                    ,fnamePr,fnamePrV,fnameV
                    ,countryPr,countryPrV,countryV
                    ,premiumPr,premiumPrV,premiumV
                    ,statusPr,statusPrV,statusV
                    ,sexPr
                    ,birthPr,birthPrV,year
                    ,emailPr,emailPrV,emailV
                    ,cityArr,fnameArr
            );
            if (listForSearch == null) {
                return ServerHandler.OK_EMPTY_R;
            }

            for (Account account : listForSearch) {
                if (account == null) {
                    break;
                }
                //SEX ============================================
                if (sexPr) {
                    if (!account.getSex().equals(sexV)) {
                        continue;
                    }
                }
                //SEX ============================================

                //EMAIL ============================================
                if (emailPr) {
                    if (emailPrV == Constants.DOMAIN_PR) {
                        if (!account.getEmail().contains(emailV)) {
                            continue;
                        }
                    } else if (emailPrV == Constants.LT_PR) {
                        if (account.getEmail().compareTo(emailV) > 0) {
                            continue;
                        }
                    } else if (emailPrV == Constants.GT_PR) {
                        if (account.getEmail().compareTo(emailV) < 0) {
                            continue;
                        }
                    }
                }
                //EMAIL ============================================

                //STATUS ============================================
                if (statusPr) {
                    if (statusPrV == Constants.EQ_PR) {
                        if (!account.getStatus().equals(statusV)) {
                            continue;
                        }
                    } else if (statusPrV == Constants.NEQ_PR) {
                        if (account.getStatus().equals(statusV)) {
                            continue;
                        }
                    }
                }
                //STATUS ============================================


                //SNAME ============================================
                if (snamePr) {
                    if (snamePrV == Constants.EQ_PR) {
                        if (!snameV.equals(account.getSname())) {
                            continue;
                        }
                    } else if (snamePrV == Constants.NULL_PR) {
                        if (snameV == Constants.NULL_PR_VAL_ONE) {
                            if (account.getSname() != null) {
                                continue;
                            }
                        } else {
                            if (account.getSname() == null) {
                                continue;
                            }
                        }
                    } else if (snamePrV == Constants.STARTS_PR) {
                        if (account.getSname() != null) {
                            if (!account.getSname().startsWith(snameV)) {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                }
                //SNAME ============================================

                //PHONE ============================================
                if (phonePr) {

                    if (phonePrV == Constants.CODE_PR) {
                        if (account.getPhone() != null) {
                            if (!account.getPhone()
                                    .substring(account.getPhone().indexOf("(") + 1
                                            , account.getPhone().indexOf(")"))
                                    .equals(phoneV)) {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    } else if (phonePrV == Constants.NULL_PR) {
                        if (phoneV == Constants.NULL_PR_VAL_ONE) {
                            if (account.getPhone() != null) {
                                continue;
                            }
                        } else {
                            if (account.getPhone() == null) {
                                continue;
                            }
                        }
                    }
                }
                //PHONE ============================================


                //COUNTRY ============================================
                if (countryPr) {
                    if (countryPrV == Constants.EQ_PR) {
                        if (!countryV.equals(account.getCountry())) {
                            continue;
                        }
                    } else if (countryPrV == Constants.NULL_PR) {
                        if (countryV == Constants.NULL_PR_VAL_ONE) {
                            if (account.getCountry() != null) {
                                continue;
                            }
                        } else {
                            if (account.getCountry() == null) {
                                continue;
                            }
                        }
                    }
                }
                //COUNTRY ============================================


                //PREMIUM ============================================
                if (premiumPr) {
                    if (premiumPrV == Constants.NOW_PR) {
                        if (account.getStart() != 0) {
                            if (currentTimeStamp2 < account.getFinish()
                                    && currentTimeStamp2 > account.getStart()) {
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    } else if (premiumPrV == Constants.NULL_PR) {
                        if (premiumV == Constants.NULL_PR_VAL_ONE) {
                            if (account.getStart() != 0) {
                                continue;
                            }
                        } else {
                            if (account.getStart() == 0) {
                                continue;
                            }
                        }
                    }
                }
                //PREMIUM ============================================
                //BIRTH ============================================
                if (birthPr) {
                    if (birthPrV == Constants.YEAR_PR) {
                        Calendar calendar = LocalPool.threadLocalCalendar.get();
                        calendar.setTimeInMillis((long) account.getBirth() * 1000);
                        if (year != calendar.get(Calendar.YEAR)) {
                            continue;
                        }
                    } else if (birthPrV == Constants.LT_PR) {
                        if (account.getBirth() > year) {
                            continue;
                        }
                    } else if (birthPrV == Constants.GT_PR) {
                        if (account.getBirth() < year) {
                            continue;
                        }
                    }
                }
                //BIRTH ============================================

                //CITY ============================================
                if (cityPr) {

                    if (cityPrV == Constants.EQ_PR) {
                        if (!cityV.equals(account.getCity())) {
                            continue;
                        }
                    } else if (cityPrV == Constants.ANY_PR) {
                        boolean isValid = false;
                        for (String value : cityArr) {
                            if (value.equals(account.getCity())) {
                                isValid = true;
                                break;
                            }
                        }
                        if (!isValid) {
                            continue;
                        }
                    } else if (cityPrV == Constants.NULL_PR) {
                        if (cityV == Constants.NULL_PR_VAL_ONE) {
                            if (account.getCity() != null) {
                                continue;
                            }
                        } else {
                            if (account.getCity() == null) {
                                continue;
                            }
                        }
                    }
                }
                //CITY ============================================


                //FNAME ============================================
                if (fnamePr) {

                    if (fnamePrV == Constants.EQ_PR) {
                        if (!fnameV.equals(account.getFname())) {
                            continue;
                        }
                    } else if (fnamePrV == Constants.ANY_PR) {
                        boolean isValid = false;
                        for (String value : fnameArr) {
                            if (value.equals(account.getFname())) {
                                isValid = true;
                                break;
                            }
                        }
                        if (!isValid) {
                            continue;
                        }
                    } else if (fnamePrV == Constants.NULL_PR) {
                        if (fnameV == Constants.NULL_PR_VAL_ONE) {
                            if (account.getFname() != null) {
                                continue;
                            }
                        } else {
                            if (account.getFname() == null) {
                                continue;
                            }
                        }
                    }
                }
                //FNAME ============================================

                //INTERESTS ============================================
                if (interestsPr) {
                    if (account.getInterests() != null) {
                        if (interestsPrV == Constants.ANY_PR) {
                            boolean isValid = false;
                            for (String value : interArr) {
                                //todo упорядочить
                                if (Utils.contains(account.getInterests(),value)) {
                                    isValid = true;
                                    break;
                                }
                            }
                            if (!isValid) {
                                continue;
                            }
                        } else if (interestsPrV == Constants.CONTAINS_PR) {
                            //todo чекнуть битмапы
                            if (interArr.length <= account.getInterests().length) {
                                /*RoaringBitmap bitmapQ = Repository.getInterestBitMap(interArr);
                                bitmapQ.or(account.getInterestBitmap());
                                if (!bitmapQ.equals(account.getInterestBitmap())) {
                                    continue;
                                }*/
                                boolean isValid = true;
                                for (String value : interArr) {
                                    if (!Utils.contains(account.getInterests(),value)) {
                                        isValid = false;
                                        break;
                                    }
                                }
                                if (!isValid) {
                                    continue;
                                }
                            } else {
                                continue;
                            }
                        }

                    } else {
                        continue;
                    }
                }
                //INTERESTS ============================================

                if (likesPr) {
                        if (account.getLikes() != null) {
                            int[] likes = account.getLikes();
                            if (likesArr.length <= likes.length) {
                                boolean isValid = true;
                                for (int value : likesArr) {
                                    if (likes[0] > value
                                            || likes[likes.length - 1] < value) {
                                        isValid = false;
                                        break;
                                    }
                                }
                                if (!isValid) {
                                    continue;
                                }
                                for (int value : likesArr) {
                                    if (!Utils.contains(account.getLikes(),value)) {
                                        isValid = false;
                                        break;
                                    }
                                }
                                if (!isValid) {
                                    continue;
                                }
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                }

                accounts.add(account);
                if (accounts.size() == limit) {
                    break;
                }
            }
            if (accounts.size() == 0) {
                return ServerHandler.OK_EMPTY_R;
            }
            return ServerHandler.createOK(Utils.accountToString(accounts
                    ,sexPr
                    ,fnamePr
                    ,statusPr
                    ,premiumPr
                    ,phonePr
                    ,birthPr
                    ,cityPr
                    ,countryPr
                    ,snamePr).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.out.println(uri);
            e.printStackTrace();
            return ServerHandler.INTERNAL_ERROR_R;
        } finally {
            lock.readLock().unlock();
        }
    }

    private static Account[] getIndexForFilter(String[] interArr, String interestsPrV
            , boolean phonePr, String phonePrV, String phoneV
            , boolean snamePr, String snamePrV, String snameV
            , boolean cityPr, String cityPrV, String cityV, String sexV
            , boolean fnamePr, String fnamePrV, String fnameV
            , boolean countryPr, String countryPrV, String countryV
            , boolean premiumPr, String premiumPrV, String premiumV
            , boolean statusPr, String statusPrV, String statusV
            ,boolean sexPr
            , boolean birthPr, String birthPrV, Integer year
            ,boolean emailPr,String emailPrV,String emailV
            ,String[] cityArr,String[] fnameArr) {
        Account[] resultIndex = Repository.list;


        if (snamePr) {
            if (snamePrV == Constants.NULL_PR) {
                if (snameV == Constants.NULL_PR_VAL_ONE) {
                    return Repository.sname_by_name.get(null);
                } else {
                    // resultIndex = compareIndex(Repository.sname_not_null,resultIndex);
                }
            } else if (snamePrV == Constants.EQ_PR) {
                return Repository.sname_by_name.get(snameV);
            }
        }

        if (cityPr) {
            if (cityPrV == Constants.ANY_PR) {
                if (cityArr.length == 1) {
                    return Repository.city_by_name.get(cityArr[0]);
                } else {
                    //return Repository.city_not_null;
                }
            } else {
                if (cityPrV == Constants.NULL_PR) {
                    if (cityV == Constants.NULL_PR_VAL_ONE) {
                        return Repository.city_by_name.get(null);
                    } else {
                        //return Repository.city_not_null;
                    }
                }
                if (cityPrV == Constants.EQ_PR) {
                    return Repository.city_by_name.get(cityV);
                }
            }
        }

        if (fnamePr) {
            if (fnamePrV == Constants.NULL_PR) {
                if (fnameV == Constants.NULL_PR_VAL_ONE) {
                    return Repository.fname_by_name.get(null);
                } else {
                   // resultIndex = compareIndex(Repository.fname_not_null,resultIndex);
                }
            } else if (fnamePrV == Constants.ANY_PR) {
                if (fnameArr.length == 1) {
                    return Repository.fname_by_name.get(fnameArr[0]);
                } else {
                   // resultIndex = compareIndex(Repository.fname_not_null, resultIndex);
                }
            } else if (fnamePrV == Constants.EQ_PR) {
                return Repository.fname_by_name.get(fnameV);
            }
        }

        if (phonePr) {
            if (phonePrV == Constants.CODE_PR) {
                return Repository.phone_code_by_name.get(phoneV);
            } else {
                if (phoneV == Constants.NULL_PR_VAL_ONE) {
                   // resultIndex = compareIndex(Repository.phone_null, resultIndex);
                } else {
                    //resultIndex = compareIndex(Repository.phone_not_null, resultIndex);
                }
            }
        }


        if (countryPr) {
            if (countryPrV == Constants.NULL_PR) {
                if (countryV == Constants.NULL_PR_VAL_ONE) {
                    return Repository.country_by_name.get(null);
                } else {
                   // return Repository.country_not_null;
                }
            }
            if (countryPrV == Constants.EQ_PR) {
                return Repository.country_by_name.get(countryV);
            }
        }

        if (interArr != null && interestsPrV == Constants.CONTAINS_PR) {
           return Repository.interests_by_name.get(interArr[0]);
        }
        if (birthPr) {
            if (birthPrV == Constants.YEAR_PR) {
                return Repository.year.get(year);
            } else {
                /*Account[] curInd = birth_idx_lt;
                if (birthPrV == GT_PR) {
                    curInd = birth_idx_gt;
                }
                int startPos = Utils.binarySearchStartPos(curInd,year,0,curInd.length - 1);
                Account[] newArr = new Account[curInd.length - startPos];
                System.arraycopy(curInd, 0, newArr, 0, curInd.length - startPos);
                Arrays.sort(newArr,Repository.idsComparator);
                return newArr;*/
            }
        }

        if (emailPr) {
            if (emailPrV == Constants.DOMAIN_PR) {
                return Repository.email_domain_by_name.get(emailV);
            }
        }

        if (premiumPr) {
            if (premiumPrV == Constants.NOW_PR) {
                return Repository.premium_1;
            } else if (premiumPrV == Constants.NULL_PR) {
                if (premiumV == Constants.NULL_PR_VAL_ONE) {
                    return Repository.premium_3;
                } else {
                    return Repository.premium_2;
                }
            }
        }

        if (statusPr) {
            if (statusPrV == Constants.EQ_PR) {
                if (Constants.STATUS1.equals(statusV)) {
                    return Repository.status_1;
                }
                if (Constants.STATUS2.equals(statusV)) {
                    return Repository.status_2;
                }
                if (Constants.STATUS3.equals(statusV)) {
                    return Repository.status_3;
                }
            } else {
                if (statusV.equals(Constants.STATUS1)) {
                    return Repository.status_1_not;
                } else if (statusV.equals(Constants.STATUS2)) {
                    return Repository.status_2_not;
                } else {
                    return Repository.status_3_not;
                }
            }
        }

        if (sexPr) {
            if (Constants.F.equals(sexV)) {
                return Repository.list_f;
            }
            if (Constants.M.equals(sexV)) {
                return Repository.list_m;
            }
        }

        return resultIndex;

    }

}
