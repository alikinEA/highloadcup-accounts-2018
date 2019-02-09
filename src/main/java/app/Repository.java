package app;

import app.models.Account;
import app.models.Like;
import app.server.Server;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import gnu.trove.list.linked.TLinkedList;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.roaringbitmap.RoaringBitmap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Alikin E.A. on 13.12.18.
 */
public class Repository {

    static volatile long currentTimeStamp = 0l;
    static volatile Long currentTimeStamp2 = 0l;
    static volatile boolean isRait = false;

    //private static final String dataPath = "/tmp/data/";
    private static final String dataPath = "/mnt/data/";

    public static final Comparator idsComparator = (Comparator<Account>) (o1, o2) -> {
        if (o1 == null) {
            return 0;
        }
        if (o2 == null) {
            return 0;
        }
        return o2.getId() - o1.getId();
    };

    private static final Comparator birthComparatorLt = (Comparator<Account>) (o1, o2) -> {
        if (o1 == null) {
            return 0;
        }
        if (o2 == null) {
            return 0;
        }
        return o2.getBirth() - o1.getBirth();
    };

    private static final Comparator birthComparatorGt = (Comparator<Account>) (o1, o2) -> {
        if (o1 == null) {
            return 0;
        }
        if (o2 == null) {
            return 0;
        }
        return o1.getBirth() - o2.getBirth();
    };

    public static ThreadLocal<Calendar> threadLocalCalendar =
            new ThreadLocal<>() {
                @Override
                protected Calendar initialValue() {
                    return new GregorianCalendar();
                }

                @Override
                public Calendar get() {
                    Calendar b = super.get();
                    return b;
                }

            };

    private static final int elementCount = 1300_000  + 21_600;

    public static final AtomicInteger index = new AtomicInteger(-1);
    public static final AtomicInteger index_premium_1 = new AtomicInteger(-1);
    public static final AtomicInteger index_premium_2 = new AtomicInteger(-1);
    public static final AtomicInteger index_premium_3 = new AtomicInteger(-1);

    public static final AtomicInteger index_status_1 = new AtomicInteger(-1);
    public static final AtomicInteger index_status_2 = new AtomicInteger(-1);
    public static final AtomicInteger index_status_3 = new AtomicInteger(-1);
    public static final AtomicInteger index_status_1_not = new AtomicInteger(-1);
    public static final AtomicInteger index_status_2_not = new AtomicInteger(-1);
    public static final AtomicInteger index_status_3_not = new AtomicInteger(-1);

    //public static final AtomicInteger index_city_not_null = new AtomicInteger(-1);
    //public static final AtomicInteger index_country_not_null = new AtomicInteger(-1);

    public static final AtomicInteger index_f = new AtomicInteger(-1);
    public static final AtomicInteger index_m = new AtomicInteger(-1);

    static final Account[] ids = new Account[2_000_000];
    static final Set<String> emails = new THashSet<>(elementCount);
    static final Account[] list = new Account[elementCount];
    static final Map<String,Byte> interests = new THashMap(90);

    static final Map<String,Account[]> sname_by_name = new THashMap(1700);
    static final Map<String,Integer> sname_by_name_idx_num = new THashMap(1700);

    static final Map<String,Account[]> city_by_name = new THashMap(650);
    static final Map<String,Integer> city_by_name_idx_num = new THashMap(650);

    static final Map<String,Account[]> fname_by_name = new THashMap(120);
    static final Map<String,Integer> fname_by_name_idx_num = new THashMap(120);

    static final Map<String,Account[]> phone_code_by_name = new THashMap(110);
    static final Map<String,Integer> phone_code_by_name_idx_num = new THashMap(110);

    static final Map<String,Account[]> country_by_name = new THashMap(100);
    static final Map<String,Integer> country_by_name_idx_num = new THashMap(100);

    static final Map<String,Account[]> interests_by_name = new THashMap(90);
    static final Map<String,Integer> interests_by_name_idx_num = new THashMap(90);

    static final Map<Integer,Account[]> year = new THashMap(30);
    static final Map<Integer,Integer> year_idx_num = new THashMap(30);

    static final Map<String,Account[]> email_domain_by_name = new THashMap(15);
    static final Map<String,Integer> email_domain_by_name_idx_num = new THashMap(15);

    //static final Account[] birth_idx_lt = new Account[elementCount];
    //static final Account[] birth_idx_gt = new Account[elementCount];

    static final Account[] premium_1 = new Account[200_000];
    static final Account[] premium_2 = new Account[500_000];
    static final Account[] premium_3 = new Account[900_000];


    static final Account[] status_1 = new Account[700_000];
    static final Account[] status_2 = new Account[300_000];
    static final Account[] status_3 = new Account[400_000];
    static final Account[] status_1_not = new Account[700_000];
    static final Account[] status_2_not = new Account[1_100_000];
    static final Account[] status_3_not = new Account[1_000_000];

    //static final Account[] city_not_null = new Account[elementCount];
    //static final Account[] country_not_null = new Account[elementCount];

    static final Account[] list_f = new Account[700_000];
    static final Account[] list_m = new Account[700_000];

    public static void initData() {
        long start = new Date().getTime();
        System.out.println("Start = " + start);
        try {
            Service.lock.writeLock().lock();
            int fileCount = 3;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataPath + "options.txt")))){
                String timestamp = reader.readLine();
                currentTimeStamp = Long.valueOf(timestamp + "000");
                currentTimeStamp2 = Long.valueOf(timestamp);
                String flag = reader.readLine();
                if (Integer.parseInt(flag) == 1) {
                    isRait = true;
                    fileCount = 130;
                }
                System.out.println("isRait = " + isRait);
                System.out.println("external timestamp = " + currentTimeStamp);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Byte intIndex = 0;
            for (int i = fileCount; i > 0; i--) {
                ZipFile zipFile = new ZipFile(dataPath + "data.zip");
                FileHeader fileHeader = zipFile.getFileHeader("accounts_" + i + ".json");
                 if (fileHeader.getFileName().contains("accounts")) {
                    System.out.println("file= " + fileHeader.getFileName() + ",time = " + new Date().getTime());
                    try (InputStream inputStream = zipFile.getInputStream(fileHeader)) {
                        List<Any> json = JsonIterator.deserialize(Utils.readBytes(inputStream)).get("accounts").asList();
                        for (Any accountAny : json) {
                            Account account = Utils.anyToAccount(accountAny);
                            emails.add(account.getEmail());
                            ids[account.getId()] = account;
                            list[index.incrementAndGet()] = account;
                            if (account.getInterests() != null) {
                                for (String interest : account.getInterests()) {
                                    Byte intId = interests.get(interest);
                                    if (intId == null) {
                                        interests.put(interest,intIndex);
                                        intIndex++;
                                    }
                                    Integer count = interests_by_name_idx_num.get(interest);
                                    if (count == null) {
                                        interests_by_name_idx_num.put(interest,1);
                                    } else {
                                        count++;
                                        interests_by_name_idx_num.put(interest,count);
                                    }
                                }
                            }
                            Calendar calendar = threadLocalCalendar.get();
                            calendar.setTimeInMillis((long)account.getBirth() * 1000);
                            Integer yearValue = calendar.get(Calendar.YEAR);

                            Integer countYear = year_idx_num.get(yearValue);
                            if (countYear == null) {
                                year_idx_num.put(yearValue,1);
                            } else {
                                countYear++;
                                year_idx_num.put(yearValue,countYear);
                            }

                            Integer countCity = city_by_name_idx_num.get(account.getCity());
                            if (countCity == null) {
                                city_by_name_idx_num.put(account.getCity(),1);
                            } else {
                                countCity++;
                                city_by_name_idx_num.put(account.getCity(),countCity);
                            }

                            Integer countSname = sname_by_name_idx_num.get(account.getSname());
                            if (countSname == null) {
                                sname_by_name_idx_num.put(account.getSname(),1);
                            } else {
                                countSname++;
                                sname_by_name_idx_num.put(account.getSname(),countSname);
                            }

                            Integer countFname = fname_by_name_idx_num.get(account.getFname());
                            if (countFname == null) {
                                fname_by_name_idx_num.put(account.getFname(),1);
                            } else {
                                countFname++;
                                fname_by_name_idx_num.put(account.getFname(),countFname);
                            }

                            if (account.getPhone() != null) {
                                String code = account.getPhone()
                                        .substring(account.getPhone().indexOf("(") + 1
                                                , account.getPhone().indexOf(")")).intern();
                                Integer countPhoneCode = phone_code_by_name_idx_num.get(code);
                                if (countPhoneCode == null) {
                                    phone_code_by_name_idx_num.put(code, 1);
                                } else {
                                    countPhoneCode++;
                                    phone_code_by_name_idx_num.put(code, countPhoneCode);
                                }
                            }

                            Integer countCountry = country_by_name_idx_num.get(account.getCountry());
                            if (countCountry == null) {
                                country_by_name_idx_num.put(account.getCountry(),1);
                            } else {
                                countCountry++;
                                country_by_name_idx_num.put(account.getCountry(),countCountry);
                            }

                            String domain = account.getEmail().substring(account.getEmail().indexOf("@") + 1).intern();
                            Integer countDomain = email_domain_by_name_idx_num.get(domain);
                            if (countDomain == null) {
                                email_domain_by_name_idx_num.put(domain,1);
                            } else {
                                countDomain++;
                                email_domain_by_name_idx_num.put(domain,countDomain);
                            }
                        }
                        json = null;
                    }
                }
                System.gc();
            }
            for(Map.Entry<Integer, Integer> entry : year_idx_num.entrySet()) {
                year.put(entry.getKey(),new Account[year_idx_num.get(entry.getKey()) + 21_600]);
                year_idx_num.put(entry.getKey(), 0);
            }

            for(Map.Entry<String, Integer> entry : interests_by_name_idx_num.entrySet()) {
                interests_by_name.put(entry.getKey(),new Account[interests_by_name_idx_num.get(entry.getKey()) + 21_600]);
                interests_by_name_idx_num.put(entry.getKey(), 0);
            }

            for(Map.Entry<String, Integer> entry : sname_by_name_idx_num.entrySet()) {
                sname_by_name.put(entry.getKey(),new Account[sname_by_name_idx_num.get(entry.getKey()) + 21_600]);
                sname_by_name_idx_num.put(entry.getKey(), 0);
            }

            for(Map.Entry<String, Integer> entry : fname_by_name_idx_num.entrySet()) {
                fname_by_name.put(entry.getKey(),new Account[fname_by_name_idx_num.get(entry.getKey()) + 21_600]);
                fname_by_name_idx_num.put(entry.getKey(), 0);
            }

            for(Map.Entry<String, Integer> entry : phone_code_by_name_idx_num.entrySet()) {
                phone_code_by_name.put(entry.getKey(),new Account[phone_code_by_name_idx_num.get(entry.getKey()) + 21_600]);
                phone_code_by_name_idx_num.put(entry.getKey(), 0);
            }

            for(Map.Entry<String, Integer> entry : city_by_name_idx_num.entrySet()) {
                city_by_name.put(entry.getKey(),new Account[city_by_name_idx_num.get(entry.getKey()) + 21_600]);
                city_by_name_idx_num.put(entry.getKey(), 0);
            }

            for(Map.Entry<String, Integer> entry : country_by_name_idx_num.entrySet()) {
                country_by_name.put(entry.getKey(),new Account[country_by_name_idx_num.get(entry.getKey()) + 21_600]);
                country_by_name_idx_num.put(entry.getKey(), 0);
            }

            for(Map.Entry<String, Integer> entry : email_domain_by_name_idx_num.entrySet()) {
                email_domain_by_name.put(entry.getKey(),new Account[email_domain_by_name_idx_num.get(entry.getKey()) + 21_600]);
                email_domain_by_name_idx_num.put(entry.getKey(), 0);
            }

            for (Account account : list) {
                insertToIndex(account);
            }

            reSortIndex();
            System.out.println("list size = " + index);
            System.out.println("emails size = " + emails.size());
            System.gc();//¯\_(ツ)_/¯
            System.out.println("End" + (new Date().getTime() - start));

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            Service.lock.writeLock().unlock();
        }
    }

    private static void insertToIndex(Account account) {
        if (account != null) {
            //account.setInterestBitmap(getInterestBitMap(account.getInterests()));
           // account.setLikesBitmap(getLikesBitMap(account.getLikes()));

            if (account.getPhone() != null) {
               // phone_not_null.add(account);
                String code = account.getPhone()
                        .substring(account.getPhone().indexOf("(") + 1
                                , account.getPhone().indexOf(")")).intern();
                Account[] index = phone_code_by_name.get(code);
                Integer idx = phone_code_by_name_idx_num.get(code);
                index[idx] = account;
                idx++;
                phone_code_by_name_idx_num.put(code,idx);
            } else {
                //phone_null.add(account);
            }

            /*if (account.getCity() != null) {
                city_not_null[index_city_not_null.incrementAndGet()] = account;
            }

            if (account.getCountry() != null) {
                country_not_null[index_country_not_null.incrementAndGet()] = account;
            }*/

            if (account.getInterests() != null) {
                for (String interest : account.getInterests()) {
                    Account[] index = interests_by_name.get(interest);
                    Integer idx = interests_by_name_idx_num.get(interest);
                    index[idx] = account;
                    idx++;
                    interests_by_name_idx_num.put(interest,idx);
                }
            }

            Account[] index = city_by_name.get(account.getCity());
            Integer idx = city_by_name_idx_num.get(account.getCity());
            index[idx] = account;
            idx++;
            city_by_name_idx_num.put(account.getCity(),idx);

            index = country_by_name.get(account.getCountry());
            idx = country_by_name_idx_num.get(account.getCountry());
            index[idx] = account;
            idx++;
            country_by_name_idx_num.put(account.getCountry(),idx);

            index = sname_by_name.get(account.getSname());
            idx = sname_by_name_idx_num.get(account.getSname());
            index[idx] = account;
            idx++;
            sname_by_name_idx_num.put(account.getSname(),idx);

            index = fname_by_name.get(account.getFname());
            idx = fname_by_name_idx_num.get(account.getFname());
            index[idx] = account;
            idx++;
            fname_by_name_idx_num.put(account.getFname(),idx);

            String email = account.getEmail();
            String domain = email.substring(email.indexOf("@") + 1).intern();
            index = email_domain_by_name.get(domain);
            idx = email_domain_by_name_idx_num.get(domain);
            index[idx] = account;
            idx++;
            email_domain_by_name_idx_num.put(domain,idx);

            Calendar calendar = threadLocalCalendar.get();
            calendar.setTimeInMillis((long)account.getBirth() * 1000);
            Integer yearValue = calendar.get(Calendar.YEAR);
            index = Repository.year.get(yearValue);
            idx = year_idx_num.get(yearValue);
            index[idx] = account;
            idx++;
            year_idx_num.put(yearValue,idx);

            if (account.getStart() != 0) {
                if (currentTimeStamp2 < account.getFinish()
                        && currentTimeStamp2 > account.getStart()) {
                    premium_1[index_premium_1.incrementAndGet()] = account;
                }
                premium_2[index_premium_2.incrementAndGet()] = account;
            } else {
                premium_3[index_premium_3.incrementAndGet()] = account;
            }

            if (account.getSex() == Service.F) {
                list_f[index_f.incrementAndGet()] = account;
            } else {
                list_m[index_m.incrementAndGet()] = account;
            }

            if (account.getStatus().equals(Service.STATUS1)) {
                Repository.status_1[index_status_1.incrementAndGet()] = account;
                Repository.status_2_not[index_status_2_not.incrementAndGet()] = account;
                Repository.status_3_not[index_status_3_not.incrementAndGet()] = account;
            } else if (account.getStatus().equals(Service.STATUS2)) {
                Repository.status_2[index_status_2.incrementAndGet()] = account;
                Repository.status_1_not[index_status_1_not.incrementAndGet()] = account;
                Repository.status_3_not[index_status_3_not.incrementAndGet()] = account;
            } else {
                Repository.status_3[index_status_3.incrementAndGet()] = account;
                Repository.status_2_not[index_status_2_not.incrementAndGet()] = account;
                Repository.status_1_not[index_status_1_not.incrementAndGet()] = account;
            }

            //birth_idx_gt[idxC] = account;
            //birth_idx_lt[idxC] = account;
        }
    }

    public static RoaringBitmap getLikesBitMap(int[] likes) {
        if (likes == null) {
            return null;
        }
        return RoaringBitmap.bitmapOf(likes);
    }

    public static RoaringBitmap getInterestBitMap(String[] interests) {
        if (interests == null) {
            return null;
        }

        RoaringBitmap bitmap = new RoaringBitmap();
        for (String interest : interests) {
            bitmap.add(Repository.interests.get(interest));
        }

        return bitmap;
    }

    public static void reSortIndex() {
        long start = new Date().getTime();
        System.out.println("Start reindex = " + start);

        Arrays.sort(list,idsComparator);

        Arrays.sort(list_f,idsComparator);
        Arrays.sort(list_m,idsComparator);

        Arrays.sort(premium_1,idsComparator);
        Arrays.sort(premium_2,idsComparator);
        Arrays.sort(premium_3,idsComparator);

        Arrays.sort(status_1,idsComparator);
        Arrays.sort(status_2,idsComparator);
        Arrays.sort(status_3,idsComparator);

        Arrays.sort(status_1_not,idsComparator);
        Arrays.sort(status_2_not,idsComparator);
        Arrays.sort(status_3_not,idsComparator);

        //Arrays.sort(city_not_null,idsComparator);
        //Arrays.sort(country_not_null,idsComparator);

        for(Map.Entry<String, Account[]> entry : phone_code_by_name.entrySet()) {
            Arrays.sort(entry.getValue(),idsComparator);
        }
        for(Map.Entry<String, Account[]> entry : interests_by_name.entrySet()) {
            Arrays.sort(entry.getValue(),idsComparator);
        }
        for(Map.Entry<String, Account[]> entry : city_by_name.entrySet()) {
            Arrays.sort(entry.getValue(),idsComparator);
        }
        for(Map.Entry<String, Account[]> entry : sname_by_name.entrySet()) {
            Arrays.sort(entry.getValue(),idsComparator);
        }
        for(Map.Entry<String, Account[]> entry : fname_by_name.entrySet()) {
            Arrays.sort(entry.getValue(),idsComparator);
        }
        for(Map.Entry<String, Account[]> entry : country_by_name.entrySet()) {
            Arrays.sort(entry.getValue(),idsComparator);
        }
        for(Map.Entry<String, Account[]> entry : email_domain_by_name.entrySet()) {
            Arrays.sort(entry.getValue(),idsComparator);
        }
        for(Map.Entry<Integer, Account[]> entry : year.entrySet()) {
            Arrays.sort(entry.getValue(),idsComparator);
        }

        //Arrays.sort(birth_idx_lt,birthComparatorLt);
        //Arrays.sort(birth_idx_gt,birthComparatorGt);
        System.gc();// перерыв между фазами
        System.out.println("end reindex = " + (new Date().getTime() - start) );
    }



}
