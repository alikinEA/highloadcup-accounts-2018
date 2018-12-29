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

import static app.Repository.currentTimeStamp2;

/**
 * Created by Alikin E.A. on 15.12.18.
 */
public class Service {

    private static final byte[] EMPTY = "{}".getBytes();
    private static final byte[] EMPTY_ACCOUNTS = "{\"accounts\":[]}".getBytes();
    private static final Result OK_EMPTY_ACCOUNTS = new Result(EMPTY_ACCOUNTS,HttpResponseStatus.OK);
    private static final Result ACCEPTED = new Result(EMPTY,HttpResponseStatus.ACCEPTED);
    private static final Result CREATED = new Result(EMPTY,HttpResponseStatus.CREATED);
    private static final Result BAD_REQUEST = new Result(EMPTY,HttpResponseStatus.BAD_REQUEST);
    private static final Result NOT_FOUND = new Result(EMPTY,HttpResponseStatus.NOT_FOUND);


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
    private static final String ACCOUNTS =  "/accounts/";

    public static final String F =  "f";
    public static final String M =  "m";

    public static final String STATUS1 = "свободны";
    public static final String STATUS2 = "всё сложно";
    public static final String STATUS3 = "заняты";

    private static final String utf8 = "UTF-8";
    private static final String delim = ",";

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
            String curId = req.uri().substring(req.uri().indexOf(ACCOUNTS) + 10, req.uri().lastIndexOf("/?"));
            if (!Character.isDigit(curId.charAt(0))) {
                return NOT_FOUND;
            }
            if (Repository.ids.containsKey(curId)) {
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
                    } else {
                        Account accountData = Repository.ids.get(curId);
                        if (accountData != null) {
                            if (account.getLikesArr() != null) {
                                accountData.setLikesArr(account.getLikesArr());
                            }
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
                                if (accountData.getStatus().equals(STATUS1)){
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
                                if (accountData.getStatus().equals(STATUS1)){
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
                        }
                        return ACCEPTED;
                    }
                }
            } else {
                return NOT_FOUND;
            }
            return ACCEPTED;
        }finally {
            lock.writeLock().unlock();
        }
    }

    private static Result handleRecomended(FullHttpRequest req) {
        lock.readLock().lock();
        try {

            String replAcc = req.uri().substring(10);
            String id = replAcc.substring(0, replAcc.indexOf("/"));

            TreeSet<AccountC> compat = new TreeSet<>(Comparator.comparing(AccountC::getC).reversed());
            if (!Repository.ids.containsKey(id)) {
                return NOT_FOUND;
            } else {
                List<String> params = getTokens(req.uri().substring(req.uri().indexOf(URI_RECOMENDED) + 12), "&");
                int limit = 0;
                String country = "";
                String city = "";
                for (String param : params) {
                    if (param.startsWith(LIMIT)) {
                        try {
                            limit = Integer.parseInt(getValue(param));
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


                Account accountData = Repository.ids.get(id);
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
                return new Result(Utils.accountToString2(compat,limit).getBytes(utf8), HttpResponseStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
        return OK_EMPTY_ACCOUNTS;
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
        return NOT_FOUND;
    }

    private static Result handleGroup(FullHttpRequest req) throws UnsupportedEncodingException {
        StringTokenizer t = new StringTokenizer(req.uri().substring(17),"&");
        while(t.hasMoreTokens()) {
            String param = t.nextToken();

            if (param.startsWith(KEYS)) {
                String value = getValue(param);
                StringTokenizer t2 = new StringTokenizer(value,delim);
                while(t2.hasMoreTokens()) {
                    String keyValue = t2.nextToken();
                    if (!SEX.equals(keyValue)
                            && !STATUS.equals(keyValue)
                            && !INTERESTS.equals(keyValue)
                            && !COUNTRY.equals(keyValue)
                            && !CITY.equals(keyValue)) {
                        return BAD_REQUEST;
                    }
                }
            }
        }
        return NOT_FOUND;
    }

    private static Result handleLikes(FullHttpRequest req) {
        lock.writeLock().lock();
        try {
            int countCur = count.incrementAndGet();
            if (countCur == 200 || countCur == 2000) {
                System.gc();
                Server.printCurrentMemoryUsage();
                System.out.println("GC run (perhaps)");
            }
            try {
                LikesRequest likesReq = JsonIterator.deserialize(req.content().toString(StandardCharsets.UTF_8), LikesRequest.class);
                for (LikeRequest like : likesReq.getLikes()) {
                    if (like.getTs() == null) {
                        return BAD_REQUEST;
                    }
                    if (like.getLiker() == null
                            || !Repository.ids.containsKey(like.getLiker().toString())) {
                        return BAD_REQUEST;
                    }
                    if (like.getLikee() == null
                            || !Repository.ids.containsKey(like.getLikee().toString())) {
                        return BAD_REQUEST;
                    }
                }
                for (LikeRequest like : likesReq.getLikes()) {
                    Account accountData = Repository.ids.get(like.getLiker().toString());
                    if (accountData != null) {
                        if (accountData.getLikesArr() == null) {
                            List<Integer> likesArr = new ArrayList<>(20);
                            likesArr.add(like.getLikee());
                            accountData.setLikesArr(likesArr);
                        } else {
                            accountData.getLikesArr().add(like.getLikee());
                        }
                    }
                }

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
            if (Repository.ids.containsKey(account.getId().toString())) {
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
                    Repository.ids.put(account.getId().toString(), account);
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

    private static boolean compareArrays(List<String> params, Map<String,Object> enableProp) {
        if (params.size() != enableProp.size()) {
            return false;
        }
        for (String key : enableProp.keySet()) {
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
        return URLDecoder.decode(param.substring(param.indexOf("=") + 1), utf8);
    }

    private static String getPredicate(String param) {
        return param.substring(param.indexOf("_") + 1,param.indexOf("="));
    }

    public static Result handleFilterv2(String uri) throws UnsupportedEncodingException {
        lock.readLock().lock();
        try {
            List<String> params = getTokens(uri.substring(18), "&");
            int i = 0;
            int limit = 0;
            for (String param : params) {
                if (param.startsWith(LIMIT)) {
                    limit = Integer.parseInt(getValue(param));
                }
            }

            Map<String, String> valueCache = new HashMap<>(params.size());
            Map<String, String> predicateCache = new HashMap<>(params.size());
            Map<String, Object> finalFieldSet = null;

            String sex = null;
            String status = null;
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

            Map<String, Object> enableProp = new HashMap<>(valueCache.size());
            List<Account> accounts = new ArrayList<>(limit);

            Iterator<Account> listIterator = listForRearch.iterator();
            while (listIterator.hasNext()) {
                Account account = listIterator.next();
                if (i == limit) {
                    break;
                }
                enableProp.clear();
                for (String param : params) {
                    //SEX ============================================
                    if (param.startsWith(SEX)) {
                        if (account.getSex().equals(valueCache.get(param))) {
                            enableProp.put(SEX, Repository.PRESENT);
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
                                enableProp.put(EMAIL, Repository.PRESENT);
                            } else {
                                break;
                            }
                        } else if (predicate.equals(LT_PR)) {
                            if (account.getEmail().compareTo(valueCache.get(param)) < 0) {
                                enableProp.put(EMAIL, Repository.PRESENT);
                            } else {
                                break;
                            }
                        } else if (predicate.equals(GT_PR)) {
                            if (account.getEmail().compareTo(valueCache.get(param)) > 0) {
                                enableProp.put(EMAIL, Repository.PRESENT);
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
                                enableProp.put(STATUS, Repository.PRESENT);
                            } else {
                                break;
                            }
                        } else if (predicate.equals(NEQ_PR)) {
                            if (!account.getStatus().equals(valueCache.get(param))) {
                                enableProp.put(STATUS, Repository.PRESENT);
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
                                enableProp.put(SNAME, Repository.PRESENT);
                            } else {
                                break;
                            }
                        } else if (predicate.equals(NULL_PR)) {
                            String value = valueCache.get(param);
                            if (value.equals(NULL_PR_VAL_ONE)) {
                                if (account.getSname() == null) {
                                    enableProp.put(SNAME, Repository.PRESENT);
                                } else {
                                    break;
                                }
                            } else {
                                if (account.getSname() != null) {
                                    enableProp.put(SNAME, Repository.PRESENT);
                                } else {
                                    break;
                                }
                            }
                        } else if (predicate.equals(STARTS_PR)) {
                            if (account.getSname() != null)
                                if (account.getSname().startsWith(valueCache.get(param))) {
                                    enableProp.put(SNAME, Repository.PRESENT);
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
                                    enableProp.put(PHONE, Repository.PRESENT);
                                } else {
                                    break;
                                }
                            }
                        } else if (predicate.equals(NULL_PR)) {
                            String value = valueCache.get(param);
                            if (value.equals(NULL_PR_VAL_ONE)) {
                                if (account.getPhone() == null) {
                                    enableProp.put(PHONE, Repository.PRESENT);
                                } else {
                                    break;
                                }
                            } else {
                                if (account.getPhone() != null) {
                                    enableProp.put(PHONE, Repository.PRESENT);
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
                                enableProp.put(COUNTRY, Repository.PRESENT);
                            } else {
                                break;
                            }
                        } else if (predicate.equals(NULL_PR)) {
                            String value = valueCache.get(param);
                            if (value.equals(NULL_PR_VAL_ONE)) {
                                if (account.getCountry() == null) {
                                    enableProp.put(COUNTRY, Repository.PRESENT);
                                } else {
                                    break;
                                }
                            } else {
                                if (account.getCountry() != null) {
                                    enableProp.put(COUNTRY, Repository.PRESENT);
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
                                    enableProp.put(PREMIUM, Repository.PRESENT);
                                } else {
                                    break;
                                }
                            }
                        } else if (predicate.equals(NULL_PR)) {
                            String value = valueCache.get(param);
                            if (value.equals(NULL_PR_VAL_ONE)) {
                                if (account.getPremium() == null) {
                                    enableProp.put(PREMIUM, Repository.PRESENT);
                                } else {
                                    break;
                                }
                            } else {
                                if (account.getPremium() != null) {
                                    enableProp.put(PREMIUM, Repository.PRESENT);
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
                            Calendar calendar = new GregorianCalendar();
                            calendar.setTimeInMillis(account.getBirth().longValue() * 1000);
                            if (Integer.parseInt(getValue(param)) == calendar.get(Calendar.YEAR)) {
                                enableProp.put(BIRTH, Repository.PRESENT);
                            } else {
                                break;
                            }
                        } else if (predicate.equals(LT_PR)) {
                            if (account.getBirth().compareTo(Integer.parseInt(valueCache.get(param))) < 0) {
                                enableProp.put(BIRTH, Repository.PRESENT);
                            } else {
                                break;
                            }
                        } else if (predicate.equals(GT_PR)) {
                            if (account.getBirth().compareTo(Integer.parseInt(valueCache.get(param))) > 0) {
                                enableProp.put(BIRTH, Repository.PRESENT);
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
                                enableProp.put(CITY, Repository.PRESENT);
                            } else {
                                break;
                            }
                        } else if (predicate.equals(ANY_PR)) {
                            StringTokenizer t = new StringTokenizer(valueCache.get(param), delim);
                            while (t.hasMoreTokens()) {
                                if (t.nextToken().equals(account.getCity())) {
                                    enableProp.put(CITY, Repository.PRESENT);
                                    break;
                                }
                            }
                        } else if (predicate.equals(NULL_PR)) {
                            String value = valueCache.get(param);
                            if (value.equals(NULL_PR_VAL_ONE)) {
                                if (account.getCity() == null) {
                                    enableProp.put(CITY, Repository.PRESENT);
                                } else {
                                    break;
                                }
                            } else {
                                if (account.getCity() != null) {
                                    enableProp.put(CITY, Repository.PRESENT);
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
                                enableProp.put(FNAME, Repository.PRESENT);
                            } else {
                                break;
                            }
                        } else if (predicate.equals(ANY_PR)) {
                            StringTokenizer t = new StringTokenizer(valueCache.get(param), delim);
                            while (t.hasMoreTokens()) {
                                if (t.nextToken().equals(account.getFname())) {
                                    enableProp.put(FNAME, Repository.PRESENT);
                                    break;
                                }
                            }
                        } else if (predicate.equals(NULL_PR)) {
                            String value = valueCache.get(param);
                            if (value.equals(NULL_PR_VAL_ONE)) {
                                if (account.getFname() == null) {
                                    enableProp.put(FNAME, Repository.PRESENT);
                                } else {
                                    break;
                                }
                            } else {
                                if (account.getFname() != null) {
                                    enableProp.put(FNAME, Repository.PRESENT);
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
                                StringTokenizer t = new StringTokenizer(valueCache.get(param), delim);
                                while (t.hasMoreTokens()) {
                                    if (account.getInterests().contains(t.nextToken())) {
                                        enableProp.put(INTERESTS, Repository.PRESENT);
                                        break;
                                    }
                                }
                            } else if (predicate.equals(CONTAINS_PR)) {
                                List<String> splitedValue = getTokens(valueCache.get(param), delim);
                                if (splitedValue.size() <= account.getInterests().size()) {
                                    enableProp.put(INTERESTS, Repository.PRESENT);
                                    for (String value : splitedValue) {
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
                            List<String> splitedValue = getTokens(valueCache.get(param), delim);
                            if (splitedValue.size() <= account.getLikesArr().size()) {
                                enableProp.put(LIKES, Repository.PRESENT);
                                for (String value : splitedValue) {
                                    if (!account.getLikesArr().contains(Integer.parseInt(value))) {
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
                enableProp.put(QUERY_ID, Repository.PRESENT);
                enableProp.put(LIMIT, Repository.PRESENT);
                if (compareArrays(params, enableProp)) {
                    if (finalFieldSet == null) {
                        finalFieldSet = new HashMap<>(enableProp.size());
                        for (String key : enableProp.keySet()) {
                            finalFieldSet.put(key, Repository.PRESENT);
                        }
                    }
                    accounts.add(account);
                    i++;
                }
            }
            try {
                return new Result(Utils.accountToString(accounts, finalFieldSet).getBytes(utf8), HttpResponseStatus.OK);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return OK_EMPTY_ACCOUNTS;
        } finally {
            lock.readLock().unlock();
        }
    }

    private static void fillValueCacheValue(String param, Map<String, String> valueCache) throws UnsupportedEncodingException {
        valueCache.put(param,getValue(param));
    }

    public static List<String> getTokens(String str,String ch) {
        List<String> tokens = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(str, ch);
        while (tokenizer.hasMoreElements()) {
            tokens.add(tokenizer.nextToken());
        }
        return tokens;
    }

}
