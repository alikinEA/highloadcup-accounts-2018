package app;

import app.models.Account;
import app.server.ServerHandler;
import com.jsoniter.JsonIterator;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

import javax.management.BadAttributeValueExpException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static app.Repository.*;

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
    public static final String LIMIT = "limit";
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
    private static final char NULL_PR_VAL_ONE = '1';
    private static final String URI_SUGGEST = "/suggest";
    private static final String URI_RECOMENDED = "/recommend";

    public static final String F = "f";
    public static final String M = "m";

    public static final String STATUS1 = "свободны";
    public static final String STATUS2 = "всё сложно";
    public static final String STATUS3 = "заняты";

    private static final String utf8 = "UTF-8";
    private static final char delim = ',';

    private static final AtomicBoolean phase1 = new AtomicBoolean(true);
    //private static final AtomicInteger badIndexCount = new AtomicInteger(0);

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
        if (uri.charAt(10) == 'f' && uri.charAt(11) == 'i' && uri.charAt(12) == 'l') {
            if (uri.charAt(17) == '?') {
                return handleFilterv2(uri);
            } else {
                return ServerHandler.NOT_FOUND_R;
            }
        } else if (uri.charAt(10) == 'n' && uri.charAt(11) == 'e') {
            if (uri.charAt(14) != '?') {
                return ServerHandler.NOT_FOUND_R;
            } else {
                return handleNew(req);
            }
        } else if (uri.charAt(10) == 'l' && uri.charAt(11) == 'i') {
            if (uri.charAt(16) != '?') {
                return ServerHandler.NOT_FOUND_R;
            } else {
                return handleLikes(req);
            }
        } else if (uri.charAt(10) == 'g' && uri.charAt(11) == 'r') {
            if (uri.charAt(16) == '?') {
                return handleGroup(req);
            } else {
                return ServerHandler.NOT_FOUND_R;
            }
        } else if (uri.contains(URI_SUGGEST)) {
            int index = uri.indexOf(URI_SUGGEST) + 9;
            if (uri.charAt(index) == '?') {
                if (Character.isDigit(uri.charAt(10))) {
                    return handleSuggest(req);
                } else {
                    return ServerHandler.NOT_FOUND_R;
                }
            } else {
                return ServerHandler.NOT_FOUND_R;
            }
        } else if (uri.contains(URI_RECOMENDED)) {
            if (Character.isDigit(uri.charAt(10))) {
                return handleRecomended(req);
            } else {
                return ServerHandler.NOT_FOUND_R;
            }
        } else {
            if (Character.isDigit(uri.charAt(10))) {
                return handleUpdate(req);
            } else {
                return ServerHandler.NOT_FOUND_R;
            }
        }
    }

    private static DefaultFullHttpResponse handleUpdate(FullHttpRequest req) {
        if (!Character.isDigit(req.uri().charAt(10))) {
            return ServerHandler.NOT_FOUND_R;
        }
        String curId = req.uri().substring(10, req.uri().lastIndexOf("/?"));
        boolean isNull ;
        lock.readLock().lock();
        try {
            isNull = Repository.ids[Integer.parseInt(curId)] != null;
        } finally {
            lock.readLock().unlock();
        }

        if (isNull) {
            Account account = Utils.anyToAccount(JsonIterator.deserialize(req.content().toString(StandardCharsets.UTF_8)),true);
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
            lock.readLock().lock();
            Account accountData;
            try {
            if (account.getEmail() != null) {
                    if (!account.getEmail().contains("@")) {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                    if (Repository.emails.containsKey(account.getEmail())) {
                      return ServerHandler.BAD_REQUEST_R;
                    }
                }
                accountData = Repository.ids[Integer.parseInt(curId)];
            } finally {
                lock.readLock().unlock();
            }

            if (accountData != null && !accountData.equals(Repository.PRESENT_AC)) {
                lock.writeLock().lock();
                try {
                    if (account.getEmail() != null) {
                        Repository.emails.remove(accountData.getEmail());
                        Repository.emails.put(account.getEmail(), Repository.PRESENT);
                        accountData.setEmail(account.getEmail());

                        String email = accountData.getEmail();
                        String domain = email.substring(email.indexOf("@") + 1).intern();
                        TreeSet<Account> domainIndex = email_domain.get(domain);
                        if (domainIndex == null) {
                            domainIndex = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                            domainIndex.add(accountData);
                            email_domain.put(domain,domainIndex);
                        } else {
                            domainIndex.add(accountData);
                        }
                    }
                    if (account.getSex() != null) {
                        if (accountData.getSex().equals(F) && account.getSex().equals(M)) {
                            Repository.list_m.add(accountData);
                            if (accountData.getStatus().equals(STATUS1)) {
                                Repository.list_status_1_m.add(accountData);
                            }
                            if (accountData.getStatus().equals(STATUS2)) {
                                Repository.list_status_2_m.add(accountData);
                            }
                            if (accountData.getStatus().equals(STATUS3)) {
                                Repository.list_status_3_m.add(accountData);
                            }
                        }
                        if (accountData.getSex().equals(M) && account.getSex().equals(F)) {
                            Repository.list_f.add(accountData);

                            if (accountData.getStatus().equals(STATUS1)) {
                                Repository.list_status_1_f.add(accountData);
                            }
                            if (accountData.getStatus().equals(STATUS2)) {
                                Repository.list_status_2_f.add(accountData);
                            }
                            if (accountData.getStatus().equals(STATUS3)) {
                                Repository.list_status_3_f.add(accountData);
                            }
                        }
                        accountData.setSex(account.getSex());
                    }
                    if (account.getFname() != null) {
                        accountData.setFname(account.getFname());
                        TreeSet<Account> list = Repository.fname.get(accountData.getFname());
                        if (list != null) {
                            list.add(accountData);
                        } else {
                            list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                            list.add(accountData);
                            Repository.fname.put(account.getFname(),list);
                        }
                        Repository.fname_not_null.add(accountData);
                    }
                    if (account.getInterests() != null) {
                        accountData.setInterests(account.getInterests());
                        if (accountData.getInterests() != null && accountData.getInterests().size() > 0) {
                            for (int size = accountData.getInterests().size(); size > 0; size--) {
                                TreeSet<Account> interestCountIndex = interests_count.get(size);
                                interestCountIndex.add(accountData);
                            }
                        }
                    }
                    if (account.getStatus() != null) {
                        accountData.setStatus(account.getStatus());
                        if (accountData.getStatus().equals(Service.STATUS1)) {
                            Repository.list_status_1.add(accountData);
                            Repository.list_status_2_not.add(accountData);
                            Repository.list_status_3_not.add(accountData);
                        } else if (accountData.getStatus().equals(Service.STATUS2)) {
                            Repository.list_status_2.add(accountData);
                            Repository.list_status_1_not.add(accountData);
                            Repository.list_status_3_not.add(accountData);
                        } else {
                            Repository.list_status_3.add(accountData);
                            Repository.list_status_2_not.add(accountData);
                            Repository.list_status_1_not.add(accountData);
                        }
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
                    if (account.getStart() != 0) {
                        accountData.setStart(account.getStart());
                        accountData.setFinish(account.getFinish());
                        if (account.getStart() != 0) {
                            if (currentTimeStamp2 < account.getFinish()
                                    && currentTimeStamp2 > account.getStart()) {
                                premium_1.add(accountData);
                            }
                            premium_2.add(accountData);
                        } else {
                            premium_3.add(accountData);
                        }

                    }
                    if (account.getPhone() != null) {
                        accountData.setPhone(account.getPhone());
                        phone_not_null.add(accountData);
                        String code = accountData.getPhone()
                                .substring(accountData.getPhone().indexOf("(") + 1
                                        , accountData.getPhone().indexOf(")"));
                        TreeSet<Account> codeIndex = phone_code.get(code);
                        if (codeIndex == null) {
                            codeIndex = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                            codeIndex.add(accountData);
                            phone_code.put(code,codeIndex);
                        } else {
                            codeIndex.add(accountData);
                        }
                    } else {
                        phone_null.add(accountData);
                    }
                    if (account.getBirth() != 0) {
                        Calendar calendar = Repository.threadLocalCalendar.get();
                        calendar.setTimeInMillis((long)account.getBirth() * 1000);
                        Integer yearValue = calendar.get(Calendar.YEAR);
                        TreeSet<Account> list = year.get(yearValue);
                        if (list != null) {
                            list.add(account);
                        } else {
                            list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                            list.add(account);
                            Repository.year.put(yearValue,list);
                        }

                        accountData.setBirth(account.getBirth());
                    }
                    if (account.getCity() != null) {
                        accountData.setCity(account.getCity());
                        TreeSet<Account> list = Repository.city.get(accountData.getCity());
                        if (list != null) {
                            list.add(accountData);
                        } else {
                            list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                            list.add(accountData);
                            Repository.city.put(accountData.getCity(),list);
                        }
                        Repository.city_not_null.add(accountData);
                    }
                    if (account.getCountry() != null) {
                        accountData.setCountry(account.getCountry());
                        TreeSet<Account> list = Repository.country.get(accountData.getCountry());
                        if (list != null) {
                            list.add(accountData);
                        } else {
                            list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                            list.add(accountData);
                            Repository.country.put(accountData.getCountry(),list);
                        }
                        Repository.country_not_null.add(accountData);
                    }
                    if (account.getSname() != null) {
                        accountData.setSname(account.getSname());
                        TreeSet<Account> list = Repository.sname.get(accountData.getSname());
                        if (list != null) {
                            list.add(accountData);
                        } else {
                            list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                            list.add(accountData);
                            Repository.sname.put(account.getSname(),list);
                        }
                        Repository.sname_not_null.add(accountData);
                    }
                    if (account.getSex() != null || account.getCity() != null || account.getCountry() != null) {
                        if (accountData.getSex().equals(Service.M)) {
                            if (account.getCity() != null) {
                                TreeSet<Account> list = Repository.city.get(account.getCity() + "_m");
                                if (list != null) {
                                    list.add(accountData);
                                } else {
                                    list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                                    list.add(accountData);
                                    Repository.city.put(account.getCity() + "_m", list);
                                }
                            }
                            if (account.getCountry() != null) {
                                TreeSet<Account> list = Repository.country.get(account.getCountry() + "_m");
                                if (list != null) {
                                    list.add(accountData);
                                } else {
                                    list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                                    list.add(accountData);
                                    Repository.country.put(account.getCountry() + "_m", list);
                                }
                            }
                        }
                        if (accountData.getSex().equals(Service.F)) {
                            if (account.getCity() != null) {
                                TreeSet<Account> list = Repository.city.get(account.getCity() + "_f");
                                if (list != null) {
                                    list.add(accountData);
                                } else {
                                    list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                                    list.add(accountData);
                                    Repository.city.put(account.getCity() + "_f", list);
                                }
                            }
                            if (account.getCountry() != null) {
                                TreeSet<Account> list = Repository.country.get(account.getCountry() + "_f");
                                if (list != null) {
                                    list.add(accountData);
                                } else {
                                    list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                                    list.add(accountData);
                                    Repository.country.put(account.getCountry() + "_f", list);
                                }
                            }
                        }
                    }

                } finally {
                    lock.writeLock().unlock();
                }
            } else {
                return ServerHandler.ACCEPTED_R;
            }
            return ServerHandler.ACCEPTED_R;
        } else {
            return ServerHandler.NOT_FOUND_R;
        }

    }

    private static DefaultFullHttpResponse handleRecomended(FullHttpRequest req) {
        lock.readLock().lock();
        try {

            String replAcc = req.uri().substring(10);
            String id = replAcc.substring(0, replAcc.indexOf("/"));

            Account accountData = Repository.ids[Integer.parseInt(id)];
            if (accountData == null) {
                return ServerHandler.NOT_FOUND_R;
            } else {
                String[] params = Utils.tokenize(req.uri().substring(req.uri().indexOf(URI_RECOMENDED) + 12), '&');
                int limit;
                String country;
                String city;
                for (String param : params) {
                    if (param.startsWith(LIMIT)) {
                        try {
                            limit = Integer.parseInt(getValue(param));
                            if (limit <= 0) {
                                return ServerHandler.BAD_REQUEST_R;
                            }
                        } catch (Exception e) {
                            return ServerHandler.BAD_REQUEST_R;
                        }
                    }
                    if (param.startsWith(COUNTRY)) {
                        country = getValue(param);
                        if (country.isEmpty()) {
                            return ServerHandler.BAD_REQUEST_R;
                        }
                    }
                    if (param.startsWith(CITY)) {
                        city = getValue(param);
                        if (city.isEmpty()) {
                            return ServerHandler.BAD_REQUEST_R;
                        }
                    }
                }

                return ServerHandler.OK_EMPTY_R;

                /*if (!accountData.equals(Repository.PRESENT_AC)) {
                    TreeSet<AccountC> compat = new TreeSet<>(Comparator.comparing(AccountC::getC).reversed());
                    TreeSet<Account> list = null;

                    if (accountData.getSex().equals(F)) {
                        if (!country.isEmpty()) {
                            list = Repository.country.get(country + "_m");
                        }
                        if (!city.isEmpty()) {
                            list = Repository.country.get(city + "_m");
                        }
                        if (list == null) {
                            list = Repository.list_status_1_m;
                        }
                    } else {
                        if (!country.isEmpty()) {
                            list = Repository.country.get(country + "_f");
                        }
                        if (!city.isEmpty()) {
                            list = Repository.country.get(city + "_m");
                        }
                        if (list == null) {
                            list = Repository.list_status_1_f;
                        }
                    }

                    Iterator<Account> iterator = list.descendingIterator();
                    calcCompat(accountData, country, city, compat, iterator);
                    if (list.equals(Repository.list_status_1_f) || list.equals(Repository.list_status_1_m)) {
                        if (compat.size() >= limit) {
                            return new Result(Utils.accountToString2(compat, limit).getBytes(StandardCharsets.UTF_8), HttpResponseStatus.OK);
                        } else {
                            if (list.equals(Repository.list_status_1_f)) {
                                list = Repository.list_status_2_f;
                            } else {
                                list = Repository.list_status_2_m;
                            }
                            iterator = list.descendingIterator();
                            calcCompat(accountData, country, city, compat, iterator);
                            if (compat.size() >= limit) {
                                return new Result(Utils.accountToString2(compat, limit).getBytes(StandardCharsets.UTF_8), HttpResponseStatus.OK);
                            } else {
                                if (list.equals(Repository.list_status_2_f)) {
                                    iterator = Repository.list_status_3_f.descendingIterator();
                                } else {
                                    iterator = Repository.list_status_3_m.descendingIterator();
                                }
                                calcCompat(accountData, country, city, compat, iterator);
                                return new Result(Utils.accountToString2(compat, limit).getBytes(StandardCharsets.UTF_8), HttpResponseStatus.OK);
                            }
                        }
                    } else {
                        return new Result(Utils.accountToString2(compat, limit).getBytes(StandardCharsets.UTF_8), HttpResponseStatus.OK);
                    }
                }*/
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ServerHandler.BAD_REQUEST_R;
        } finally {
            lock.readLock().unlock();
        }
    }

    /*private static void calcCompat(Account accountData, String country, String city, TreeSet<AccountC> compat, Iterator<Account> iterator) {
        while (iterator.hasNext()) {
            Account account1 = iterator.next();
            if (!account1.getId().equals(accountData.getId())) {
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
    }*/

    /*private static Integer getCompatibility(Account accountData, Account account1) {
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
    }*/

    private static DefaultFullHttpResponse handleSuggest(FullHttpRequest req) {
        lock.readLock().lock();
        try {
            String replAcc = req.uri().substring(10);
            String id = replAcc.substring(0, replAcc.indexOf("/"));

            Account accountData = Repository.ids[Integer.parseInt(id)];
            if (accountData == null) {
                return ServerHandler.NOT_FOUND_R;
            } else {
                String[] params = Utils.tokenize(req.uri().substring(req.uri().indexOf(URI_SUGGEST) + 10), '&');
                for (String param : params) {
                    if (param.startsWith(LIMIT)) {
                        try {
                            Integer limit = Integer.parseInt(getValue(param));
                            if (limit <= 0) {
                                return ServerHandler.BAD_REQUEST_R;
                            }
                        } catch (Exception e) {
                            return ServerHandler.BAD_REQUEST_R;
                        }
                    }
                    if (param.startsWith(COUNTRY)) {
                        if (getValue(param).isEmpty()) {
                            return ServerHandler.BAD_REQUEST_R;
                        }
                    }
                    if (param.startsWith(CITY)) {
                        if (getValue(param).isEmpty()) {
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

    private static DefaultFullHttpResponse handleGroup(FullHttpRequest req) {
        try {
            String[] t = Utils.tokenize(req.uri().substring(17),'&');
            String sex = null;
            String countryKey = null;
            String cityKey = null;
            Integer limit = null;
            String order = null;
            for (String param : t) {
                if (param.startsWith("query_id")) {
                    continue;
                }
                if (param.startsWith(LIMIT)) {
                    try {
                        limit = Integer.parseInt(getValue(param));
                        if (limit <= 0 || limit > 50) {
                            return ServerHandler.BAD_REQUEST_R;
                        }
                    } catch (Exception e) {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.startsWith("order")) {
                    order = getValue(param);
                    if (!order.equals("-1") && !order.equals("1")) {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.startsWith(SEX)) {
                    sex = getValue(param);
                    if (!Service.F.equals(sex) && !Service.M.equals(sex)) {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.startsWith(KEYS)) {
                    String value = getValue(param);
                    String[] tokens = Utils.tokenize(value, delim);
                    for (String token : tokens) {
                        if (!SEX.equals(token)
                                && !STATUS.equals(token)
                                && !INTERESTS.equals(token)
                                && !COUNTRY.equals(token)
                                && !CITY.equals(token)) {
                            return ServerHandler.BAD_REQUEST_R;
                        }
                    }
                    if (value.equals(COUNTRY)) {
                        countryKey = value;
                    }
                    if (value.equals(CITY)) {
                        cityKey = value;
                    }
                }
            }
            if (order == null || limit == null) {
                return ServerHandler.BAD_REQUEST_R;
            }
        if (phase1.get()) {
            if (t.length == 5) {
                if (sex != null && countryKey != null) {
                    if (sex.equals(Service.F)) {
                        return ServerHandler.createOK(Utils.groupCSToString(Repository.country_f_gr, limit, order).getBytes(StandardCharsets.UTF_8));
                    }
                    if (sex.equals(Service.M)) {
                        return ServerHandler.createOK(Utils.groupCSToString(Repository.country_m_gr, limit, order).getBytes(StandardCharsets.UTF_8));
                    }
                }
                if (sex != null && cityKey != null) {
                    if (sex.equals(Service.F)) {
                        return ServerHandler.createOK(Utils.groupCiSToString(Repository.city_f_gr, limit, order).getBytes(StandardCharsets.UTF_8));
                    }
                    if (sex.equals(Service.M)) {
                        return ServerHandler.createOK(Utils.groupCiSToString(Repository.city_m_gr, limit, order).getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
            if (t.length == 4) {
                if (sex == null) {
                    if (countryKey != null) {
                        return ServerHandler.createOK(Utils.groupCSToString(Repository.country_gr, limit, order).getBytes(StandardCharsets.UTF_8));
                    }
                    if (cityKey != null) {
                        return ServerHandler.createOK(Utils.groupCiSToString(Repository.city_gr, limit, order).getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
        }
            return ServerHandler.OK_EMPTY_GR_R;
        } catch (Exception e) {
            e.printStackTrace();
            return ServerHandler.NOT_FOUND_R;
        }
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
        phase1.set(false);
        Account account = Utils.anyToAccount(JsonIterator.deserialize(req.content().toString(StandardCharsets.UTF_8)),false);
        if (account == null) {
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
                contains = Repository.emails.containsKey(account.getEmail());
            } finally {
                lock.readLock().unlock();
            }

            if (contains) {
                return ServerHandler.BAD_REQUEST_R;
            } else {
                lock.writeLock().lock();
                try {
                    Repository.list.add(account);
                    Repository.ids[account.getId()] = account;
                    Repository.emails.put(account.getEmail(), Repository.PRESENT);
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
                    if (!predicate.equals(CONTAINS_PR)) {
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


            Set<Account> listForSearch = getIndexForFilter(interArr,interestsPrV
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
            if (likesPr) {
                return ServerHandler.OK_EMPTY_R;
            }

            for (Account account : listForSearch) {
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
                        if (snameV.charAt(0) == NULL_PR_VAL_ONE) {
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
                        if (phoneV.charAt(0) == NULL_PR_VAL_ONE) {
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
                        if (countryV.charAt(0) == NULL_PR_VAL_ONE) {
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
                        if (premiumV.charAt(0) == NULL_PR_VAL_ONE) {
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
                        if (cityV.charAt(0) == NULL_PR_VAL_ONE) {
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
                        if (fnameV.charAt(0) == NULL_PR_VAL_ONE) {
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
                                if (account.getInterests().contains(value)) {
                                    isValid = true;
                                    break;
                                }
                            }
                            if (!isValid) {
                                continue;
                            }
                        } else if (interestsPrV == CONTAINS_PR) {
                            if (interArr.length <= account.getInterests().size()) {
                                boolean isValid = true;
                                for (String value : interArr) {
                                    if (!account.getInterests().contains(value)) {
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

                   /*if (likesPr) {
                        if (account.getLikesArr() != null) {
                            if (likesArr.length <= account.getLikesArr().size()) {
                                boolean isValid = true;
                                for (Integer value : likesArr) {
                                    if (!account.getLikesArr().contains(value)) {
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
                    }*/

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
        } finally {
            lock.readLock().unlock();
        }
    }

    private static Set<Account> getIndexForFilter(String[] interArr, String interestsPrV
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
                                                  ,String[] cityArr,String[] fnameArr
    ) {
        Set<Account> resultIndex = Repository.list;

        if (emailPr) {
            if (emailPrV == DOMAIN_PR) {
                resultIndex = compareIndex(Repository.email_domain.get(emailV),resultIndex);
            }
        }

        if (interArr != null && interestsPrV == CONTAINS_PR) {
            resultIndex = compareIndex(Repository.interests_count.get(interArr.length),resultIndex);
        }

        if (phonePr) {
            if (phonePrV == CODE_PR) {
                resultIndex = compareIndex(Repository.phone_code.get(phoneV), resultIndex);
            } else {
                if (phoneV.charAt(0) == NULL_PR_VAL_ONE) {
                    resultIndex = compareIndex(Repository.phone_null, resultIndex);
                } else {
                    resultIndex = compareIndex(Repository.phone_not_null, resultIndex);
                }
            }
        }

        if (snamePr) {
            if (snamePrV == NULL_PR) {
                if (snameV.charAt(0) == NULL_PR_VAL_ONE) {
                    resultIndex = compareIndex(Repository.sname.get(null),resultIndex);
                } else {
                    resultIndex = compareIndex(Repository.sname_not_null,resultIndex);
                }
            } else if (snamePrV == EQ_PR) {
                resultIndex = compareIndex(Repository.sname.get(snameV),resultIndex);
            }
        }

        if (cityPr) {
            if (cityPrV == ANY_PR) {
                if (cityArr.length == 1) {
                    resultIndex = compareIndex(Repository.city.get(cityArr[0]), resultIndex);
                } else {
                    resultIndex = compareIndex(Repository.city_not_null, resultIndex);
                }
            } else {
                if (Service.F.equals(sexV)) {
                    if (cityPrV == EQ_PR) {
                        resultIndex = compareIndex(Repository.city.get(cityV + "_f"), resultIndex);
                    } else if (cityPrV == NULL_PR && cityV.charAt(0) == NULL_PR_VAL_ONE) {
                        resultIndex = compareIndex(Repository.city.get("null_f"), resultIndex);
                    }
                }
                if (Service.M.equals(sexV)) {
                    if (cityPrV == EQ_PR) {
                        resultIndex = compareIndex(Repository.city.get(cityV + "_m"), resultIndex);
                    } else if (cityPrV == NULL_PR && cityV.charAt(0) == NULL_PR_VAL_ONE) {
                        resultIndex = compareIndex(Repository.city.get("null_m"), resultIndex);
                    }
                }

                if (cityPrV == NULL_PR) {
                    if (cityV.charAt(0) == NULL_PR_VAL_ONE) {
                        resultIndex = compareIndex(Repository.city.get(null), resultIndex);
                    } else {
                        resultIndex = compareIndex(Repository.city_not_null, resultIndex);
                    }
                }
                if (cityPrV == EQ_PR) {
                    resultIndex = compareIndex(Repository.city.get(cityV), resultIndex);
                }
            }
        }

        if (fnamePr) {
            if (fnamePrV == NULL_PR) {
                if (fnameV.charAt(0) == NULL_PR_VAL_ONE) {
                    resultIndex = compareIndex(Repository.fname.get(null),resultIndex);
                } else {
                    resultIndex = compareIndex(Repository.fname_not_null,resultIndex);
                }
            } else if (fnamePrV == ANY_PR) {
                if (fnameArr.length == 1) {
                    resultIndex = compareIndex(Repository.fname.get(fnameArr[0]), resultIndex);
                } else {
                    resultIndex = compareIndex(Repository.fname_not_null, resultIndex);
                }
            } else if (fnamePrV == EQ_PR) {
                resultIndex = compareIndex(Repository.fname.get(fnameV),resultIndex);
            }
        }


        if (countryPr) {
            if (Service.F.equals(sexV)) {
                if (countryPrV == EQ_PR) {
                    resultIndex = compareIndex(Repository.country.get(countryV + "_f"), resultIndex);
                } else if (countryPrV == NULL_PR && countryV.charAt(0) == NULL_PR_VAL_ONE) {
                    resultIndex = compareIndex(Repository.country.get("null_f"), resultIndex);
                }
            }
            if (Service.M.equals(sexV)) {
                if (countryPrV == EQ_PR) {
                    resultIndex = compareIndex(Repository.country.get(countryV + "_m"), resultIndex);
                } else if (countryPrV == NULL_PR && countryV.charAt(0) == NULL_PR_VAL_ONE) {
                    resultIndex = compareIndex(Repository.country.get("null_m"), resultIndex);
                }
            }

            if (countryPrV == NULL_PR) {
                if (countryV.charAt(0) == NULL_PR_VAL_ONE) {
                    resultIndex = compareIndex(Repository.country.get(null), resultIndex);
                } else {
                    resultIndex = compareIndex(Repository.country_not_null, resultIndex);
                }
            }
            if (countryPrV == EQ_PR) {
                resultIndex = compareIndex(Repository.country.get(countryV), resultIndex);
            }
        }

        if (premiumPr) {
            if (premiumPrV == NOW_PR) {
                resultIndex = compareIndex(Repository.premium_1,resultIndex);
            } else if (premiumPrV == NULL_PR) {
                if (premiumV.charAt(0) == NULL_PR_VAL_ONE) {
                    resultIndex = compareIndex(Repository.premium_3,resultIndex);
                } else {
                    resultIndex = compareIndex(Repository.premium_2,resultIndex);
                }
            }
        }

        if (statusPr) {
            if (statusPrV == EQ_PR) {
                if (Service.STATUS1.equals(statusV) && Service.F.equals(sexV)) {
                    resultIndex = compareIndex(Repository.list_status_1_f,resultIndex);
                }

                if (Service.STATUS2.equals(statusV) && Service.F.equals(sexV)) {
                    resultIndex = compareIndex(Repository.list_status_2_f,resultIndex);
                }

                if (Service.STATUS3.equals(statusV) && Service.F.equals(sexV)) {
                    resultIndex = compareIndex(Repository.list_status_3_f,resultIndex);
                }

                if (Service.STATUS1.equals(statusV) && Service.M.equals(sexV)) {
                    resultIndex = compareIndex(Repository.list_status_1_m,resultIndex);
                }

                if (Service.STATUS2.equals(statusV) && Service.M.equals(sexV)) {
                    resultIndex = compareIndex(Repository.list_status_2_m,resultIndex);
                }

                if (Service.STATUS3.equals(statusV) && Service.M.equals(sexV)) {
                    resultIndex = compareIndex(Repository.list_status_3_m,resultIndex);
                }

                if (sexV == null && Service.STATUS1.equals(statusV)) {
                    resultIndex = compareIndex(Repository.list_status_1,resultIndex);
                }
                if (sexV == null && Service.STATUS2.equals(statusV)) {
                    resultIndex = compareIndex(Repository.list_status_2,resultIndex);
                }
                if (sexV == null && Service.STATUS3.equals(statusV)) {
                    resultIndex = compareIndex(Repository.list_status_3,resultIndex);
                }
            } else {
                if (statusV.equals(Service.STATUS1)) {
                    resultIndex = compareIndex(Repository.list_status_1_not,resultIndex);
                } else if (statusV.equals(Service.STATUS2)) {
                    resultIndex = compareIndex(Repository.list_status_2_not,resultIndex);
                } else {
                    resultIndex = compareIndex(Repository.list_status_3_not,resultIndex);
                }
            }
        }

        if (sexPr) {
            if (!statusPr && Service.F.equals(sexV)) {
                resultIndex = compareIndex(Repository.list_f, resultIndex);
            }
            if (!statusPr && Service.M.equals(sexV)) {
                resultIndex = compareIndex(Repository.list_m, resultIndex);
            }
        }

        if (birthPr) {
            if (birthPrV == YEAR_PR) {
                resultIndex = compareIndex(Repository.year.get(year), resultIndex);
            }
        }

        return resultIndex;

    }

    private static Set<Account> compareIndex(Set<Account> newIndex, Set<Account> resultIndex) {
        if (newIndex != null && resultIndex != null) {
            if (newIndex.size() < resultIndex.size()) {
                return newIndex;
            } else {
                return resultIndex;
            }
        } else {
            return null;
        }
    }


}
