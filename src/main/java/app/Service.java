package app;

import app.models.Account;
import app.models.Accounts;
import app.models.Like;
import app.models.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Alikin E.A. on 15.12.18.
 */
public class Service {

    private static final byte[] EMPTY = "{}".getBytes();
    private static final Result OK_ACCEPTED = new Result(EMPTY,HttpResponseStatus.ACCEPTED);

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

    public static Result handle(FullHttpRequest req) throws IOException {
        if (req.uri().startsWith(URI_FILTER)) {
            return handleFilter(req);
        } else {
            return OK_ACCEPTED;
        }
    }

    private static Result handleFilter(FullHttpRequest req) throws IOException {
        String uri = req.uri();
        String[] params = uri.replace(URI_FILTER,"").split("&");
        int i = 0;
        int limit = 0;
        for (String param : params) {
            if (param.startsWith(LIMIT)) {
                limit = Integer.parseInt(getValue(param));
            }
        }

        for (String param : params) {
            validate(param);
        }
        List<String> enableProp = new ArrayList<>();
        List<Account> accounts = new ArrayList<>(limit);

        for (int i1 = Repository.fileNames.size() - 1; i1 >= 0; i1--) {
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(Repository.fileNames.get(i1)), StandardCharsets.UTF_8)) {
                for (;;) {
                    if (i == limit) {
                        break;
                    }
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    Account account = mapper.readValue(line,Account.class);
                    for (String param : params) {
                        //SEX ============================================
                        if (param.startsWith(SEX)) {
                            if (account.getSex().equals(getValue(param))) {
                                enableProp.add(SEX);
                            }
                        }
                        //SEX ============================================

                        //EMAIL ============================================
                        if (param.startsWith(EMAIL)) {
                            String predicate = getPredicate(param);
                            if (predicate.equals(DOMAIN_PR)) {
                                if (account.getEmail().contains(getValue(param))) {
                                    enableProp.add(EMAIL);
                                }
                            } else if (predicate.equals(LT_PR)) {
                                if (account.getEmail().compareTo(getValue(param)) < 0) {
                                    enableProp.add(EMAIL);
                                }
                            } else if (predicate.equals(GT_PR)) {
                                if (account.getEmail().compareTo(getValue(param)) > 0) {
                                    enableProp.add(EMAIL);
                                }
                            }
                        }
                        //EMAIL ============================================

                        //STATUS ============================================
                        if (param.startsWith(STATUS)) {
                            String predicate = getPredicate(param);
                            if (predicate.equals(EQ_PR)) {
                                if (account.getStatus().equals(getValue(param))) {
                                    enableProp.add(STATUS);
                                }
                            } else if (predicate.equals(NEQ_PR)) {
                                if (!account.getStatus().equals(getValue(param))) {
                                    enableProp.add(STATUS);
                                }
                            }
                        }
                        //STATUS ============================================

                        //FNAME ============================================
                        if (param.startsWith(FNAME)) {
                            String predicate = getPredicate(param);

                            if (predicate.equals(EQ_PR)) {
                                if (getValue(param).equals(account.getFname())) {
                                    enableProp.add(FNAME);
                                }
                            } else if (predicate.equals(ANY_PR)) {
                                String[] splitedValue = getValue(param).split(",");
                                for (String value: splitedValue) {
                                    if (value.equals(account.getFname())) {
                                        enableProp.add(FNAME);
                                        break;
                                    }
                                }
                            } else if (predicate.equals(NULL_PR)) {
                                String value = getValue(param);
                                if (value.equals(NULL_PR_VAL_ONE)) {
                                    if (account.getFname() == null) {
                                        enableProp.add(FNAME);
                                    }
                                } else {
                                    if (account.getFname() != null) {
                                        enableProp.add(FNAME);
                                    }
                                }
                            }
                        }
                        //FNAME ============================================


                        //SNAME ============================================
                        if (param.startsWith(SNAME)) {
                            String predicate = getPredicate(param);

                            if (predicate.equals(EQ_PR)) {
                                if (getValue(param).equals(account.getSname())) {
                                    enableProp.add(SNAME);
                                }
                            } else if (predicate.equals(NULL_PR)) {
                                String value = getValue(param);
                                if (value.equals(NULL_PR_VAL_ONE)) {
                                    if (account.getSname() == null) {
                                        enableProp.add(SNAME);
                                    }
                                } else {
                                    if (account.getSname() != null) {
                                        enableProp.add(SNAME);
                                    }
                                }
                            } else if (predicate.equals(STARTS_PR)) {
                                if (account.getSname() != null)
                                    if (account.getSname().startsWith(getValue(param))) {
                                        enableProp.add(SNAME);
                                    }
                            }
                        }
                        //SNAME ============================================


                        //PHONE ============================================
                        if (param.startsWith(PHONE)) {
                            String predicate = getPredicate(param);

                            if (predicate.equals(CODE_PR)) {
                                if (account.getPhone() != null) {
                                    if (account.getPhone()
                                            .substring(account.getPhone().indexOf("(") + 1
                                                    , account.getPhone().indexOf(")"))
                                            .equals(getValue(param))) {
                                        enableProp.add(PHONE);
                                    }
                                }
                            } else if (predicate.equals(NULL_PR)) {
                                String value = getValue(param);
                                if (value.equals(NULL_PR_VAL_ONE)) {
                                    if (account.getPhone() == null) {
                                        enableProp.add(PHONE);
                                    }
                                } else {
                                    if (account.getPhone() != null) {
                                        enableProp.add(PHONE);
                                    }
                                }
                            }
                        }
                        //PHONE ============================================

                        //COUNTRY ============================================
                        if (param.startsWith(COUNTRY)) {
                            String predicate = getPredicate(param);

                            if (predicate.equals(EQ_PR)) {
                                if (getValue(param).equals(account.getCountry())) {
                                    enableProp.add(COUNTRY);
                                }
                            } else if (predicate.equals(NULL_PR)) {
                                String value = getValue(param);
                                if (value.equals(NULL_PR_VAL_ONE)) {
                                    if (account.getCountry() == null) {
                                        enableProp.add(COUNTRY);
                                    }
                                } else {
                                    if (account.getCountry() != null) {
                                        enableProp.add(COUNTRY);
                                    }
                                }
                            }
                        }
                        //COUNTRY ============================================

                        //CITY ============================================
                        if (param.startsWith(CITY)) {
                            String predicate = getPredicate(param);

                            if (predicate.equals(EQ_PR)) {
                                if (getValue(param).equals(account.getCity())) {
                                    enableProp.add(CITY);
                                }
                            } else if (predicate.equals(ANY_PR)) {
                                String[] splitedValue = getValue(param).split(",");
                                for (String value: splitedValue) {
                                    if (value.equals(account.getCity())) {
                                        enableProp.add(CITY);
                                        break;
                                    }
                                }
                            } else if (predicate.equals(NULL_PR)) {
                                String value = getValue(param);
                                if (value.equals(NULL_PR_VAL_ONE)) {
                                    if (account.getCity() == null) {
                                        enableProp.add(CITY);
                                    }
                                } else {
                                    if (account.getCity() != null) {
                                        enableProp.add(CITY);
                                    }
                                }
                            }
                        }
                        //CITY ============================================

                        //BIRTH ============================================
                        if (param.startsWith(BIRTH)) {
                            String predicate = getPredicate(param);
                            if (predicate.equals(YEAR_PR)) {
                                Date date = new Date(Long.parseLong(account.getBirth() + "000"));
                                Calendar calendar = new GregorianCalendar();
                                calendar.setTime(date);
                                if (Integer.parseInt(getValue(param)) == calendar.get(Calendar.YEAR)) {
                                    enableProp.add(BIRTH);
                                }
                            } else if (predicate.equals(LT_PR)) {
                                if (account.getBirth().compareTo(Integer.parseInt(getValue(param))) < 0) {
                                    enableProp.add(BIRTH);
                                }
                            } else if (predicate.equals(GT_PR)) {
                                if (account.getBirth().compareTo(Integer.parseInt(getValue(param))) > 0) {
                                    enableProp.add(BIRTH);
                                }
                            }
                        }
                        //BIRTH ============================================


                        //INTERESTS ============================================
                        if (param.startsWith(INTERESTS)) {
                            String predicate = getPredicate(param);
                            if (account.getInterests() != null) {
                                if (predicate.equals(ANY_PR)) {
                                    String[] splitedValue = getValue(param).split(",");
                                    for (String value : splitedValue) {
                                        if (account.getInterests().contains(value)) {
                                            enableProp.add(INTERESTS);
                                            break;
                                        }
                                    }
                                } else if (predicate.equals(CONTAINS_PR)) {
                                    String[] splitedValue = getValue(param).split(",");
                                    if (splitedValue.length <= account.getInterests().size()) {
                                        enableProp.add(INTERESTS);
                                        for (String value : splitedValue) {
                                            if (!account.getInterests().contains(value)) {
                                                enableProp.remove(enableProp.size()-1);
                                                break;
                                            }
                                        }
                                    }
                                }

                            }
                        }
                        //INTERESTS ============================================



                        //LIKES ============================================
                        if (param.startsWith(LIKES)) {
                            if (account.getLikes() != null) {
                           /* String[] splitedValue = getValue(param).split(",");
                                for (String value: splitedValue) {
                                    if (account.getLikes().stream().map(Like::getId).collect(Collectors.toList()).contains(Integer.parseInt(value))) {
                                        enableProp.add(LIKES);
                                        break;
                                    }
                                }*/
                                String[] splitedValue = getValue(param).split(",");
                                if (splitedValue.length <= account.getLikes().size()) {
                                    enableProp.add(LIKES);
                                    List<Integer> likesArr = account.getLikes().stream().map(Like::getId).collect(Collectors.toList());
                                    for (String value : splitedValue) {
                                        if (!likesArr.contains(Integer.parseInt(value))) {
                                            enableProp.remove(enableProp.size()-1);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        //LIKES ============================================


                        //PREMIUM ============================================
                        if (param.startsWith(PREMIUM)) {
                            String predicate = getPredicate(param);
                            if (predicate.equals(NOW_PR)) {
                                if (account.getPremium() != null) {
                                    /*if (Repository.currentTimeStamp < new Date(Long.parseLong(account.getPremium().getFinish() + "000")).getTime()) {
                                        enableProp.add(PREMIUM);
                                    }*/
                                    if (Repository.currentTimeStamp2 < account.getPremium().getFinish()
                                            && Repository.currentTimeStamp2 > account.getPremium().getStart()) {
                                        enableProp.add(PREMIUM);
                                    }
                                }
                            } else if (predicate.equals(NULL_PR)) {
                                String value = getValue(param);
                                if (value.equals(NULL_PR_VAL_ONE)) {
                                    if (account.getPremium() == null) {
                                        enableProp.add(PREMIUM);
                                    }
                                } else {
                                    if (account.getPremium() != null) {
                                        enableProp.add(PREMIUM);
                                    }
                                }
                            }
                        }
                        //PREMIUM ============================================
                    }
                    enableProp.add(QUERY_ID);
                    enableProp.add(LIMIT);
                    if (compareArrays(params,enableProp)) {
                        if (!enableProp.contains(SEX)) {
                            account.setSex(null);
                        }
                        if (!enableProp.contains(STATUS)) {
                            account.setStatus(null);
                        }
                        if (!enableProp.contains(FNAME)) {
                            account.setFname(null);
                        }
                        if (!enableProp.contains(SNAME)) {
                            account.setSname(null);
                        }
                        if (!enableProp.contains(PHONE)) {
                            account.setPhone(null);
                        }
                        if (!enableProp.contains(COUNTRY)) {
                            account.setCountry(null);
                        }
                        if (!enableProp.contains(CITY)) {
                            account.setCity(null);
                        }
                        if (!enableProp.contains(BIRTH)) {
                            account.setBirth(null);
                        }
                        //if (!enableProp.contains(INTERESTS)) {
                            account.setInterests(null);
                        //}
                        //if (!enableProp.contains(LIKES)) {
                            account.setLikes(null);
                        //}
                        if (!enableProp.contains(PREMIUM)) {
                            account.setPremium(null);
                        }
                        account.setJoined(null);
                        accounts.add(account);
                        i++;
                    }
                    enableProp.clear();
                }
            }
        }
        return new Result(mapper.writeValueAsBytes(new Accounts(accounts)),HttpResponseStatus.OK);
    }

    private static void validate(String param) {
        String predicate = getPredicate(param);
        if (param.startsWith(SEX)) {
            if (!predicate.equals(EQ_PR)) {
                throw new RuntimeException();
            }
        }
        if (param.startsWith(EMAIL)) {
            if (!predicate.equals(DOMAIN_PR)
                    && !predicate.equals(LT_PR)
                    && !predicate.equals(GT_PR)) {
                throw new RuntimeException();
            }
        }
        if (param.startsWith(STATUS)) {
            if (!predicate.equals(EQ_PR)
                    && !predicate.equals(NEQ_PR)) {
                throw new RuntimeException();
            }
        }
        if (param.startsWith(FNAME)) {
            if (!predicate.equals(EQ_PR)
                    && !predicate.equals(ANY_PR)
                    && !predicate.equals(NULL_PR)) {
                throw new RuntimeException();
            }
        }
        if (param.startsWith(SNAME)) {
            if (!predicate.equals(EQ_PR)
                    && !predicate.equals(STARTS_PR)
                    && !predicate.equals(NULL_PR)) {
                throw new RuntimeException();
            }
        }
        if (param.startsWith(PHONE)) {
            if (!predicate.equals(NULL_PR)
                    && !predicate.equals(CODE_PR)) {
                throw new RuntimeException();
            }
        }
        if (param.startsWith(COUNTRY)) {
            if (!predicate.equals(NULL_PR)
                    && !predicate.equals(EQ_PR)) {
                throw new RuntimeException();
            }
        }
        if (param.startsWith(CITY)) {
            if (!predicate.equals(EQ_PR)
                    && !predicate.equals(ANY_PR)
                    && !predicate.equals(NULL_PR)) {
                throw new RuntimeException();
            }
        }
        if (param.startsWith(BIRTH)) {
            if (!predicate.equals(YEAR_PR)
                    && !predicate.equals(LT_PR)
                    && !predicate.equals(GT_PR)) {
                throw new RuntimeException();
            }
        }
        if (param.startsWith(INTERESTS)) {
            if (!predicate.equals(CONTAINS_PR)
                    && !predicate.equals(ANY_PR)) {
                throw new RuntimeException();
            }
        }
        if (param.startsWith(LIKES)) {
            if (!predicate.equals(CONTAINS_PR)) {
                throw new RuntimeException();
            }
        }
        if (param.startsWith(PREMIUM)) {
            if (!predicate.equals(NULL_PR)
                    && !predicate.equals(NOW_PR)) {
                throw new RuntimeException();
            }
        }

    }

    private static boolean compareArrays(String[] params, List<String> enableProp) {
        if (params.length != enableProp.size()) {
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

    private static String getValue(String param) throws UnsupportedEncodingException {
        return URLDecoder.decode(param.substring(param.indexOf("=") + 1), "UTF-8");
    }

    private static String getPredicate(String param) {
        return param.substring(param.indexOf("_") + 1,param.indexOf("="));
    }
}
