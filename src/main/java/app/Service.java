package app;

import app.models.Account;
import app.server.ServerHandler;
import com.jsoniter.JsonIterator;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import org.roaringbitmap.RoaringBitmap;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static app.Repository.currentTimeStamp2;
import static app.Repository.resortIndexForStage;

/**
 * Created by Alikin E.A. on 15.12.18.
 */
public class Service {


    public static final String SEX = "sex";
    public static final String EMAIL = "email";
    public static final String STATUS = "status";
    public static final String FNAME = "fname";
    public static final String JOINED = "joined";
    public static final String SNAME = "sname";
    public static final String PHONE = "phone";
    public static final String COUNTRY = "country";
    public static final String CITY = "city";
    public static final String BIRTH = "birth";
    public static final String INTERESTS = "interests";
    public static final String LIKES = "likes";
    public static final String KEYS = "keys";
    public static final String PREMIUM = "premium";
    public static final String ID = "id";
    public static final String TS = "ts";
    public static final String LIKEE = "likee";
    public static final String LIKER = "liker";

    private static final String EQ_PR = "eq";
    private static final String NEQ_PR = "neq";
    private static final String DOMAIN_PR = "domain";
    private static final String GT_PR = "gt";
    private static final String LT_PR = "lt";
    private static final String ANY_PR = "any";
    private static final String NULL_PR = "null";
    private static final String STARTS_PR = "starts";
    private static final String CODE_PR = "code";
    private static final String YEAR_PR = "year";
    private static final String CONTAINS_PR = "contains";
    private static final String NOW_PR = "now";
    private static final String NULL_PR_VAL_ONE = "1";
    private static final String URI_SUGGEST = "/suggest";
    private static final String URI_RECOMENDED = "/recommend";

    public static final String F = "f";
    public static final String M = "m";

    public static final String STATUS1 = "свободны";
    public static final String STATUS2 = "всё сложно";
    public static final String STATUS3 = "заняты";

    private static final String utf8 = "UTF-8";
    private static final char delim = ',';

    public static ReadWriteLock lock = new ReentrantReadWriteLock();

    public static ThreadLocal<List<Account>> threadLocalAccounts =
            new ThreadLocal<>() {
                @Override
                protected List<Account> initialValue() {
                    return new LinkedList<>();
                }

                @Override
                public List<Account> get() {
                    List<Account> b = super.get();
                    b.clear();
                    return b;
                }
            };



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
        } else if (uri.contains(URI_SUGGEST)) {
            int index = uri.indexOf(URI_SUGGEST) + 9;
            if (uri.charAt(index) == '?' && Character.isDigit(uri.charAt(10))) {
                return handleSuggest(req);
            }
            return ServerHandler.NOT_FOUND_R;
        } else if (uri.contains(URI_RECOMENDED)) {
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

        lock.readLock().lock();
        Account accountData;
        Account account;
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
                if (!account.getSex().equals(F)
                        && !account.getSex().equals(M)) {
                    return ServerHandler.BAD_REQUEST_R;
                }
            }
            if (account.getStatus() != null) {
                if (!account.getStatus().equals(STATUS1)
                        && !account.getStatus().equals(STATUS2)
                        && !account.getStatus().equals(STATUS3)) {
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
        lock.readLock().lock();
        try {
            try {
                boolean isValid = Utils.validateLikes(req.content().toString(StandardCharsets.UTF_8));
                if (!isValid) {
                    return ServerHandler.BAD_REQUEST_R;
                }
                return ServerHandler.ACCEPTED_R;
            } catch (Exception e) {
                return ServerHandler.BAD_REQUEST_R;
            }
        }finally {
            lock.readLock().unlock();
        }
    }

    private static DefaultFullHttpResponse handleNew(FullHttpRequest req) {
        Account account = Utils.anyToAccount(JsonIterator.deserialize(req.content().toString(StandardCharsets.UTF_8)));
        if (account == null || account.getId() == -1) {
            return ServerHandler.BAD_REQUEST_R;
        }
        lock.readLock().lock();
        try {
            if (Repository.ids[account.getId()] != null) {
                return ServerHandler.BAD_REQUEST_R;
            }
        } finally {
            lock.readLock().unlock();
        }
        if (account.getSex() != null) {
            if (!account.getSex().equals(F)
                    && !account.getSex().equals(M)) {
                return ServerHandler.BAD_REQUEST_R;
            }
        } else {
            return ServerHandler.BAD_REQUEST_R;
        }
        if (account.getStatus() != null) {
            if (!account.getStatus().equals(STATUS1)
                    && !account.getStatus().equals(STATUS2)
                    && !account.getStatus().equals(STATUS3)) {
                return ServerHandler.BAD_REQUEST_R;
            }
        } else {
            return ServerHandler.BAD_REQUEST_R;
        }
        if (account.getEmail() != null) {
            if (!account.getEmail().contains("@")) {
                return ServerHandler.BAD_REQUEST_R;
            }
            boolean contains;
            lock.readLock().lock();
            try {
                contains = Repository.emails.contains(account.getEmail());
            } finally {
                lock.readLock().unlock();
            }

            if (contains) {
                return ServerHandler.BAD_REQUEST_R;
            } else {
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
        }
        return ServerHandler.CREATED_R;
    }


    private static String getValue(String param) throws UnsupportedEncodingException {
        if (param.startsWith(COUNTRY)
                || param.startsWith(CITY)
                || param.startsWith(INTERESTS)
                || param.startsWith(FNAME)
                || param.startsWith(SNAME)
                || param.startsWith(STATUS)
                || param.startsWith(LIKES)
                || param.startsWith(KEYS)
        ) {
            return URLDecoder.decode(param.substring(param.indexOf("=") + 1), utf8);
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
                    if (!predicate.equals(EQ_PR)) {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'e') {
                    emailPr = true;
                    emailV = valueParam;
                    if (predicate.equals(DOMAIN_PR)) {
                        emailPrV = DOMAIN_PR;
                    } else if (predicate.equals(LT_PR)) {
                        emailPrV = LT_PR;
                    } else if (predicate.equals(GT_PR)) {
                        emailPrV = GT_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 's' && param.charAt(1) == 't') {
                    statusPr = true;
                    statusV = valueParam;
                    if (predicate.equals(EQ_PR)) {
                        statusPrV = EQ_PR;
                    } else if(predicate.equals(NEQ_PR)) {
                        statusPrV = NEQ_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'f') {
                    fnamePr = true;
                    fnameV = valueParam;
                    if (predicate.equals(EQ_PR)) {
                        fnamePrV = EQ_PR;
                    } else if (predicate.equals(ANY_PR)) {
                        fnamePrV = ANY_PR;
                    } else if (predicate.equals(NULL_PR)) {
                        fnamePrV = NULL_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 's' && param.charAt(1) == 'n') {
                    snamePr = true;
                    snameV = valueParam;
                    if (predicate.equals(EQ_PR)) {
                        snamePrV = EQ_PR;
                    } else if (predicate.equals(STARTS_PR)) {
                        snamePrV = STARTS_PR;
                    } else if (predicate.equals(NULL_PR)){
                        snamePrV = NULL_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'p' && param.charAt(1) == 'h') {
                    phonePr = true;
                    phoneV = valueParam;
                    if (predicate.equals(NULL_PR)) {
                        phonePrV = NULL_PR;
                    } else if (predicate.equals(CODE_PR)) {
                        phonePrV = CODE_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'c' && param.charAt(1) == 'o') {
                    countryPr = true;
                    countryV = valueParam;
                    if (predicate.equals(NULL_PR)) {
                        countryPrV = NULL_PR;
                    } else if (predicate.equals(EQ_PR)) {
                        countryPrV = EQ_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'c' && param.charAt(1) == 'i') {
                    cityPr = true;
                    cityV = valueParam;
                    if (predicate.equals(EQ_PR)) {
                        cityPrV = EQ_PR;
                    } else if (predicate.equals(ANY_PR)) {
                        cityPrV = ANY_PR;
                    } else if (predicate.equals(NULL_PR)) {
                        cityPrV = NULL_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'b') {
                    birthPr = true;
                    birthV = valueParam;
                    if (predicate.equals(YEAR_PR)) {
                        birthPrV = YEAR_PR;
                    } else if (predicate.equals(LT_PR)) {
                        birthPrV = LT_PR;
                    }  else if (predicate.equals(GT_PR)) {
                        birthPrV = GT_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'i') {
                    interestsPr = true;
                    interestsV = valueParam;
                    if (predicate.equals(CONTAINS_PR)) {
                        interestsPrV = CONTAINS_PR;
                    } else if (predicate.equals(ANY_PR)) {
                        interestsPrV = ANY_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'l' && param.charAt(1) == 'i' && param.charAt(2) == 'k') {
                    likesPr = true;
                    if (predicate.equals(CONTAINS_PR)) {
                        likesV = valueParam;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'p' && param.charAt(1) == 'r') {
                    premiumPr = true;
                    premiumV = valueParam;
                    if (predicate.equals(NULL_PR)) {
                        premiumPrV = NULL_PR;
                    } else if (predicate.equals(NOW_PR)) {
                        premiumPrV = NOW_PR;
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

            List<Account> accounts = threadLocalAccounts.get();
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
                if (cityPrV == ANY_PR) {
                    cityArr = Utils.tokenize(cityV, delim);
                }
            }
            if (fnamePr) {
                if (fnamePrV == ANY_PR) {
                    fnameArr = Utils.tokenize(fnameV, delim);
                }
            }
            if (interestsPr) {
                interArr = Utils.tokenize(interestsV, delim);
            }
            if (likesPr) {
                String[] likesArrStr = Utils.tokenize(likesV, delim);
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
                    if (emailPrV == DOMAIN_PR) {
                        if (!account.getEmail().contains(emailV)) {
                            continue;
                        }
                    } else if (emailPrV == LT_PR) {
                        if (account.getEmail().compareTo(emailV) > 0) {
                            continue;
                        }
                    } else if (emailPrV == GT_PR) {
                        if (account.getEmail().compareTo(emailV) < 0) {
                            continue;
                        }
                    }
                }
                //EMAIL ============================================

                //STATUS ============================================
                if (statusPr) {
                    if (statusPrV == EQ_PR) {
                        if (!account.getStatus().equals(statusV)) {
                            continue;
                        }
                    } else if (statusPrV == NEQ_PR) {
                        if (account.getStatus().equals(statusV)) {
                            continue;
                        }
                    }
                }
                //STATUS ============================================


                //SNAME ============================================
                if (snamePr) {
                    if (snamePrV == EQ_PR) {
                        if (!snameV.equals(account.getSname())) {
                            continue;
                        }
                    } else if (snamePrV == NULL_PR) {
                        if (snameV == NULL_PR_VAL_ONE) {
                            if (account.getSname() != null) {
                                continue;
                            }
                        } else {
                            if (account.getSname() == null) {
                                continue;
                            }
                        }
                    } else if (snamePrV == STARTS_PR) {
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

                    if (phonePrV == CODE_PR) {
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
                    } else if (phonePrV == NULL_PR) {
                        if (phoneV == NULL_PR_VAL_ONE) {
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
                    if (countryPrV == EQ_PR) {
                        if (!countryV.equals(account.getCountry())) {
                            continue;
                        }
                    } else if (countryPrV == NULL_PR) {
                        if (countryV == NULL_PR_VAL_ONE) {
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
                    if (premiumPrV == NOW_PR) {
                        if (account.getStart() != 0) {
                            if (currentTimeStamp2 < account.getFinish()
                                    && currentTimeStamp2 > account.getStart()) {
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    } else if (premiumPrV == NULL_PR) {
                        if (premiumV == NULL_PR_VAL_ONE) {
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
                    if (birthPrV == YEAR_PR) {
                        Calendar calendar = Repository.threadLocalCalendar.get();
                        calendar.setTimeInMillis((long) account.getBirth() * 1000);
                        if (year != calendar.get(Calendar.YEAR)) {
                            continue;
                        }
                    } else if (birthPrV == LT_PR) {
                        if (account.getBirth() > year) {
                            continue;
                        }
                    } else if (birthPrV == GT_PR) {
                        if (account.getBirth() < year) {
                            continue;
                        }
                    }
                }
                //BIRTH ============================================

                //CITY ============================================
                if (cityPr) {

                    if (cityPrV == EQ_PR) {
                        if (!cityV.equals(account.getCity())) {
                            continue;
                        }
                    } else if (cityPrV == ANY_PR) {
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
                    } else if (cityPrV == NULL_PR) {
                        if (cityV == NULL_PR_VAL_ONE) {
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

                    if (fnamePrV == EQ_PR) {
                        if (!fnameV.equals(account.getFname())) {
                            continue;
                        }
                    } else if (fnamePrV == ANY_PR) {
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
                    } else if (fnamePrV == NULL_PR) {
                        if (fnameV == NULL_PR_VAL_ONE) {
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
                        if (interestsPrV == ANY_PR) {
                            boolean isValid = false;
                            for (String value : interArr) {
                                if (Utils.contains(account.getInterests(),value)) {
                                    isValid = true;
                                    break;
                                }
                            }
                            if (!isValid) {
                                continue;
                            }
                        } else if (interestsPrV == CONTAINS_PR) {
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
                            if (likesArr.length <= account.getLikes().length) {
                                boolean isValid = true;
                                for (int value : likesArr) {
                                    if (!Utils.contains(account.getLikes(),value)) {
                                        isValid = false;
                                        break;
                                    }
                                }
                                if (!isValid) {
                                    continue;
                                }
                                /*RoaringBitmap bitmapQ = Repository.getLikesBitMap(likesArr);
                                bitmapQ.or(account.getInterestBitmap());
                                if (!bitmapQ.equals(account.getInterestBitmap())) {
                                    continue;
                                }*/
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
            if (snamePrV == NULL_PR) {
                if (snameV == NULL_PR_VAL_ONE) {
                    return Repository.sname_by_name.get(null);
                } else {
                    // resultIndex = compareIndex(Repository.sname_not_null,resultIndex);
                }
            } else if (snamePrV == EQ_PR) {
                return Repository.sname_by_name.get(snameV);
            }
        }

        if (cityPr) {
            if (cityPrV == ANY_PR) {
                if (cityArr.length == 1) {
                    return Repository.city_by_name.get(cityArr[0]);
                } else {
                    //return Repository.city_not_null;
                }
            } else {
                if (cityPrV == NULL_PR) {
                    if (cityV == NULL_PR_VAL_ONE) {
                        return Repository.city_by_name.get(null);
                    } else {
                        //return Repository.city_not_null;
                    }
                }
                if (cityPrV == EQ_PR) {
                    return Repository.city_by_name.get(cityV);
                }
            }
        }

        if (fnamePr) {
            if (fnamePrV == NULL_PR) {
                if (fnameV == NULL_PR_VAL_ONE) {
                    return Repository.fname_by_name.get(null);
                } else {
                   // resultIndex = compareIndex(Repository.fname_not_null,resultIndex);
                }
            } else if (fnamePrV == ANY_PR) {
                if (fnameArr.length == 1) {
                    return Repository.fname_by_name.get(fnameArr[0]);
                } else {
                   // resultIndex = compareIndex(Repository.fname_not_null, resultIndex);
                }
            } else if (fnamePrV == EQ_PR) {
                return Repository.fname_by_name.get(fnameV);
            }
        }

        if (phonePr) {
            if (phonePrV == CODE_PR) {
                return Repository.phone_code_by_name.get(phoneV);
            } else {
                if (phoneV == NULL_PR_VAL_ONE) {
                   // resultIndex = compareIndex(Repository.phone_null, resultIndex);
                } else {
                    //resultIndex = compareIndex(Repository.phone_not_null, resultIndex);
                }
            }
        }


        if (countryPr) {
            if (countryPrV == NULL_PR) {
                if (countryV == NULL_PR_VAL_ONE) {
                    return Repository.country_by_name.get(null);
                } else {
                   // return Repository.country_not_null;
                }
            }
            if (countryPrV == EQ_PR) {
                return Repository.country_by_name.get(countryV);
            }
        }

        if (interArr != null && interestsPrV == CONTAINS_PR) {
           return Repository.interests_by_name.get(interArr[0]);
        }
        if (birthPr) {
            if (birthPrV == YEAR_PR) {
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
            if (emailPrV == DOMAIN_PR) {
                return Repository.email_domain_by_name.get(emailV);
            }
        }

        if (premiumPr) {
            if (premiumPrV == NOW_PR) {
                return Repository.premium_1;
            } else if (premiumPrV == NULL_PR) {
                if (premiumV == NULL_PR_VAL_ONE) {
                    return Repository.premium_3;
                } else {
                    return Repository.premium_2;
                }
            }
        }

        if (statusPr) {
            if (statusPrV == EQ_PR) {
                if (Service.STATUS1.equals(statusV)) {
                    return Repository.status_1;
                }
                if (Service.STATUS2.equals(statusV)) {
                    return Repository.status_2;
                }
                if (Service.STATUS3.equals(statusV)) {
                    return Repository.status_3;
                }
            } else {
                if (statusV.equals(Service.STATUS1)) {
                    return Repository.status_1_not;
                } else if (statusV.equals(Service.STATUS2)) {
                    return Repository.status_2_not;
                } else {
                    return Repository.status_3_not;
                }
            }
        }

        if (sexPr) {
            if (Service.F.equals(sexV)) {
                return Repository.list_f;
            }
            if (Service.M.equals(sexV)) {
                return Repository.list_m;
            }
        }

        return resultIndex;

    }

}
