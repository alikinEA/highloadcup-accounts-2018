package app;

import app.models.*;
import app.server.Server;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

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
    private static final String ID = "id";
    private static final String QUERY_ID = "query_id";
    private static final String LIMIT = "limit";

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
    private static final String URI_NEW = "/accounts/new";
    private static final String URI_LIKES = "/accounts/likes";
    private static final String URI_GROUP = "/accounts/group/?";
    private static final String URI_SUGGEST = "/suggest";
    private static final String URI_RECOMENDED = "/recommend";
    private static final String ACCOUNTS =  "/accounts/";

    public static Result handle(FullHttpRequest req) {
        if (req.uri().startsWith(URI_FILTER)) {
            return handleFilterv2(req);
        } else if (req.uri().startsWith(URI_NEW)) {
            return handleNew(req);
        } else if (req.uri().startsWith(URI_LIKES)) {
            return handleLikes(req);
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
        String curId = req.uri().substring(req.uri().indexOf(ACCOUNTS) + 10,req.uri().lastIndexOf("/?"));
        if (Repository.ids.containsKey(curId)) {
            try {
                Account account = mapper.readValue(req.content().toString(StandardCharsets.UTF_8),Account.class);
                if (account.getSex() != null) {
                    if (!account.getSex().equals("f")
                            && !account.getSex().equals("m")) {
                        return BAD_REQUEST;
                    }
                }
                if (account.getStatus() != null) {
                    if (!account.getStatus().equals("свободны")
                            && !account.getStatus().equals("всё сложно")
                            && !account.getStatus().equals("заняты")) {
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
                        Repository.emails.put(account.getEmail(),Repository.PRESENT);
                        return ACCEPTED;
                    }
                }
            } catch (IOException e) {
                return BAD_REQUEST;
            }
        } else {
            return NOT_FOUND;
        }
        return ACCEPTED;
    }

    private static Result handleRecomended(FullHttpRequest req) {
        return NOT_FOUND;
    }

    private static Result handleSuggest(FullHttpRequest req) {
        return NOT_FOUND;
    }

    private static Result handleGroup(FullHttpRequest req) {
        StringTokenizer t = new StringTokenizer(req.uri().replace(URI_GROUP,""),",");
        while(t.hasMoreTokens()) {
            String param = t.nextToken();
            if (param.startsWith("keys")) {
                String value = getValue(param);
                if (!SEX.equals(value)
                        && !STATUS.equals(value)
                        && !INTERESTS.equals(value)
                        && !COUNTRY.equals(value)
                        && !CITY.equals(value)) {
                    return BAD_REQUEST;
                }
            }
            if (param.startsWith("order")) {
                String value = getValue(param);
                if (!"1".equals(value) && !"-1".equals(value)) {
                    return BAD_REQUEST;
                }
            }
        }
        return NOT_FOUND;
    }

    private static Result handleLikes(FullHttpRequest req) {
        try {
            LikesRequest likesReq = mapper.readValue(req.content().toString(StandardCharsets.UTF_8), LikesRequest.class);
            for (LikeRequest like : likesReq.getLikes()) {
                if (like.getLiker() == null || !Repository.ids.containsKey(like.getLiker().toString())) {
                    return BAD_REQUEST;
                }
                if (like.getLikee() == null || !Repository.ids.containsKey(like.getLikee().toString())) {
                    return BAD_REQUEST;
                }
            }
        } catch (IOException e) {
            return BAD_REQUEST;
        }
        return ACCEPTED;
    }

    private static Result handleNew(FullHttpRequest req) {
        try {
            Account account = mapper.readValue(req.content().toString(StandardCharsets.UTF_8),Account.class);
            if (account.getId() == null) {
                return BAD_REQUEST;
            }
            if (Repository.ids.containsKey(account.getId().toString())) {
                return BAD_REQUEST;
            }
            if (account.getSex() != null) {
                if (!account.getSex().equals("f")
                        && !account.getSex().equals("m")) {
                    return BAD_REQUEST;
                }
            } else {
                return BAD_REQUEST;
            }
            if (account.getStatus() != null) {
                if (!account.getStatus().equals("свободны")
                        && !account.getStatus().equals("всё сложно")
                        && !account.getStatus().equals("заняты")) {
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
                    Repository.emails.put(account.getEmail(),Repository.PRESENT);
                    Repository.ids.put(account.getId().toString(),Repository.PRESENT);
                    return CREATED;
                }
            }
        } catch (IOException e) {
            return BAD_REQUEST;
        }
        return CREATED;
    }


    private static boolean validate(String param, Map<String, String> predicateCache) {
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

    private static boolean compareArrays(List<String> params, List<String> enableProp) {
        if (params.size() != enableProp.size()) {
            return false;
        }
        for (String prop : enableProp) {
            boolean isValid = false;
            for (String param : params) {
                if (param.contains(prop)) {
                    return true;
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
            return URLDecoder.decode(param.substring(param.indexOf("=") + 1), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException();
        }
    }

    private static String getPredicate(String param) {
        return param.substring(param.indexOf("_") + 1,param.indexOf("="));
    }

    private static Result handleFilterv2(FullHttpRequest req) {
        List<String> params = getTokens(req.uri().replace(URI_FILTER,""),"&");
        int i = 0;
        int limit = 0;
        for (String param : params) {
            if (param.startsWith(LIMIT)) {
                limit = Integer.parseInt(getValue(param));
            }
        }

        Map<String,String> valueCache = new HashMap<>(10);
        Map<String,String> predicateCache = new HashMap<>(10);
        for (String param : params) {
            if (!validate(param,predicateCache)) {
                return BAD_REQUEST;
            } else {
                fillValueCacheValue(param,valueCache);
            }
        }
        List<String> enableProp = new ArrayList<>();
        List<Account> accounts = new ArrayList<>(limit);

        for (Account account : Repository.list) {
            enableProp.clear();
            if (i == limit) {
                break;
            }
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
                            if (Repository.currentTimeStamp2 < account.getPremium().getFinish()
                                    && Repository.currentTimeStamp2 > account.getPremium().getStart()) {
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
                        Date date = new Date(Long.parseLong(account.getBirth() + "000"));
                        Calendar calendar = new GregorianCalendar();
                        calendar.setTime(date);
                        if (Integer.parseInt(getValue(param)) == calendar.get(Calendar.YEAR)) {
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
                        StringTokenizer t = new StringTokenizer(valueCache.get(param),",");
                        while(t.hasMoreTokens()) {
                            if (t.nextToken().equals(account.getCity())) {
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
                        StringTokenizer t = new StringTokenizer(valueCache.get(param),",");
                        while(t.hasMoreTokens()) {
                            if (t.nextToken().equals(account.getFname())) {
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
                            StringTokenizer t = new StringTokenizer(valueCache.get(param),",");
                            while(t.hasMoreTokens()) {
                                if (account.getInterests().contains(t.nextToken())) {
                                    enableProp.add(INTERESTS);
                                    break;
                                }
                            }
                        } else if (predicate.equals(CONTAINS_PR)) {
                            List<String> splitedValue = getTokens(valueCache.get(param),",");
                            if (splitedValue.size() <= account.getInterests().size()) {
                                enableProp.add(INTERESTS);
                                for (String value : splitedValue) {
                                    if (!account.getInterests().contains(value)) {
                                        enableProp.remove(enableProp.size()-1);
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
                        List<String> splitedValue = getTokens(valueCache.get(param),",");
                        if (splitedValue.size() <= account.getLikesArr().size()) {
                            enableProp.add(LIKES);
                            for (String value : splitedValue) {
                                if (!account.getLikesArr().contains(value)) {
                                    enableProp.remove(enableProp.size()-1);
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
            if (compareArrays(params,enableProp)) {
                Account result = new Account();
                result.setId(account.getId());
                result.setEmail(account.getEmail());
                if (enableProp.contains(SEX)) {
                    result.setSex(account.getSex());
                }
                if (enableProp.contains(STATUS)) {
                    result.setStatus(account.getStatus());
                }
                if (enableProp.contains(FNAME)) {
                    result.setFname(account.getFname());
                }
                if (enableProp.contains(SNAME)) {
                    result.setSname(account.getSname());
                }
                if (enableProp.contains(PHONE)) {
                    result.setPhone(account.getPhone());
                }
                if (enableProp.contains(COUNTRY)) {
                    result.setCountry(account.getCountry());
                }
                if (enableProp.contains(CITY)) {
                    result.setCity(account.getCity());
                }
                if (enableProp.contains(BIRTH)) {
                    result.setBirth(account.getBirth());
                }
                if (enableProp.contains(PREMIUM)) {
                    result.setPremium(account.getPremium());
                }
                accounts.add(result);
                i++;
            }
        }
        try {
            return new Result(mapper.writeValueAsBytes(new Accounts(accounts)),HttpResponseStatus.OK);
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
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
}
