package app;

import app.models.*;
import app.server.Server;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoniter.JsonIterator;
import com.jsoniter.spi.JsonException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.internal.StringUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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


    private static final String SEX = "sex";
    private static final String EMAIL = "email";
    private static final String STATUS = "status";
    private static final String FNAME = "fname";
    private static final String SNAME = "sname";
    private static final String PHONE = "phone";
    private static final String COUNTRY = "country";
    private static final String CITY = "city";
    private static final String BIRTH = "birth";
    private static final String INTERESTS = "interests";
    private static final String LIKES = "likes";
    private static final String PREMIUM = "premium";
    private static final String QUERY_ID = "query_id";
    private static final String LIMIT = "limit";
    //private static final String ORDER = "order";
    private static final String KEYS = "keys";


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

    private static final ObjectMapper mapper = new ObjectMapper();


    private static final String URI_FILTER = "/accounts/filter/?";
    private static final String URI_NEW = "/accounts/new/";
    private static final String URI_LIKES = "/accounts/likes/";
    private static final String URI_GROUP = "/accounts/group/?";
    private static final String URI_SUGGEST = "/suggest";
    private static final String URI_RECOMENDED = "/recommend";
    private static final String ACCOUNTS =  "/accounts/";

    private static final String F =  "f";
    private static final String M =  "m";


    private static final String STATUS1 = "свободны";
    private static final String STATUS2 = "всё сложно";
    private static final String STATUS3 = "заняты";


    private static final String utf8 = "UTF-8";
    private static final String delim = ",";
    private static final String delim2 = "=";
    private static final char delim3 = '?';
    private static final String delim4 = "/?";
    //private static final String delim5 = "1";
    //private static final String delim6 = "-1";
    private static final String delim7 = "@";
    private static final String delim8 = "&";
    private static final String delim9 = "_";
    private static final String delim10 = "(";
    private static final String delim11 = ")";


    public static Result handle(FullHttpRequest req) {
        if (req.uri().startsWith(URI_FILTER)) {
            return handleFilterv2(req);
        } else if (req.uri().startsWith(URI_NEW)) {
            if (req.uri().substring(14).charAt(0) != delim3) {
                return NOT_FOUND;
            } else {
                return handleNew(req);
            }
        } else if (req.uri().startsWith(URI_LIKES)) {
            if (req.uri().substring(16).charAt(0) != delim3) {
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
        String curId = req.uri().substring(req.uri().indexOf(ACCOUNTS) + 10,req.uri().lastIndexOf(delim4));
        if (!Character.isDigit(curId.charAt(0))) {
            return NOT_FOUND;
        }
        if (Repository.ids.containsKey(curId)) {
            try {
                Account account = JsonIterator.deserialize(req.content().toString(StandardCharsets.UTF_8),Account.class);
               // Account account = mapper.readValue(req.content().toString(StandardCharsets.UTF_8),Account.class);
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
                    if (!account.getEmail().contains(delim7)) {
                        return BAD_REQUEST;
                    }
                    if (Repository.emails.containsKey(account.getEmail())) {
                        return BAD_REQUEST;
                    } else {
                        account.setId(Integer.parseInt(curId));
                        Account accountData = Repository.list.ceiling(account);
                        if (accountData != null) {
                            if (account.getLikes() != null) {
                                List<Integer> likesNew;
                                if (accountData.getLikesArr() != null) {
                                    likesNew = accountData.getLikesArr();
                                } else {
                                    likesNew = new ArrayList<>(10);
                                }
                                for (Like like : account.getLikes()) {
                                    likesNew.add(like.getId());
                                }
                                accountData.setLikesArr(likesNew);
                            }
                            if (account.getEmail() != null) {
                                Repository.emails.remove(accountData.getEmail());
                                Repository.emails.put(account.getEmail(),Repository.PRESENT);
                                accountData.setEmail(account.getEmail());
                            }
                            if (account.getSex() != null) {
                                accountData.setSex(account.getSex());
                            }
                            if (account.getFname() != null) {
                                accountData.setFname(account.getFname());
                            }
                            if (account.getInterests() != null) {
                                accountData.setInterests(account.getInterests());
                            }
                            if (account.getStatus() != null) {
                                accountData.setStatus(account.getStatus());
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
            } catch (Exception e) {
                return BAD_REQUEST;
            }
        } else {
            return NOT_FOUND;
        }
        return ACCEPTED;
    }

    private static Result handleRecomended(FullHttpRequest req) {
       /* String replAcc = req.uri().substring(10);
        String id = replAcc.substring(0,replAcc.indexOf("/"));
        if (!Repository.ids.containsKey(id)) {
            return NOT_FOUND;
        } else {

        }*/
        return NOT_FOUND;
    }

    private static Result handleSuggest(FullHttpRequest req) {
        return NOT_FOUND;
    }

    private static Result handleGroup(FullHttpRequest req) {
        StringTokenizer t = new StringTokenizer(req.uri().substring(17),delim8);
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
        try {
            LikesRequest likesReq = JsonIterator.deserialize(req.content().toString(StandardCharsets.UTF_8), LikesRequest.class);
            //LikesRequest likesReq = mapper.readValue(req.content().toString(StandardCharsets.UTF_8), LikesRequest.class);
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
                Account account = new Account();
                account.setId(like.getLiker());
                Account accountData = Repository.list.ceiling(account);
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
    }

    private static Result handleNew(FullHttpRequest req) {
        try {
            Account account = JsonIterator.deserialize(req.content().toString(StandardCharsets.UTF_8),Account.class);
            //Account account = mapper.readValue(req.content().toString(StandardCharsets.UTF_8),Account.class);
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
                if (!account.getEmail().contains(delim7)) {
                    return BAD_REQUEST;
                }
                if (Repository.emails.containsKey(account.getEmail())) {
                    return BAD_REQUEST;
                } else {
                    Repository.ids.put(account.getId().toString(),Repository.PRESENT);
                    Repository.emails.put(account.getEmail(),Repository.PRESENT);
                    if (account.getLikes() != null) {
                        List<Integer> likesNew;
                        if (account.getLikesArr() != null) {
                            likesNew = account.getLikesArr();
                            likesNew.clear();
                        } else {
                            likesNew = new ArrayList<>(10);
                        }
                        for (Like like : account.getLikes()) {
                            likesNew.add(like.getId());
                        }
                        account.setLikesArr(likesNew);
                        account.setLikes(null);
                    }
                    account.setJoined(null);
                    Repository.list.add(account);
                    return CREATED;
                }
            }
        } catch (Exception e) {
            return BAD_REQUEST;
        }
        return CREATED;
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

    private static String getValue(String param) {
        try {
            return URLDecoder.decode(param.substring(param.indexOf(delim2) + 1), utf8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException();
        }
    }

    private static String getPredicate(String param) {
        return param.substring(param.indexOf(delim9) + 1,param.indexOf(delim2));
    }

    private static Result handleFilterv2(FullHttpRequest req) {
        List<String> params = getTokens(req.uri().substring(18),delim8);
        int i = 0;
        int limit = 0;
        for (String param : params) {
            if (param.startsWith(LIMIT)) {
                limit = Integer.parseInt(getValue(param));
            }
        }

        Map<String,String> valueCache = new HashMap<>(10);
        Map<String,String> predicateCache = new HashMap<>(10);
        Map<String,Object> finalFieldSet = null;
        for (String param : params) {
            if (!fillCacheAndvalidate(param,predicateCache)) {
                return BAD_REQUEST;
            } else {
                fillValueCacheValue(param,valueCache);
            }
        }
        Map<String,Object> enableProp = new HashMap<>(valueCache.size());
        List<Account> accounts = new ArrayList<>(limit);

        for (Account account : Repository.list) {
            if (i == limit) {
                break;
            }
            enableProp.clear();
            for (String param : params) {
                //SEX ============================================
                if (param.startsWith(SEX)) {
                    if (account.getSex().equals(valueCache.get(param))) {
                        enableProp.put(SEX,Repository.PRESENT);
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
                            enableProp.put(EMAIL,Repository.PRESENT);
                        } else {
                            break;
                        }
                    } else if (predicate.equals(LT_PR)) {
                        if (account.getEmail().compareTo(valueCache.get(param)) < 0) {
                            enableProp.put(EMAIL,Repository.PRESENT);
                        } else {
                            break;
                        }
                    } else if (predicate.equals(GT_PR)) {
                        if (account.getEmail().compareTo(valueCache.get(param)) > 0) {
                            enableProp.put(EMAIL,Repository.PRESENT);
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
                            enableProp.put(STATUS,Repository.PRESENT);
                        } else {
                            break;
                        }
                    } else if (predicate.equals(NEQ_PR)) {
                        if (!account.getStatus().equals(valueCache.get(param))) {
                            enableProp.put(STATUS,Repository.PRESENT);
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
                            enableProp.put(SNAME,Repository.PRESENT);
                        } else {
                            break;
                        }
                    } else if (predicate.equals(NULL_PR)) {
                        String value = valueCache.get(param);
                        if (value.equals(NULL_PR_VAL_ONE)) {
                            if (account.getSname() == null) {
                                enableProp.put(SNAME,Repository.PRESENT);
                            } else {
                                break;
                            }
                        } else {
                            if (account.getSname() != null) {
                                enableProp.put(SNAME,Repository.PRESENT);
                            } else {
                                break;
                            }
                        }
                    } else if (predicate.equals(STARTS_PR)) {
                        if (account.getSname() != null)
                            if (account.getSname().startsWith(valueCache.get(param))) {
                                enableProp.put(SNAME,Repository.PRESENT);
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
                                    .substring(account.getPhone().indexOf(delim10) + 1
                                            , account.getPhone().indexOf(delim11))
                                    .equals(valueCache.get(param))) {
                                enableProp.put(PHONE,Repository.PRESENT);
                            } else {
                                break;
                            }
                        }
                    } else if (predicate.equals(NULL_PR)) {
                        String value = valueCache.get(param);
                        if (value.equals(NULL_PR_VAL_ONE)) {
                            if (account.getPhone() == null) {
                                enableProp.put(PHONE,Repository.PRESENT);
                            } else {
                                break;
                            }
                        } else {
                            if (account.getPhone() != null) {
                                enableProp.put(PHONE,Repository.PRESENT);
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
                            enableProp.put(COUNTRY,Repository.PRESENT);
                        } else {
                            break;
                        }
                    } else if (predicate.equals(NULL_PR)) {
                        String value = valueCache.get(param);
                        if (value.equals(NULL_PR_VAL_ONE)) {
                            if (account.getCountry() == null) {
                                enableProp.put(COUNTRY,Repository.PRESENT);
                            } else {
                                break;
                            }
                        } else {
                            if (account.getCountry() != null) {
                                enableProp.put(COUNTRY,Repository.PRESENT);
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
                            if (Repository.currentTimeStamp2 < account.getPremium().getFinish()
                                    && Repository.currentTimeStamp2 > account.getPremium().getStart()) {
                                enableProp.put(PREMIUM,Repository.PRESENT);
                            } else {
                                break;
                            }
                        }
                    } else if (predicate.equals(NULL_PR)) {
                        String value = valueCache.get(param);
                        if (value.equals(NULL_PR_VAL_ONE)) {
                            if (account.getPremium() == null) {
                                enableProp.put(PREMIUM,Repository.PRESENT);
                            } else {
                                break;
                            }
                        } else {
                            if (account.getPremium() != null) {
                                enableProp.put(PREMIUM,Repository.PRESENT);
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
                            enableProp.put(BIRTH,Repository.PRESENT);
                        } else {
                            break;
                        }
                    } else if (predicate.equals(LT_PR)) {
                        if (account.getBirth().compareTo(Integer.parseInt(valueCache.get(param))) < 0) {
                            enableProp.put(BIRTH,Repository.PRESENT);
                        } else {
                            break;
                        }
                    } else if (predicate.equals(GT_PR)) {
                        if (account.getBirth().compareTo(Integer.parseInt(valueCache.get(param))) > 0) {
                            enableProp.put(BIRTH,Repository.PRESENT);
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
                            enableProp.put(CITY,Repository.PRESENT);
                        } else {
                            break;
                        }
                    } else if (predicate.equals(ANY_PR)) {
                        StringTokenizer t = new StringTokenizer(valueCache.get(param),delim);
                        while(t.hasMoreTokens()) {
                            if (t.nextToken().equals(account.getCity())) {
                                enableProp.put(CITY,Repository.PRESENT);
                                break;
                            }
                        }
                    } else if (predicate.equals(NULL_PR)) {
                        String value = valueCache.get(param);
                        if (value.equals(NULL_PR_VAL_ONE)) {
                            if (account.getCity() == null) {
                                enableProp.put(CITY,Repository.PRESENT);
                            } else {
                                break;
                            }
                        } else {
                            if (account.getCity() != null) {
                                enableProp.put(CITY,Repository.PRESENT);
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
                            enableProp.put(FNAME,Repository.PRESENT);
                        } else {
                            break;
                        }
                    } else if (predicate.equals(ANY_PR)) {
                        StringTokenizer t = new StringTokenizer(valueCache.get(param),delim);
                        while(t.hasMoreTokens()) {
                            if (t.nextToken().equals(account.getFname())) {
                                enableProp.put(FNAME,Repository.PRESENT);
                                break;
                            }
                        }
                    } else if (predicate.equals(NULL_PR)) {
                        String value = valueCache.get(param);
                        if (value.equals(NULL_PR_VAL_ONE)) {
                            if (account.getFname() == null) {
                                enableProp.put(FNAME,Repository.PRESENT);
                            } else {
                                break;
                            }
                        } else {
                            if (account.getFname() != null) {
                                enableProp.put(FNAME,Repository.PRESENT);
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
                            StringTokenizer t = new StringTokenizer(valueCache.get(param),delim);
                            while(t.hasMoreTokens()) {
                                if (account.getInterests().contains(t.nextToken())) {
                                    enableProp.put(INTERESTS,Repository.PRESENT);
                                    break;
                                }
                            }
                        } else if (predicate.equals(CONTAINS_PR)) {
                            List<String> splitedValue = getTokens(valueCache.get(param),delim);
                            if (splitedValue.size() <= account.getInterests().size()) {
                                enableProp.put(INTERESTS,Repository.PRESENT);
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
                        List<String> splitedValue = getTokens(valueCache.get(param),delim);
                        if (splitedValue.size() <= account.getLikesArr().size()) {
                            enableProp.put(LIKES,Repository.PRESENT);
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
            enableProp.put(QUERY_ID,Repository.PRESENT);
            enableProp.put(LIMIT,Repository.PRESENT);
            if (compareArrays(params,enableProp)) {
                if (finalFieldSet == null) {
                    finalFieldSet = new HashMap<>(enableProp.size());
                    for (String key : enableProp.keySet()) {
                        finalFieldSet.put(key,Repository.PRESENT);
                    }
                }
                accounts.add(account);
                i++;
            }
        }
        try {
            return new Result(accountToString(accounts,finalFieldSet).getBytes(utf8),HttpResponseStatus.OK);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return OK_EMPTY_ACCOUNTS;
    }

    private static void fillValueCacheValue(String param, Map<String, String> valueCache) {
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

    public static String replaceString(String source, String os) {
        if (source == null) {
            return null;
        }
        int i = 0;
        if ((i = source.indexOf(os, i)) >= 0) {
            char[] sourceArray = source.toCharArray();
            char[] nsArray = "".toCharArray();
            int oLength = os.length();
            StringBuilder buf = new StringBuilder (sourceArray.length);
            buf.append (sourceArray, 0, i).append(nsArray);
            i += oLength;
            int j = i;
            while ((i = source.indexOf(os, i)) > 0) {
                buf.append (sourceArray, j, i - j).append(nsArray);
                i += oLength;
                j = i;
            }
            buf.append (sourceArray, j, sourceArray.length - j);
            source = buf.toString();
            buf.setLength (0);
        }
        return source;
    }

    public static String accountToString(List<Account> accounts, Map<String,Object> enableProp) {

        StringBuilder sb = new StringBuilder();
        sb.append("{\"accounts\":[");
        for (Account account : accounts) {

            sb.append("{");

            sb.append("\"id\":");
            sb.append(account.getId());
            sb.append(",");

            sb.append("\"email\":");
            sb.append("\"");
            sb.append(account.getEmail());
            sb.append("\",");

            if (enableProp.containsKey(SEX) && account.getSex() != null) {
                sb.append("\"sex\":");
                sb.append("\"");
                sb.append(account.getSex());
                sb.append("\",");
            }

            if (enableProp.containsKey(FNAME) && account.getFname() != null) {
                sb.append("\"fname\":");
                sb.append("\"");
                sb.append(account.getFname());
                sb.append("\",");
            }

            if (enableProp.containsKey(STATUS) && account.getStatus() != null) {
                sb.append("\"status\":");
                sb.append("\"");
                sb.append(account.getStatus());
                sb.append("\",");
            }

            if (enableProp.containsKey(PHONE) && account.getPhone() != null) {
                sb.append("\"phone\":");
                sb.append("\"");
                sb.append(account.getPhone());
                sb.append("\",");
            }

            if (enableProp.containsKey(BIRTH) && account.getBirth() != null) {
                sb.append("\"birth\":");
                sb.append(account.getBirth());
                sb.append(",");
            }

            if (enableProp.containsKey(CITY) && account.getCity() != null) {
                sb.append("\"city\":");
                sb.append("\"");
                sb.append(account.getCity());
                sb.append("\",");
            }

            if (enableProp.containsKey(COUNTRY) && account.getCountry() != null) {
                sb.append("\"country\":");
                sb.append("\"");
                sb.append(account.getCountry());
                sb.append("\",");
            }

            if (enableProp.containsKey(SNAME) && account.getSname() != null) {
                sb.append("\"sname\":");
                sb.append("\"");
                sb.append(account.getSname());
                sb.append("\",");
            }

            if (enableProp.containsKey(PREMIUM) && account.getPremium() != null) {
                sb.append("\"premium\":");
                sb.append("{");

                sb.append("\"start\":");
                sb.append(account.getPremium().getStart());
                sb.append(",");

                sb.append("\"finish\":");
                sb.append(account.getPremium().getFinish());

                sb.append("},");
            }
            sb.setLength(sb.length() -1);
            sb.append("},");
        }
        if (accounts.size() > 0) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("]}");
        return sb.toString();
    }
}
