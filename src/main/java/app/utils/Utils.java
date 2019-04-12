package app.utils;

import app.Repository.Repository;
import app.models.*;
import app.service.LocalPoolService;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
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
                    //int[] listTs = new int[listLike.size()];
                    int index = 0;
                    for (Any anyLike : listLike) {
                        Any anyTs = anyLike.get(Constants.TS);
                        if (!ValueType.NUMBER.equals(anyTs.valueType())) {
                            return null;
                        }
                        Any any = anyLike.get(Constants.ID);
                        if (!ValueType.NUMBER.equals(any.valueType())) {
                            return null;
                        }
                        list[index] = any.toInt();
                        //listTs[index] = anyTs.toInt();
                        index++;
                    }
                    account.setLikes(list);
                    //account.setLikesTs(listTs);
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


    public static byte[] accountToString(Set<Account> accounts,boolean sexPr, boolean fnamePr, boolean statusPr, boolean premiumPr, boolean phonePr, boolean birthPr, boolean cityPr, boolean countryPr, boolean snamePr) {
        StringBuilder sb = LocalPoolService.threadLocalBuilder.get();
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

            if (sexPr) {
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

            if (statusPr) {
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

            if (birthPr) {
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
        sb.setLength(sb.length() - 1);
        sb.append("]}");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }


    public static byte[] groupToString(LinkedList<GroupObj> store, Integer limit,String keyName) {
        StringBuilder sb = LocalPoolService.threadLocalBuilder.get();
        sb.append("{\"groups\":[");
        int limitC = 0;
        Iterator<GroupObj> itr = store.iterator();
        while (itr.hasNext()) {
            GroupObj gr = itr.next();
            if (gr.getCount().get() < 1) {
                continue;
            }
            if (limitC < limit) {
                limitC++;
                sb.append("{");
                if (gr.getName() != Constants.NULL) {
                    sb.append("\"");
                    sb.append(keyName);
                    sb.append("\":\"");
                    sb.append(gr.getName());
                    sb.append("\",");
                }
                sb.append("\"count\":");
                sb.append(gr.getCount());
                sb.append("},");
            }
        }
        sb.setLength(sb.length() - 1);
        sb.append("]}");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
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

    public static String getPredicate(String param) {
        return param.substring(param.indexOf("_") + 1,param.indexOf("="));
    }

    public static String getValue(String param) throws UnsupportedEncodingException {
        if (param.startsWith(Constants.COUNTRY)
                || param.startsWith(Constants.CITY)
                || param.startsWith(Constants.INTERESTS)
                || param.startsWith(Constants.FNAME)
                || param.startsWith(Constants.SNAME)
                || param.startsWith(Constants.STATUS)
                || param.startsWith(Constants.LIKES)
                || param.startsWith(Constants.KEYS)
        ) {
            return URLDecoder.decode(param.substring(param.indexOf("=") + 1), Constants.UTF_8);
        } else {
            return param.substring(param.indexOf("=") + 1);
        }
    }

    public static void updateStrValue(String newV, String oldV, LinkedList<GroupObj> gr_index) {
        for (GroupObj groupObj : gr_index) {
            if (groupObj.getName().equals(newV)) {
                groupObj.getCount().incrementAndGet();
                continue;
            }
            if (groupObj.getName().equals(oldV)
                    || (groupObj.getName() == Constants.NULL && oldV == null)) {
                groupObj.getCount().decrementAndGet();
            }
        }
    }

    public static void insertStrToIndexGr(String value, LinkedList<GroupObj> index) {
        String str = Constants.NULL;
        if (value != null) {
            str = value;
        }
        GroupObj gr = null;
        for (GroupObj groupObj : index) {
            if (groupObj.getName().equals(str)) {
                gr = groupObj;
                break;
            }
        }
        if (gr == null) {
            index.add(new GroupObj(str));
        } else {
            gr.getCount().incrementAndGet();
        }
    }

    public static byte[] accountCToString(TreeSet<AccountC> result, int limit) {
        StringBuilder sb = LocalPoolService.threadLocalBuilder.get();
        sb.append("{\"accounts\":[");
        int count = 0;
        for (AccountC accountC : result) {
            Account account = accountC.getAccount();
            count++;
            sb.append("{");

            sb.append("\"id\":");
            sb.append(account.getId());
            sb.append(",");

            sb.append("\"email\":");
            sb.append("\"");
            sb.append(account.getEmail());
            sb.append("\",");

            sb.append("\"status\":");
            sb.append("\"");
            sb.append(account.getStatus());
            sb.append("\",");

            sb.append("\"birth\":");
            sb.append(account.getBirth());
            sb.append(",");

            if (account.getFname() != null) {
                sb.append("\"fname\":");
                sb.append("\"");
                sb.append(account.getFname());
                sb.append("\",");
            }

            if (account.getSname() != null) {
                sb.append("\"sname\":");
                sb.append("\"");
                sb.append(account.getSname());
                sb.append("\",");
            }

            if (account.getStart() != 0) {
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

            if (limit == count) {
                break;
            }
        }
        sb.setLength(sb.length() - 1);
        sb.append("]}");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] accountRecToString(Set<Account> result) {
        StringBuilder sb = LocalPoolService.threadLocalBuilder.get();
        sb.append("{\"accounts\":[");
        for (Account account : result) {
            sb.append("{");

            sb.append("\"id\":");
            sb.append(account.getId());
            sb.append(",");

            sb.append("\"email\":");
            sb.append("\"");
            sb.append(account.getEmail());
            sb.append("\",");

            sb.append("\"status\":");
            sb.append("\"");
            sb.append(account.getStatus());
            sb.append("\",");

            if (account.getFname() != null) {
                sb.append("\"fname\":");
                sb.append("\"");
                sb.append(account.getFname());
                sb.append("\",");
            }

            if (account.getSname() != null) {
                sb.append("\"sname\":");
                sb.append("\"");
                sb.append(account.getSname());
                sb.append("\",");
            }

            sb.setLength(sb.length() -1);
            sb.append("},");
        }
        sb.setLength(sb.length() - 1);
        sb.append("]}");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static void quickSortForLikes(int arrId[],int arrTs[], int begin, int end) {
        if (begin < end) {
            int partitionIndex = partition(arrId, arrTs, begin, end);

            quickSortForLikes(arrId, arrTs, begin, partitionIndex - 1);
            quickSortForLikes(arrId, arrTs, partitionIndex + 1, end);
        }
    }

    private static int partition(int arrId[],int arrTs[], int begin, int end) {
        int pivot = arrId[end];
        int i = (begin-1);

        for (int j = begin; j < end; j++) {
            if (arrId[j] <= pivot) {
                i++;

                int swapTemp = arrId[i];
                arrId[i] = arrId[j];
                arrId[j] = swapTemp;

                swapTemp = arrTs[i];
                arrTs[i] = arrTs[j];
                arrTs[j] = swapTemp;
            }
        }

        int swapTemp = arrId[i+1];
        arrId[i+1] = arrId[end];
        arrId[end] = swapTemp;

        swapTemp = arrTs[i+1];
        arrTs[i+1] = arrTs[end];
        arrTs[end] = swapTemp;

        return i+1;
    }

}
