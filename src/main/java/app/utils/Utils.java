package app.utils;

import app.Repository.Repository;
import app.models.Constants;
import app.service.LocalPool;
import app.service.Service;
import app.models.Account;
import app.models.GroupObj;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static app.models.Constants.FINISH;
import static app.models.Constants.START;

/**
 * Created by Alikin E.A. on 27.12.18.
 */
public class Utils {

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
        try {
            for (String key : accountAny.keys()) {
                if (!key.equals(Constants.ID)
                        && !key.equals(Constants.INTERESTS)
                        && !key.equals(Constants.LIKES)
                        && !key.equals(Constants.PREMIUM)
                        && !key.equals(Constants.EMAIL)
                        && !key.equals(Constants.CITY)
                        && !key.equals(Constants.COUNTRY)
                        && !key.equals(Constants.SNAME)
                        && !key.equals(Constants.PHONE)
                        && !key.equals(Constants.BIRTH)
                        && !key.equals(Constants.SEX)
                        && !key.equals(Constants.FNAME)
                        && !key.equals(Constants.STATUS)
                        && !key.equals(Constants.JOINED)
                ) {
                    return null;
                }
            }

            Account account = new Account();
            for (String key : accountAny.keys()) {
                if (key.equals(Constants.SEX)) {
                    account.setSex(accountAny.get(Constants.SEX).toString().intern());
                }

                if (key.equals(Constants.STATUS)) {
                    account.setStatus(accountAny.get(Constants.STATUS).toString().intern());
                }

                if (key.equals(Constants.JOINED)) {
                    Any any = accountAny.get(Constants.JOINED);
                    if (!ValueType.NUMBER.equals(any.valueType())) {
                        return null;
                    } /*else {
                        account.setJoined(any.toInt());
                    }*/

                }

                if (key.equals(Constants.ID)) {
                    Any any = accountAny.get(Constants.ID);
                    if (ValueType.NUMBER.equals(any.valueType())) {
                        account.setId(any.toInt());
                    } else {
                        return null;
                    }
                }
                if (key.equals(Constants.INTERESTS)) {
                    List<Any> listInter = accountAny.get(Constants.INTERESTS).asList();
                    String[] list = new String[listInter.size()];
                    int index = 0;
                    for (Any anyInter : listInter) {
                        list[index] = anyInter.toString().intern();
                        index++;
                    }
                    Arrays.sort(list);
                    account.setInterests(list);
                }
                if (key.equals(Constants.LIKES)) {
                    List<Any> listLike = accountAny.get(Constants.LIKES).asList();
                    int[] list = new int[listLike.size()];
                    int index = 0;
                    for (Any anyLike : listLike) {
                        if (!ValueType.NUMBER.equals(anyLike.get(Constants.TS).valueType())) {
                            return null;
                        }
                        Any any = anyLike.get(Constants.ID);
                        if (!ValueType.NUMBER.equals(any.valueType())) {
                            return null;
                        }
                        list[index] = any.toInt();
                        index++;
                    }
                    Arrays.sort(list);
                    account.setLikes(list);
                }

                if (key.equals(Constants.PREMIUM)) {
                    Any any = accountAny.get(Constants.PREMIUM);
                    if (!any.keys().contains(FINISH) || !any.keys().contains(START)) {
                        return null;
                    }
                    if (!ValueType.NUMBER.equals(any.get(FINISH).valueType())) {
                        return null;
                    }
                    if (!ValueType.NUMBER.equals(any.get(START).valueType())) {
                        return null;
                    }
                    account.setFinish(any.get(FINISH).toInt());
                    account.setStart(any.get(START).toInt());
                }

                if (key.equals(Constants.EMAIL)) {
                    account.setEmail(accountAny.get(Constants.EMAIL).toString().intern());
                }

                if (key.equals(Constants.CITY)) {
                    account.setCity(accountAny.get(Constants.CITY).toString().intern());
                }
                if (key.equals(Constants.COUNTRY)) {
                    account.setCountry(accountAny.get(Constants.COUNTRY).toString().intern());
                }
                if (key.equals(Constants.SNAME)) {
                    account.setSname(accountAny.get(Constants.SNAME).toString().intern());
                }
                if (key.equals(Constants.PHONE)) {
                    account.setPhone(accountAny.get(Constants.PHONE).toString().intern());
                }
                if (key.equals(Constants.FNAME)) {
                    account.setFname(accountAny.get(Constants.FNAME).toString().intern());
                }
                /*if (key.equals(Service.JOINED)) {
                    account.setJoined(accountAny.get(Service.JOINED).toInt());
                }*/
                if (key.equals(Constants.BIRTH)) {
                    Any any = accountAny.get(Constants.BIRTH);
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


    public static String accountToString(Set<Account> accounts,boolean sexPr, boolean fnamePr, boolean statusPr, boolean premiumPr, boolean phonePr, boolean birthPr, boolean cityPr, boolean countryPr, boolean snamePr) {
        StringBuilder sb = LocalPool.threadLocalBuilder.get();
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

            if (sexPr && account.getSex() != null) {
                sb.append("\"sex\":");
                sb.append("\"");
                sb.append(account.getSex());
                sb.append("\",");
            }

            if (fnamePr && account.getFname() != null) {
                sb.append("\"fname\":");
                sb.append("\"");
                sb.append(account.getFname());
                sb.append("\",");
            }

            if (statusPr && account.getStatus() != null) {
                sb.append("\"status\":");
                sb.append("\"");
                sb.append(account.getStatus());
                sb.append("\",");
            }

            if (phonePr && account.getPhone() != null) {
                sb.append("\"phone\":");
                sb.append("\"");
                sb.append(account.getPhone());
                sb.append("\",");
            }

            if (birthPr && account.getBirth() != 0) {
                sb.append("\"birth\":");
                sb.append(account.getBirth());
                sb.append(",");
            }

            if (cityPr && account.getCity() != null) {
                sb.append("\"city\":");
                sb.append("\"");
                sb.append(account.getCity());
                sb.append("\",");
            }

            if (countryPr && account.getCountry() != null) {
                sb.append("\"country\":");
                sb.append("\"");
                sb.append(account.getCountry());
                sb.append("\",");
            }

            if (snamePr && account.getSname() != null) {
                sb.append("\"sname\":");
                sb.append("\"");
                sb.append(account.getSname());
                sb.append("\",");
            }

            if (premiumPr && account.getStart() != 0) {
                sb.append("\"premium\":");
                sb.append("{");

                sb.append("\"start\":");
                sb.append(account.getStart());
                sb.append(",");

                sb.append("\"finish\":");
                sb.append(account.getFinish());

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


    public static String groupCSToString(TreeSet<GroupObj> store ,int limit, String order) {
        StringBuilder sb = LocalPool.threadLocalBuilder.get();
        sb.append("{\"groups\":[");
        int limitC = 0;
        Iterator<GroupObj> itr;
        if (order.equals("1")) {
            itr = store.iterator();
        } else {
            itr = store.descendingIterator();
        }
        while (itr.hasNext()) {
            GroupObj gr = itr.next();
            if (limitC < limit) {
                limitC++;
                sb.append("{\"country\":\"");
                sb.append(gr.getName());
                sb.append("\",\"count\":");
                sb.append(gr.getCount());
                sb.append("},");
            }
        }
        sb.setLength(sb.length() - 1);
        sb.append("]}");
        return sb.toString();
    }

    public static String groupCiSToString(TreeSet<GroupObj> store, Integer limit, String order) {
        StringBuilder sb = LocalPool.threadLocalBuilder.get();
        sb.append("{\"groups\":[");
        int limitC = 0;
        Iterator<GroupObj> itr;
        if (order.equals("1")) {
            itr = store.iterator();
        } else {
            itr = store.descendingIterator();
        }
        while (itr.hasNext()) {
            GroupObj gr = itr.next();
            if (limitC < limit) {
                limitC++;
                sb.append("{\"city\":\"");
                sb.append(gr.getName());
                sb.append("\",\"count\":");
                sb.append(gr.getCount());
                sb.append("},");
            }
        }
        sb.setLength(sb.length() - 1);
        sb.append("]}");
        return sb.toString();
    }

    public static String groupSCiToString(String city,Integer limit, String order) {
        //Integer countF = Repository.country_f.get(country);
        //Integer countM = Repository.country_m.get(country);
        return null;
    }

    public static String groupSCToString(String country, Integer limit, String order) {
        //Integer countF = Repository.country_f.get(country);
        //Integer countM = Repository.country_m.get(country);
        return null;
    }

    private static String fillStringGroup(Integer limit, String order, Integer countF, Integer countM) {
        StringBuilder sb = LocalPool.threadLocalBuilder.get();
        sb.append("{\"groups\":[");
        if (order.equals("-1")) {
            if (countF > countM) {
                sb.append("{\"sex\":\"f\",\"count\":");
                sb.append(countF);
                sb.append("}");

                if (limit > 1) {
                    sb.append(",{\"sex\":\"m\",\"count\":");
                    sb.append(countM);
                    sb.append("}");
                }
            } else {
                sb.append("{\"sex\":\"m\",\"count\":");
                sb.append(countM);
                sb.append("}");

                if (limit > 1) {
                    sb.append(",{\"sex\":\"f\",\"count\":");
                    sb.append(countF);
                    sb.append("}");
                }

            }
        } else {
            if (countF < countM) {
                sb.append("{\"sex\":\"f\",\"count\":");
                sb.append(countF);
                sb.append("}");

                if (limit > 1) {
                    sb.append(",{\"sex\":\"m\",\"count\":");
                    sb.append(countM);
                    sb.append("}");
                }
            } else {
                sb.append("{\"sex\":\"m\",\"count\":");
                sb.append(countM);
                sb.append("}");

                if (limit > 1) {
                    sb.append(",{\"sex\":\"f\",\"count\":");
                    sb.append(countF);
                    sb.append("}");
                }
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    public static boolean contains(String[] strings, String searchString) {
        for (String string : strings) {
            if (string.equals(searchString)) {
                return true;
            }
        }

        return false;
    }



    public static int binarySearchStartPos(Account[] listForSearch,Integer value, int startIndex, int endIndex) {

        if (listForSearch[startIndex] == null)  {
            return startIndex;
        }
        boolean isSearchElement = listForSearch[startIndex].getBirth() == value;

        if (isSearchElement) {
            return startIndex;
        } else {
            if (endIndex - startIndex == 1) {
                return startIndex;
            }
        }

        int middleIndex = startIndex + ((endIndex - startIndex) / 2);
        if (listForSearch[middleIndex] == null)  {
            return startIndex;
        }
        if (listForSearch[middleIndex].getBirth() < value) {
            return binarySearchStartPos(listForSearch,value, startIndex, middleIndex);
        } else {
            return binarySearchStartPos(listForSearch,value, middleIndex, endIndex);
        }
    }

    public static void printIndexSize(Account[] index, String name) {
        if (Repository.queryCount.get() > 17_000) {
            int count = 0;
            for (Account account : index) {
                if (account != null) {
                    count++;
                }
            }
            System.out.println("Index: " + name + " - " + count);
        }
    }
}
