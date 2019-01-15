package app;

import app.models.Account;
import app.models.Premium;
import com.jsoniter.JsonIterator;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by Alikin E.A. on 27.12.18.
 */
public class Utils {
    private static ThreadLocal<StringBuilder> threadLocalBuilder =
            new ThreadLocal<StringBuilder>() {
                @Override
                protected StringBuilder initialValue() {
                    return new StringBuilder();
                }

                @Override
                public StringBuilder get() {
                    StringBuilder b = super.get();
                    b.setLength(0); // clear/reset the buffer
                    return b;
                }

            };

    private static final String START = "start";
    private static final String FINISH = "finish";

    public static byte[] readBytes(InputStream stream) throws IOException {
        if (stream == null) return new byte[] {};
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        boolean error = false;
        try {
            int numRead = 0;
            while ((numRead = stream.read(buffer)) > -1) {
                output.write(buffer, 0, numRead);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                if (!error) throw e;
            }
        }
        output.flush();
        return output.toByteArray();
    }

    public static Account anyToAccount(Any accountAny, boolean forUpdate) {
        try {
            for (String key : accountAny.keys()) {
                if (!key.equals(Service.ID)
                        && !key.equals(Service.INTERESTS)
                        && !key.equals(Service.LIKES)
                        && !key.equals(Service.PREMIUM)
                        && !key.equals(Service.EMAIL)
                        && !key.equals(Service.CITY)
                        && !key.equals(Service.COUNTRY)
                        && !key.equals(Service.SNAME)
                        && !key.equals(Service.PHONE)
                        && !key.equals(Service.BIRTH)
                        && !key.equals(Service.SEX)
                        && !key.equals(Service.FNAME)
                        && !key.equals(Service.STATUS)
                        && !key.equals(Service.JOINED)
                ) {
                    return null;
                }
            }

            if (!forUpdate) {
                if (!accountAny.keys().contains(Service.ID)) {
                    return null;
                }
            }


            Account account = new Account();
            for (String key : accountAny.keys()) {
                if (key.equals(Service.SEX)) {
                    String valueStr = accountAny.get(Service.SEX).toString();
                    if (valueStr.equals(Service.F)) {
                        account.setSex(Service.F);
                    } else if (valueStr.equals(Service.M)) {
                        account.setSex(Service.M);
                    } else {
                        return null;
                    }
                }

                if (key.equals(Service.STATUS)) {
                    String valueStr = accountAny.get(Service.STATUS).toString();
                    if (valueStr.equals(Service.STATUS1)) {
                        account.setStatus(Service.STATUS1);
                    } else if (valueStr.equals(Service.STATUS2)) {
                        account.setStatus(Service.STATUS2);
                    } else if (valueStr.equals(Service.STATUS3)) {
                        account.setStatus(Service.STATUS3);
                    } else {
                        return null;
                    }
                }

                if (key.equals(Service.JOINED)) {
                    Any any = accountAny.get(Service.JOINED);
                    if (!ValueType.NUMBER.equals(any.valueType())) {
                        return null;
                    }

                }

                if (key.equals(Service.ID)) {
                    Any any = accountAny.get(Service.ID);
                    if (ValueType.NUMBER.equals(any.valueType())) {
                        account.setId(any.toInt());
                    } else {
                        return null;
                    }
                }
                if (key.equals(Service.INTERESTS)) {
                    List<Any> listInter = accountAny.get(Service.INTERESTS).asList();
                    List<String> list = new LinkedList<>();
                    for (Any anyInter : listInter) {
                        list.add(anyInter.toString().intern());
                    }
                    account.setInterests(list);
                }
                if (key.equals(Service.LIKES)) {
                    List<Any> listLike = accountAny.get(Service.LIKES).asList();
                    Set<Integer> list = new HashSet<>(listLike.size());
                    for (Any anyLike : listLike) {
                        if (!ValueType.NUMBER.equals(anyLike.get(Service.TS).valueType())) {
                            return null;
                        }
                        Any any = anyLike.get(Service.ID);
                        if (!ValueType.NUMBER.equals(any.valueType())) {
                            return null;
                        } else {
                            list.add(any.toInt());
                        }
                    }
                    account.setLikesArr(list);
                }

                if (key.equals(Service.PREMIUM)) {
                    Any any = accountAny.get(Service.PREMIUM);
                    if (!any.keys().contains(FINISH) || !any.keys().contains(START)) {
                        return null;
                    }
                    if (!ValueType.NUMBER.equals(any.get(FINISH).valueType())) {
                        return null;
                    }
                    if (!ValueType.NUMBER.equals(any.get(START).valueType())) {
                        return null;
                    }
                    Premium pr = new Premium();
                    pr.setFinish(any.get(FINISH).toInt());
                    pr.setStart(any.get(START).toInt());
                    account.setPremium(pr);
                }

                if (key.equals(Service.EMAIL)) {
                    account.setEmail(accountAny.get(Service.EMAIL).toString());
                }

                if (key.equals(Service.CITY)) {
                    account.setCity(accountAny.get(Service.CITY).toString().intern());
                }
                if (key.equals(Service.COUNTRY)) {
                    account.setCountry(accountAny.get(Service.COUNTRY).toString().intern());
                }
                if (key.equals(Service.SNAME)) {
                    account.setSname(accountAny.get(Service.SNAME).toString().intern());
                }
                if (key.equals(Service.PHONE)) {
                    account.setPhone(accountAny.get(Service.PHONE).toString());
                }
                if (key.equals(Service.FNAME)) {
                    account.setFname(accountAny.get(Service.FNAME).toString().intern());
                }
                if (key.equals(Service.BIRTH)) {
                    Any any = accountAny.get(Service.BIRTH);
                    if (!ValueType.NUMBER.equals(any.valueType())) {
                        return null;
                    } else {
                        account.setBirth(any.toInt());
                    }
                }

            }
            return account;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String accountToString(List<Account> accounts, Map<String,Object> enableProp) {
        StringBuilder sb = threadLocalBuilder.get();
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

            if (enableProp.containsKey(Service.SEX) && account.getSex() != null) {
                sb.append("\"sex\":");
                sb.append("\"");
                sb.append(account.getSex());
                sb.append("\",");
            }

            if (enableProp.containsKey(Service.FNAME) && account.getFname() != null) {
                sb.append("\"fname\":");
                sb.append("\"");
                sb.append(account.getFname());
                sb.append("\",");
            }

            if (enableProp.containsKey(Service.STATUS) && account.getStatus() != null) {
                sb.append("\"status\":");
                sb.append("\"");
                sb.append(account.getStatus());
                sb.append("\",");
            }

            if (enableProp.containsKey(Service.PHONE) && account.getPhone() != null) {
                sb.append("\"phone\":");
                sb.append("\"");
                sb.append(account.getPhone());
                sb.append("\",");
            }

            if (enableProp.containsKey(Service.BIRTH) && account.getBirth() != 0) {
                sb.append("\"birth\":");
                sb.append(account.getBirth());
                sb.append(",");
            }

            if (enableProp.containsKey(Service.CITY) && account.getCity() != null) {
                sb.append("\"city\":");
                sb.append("\"");
                sb.append(account.getCity());
                sb.append("\",");
            }

            if (enableProp.containsKey(Service.COUNTRY) && account.getCountry() != null) {
                sb.append("\"country\":");
                sb.append("\"");
                sb.append(account.getCountry());
                sb.append("\",");
            }

            if (enableProp.containsKey(Service.SNAME) && account.getSname() != null) {
                sb.append("\"sname\":");
                sb.append("\"");
                sb.append(account.getSname());
                sb.append("\",");
            }

            if (enableProp.containsKey(Service.PREMIUM) && account.getPremium() != null) {
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


    public static String[] tokenize(String string, char delimiter) {
        String[] temp = new String[(string.length() / 2) + 1];
        int wordCount = 0;
        int i = 0;
        int j = string.indexOf(delimiter);

        while( j >= 0) {
            temp[wordCount++] = string.substring(i, j);
            i = j + 1;
            j = string.indexOf(delimiter, i);
        }

        temp[wordCount++] = string.substring(i);
        String[] result = new String[wordCount];
        System.arraycopy(temp, 0, result, 0, wordCount);
        return result;
    }

    public static boolean validateLikes(String string) {
        Any likesRequestAny = JsonIterator.deserialize(string);
        List<Any>likesListAny = likesRequestAny.get(Service.LIKES).asList();

        for (Any any : likesListAny) {
            Any value = any.get(Service.TS);
            if (!ValueType.NUMBER.equals(value.valueType())) {
                return false;
            }
            value = any.get(Service.LIKEE);
            if (!ValueType.NUMBER.equals(value.valueType())) {
                return false;
            } else {
                if (!Repository.ids.containsKey(value.toInt())) {
                    return false;
                }
            }
            value = any.get(Service.LIKER);
            if (!ValueType.NUMBER.equals(value.valueType())) {
                return false;
            } else {
                if (!Repository.ids.containsKey(value.toInt())) {
                    return false;
                }
            }
        }
        return true;
    }
}
