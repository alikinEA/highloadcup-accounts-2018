package app;

import app.models.Account;
import app.models.Premium;
import com.jsoniter.any.Any;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Alikin E.A. on 27.12.18.
 */
public class Utils {

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

    public static Account anyToAccount(Any accountAny) {
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
                accountAny.get(Service.JOINED).toInt();
            }

            if (key.equals(Service.ID)) {
                account.setId(accountAny.get(Service.ID).toInt());
            }
            if (key.equals(Service.INTERESTS)) {
                List<Any> listInter = accountAny.get(Service.INTERESTS).asList();
                List<String> list = new ArrayList<>(listInter.size());
                for (Any anyInter : listInter) {
                    list.add(anyInter.toString());
                }
                account.setInterests(list);
            }
            if (key.equals(Service.LIKES)) {
                List<Any> listLike = accountAny.get(Service.LIKES).asList();
                List<Integer> list = new ArrayList<>(listLike.size());
                for (Any anyLike : listLike) {
                    anyLike.get(Service.TS).toInt();
                    list.add(anyLike.get(Service.ID).toInt());
                }
                account.setLikesArr(list);
            }

            if (key.equals(Service.PREMIUM)) {
                Any any = accountAny.get(Service.PREMIUM);
                if (!any.keys().contains(FINISH) || !any.keys().contains(START)) {
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
                account.setCity(accountAny.get(Service.CITY).toString());
            }
            if (key.equals(Service.COUNTRY)) {
                account.setCountry(accountAny.get(Service.COUNTRY).toString());
            }
            if (key.equals(Service.SNAME)) {
                account.setSname(accountAny.get(Service.SNAME).toString());
            }
            if (key.equals(Service.PHONE)) {
                account.setPhone(accountAny.get(Service.PHONE).toString());
            }
            if (key.equals(Service.BIRTH)) {
                account.setBirth(accountAny.get(Service.BIRTH).toInt());
            }
            if (key.equals(Service.FNAME)) {
                account.setFname(accountAny.get(Service.FNAME).toString());
            }
        }
        return account;
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

            if (enableProp.containsKey(Service.BIRTH) && account.getBirth() != null) {
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
}
