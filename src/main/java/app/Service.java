package app;

import app.models.*;
import app.server.Server;
import com.jsoniter.JsonIterator;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static app.Repository.currentTimeStamp2;
import static app.Repository.list;

/**
 * Created by Alikin E.A. on 15.12.18.
 */
public class Service {

    private static final byte[] EMPTY = "{}".getBytes();
    private static final byte[] EMPTY_ACCOUNTS = "{\"accounts\":[]}".getBytes();
    private static final Result OK_EMPTY_ACCOUNTS = new Result(EMPTY_ACCOUNTS, HttpResponseStatus.OK);
    private static final Result ACCEPTED = new Result(EMPTY, HttpResponseStatus.ACCEPTED);
    private static final Result CREATED = new Result(EMPTY, HttpResponseStatus.CREATED);
    private static final Result BAD_REQUEST = new Result(EMPTY, HttpResponseStatus.BAD_REQUEST);
    private static final Result NOT_FOUND = new Result(EMPTY, HttpResponseStatus.NOT_FOUND);


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
    public static final String LIKE = "like";
    public static final String PREMIUM = "premium";
    public static final String QUERY_ID = "query_id";
    public static final String LIMIT = "limit";
    public static final String ID = "id";
    public static final String TS = "ts";
    public static final String KEYS = "keys";

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

    private static final String URI_FILTER = "/accounts/filter/?";
    private static final String URI_NEW = "/accounts/new/";
    private static final String URI_LIKES = "/accounts/likes/";
    private static final String URI_GROUP = "/accounts/group/?";
    private static final String URI_SUGGEST = "/suggest";
    private static final String URI_RECOMENDED = "/recommend";
    private static final String ACCOUNTS = "/accounts/";

    public static final String F = "f";
    public static final String M = "m";

    public static final String STATUS1 = "свободны";
    public static final String STATUS2 = "всё сложно";
    public static final String STATUS3 = "заняты";

    private static final String utf8 = "UTF-8";
    private static final char delim = ',';

    private static final AtomicInteger count = new AtomicInteger(0);

    public static ReadWriteLock lock = new ReentrantReadWriteLock();

    public static Result handle(FullHttpRequest req) throws UnsupportedEncodingException {
        if (req.uri().startsWith(URI_FILTER)) {
            return handleFilterv2(req.uri());
        } else if (req.uri().startsWith(URI_NEW)) {
            if (req.uri().substring(14).charAt(0) != '?') {
                return NOT_FOUND;
            } else {
                return handleNew(req);
            }
        } else if (req.uri().startsWith(URI_LIKES)) {
            if (req.uri().substring(16).charAt(0) != '?') {
                return NOT_FOUND;
            } else {
                return handleLikes(req);
            }
        } else if (req.uri().startsWith(URI_GROUP)) {
            return handleGroup(req);
        } else if (req.uri().contains(URI_SUGGEST)) {
            return handleSuggest(req);
        } else if (req.uri().contains(URI_RECOMENDED)) {
            return handleRecomended(req);
        } else {
            return handleUpdate(req);
        }
    }

    private static Result handleUpdate(FullHttpRequest req) {
        lock.writeLock().lock();
        try {
            String curId = req.uri().substring(10, req.uri().lastIndexOf("/?"));
            if (!Character.isDigit(curId.charAt(0))) {
                return NOT_FOUND;
            }
            if (Repository.ids.containsKey(Integer.parseInt(curId))) {
                Account account = Utils.anyToAccount(JsonIterator.deserialize(req.content().toString(StandardCharsets.UTF_8)));
                if (account == null) {
                    return BAD_REQUEST;
                }
                if (account.getSex() != null) {
                    if (!account.getSex().equals(F)
                            && !account.getSex().equals(M)) {
                        return BAD_REQUEST;
                    }
                }
                if (account.getStatus() != null) {
                    if (!account.getStatus().equals(STATUS1)
                            && !account.getStatus().equals(STATUS2)
                            && !account.getStatus().equals(STATUS3)) {
                        return BAD_REQUEST;
                    }
                }
                if (account.getEmail() != null) {
                    if (!account.getEmail().contains("@")) {
                        return BAD_REQUEST;
                    }
                    if (Repository.emails.containsKey(account.getEmail())) {
                        return BAD_REQUEST;
                    }
                }
                Account accountData = Repository.ids.get(Integer.parseInt(curId));
                if (accountData != null && !accountData.equals(Repository.PRESENT_AC)) {
                    /*if (account.getLikes() != null) {
                        accountData.setLikes(account.getLikes());
                    }*/
                    if (account.getEmail() != null) {
                        Repository.emails.remove(accountData.getEmail());
                        Repository.emails.put(account.getEmail(), Repository.PRESENT);
                        accountData.setEmail(account.getEmail());
                    }
                    if (account.getSex() != null) {
                        if (accountData.getSex().equals(F) && account.getSex().equals(M)) {
                            Repository.list_f.remove(accountData);
                            Repository.list_m.add(accountData);
                            if (accountData.getStatus().equals(STATUS1)) {
                                Repository.list_status_1_f.remove(accountData);
                                Repository.list_status_1_m.add(accountData);
                            }
                            if (accountData.getStatus().equals(STATUS2)) {
                                Repository.list_status_2_f.remove(accountData);
                                Repository.list_status_2_m.add(accountData);
                            }
                            if (accountData.getStatus().equals(STATUS3)) {
                                Repository.list_status_3_f.remove(accountData);
                                Repository.list_status_3_m.add(accountData);
                            }
                        }
                        if (accountData.getSex().equals(M) && account.getSex().equals(F)) {
                            Repository.list_m.remove(accountData);
                            Repository.list_f.add(accountData);

                            if (accountData.getStatus().equals(STATUS1)) {
                                Repository.list_status_1_m.remove(accountData);
                                Repository.list_status_1_f.add(accountData);
                            }
                            if (accountData.getStatus().equals(STATUS2)) {
                                Repository.list_status_2_m.remove(accountData);
                                Repository.list_status_2_f.add(accountData);
                            }
                            if (accountData.getStatus().equals(STATUS3)) {
                                Repository.list_status_3_m.remove(accountData);
                                Repository.list_status_3_f.add(accountData);
                            }
                        }
                        accountData.setSex(account.getSex());
                    }
                    if (account.getFname() != null) {
                        accountData.setFname(account.getFname());
                    }
                    if (account.getInterests() != null) {
                        accountData.setInterests(account.getInterests());
                    }
                    if (account.getStatus() != null) {
                        if (accountData.getStatus().equals(STATUS1)) {
                            if (accountData.getSex().equals(F)) {
                                Repository.list_status_1_f.remove(accountData);
                            } else {
                                Repository.list_status_1_m.remove(accountData);
                            }
                        } else if (accountData.getStatus().equals(STATUS2)) {
                            if (accountData.getSex().equals(F)) {
                                Repository.list_status_2_f.remove(accountData);
                            } else {
                                Repository.list_status_2_m.remove(accountData);
                            }
                        } else {
                            if (accountData.getSex().equals(F)) {
                                Repository.list_status_3_f.remove(accountData);
                            } else {
                                Repository.list_status_3_m.remove(accountData);
                            }
                        }

                        accountData.setStatus(account.getStatus());
                        if (accountData.getStatus().equals(STATUS1)) {
                            if (accountData.getSex().equals(F)) {
                                Repository.list_status_1_f.add(accountData);
                            } else {
                                Repository.list_status_1_m.add(accountData);
                            }
                        } else if (accountData.getStatus().equals(STATUS2)) {
                            if (accountData.getSex().equals(F)) {
                                Repository.list_status_2_f.add(accountData);
                            } else {
                                Repository.list_status_2_m.add(accountData);
                            }
                        } else {
                            if (accountData.getSex().equals(F)) {
                                Repository.list_status_3_f.add(accountData);
                            } else {
                                Repository.list_status_3_m.add(accountData);
                            }
                        }

                    }
                    if (account.getPremium() != null) {
                        accountData.setPremium(account.getPremium());
                    }
                    if (account.getPhone() != null) {
                        accountData.setPhone(account.getPhone());
                    }
                    if (account.getBirth() != null) {
                        accountData.setBirth(account.getBirth());
                    }
                    if (account.getCity() != null) {
                        accountData.setCity(account.getCity());
                    }
                    if (account.getCountry() != null) {
                        accountData.setCountry(account.getCountry());
                    }
                    if (account.getSname() != null) {
                        accountData.setSname(account.getSname());
                    }
                } else {
                    return ACCEPTED;
                }
                return ACCEPTED;
            } else {
                return NOT_FOUND;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static Result handleRecomended(FullHttpRequest req) {
        lock.readLock().lock();
        try {

            String replAcc = req.uri().substring(10);
            String id = replAcc.substring(0, replAcc.indexOf("/"));
            if (!Character.isDigit(id.charAt(0))) {
                return NOT_FOUND;
            }

            Account accountData = Repository.ids.get(Integer.parseInt(id));
            if (accountData == null) {
                return NOT_FOUND;
            } else {
                String[] params = Utils.tokenize(req.uri().substring(req.uri().indexOf(URI_RECOMENDED) + 12), '&');
                int limit = 0;
                String country = "";
                String city = "";
                for (String param : params) {
                    if (param.startsWith(LIMIT)) {
                        try {
                            limit = Integer.parseInt(getValue(param));
                            if (limit <= 0) {
                                return BAD_REQUEST;
                            }
                        } catch (Exception e) {
                            return BAD_REQUEST;
                        }
                    }
                    if (param.startsWith(COUNTRY)) {
                        country = getValue(param);
                        if (country.isEmpty()) {
                            return BAD_REQUEST;
                        }
                    }
                    if (param.startsWith(CITY)) {
                        city = getValue(param);
                        if (city.isEmpty()) {
                            return BAD_REQUEST;
                        }
                    }
                }

                if (!accountData.equals(Repository.PRESENT_AC)) {
                    TreeSet<AccountC> compat = new TreeSet<>(Comparator.comparing(AccountC::getC).reversed());
                    Iterator<Account> iter;
                    if (accountData.getSex().equals(F)) {
                        iter = Repository.list_m.descendingIterator();
                    } else {
                        iter = Repository.list_f.descendingIterator();
                    }
                    while (iter.hasNext()) {
                        Account account1 = iter.next();

                        if (!account1.getId().equals(accountData.getId())) {
                            if (city.isEmpty() || city.equals(account1.getCity())) {
                                if (country.isEmpty() || country.equals(account1.getCountry())) {
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
                    return new Result(Utils.accountToString2(compat, limit).getBytes(StandardCharsets.UTF_8), HttpResponseStatus.OK);
                }
            }
        } catch (Exception e) {
            return BAD_REQUEST;
        } finally {
            lock.readLock().unlock();
        }
        return BAD_REQUEST;
    }

    private static Integer getCompatibility(Account accountData, Account account1) {
        int compt = 0;
        if (account1.getStatus().equals(STATUS1)) {
            compt = compt + 50_000_0;
        } else if (account1.getStatus().equals(STATUS2)){
            compt = compt + 20_000_0;
        } else {
            compt = compt + 1_000_0;
        }
        boolean notComp = true;
        if (accountData.getInterests() != null) {
            for (String interest : accountData.getInterests()) {
                if (account1.getInterests() != null) {
                    if (account1.getInterests().contains(interest)) {
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
        if (account1.getPremium() != null) {
            if (currentTimeStamp2 < account1.getPremium().getFinish()
                    && currentTimeStamp2 > account1.getPremium().getStart()) {
                compt = compt + 60_000_0;
            }
        }
        int daysAcc1 = (int) (((currentTimeStamp2 - account1.getBirth())) / (60*60*24));
        int daysAccData = (int) (((currentTimeStamp2 - accountData.getBirth())) / (60*60*24));

        Integer days = 36500 - Math.abs(daysAccData - daysAcc1);
        compt = compt + days;
        return compt * 100;
    }

    private static Result handleSuggest(FullHttpRequest req) {
        lock.readLock().lock();
        try {
            String replAcc = req.uri().substring(10);
            String id = replAcc.substring(0, replAcc.indexOf("/"));
            if (!Character.isDigit(id.charAt(0))) {
                return NOT_FOUND;
            }

            Account accountData = Repository.ids.get(Integer.parseInt(id));
            if (accountData == null) {
                return NOT_FOUND;
            } else {
                String[] params = Utils.tokenize(req.uri().substring(req.uri().indexOf(URI_SUGGEST) + 10), '&');
                for (String param : params) {
                    if (param.startsWith(LIMIT)) {
                        try {
                            Integer limit = Integer.parseInt(getValue(param));
                            if (limit <= 0) {
                                return BAD_REQUEST;
                            }
                        } catch (Exception e) {
                            return BAD_REQUEST;
                        }
                    }
                    if (param.startsWith(COUNTRY)) {
                        if (getValue(param).isEmpty()) {
                            return BAD_REQUEST;
                        }
                    }
                    if (param.startsWith(CITY)) {
                        if (getValue(param).isEmpty()) {
                            return BAD_REQUEST;
                        }
                    }
                }

                return OK_EMPTY_ACCOUNTS;
            }
        } catch (Exception e) {
            return BAD_REQUEST;
        } finally {
            lock.readLock().unlock();
        }
    }

    /*private static AccountSim getSimilarity(Account accountData, Account account1) {
        if (accountData.getLikes() == null || account1.getLikes() == null) {
            return null;
        } else {
            int sim = 0;
            for (Like like : accountData.getLikes()) {
                for (Like account1Like : account1.getLikes()) {
                    if (like.getId() == account1Like.getId()) {
                        if (like.getTs() == account1Like.getTs()) {
                            sim = sim + 1;
                        } else {
                            sim = sim + Math.abs(like.getTs() - account1Like.getTs());
                        }
                    }
                }
            }
            if (sim > 0) {
                List<Integer> ids = new LinkedList<>();
                for (Like account1Like : account1.getLikes()) {
                    for (Like like : accountData.getLikes()) {
                        if (like.getId() != account1Like.getId()) {
                            ids.add(account1Like.getId());
                        }
                    }
                }
                return new AccountSim(sim,ids);
            } else {
                return null;
            }
        }
    }*/

    private static Result handleGroup(FullHttpRequest req)  {
        /*String[] t = Utils.tokenize(req.uri().substring(17),'&');
        for (String param : t) {
            if (param.startsWith(KEYS)) {
                String value = getValue(param);
                String[] tokens = Utils.tokenize(value,delim);
                for (String token : tokens) {
                    if (!SEX.equals(token)
                            && !STATUS.equals(token)
                            && !INTERESTS.equals(token)
                            && !COUNTRY.equals(token)
                            && !CITY.equals(token)) {
                        return BAD_REQUEST;
                    }
                }
                return NOT_FOUND;
            }
        }
        return NOT_FOUND;*/
        return BAD_REQUEST;
    }

    private static Result handleLikes(FullHttpRequest req) {
        lock.writeLock().lock();
        try {
            int countCur = count.incrementAndGet();
            if (countCur == 200) {
                for (Account account : list) {
                    account.setLikesArr(null);
                }
                System.gc();
                Server.printCurrentMemoryUsage();
                System.out.println("GC run (perhaps) 22700");
            }
            try {
                LikesRequest likesReq = JsonIterator.deserialize(req.content().toString(StandardCharsets.UTF_8), LikesRequest.class);
                for (LikeRequest like : likesReq.getLikes()) {
                    if (like.getTs() == null) {
                        return BAD_REQUEST;
                    }
                    if (like.getLiker() == null
                            || !Repository.ids.containsKey(like.getLiker())) {
                        return BAD_REQUEST;
                    }
                    if (like.getLikee() == null
                            || !Repository.ids.containsKey(like.getLikee())) {
                        return BAD_REQUEST;
                    }
                }
                /*for (LikeRequest like : likesReq.getLikes()) {
                    Account accountData = Repository.ids.get(like.getLiker());
                    if (accountData != null && !accountData.equals(Repository.PRESENT_AC)) {
                        if (accountData.getLikesArr() == null) {
                            List<Integer> likes = new LinkedList<>();
                            likes.add(like.getLikee());
                            accountData.setLikesArr(likes);
                        } else {
                            accountData.getLikesArr().add(like.getLikee());
                        }
                    }
                }*/

                return ACCEPTED;
            } catch (Exception e) {
                return BAD_REQUEST;
            }
        }finally {
            lock.writeLock().unlock();
        }
    }

    private static Result handleNew(FullHttpRequest req) {
        lock.writeLock().lock();
        try {
            Account account = Utils.anyToAccount(JsonIterator.deserialize(req.content().toString(StandardCharsets.UTF_8)));
            if (account == null) {
                return BAD_REQUEST;
            }
            if (account.getId() == null) {
                return BAD_REQUEST;
            }
            if (Repository.ids.containsKey(account.getId())) {
                return BAD_REQUEST;
            }
            if (account.getSex() != null) {
                if (!account.getSex().equals(F)
                        && !account.getSex().equals(M)) {
                    return BAD_REQUEST;
                }
            } else {
                return BAD_REQUEST;
            }
            if (account.getStatus() != null) {
                if (!account.getStatus().equals(STATUS1)
                        && !account.getStatus().equals(STATUS2)
                        && !account.getStatus().equals(STATUS3)) {
                    return BAD_REQUEST;
                }
            } else {
                return BAD_REQUEST;
            }
            if (account.getEmail() != null) {
                if (!account.getEmail().contains("@")) {
                    return BAD_REQUEST;
                }
                if (Repository.emails.containsKey(account.getEmail())) {
                    return BAD_REQUEST;
                } else {
                    Repository.list.add(account);
                    if (account.getSex().equals(Service.M)) {
                        Repository.list_m.add(account);
                    } else {
                        Repository.list_f.add(account);
                    }
                    if (account.getStatus().equals(Service.STATUS1)) {
                        Repository.list_status_1.add(account);
                    } else if (account.getStatus().equals(Service.STATUS2)) {
                        Repository.list_status_2.add(account);
                    } else {
                        Repository.list_status_3.add(account);
                    }

                    if (account.getSex().equals(Service.M)
                            && account.getStatus().equals(Service.STATUS1)) {
                        Repository.list_status_1_m.add(account);
                    }

                    if (account.getSex().equals(Service.M)
                            && account.getStatus().equals(Service.STATUS2)) {
                        Repository.list_status_2_m.add(account);
                    }

                    if (account.getSex().equals(Service.M)
                            && account.getStatus().equals(Service.STATUS3)) {
                        Repository.list_status_3_m.add(account);
                    }

                    if (account.getSex().equals(Service.F)
                            && account.getStatus().equals(Service.STATUS1)) {
                        Repository.list_status_1_f.add(account);
                    }

                    if (account.getSex().equals(Service.F)
                            && account.getStatus().equals(Service.STATUS2)) {
                        Repository.list_status_2_f.add(account);
                    }

                    if (account.getSex().equals(Service.F)
                            && account.getStatus().equals(Service.STATUS3)) {
                        Repository.list_status_3_f.add(account);
                    }
                    Repository.ids.put(account.getId(), account);
                    Repository.emails.put(account.getEmail(), Repository.PRESENT);
                    return CREATED;
                }
            }
            return CREATED;
        } finally {
            lock.writeLock().unlock();
        }
    }


    private static boolean fillCacheAndvalidate(String param, Map<String, String> predicateCache) {
        String predicate = getPredicate(param);
        if (param.startsWith(SEX)) {
            if (!predicate.equals(EQ_PR)) {
                return false;
            }
        }
        if (param.startsWith(EMAIL)) {
            if (!predicate.equals(DOMAIN_PR)
                    && !predicate.equals(LT_PR)
                    && !predicate.equals(GT_PR)) {
                return false;
            }
        }
        if (param.startsWith(STATUS)) {
            if (!predicate.equals(EQ_PR)
                    && !predicate.equals(NEQ_PR)) {
                return false;
            }
        }
        if (param.startsWith(FNAME)) {
            if (!predicate.equals(EQ_PR)
                    && !predicate.equals(ANY_PR)
                    && !predicate.equals(NULL_PR)) {
                return false;
            }
        }
        if (param.startsWith(SNAME)) {
            if (!predicate.equals(EQ_PR)
                    && !predicate.equals(STARTS_PR)
                    && !predicate.equals(NULL_PR)) {
                return false;
            }
        }
        if (param.startsWith(PHONE)) {
            if (!predicate.equals(NULL_PR)
                    && !predicate.equals(CODE_PR)) {
                return false;
            }
        }
        if (param.startsWith(COUNTRY)) {
            if (!predicate.equals(NULL_PR)
                    && !predicate.equals(EQ_PR)) {
                return false;
            }
        }
        if (param.startsWith(CITY)) {
            if (!predicate.equals(EQ_PR)
                    && !predicate.equals(ANY_PR)
                    && !predicate.equals(NULL_PR)) {
                return false;
            }
        }
        if (param.startsWith(BIRTH)) {
            if (!predicate.equals(YEAR_PR)
                    && !predicate.equals(LT_PR)
                    && !predicate.equals(GT_PR)) {
                return false;
            }
        }
        if (param.startsWith(INTERESTS)) {
            if (!predicate.equals(CONTAINS_PR)
                    && !predicate.equals(ANY_PR)) {
                return false;
            }
        }
        if (param.startsWith(LIKES)) {
            if (!predicate.equals(CONTAINS_PR)) {
                return false;
            }
        }
        if (param.startsWith(PREMIUM)) {
            if (!predicate.equals(NULL_PR)
                    && !predicate.equals(NOW_PR)) {
                return false;
            }
        }
        predicateCache.put(param,predicate);
        return true;

    }

    private static boolean compareArrays(String[] params, List<String> enableProp) {
        if (params.length != enableProp.size()) {
            return false;
        }
        for (String key : enableProp) {
            boolean isValid = false;
            for (String param : params) {
                if (param.startsWith(key)) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid) {
                return false;
            }
        }
        return true;
    }

    private static String getValue(String param) throws UnsupportedEncodingException {
        if (param.startsWith(COUNTRY)
                || param.startsWith(CITY)
                || param.startsWith(INTERESTS)
                || param.startsWith(FNAME)
                || param.startsWith(SNAME)
                || param.startsWith(STATUS)
                || param.startsWith(LIKES)
        ) {
            return URLDecoder.decode(param.substring(param.indexOf("=") + 1), utf8);
        } else {
            return param.substring(param.indexOf("=") + 1);
        }
    }

    private static String getPredicate(String param) {
        return param.substring(param.indexOf("_") + 1,param.indexOf("="));
    }

    public static Result handleFilterv2(String uri) throws UnsupportedEncodingException {
        lock.readLock().lock();
        try {
            String[] params = Utils.tokenize(uri.substring(18), '&');
            if (count.get() > 200) {
                for (String param : params) {
                    if (param.startsWith(LIKES)) {
                        return BAD_REQUEST;
                    }
                }
            }

            Map<String, String> valueCache = new TreeMap<>();
            Map<String, String> predicateCache = new TreeMap<>();
            Map<String, Object> finalFieldSet = null;

            String sex = null;
            String status = null;
            Calendar calendar = null;
            Integer year = null;
            String[] cityArr = null;
            String[] fnameArr = null;
            String[] interArr = null;
            Integer [] likesArr = null;
            for (String param : params) {
                if (!fillCacheAndvalidate(param, predicateCache)) {
                    return BAD_REQUEST;
                } else {
                    fillValueCacheValue(param, valueCache);
                    if (param.startsWith(STATUS)) {
                        String predicate = predicateCache.get(param);
                        if (predicate.equals(EQ_PR)) {
                            status = valueCache.get(param);
                        }
                    }
                    if (param.startsWith(SEX)) {
                        sex = valueCache.get(param);
                    }
                    if (param.startsWith(BIRTH)) {
                        String predicate = predicateCache.get(param);
                        if (predicate.equals(YEAR_PR)) {
                            calendar = new GregorianCalendar();
                            year = Integer.parseInt(valueCache.get(param));
                        }
                    }
                    if (param.startsWith(CITY)) {
                        String predicate = predicateCache.get(param);
                        if (predicate.equals(ANY_PR)) {
                            cityArr = Utils.tokenize(valueCache.get(param), delim);
                        }
                    }
                    if (param.startsWith(FNAME)) {
                        String predicate = predicateCache.get(param);
                        if (predicate.equals(ANY_PR)) {
                            fnameArr = Utils.tokenize(valueCache.get(param), delim);
                        }
                    }
                    if (param.startsWith(INTERESTS)) {
                        interArr = Utils.tokenize(valueCache.get(param), delim);
                    }
                    if (param.startsWith(LIKES)) {
                        String [] likesArrStr = Utils.tokenize(valueCache.get(param), delim);
                        likesArr = new Integer[likesArrStr.length];
                        for (int i = 0; i < likesArrStr.length; i++) {
                            likesArr[i] = Integer.parseInt(likesArrStr[i]);
                        }
                    }
                }
            }
            int limit = 0;
            for (String param : params) {
                if (param.startsWith(LIMIT)) {
                    String limitStr = valueCache.get(param);
                    if (!Character.isDigit(limitStr.charAt(0))) {
                        return BAD_REQUEST;
                    } else {
                        limit = Integer.parseInt(limitStr);
                        break;
                    }
                }
            }

            TreeSet<Account> listForRearch = Repository.list;
            if (sex == null && Service.STATUS1.equals(status)) {
                listForRearch = Repository.list_status_1;
            }
            if (sex == null && Service.STATUS2.equals(status)) {
                listForRearch = Repository.list_status_2;
            }
            if (sex == null && Service.STATUS3.equals(status)) {
                listForRearch = Repository.list_status_3;
            }

            if (status == null && Service.F.equals(sex)) {
                listForRearch = Repository.list_f;
            }
            if (status == null && Service.M.equals(sex)) {
                listForRearch = Repository.list_m;
            }

            if (Service.STATUS1.equals(status) && Service.F.equals(sex)) {
               listForRearch = Repository.list_status_1_f;
            }

            if (Service.STATUS2.equals(status) && Service.F.equals(sex)) {
                listForRearch = Repository.list_status_2_f;
            }

            if (Service.STATUS3.equals(status) && Service.F.equals(sex)) {
                listForRearch = Repository.list_status_3_f;
            }

            if (Service.STATUS1.equals(status) && Service.M.equals(sex)) {
                listForRearch = Repository.list_status_1_m;
            }

            if (Service.STATUS2.equals(status) && Service.M.equals(sex)) {
                listForRearch = Repository.list_status_2_m;
            }

            if (Service.STATUS3.equals(status) && Service.M.equals(sex)) {
                listForRearch = Repository.list_status_3_m;
            }

            List<String> enableProp = new LinkedList<>();
            List<Account> accounts = new LinkedList<>();

            for(Account account : listForRearch) {
                if (accounts.size() == limit) {
                    break;
                }
                enableProp.clear();
                for (String param : params) {
                    //SEX ============================================
                    if (param.startsWith(SEX)) {
                        if (account.getSex().equals(valueCache.get(param))) {
                            enableProp.add(SEX);
                        } else {
                            break;
                        }
                    }
                    //SEX ============================================

                    //EMAIL ============================================
                    if (param.startsWith(EMAIL)) {
                        String predicate = predicateCache.get(param);
                        if (predicate.equals(DOMAIN_PR)) {
                            if (account.getEmail().contains(valueCache.get(param))) {
                                enableProp.add(EMAIL);
                            } else {
                                break;
                            }
                        } else if (predicate.equals(LT_PR)) {
                            if (account.getEmail().compareTo(valueCache.get(param)) < 0) {
                                enableProp.add(EMAIL);
                            } else {
                                break;
                            }
                        } else if (predicate.equals(GT_PR)) {
                            if (account.getEmail().compareTo(valueCache.get(param)) > 0) {
                                enableProp.add(EMAIL);
                            } else {
                                break;
                            }
                        }
                    }
                    //EMAIL ============================================

                    //STATUS ============================================
                    if (param.startsWith(STATUS)) {
                        String predicate = predicateCache.get(param);
                        if (predicate.equals(EQ_PR)) {
                            if (account.getStatus().equals(valueCache.get(param))) {
                                enableProp.add(STATUS);
                            } else {
                                break;
                            }
                        } else if (predicate.equals(NEQ_PR)) {
                            if (!account.getStatus().equals(valueCache.get(param))) {
                                enableProp.add(STATUS);
                            } else {
                                break;
                            }
                        }
                    }
                    //STATUS ============================================


                    //SNAME ============================================
                    if (param.startsWith(SNAME)) {
                        String predicate = predicateCache.get(param);

                        if (predicate.equals(EQ_PR)) {
                            if (valueCache.get(param).equals(account.getSname())) {
                                enableProp.add(SNAME);
                            } else {
                                break;
                            }
                        } else if (predicate.equals(NULL_PR)) {
                            String value = valueCache.get(param);
                            if (value.equals(NULL_PR_VAL_ONE)) {
                                if (account.getSname() == null) {
                                    enableProp.add(SNAME);
                                } else {
                                    break;
                                }
                            } else {
                                if (account.getSname() != null) {
                                    enableProp.add(SNAME);
                                } else {
                                    break;
                                }
                            }
                        } else if (predicate.equals(STARTS_PR)) {
                            if (account.getSname() != null)
                                if (account.getSname().startsWith(valueCache.get(param))) {
                                    enableProp.add(SNAME);
                                } else {
                                    break;
                                }
                        }
                    }
                    //SNAME ============================================

                    //PHONE ============================================
                    if (param.startsWith(PHONE)) {
                        String predicate = predicateCache.get(param);

                        if (predicate.equals(CODE_PR)) {
                            if (account.getPhone() != null) {
                                if (account.getPhone()
                                        .substring(account.getPhone().indexOf("(") + 1
                                                , account.getPhone().indexOf(")"))
                                        .equals(valueCache.get(param))) {
                                    enableProp.add(PHONE);
                                } else {
                                    break;
                                }
                            }
                        } else if (predicate.equals(NULL_PR)) {
                            String value = valueCache.get(param);
                            if (value.equals(NULL_PR_VAL_ONE)) {
                                if (account.getPhone() == null) {
                                    enableProp.add(PHONE);
                                } else {
                                    break;
                                }
                            } else {
                                if (account.getPhone() != null) {
                                    enableProp.add(PHONE);
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                    //PHONE ============================================


                    //COUNTRY ============================================
                    if (param.startsWith(COUNTRY)) {
                        String predicate = predicateCache.get(param);

                        if (predicate.equals(EQ_PR)) {
                            if (valueCache.get(param).equals(account.getCountry())) {
                                enableProp.add(COUNTRY);
                            } else {
                                break;
                            }
                        } else if (predicate.equals(NULL_PR)) {
                            String value = valueCache.get(param);
                            if (value.equals(NULL_PR_VAL_ONE)) {
                                if (account.getCountry() == null) {
                                    enableProp.add(COUNTRY);
                                } else {
                                    break;
                                }
                            } else {
                                if (account.getCountry() != null) {
                                    enableProp.add(COUNTRY);
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                    //COUNTRY ============================================


                    //PREMIUM ============================================
                    if (param.startsWith(PREMIUM)) {
                        String predicate = predicateCache.get(param);
                        if (predicate.equals(NOW_PR)) {
                            if (account.getPremium() != null) {
                                if (currentTimeStamp2 < account.getPremium().getFinish()
                                        && currentTimeStamp2 > account.getPremium().getStart()) {
                                    enableProp.add(PREMIUM);
                                } else {
                                    break;
                                }
                            }
                        } else if (predicate.equals(NULL_PR)) {
                            String value = valueCache.get(param);
                            if (value.equals(NULL_PR_VAL_ONE)) {
                                if (account.getPremium() == null) {
                                    enableProp.add(PREMIUM);
                                } else {
                                    break;
                                }
                            } else {
                                if (account.getPremium() != null) {
                                    enableProp.add(PREMIUM);
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                    //PREMIUM ============================================
                    //BIRTH ============================================
                    if (param.startsWith(BIRTH)) {
                        String predicate = predicateCache.get(param);
                        if (predicate.equals(YEAR_PR)) {
                            calendar.setTimeInMillis(account.getBirth().longValue() * 1000);
                            if (year == calendar.get(Calendar.YEAR)) {
                                enableProp.add(BIRTH);
                            } else {
                                break;
                            }
                        } else if (predicate.equals(LT_PR)) {
                            if (account.getBirth().compareTo(Integer.parseInt(valueCache.get(param))) < 0) {
                                enableProp.add(BIRTH);
                            } else {
                                break;
                            }
                        } else if (predicate.equals(GT_PR)) {
                            if (account.getBirth().compareTo(Integer.parseInt(valueCache.get(param))) > 0) {
                                enableProp.add(BIRTH);
                            } else {
                                break;
                            }
                        }
                    }
                    //BIRTH ============================================

                    //CITY ============================================
                    if (param.startsWith(CITY)) {
                        String predicate = predicateCache.get(param);

                        if (predicate.equals(EQ_PR)) {
                            if (getValue(param).equals(account.getCity())) {
                                enableProp.add(CITY);
                            } else {
                                break;
                            }
                        } else if (predicate.equals(ANY_PR)) {
                            for (String value : cityArr) {
                                if (value.equals(account.getCity())) {
                                    enableProp.add(CITY);
                                    break;
                                }
                            }
                        } else if (predicate.equals(NULL_PR)) {
                            String value = valueCache.get(param);
                            if (value.equals(NULL_PR_VAL_ONE)) {
                                if (account.getCity() == null) {
                                    enableProp.add(CITY);
                                } else {
                                    break;
                                }
                            } else {
                                if (account.getCity() != null) {
                                    enableProp.add(CITY);
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                    //CITY ============================================


                    //FNAME ============================================
                    if (param.startsWith(FNAME)) {
                        String predicate = predicateCache.get(param);

                        if (predicate.equals(EQ_PR)) {
                            if (valueCache.get(param).equals(account.getFname())) {
                                enableProp.add(FNAME);
                            } else {
                                break;
                            }
                        } else if (predicate.equals(ANY_PR)) {
                            for (String value : fnameArr) {
                                if (value.equals(account.getFname())) {
                                    enableProp.add(FNAME);
                                    break;
                                }
                            }
                        } else if (predicate.equals(NULL_PR)) {
                            String value = valueCache.get(param);
                            if (value.equals(NULL_PR_VAL_ONE)) {
                                if (account.getFname() == null) {
                                    enableProp.add(FNAME);
                                } else {
                                    break;
                                }
                            } else {
                                if (account.getFname() != null) {
                                    enableProp.add(FNAME);
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                    //FNAME ============================================

                    //INTERESTS ============================================
                    if (param.startsWith(INTERESTS)) {
                        String predicate = predicateCache.get(param);
                        if (account.getInterests() != null) {
                            if (predicate.equals(ANY_PR)) {
                                for (String value : interArr) {
                                    if (account.getInterests().contains(value)) {
                                        enableProp.add(INTERESTS);
                                        break;
                                    }
                                }
                            } else if (predicate.equals(CONTAINS_PR)) {
                                if (interArr.length <= account.getInterests().size()) {
                                    enableProp.add(INTERESTS);
                                    for (String value : interArr) {
                                        if (!account.getInterests().contains(value)) {
                                            enableProp.remove(INTERESTS);
                                            break;
                                        }
                                    }
                                } else {
                                    break;
                                }
                            }

                        }
                    }
                    //INTERESTS ============================================


                    //LIKES ============================================
                    if (param.startsWith(LIKES)) {
                        if (account.getLikesArr() != null) {
                            if (likesArr.length <= account.getLikesArr().size()) {
                                enableProp.add(LIKES);
                                for (Integer value : likesArr) {
                                    if (!account.getLikesArr().contains(value)) {
                                        enableProp.remove(LIKES);
                                        break;
                                    }
                                }
                            } else {
                                break;
                            }
                        }
                    }
                    //LIKES ============================================

                }
                enableProp.add(QUERY_ID);
                enableProp.add(LIMIT);
                if (compareArrays(params, enableProp)) {
                    if (finalFieldSet == null) {
                        finalFieldSet = new TreeMap<>();
                        for (String key : enableProp) {
                            finalFieldSet.put(key, Repository.PRESENT);
                        }
                    }
                    accounts.add(account);
                }
            }
            return new Result(Utils.accountToString(accounts, finalFieldSet).getBytes(StandardCharsets.UTF_8), HttpResponseStatus.OK);
        } finally {
            lock.readLock().unlock();
        }
    }

    private static void fillValueCacheValue(String param, Map<String, String> valueCache) throws UnsupportedEncodingException {
        valueCache.put(param,getValue(param));
    }

}
