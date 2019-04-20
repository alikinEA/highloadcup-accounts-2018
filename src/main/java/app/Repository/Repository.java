package app.Repository;

import app.models.Account;
import app.models.Constants;
import app.server.Server;
import app.service.LocalPoolService;
import app.service.Service;
import app.utils.Utils;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static app.utils.Comparators.idsComparator;

/**
 * Created by Alikin E.A. on 13.12.18.
 */
@SuppressWarnings("WeakerAccess")
public class Repository {

    public static volatile long currentTimeStamp = 0l;
    public static volatile Long currentTimeStamp2 = 0l;
    public static volatile boolean isRait = false;

    public static final AtomicInteger queryCount = new AtomicInteger(0);
    public static volatile Map<String,byte[]> queryCache;
    public static volatile Map<String,byte[]> queryCacheRec;
    public static volatile Map<String,byte[]> queryCacheSug;

    private static final String dataPath = "/tmp/data/";
    //private static final String dataPath = "/mnt/data/";

    private static final int NEW_ACCOUNT = 18_500;
    private static final int elementCount = 1300_000 + NEW_ACCOUNT;
    public static final int MAX_ID = 1_520_000;

    public static final AtomicInteger index = new AtomicInteger(-1);
    private static final AtomicInteger index_premium_1 = new AtomicInteger(-1);
    private static final AtomicInteger index_premium_2 = new AtomicInteger(-1);
    private static final AtomicInteger index_premium_3 = new AtomicInteger(-1);

    public static final AtomicInteger index_premium_1_f = new AtomicInteger(-1);
    public static final AtomicInteger index_premium_1_m = new AtomicInteger(-1);

    public static final AtomicInteger index_premium_2_f = new AtomicInteger(-1);
    public static final AtomicInteger index_premium_2_m = new AtomicInteger(-1);

    public static final AtomicInteger index_premium_3_f = new AtomicInteger(-1);
    public static final AtomicInteger index_premium_3_m = new AtomicInteger(-1);

    public static final AtomicInteger index_status_1_f_not_premium = new AtomicInteger(-1);
    public static final AtomicInteger index_status_1_m_not_premium = new AtomicInteger(-1);

    public static final AtomicInteger index_status_1 = new AtomicInteger(-1);
    public static final AtomicInteger index_status_2 = new AtomicInteger(-1);
    public static final AtomicInteger index_status_3 = new AtomicInteger(-1);
    public static final AtomicInteger index_status_1_not = new AtomicInteger(-1);
    public static final AtomicInteger index_status_2_not = new AtomicInteger(-1);
    public static final AtomicInteger index_status_3_not = new AtomicInteger(-1);

    public static final AtomicInteger index_f = new AtomicInteger(-1);
    public static final AtomicInteger index_m = new AtomicInteger(-1);

    public static final Map<String,Account[]> country_by_name_status_1_not_premium = new THashMap<>(200,1);
    private static final Map<String,Integer> country_by_name_status_1_not_premium_idx_num = new THashMap<>(200,1);

    public static final Account[] ids = new Account[MAX_ID];
    public static final TIntObjectHashMap<Account[]> likeInvert = new TIntObjectHashMap<>(elementCount,1);
    public static final Set<String> emails = new THashSet<>(elementCount,1);
    public static final Account[] list = new Account[elementCount];
    private static final Map<String,Byte> interests = new THashMap<>(90,1);

    public static final Map<String,Account[]> sname_by_name = new THashMap<>(1700,1);
    private static final Map<String,Integer> sname_by_name_idx_num = new THashMap<>(1700,1);

    public static final Map<String,Account[]> city_by_name = new THashMap<>(650,1);
    private static final Map<String,Integer> city_by_name_idx_num = new THashMap<>(650,1);

    public static final Map<String,Account[]> fname_by_name = new THashMap<>(120,1);
    private static final Map<String,Integer> fname_by_name_idx_num = new THashMap<>(120,1);

    public static final Map<String,Account[]> phone_code_by_name = new THashMap<>(110,1);
    private static final Map<String,Integer> phone_code_by_name_idx_num = new THashMap<>(110,1);

    public static final Map<String,Account[]> country_by_name = new THashMap<>(100,1);
    private static final Map<String,Integer> country_by_name_idx_num = new THashMap<>(100,1);

    public static final Map<String,Account[]> interests_by_name = new THashMap<>(90,1);
    private static final Map<String,Integer> interests_by_name_idx_num = new THashMap<>(90,1);

    public static final Map<Integer,Account[]> year = new THashMap<>(30,1);
    private static final Map<Integer,Integer> year_idx_num = new THashMap<>(30,1);

    public static final Map<String,Account[]> email_domain_by_name = new THashMap<>(15,1);
    private static final Map<String,Integer> email_domain_by_name_idx_num = new THashMap<>(15,1);

    public static final Account[] premium_1 = new Account[135_936];
    public static final Account[] premium_2 = new Account[413_874];
    public static final Account[] premium_3 = new Account[909_506];

    public static final Account[] premium_1_m = new Account[70_000];
    public static final Account[] premium_1_f = new Account[70_000];

    public static final Account[] premium_2_m = new Account[210_000];
    public static final Account[] premium_2_f = new Account[210_000];

    public static final Account[] premium_3_m = new Account[457_000];
    public static final Account[] premium_3_f = new Account[457_000];

    public static final Account[] status_1_f_not_premium = new Account[335_000];
    public static final Account[] status_1_m_not_premium = new Account[335_000];

    public static final Account[] status_1 = new Account[667_655];
    public static final Account[] status_2 = new Account[266_824];
    public static final Account[] status_3 = new Account[400_720];
    public static final Account[] status_1_not = new Account[667_544];
    public static final Account[] status_2_not = new Account[1_068_375];
    public static final Account[] status_3_not = new Account[934_479];

    public static final Account[] list_f = new Account[659_140];
    public static final Account[] list_m = new Account[659_140];


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
                    //System.out.println("file= " + fileHeader.getFileName() + ",time = " + new Date().getTime());
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
                            Calendar calendar = LocalPoolService.threadLocalCalendar.get();
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
                        System.gc();
                    }
                }
            }
            for(Map.Entry<Integer, Integer> entry : year_idx_num.entrySet()) {
                year.put(entry.getKey(),new Account[year_idx_num.get(entry.getKey()) + 10_000]);
                year_idx_num.put(entry.getKey(), 0);
            }

            for(Map.Entry<String, Integer> entry : interests_by_name_idx_num.entrySet()) {
                interests_by_name.put(entry.getKey(),new Account[45_000]);
                interests_by_name_idx_num.put(entry.getKey(), 0);
            }

            for(Map.Entry<String, Integer> entry : sname_by_name_idx_num.entrySet()) {
                sname_by_name.put(entry.getKey(),new Account[sname_by_name_idx_num.get(entry.getKey()) + 5_000]);
                sname_by_name_idx_num.put(entry.getKey(), 0);
            }

            for(Map.Entry<String, Integer> entry : fname_by_name_idx_num.entrySet()) {
                fname_by_name.put(entry.getKey(),new Account[fname_by_name_idx_num.get(entry.getKey()) + 5_000]);
                fname_by_name_idx_num.put(entry.getKey(), 0);
            }

            for(Map.Entry<String, Integer> entry : phone_code_by_name_idx_num.entrySet()) {
                phone_code_by_name.put(entry.getKey(),new Account[6000]);
                phone_code_by_name_idx_num.put(entry.getKey(), 0);
            }

            for(Map.Entry<String, Integer> entry : city_by_name_idx_num.entrySet()) {
                city_by_name.put(entry.getKey(),new Account[city_by_name_idx_num.get(entry.getKey()) + 8_000]);
                city_by_name_idx_num.put(entry.getKey(), 0);
            }

            for(Map.Entry<String, Integer> entry : country_by_name_idx_num.entrySet()) {
                country_by_name_status_1_not_premium.put(entry.getKey() + Constants.F,new Account[10_000]);
                country_by_name_status_1_not_premium_idx_num.put(entry.getKey() + Constants.F, 0);

                country_by_name_status_1_not_premium.put(entry.getKey() + Constants.M,new Account[10_000]);
                country_by_name_status_1_not_premium_idx_num.put(entry.getKey() + Constants.M, 0);

                country_by_name.put(entry.getKey(),new Account[country_by_name_idx_num.get(entry.getKey()) + 5_000]);
                country_by_name_idx_num.put(entry.getKey(), 0);

            }

            for(Map.Entry<String, Integer> entry : email_domain_by_name_idx_num.entrySet()) {
                email_domain_by_name.put(entry.getKey(),new Account[110_000]);
                email_domain_by_name_idx_num.put(entry.getKey(), 0);
            }

            for (Account account : list) {
                insertToIndex(account);
            }

            reSortIndex();
            initCache();
            System.out.println("emails size = " + emails.size());
            System.gc();
            Server.printCurrentMemoryUsage();
            System.out.println("End" + (new Date().getTime() - start));
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            Service.lock.writeLock().unlock();
        }
    }

    public static void insertToIndex(Account account) {
        if (account != null) {
            updatePhoneIndex(account);
            updateInterestIndex(account);
            updateCityIndex(account);
            updateCountryIndex(account);
            updateSnameIndex(account);
            updateFnameIndex(account);
            updateEmailIndex(account);
            updateYearIndex(account);
            updatePremiumIndex(account);
            updateSexIndex(account);
            updateStatusIndex(account);
            updateLikesInvertIndex(account);

            GroupRepository.insertGroupIndex(account);
        }
    }

    public static void updateLikesInvertIndex(Account account) {
        if (account.getLikes() != null) {
            for (int like : account.getLikes()) {
                Account[] invertLikesArr = likeInvert.get(like);
                if (invertLikesArr == null) {
                    invertLikesArr = new Account[1];
                    invertLikesArr[0] = account;
                    likeInvert.put(like, invertLikesArr);

                } else {
                    invertLikesArr = Arrays.copyOf(invertLikesArr, invertLikesArr.length + 1);
                    invertLikesArr[invertLikesArr.length - 1] = account;
                    Arrays.sort(invertLikesArr, idsComparator);
                    likeInvert.put(like, invertLikesArr);
                }
            }
            Utils.quickSortForLikes(account.getLikes(),account.getLikesTs(),0,account.getLikes().length -1 );
        }
    }

    public static void updateStatusIndex(Account account) {
        if (account.getStatus().equals(Constants.STATUS1)) {
            Repository.status_1[index_status_1.incrementAndGet()] = account;
            Repository.status_2_not[index_status_2_not.incrementAndGet()] = account;
            Repository.status_3_not[index_status_3_not.incrementAndGet()] = account;
        } else if (account.getStatus().equals(Constants.STATUS2)) {
            Repository.status_2[index_status_2.incrementAndGet()] = account;
            Repository.status_1_not[index_status_1_not.incrementAndGet()] = account;
            Repository.status_3_not[index_status_3_not.incrementAndGet()] = account;
        } else {
            Repository.status_3[index_status_3.incrementAndGet()] = account;
            Repository.status_2_not[index_status_2_not.incrementAndGet()] = account;
            Repository.status_1_not[index_status_1_not.incrementAndGet()] = account;
        }
    }

    public static void updateSexIndex(Account account) {
        if (account.getSex() == Constants.F) {
            list_f[index_f.incrementAndGet()] = account;
        } else {
            list_m[index_m.incrementAndGet()] = account;
        }
    }

    public static void updatePremiumIndex(Account account) {
        if (account.getStart() != 0) {
            if (currentTimeStamp2 < account.getFinish()
                    && currentTimeStamp2 > account.getStart()) {
                premium_1[index_premium_1.incrementAndGet()] = account;
                if (account.getSex() == Constants.F) {
                    premium_1_f[index_premium_1_f.incrementAndGet()] = account;
                } else {
                    premium_1_m[index_premium_1_m.incrementAndGet()] = account;
                }
            }
            if (account.getStatus() == Constants.STATUS1) {
                if (account.getCountry() != null) {
                    String postfix = Constants.F;
                    if (account.getSex() == Constants.M) {
                        postfix = Constants.M;
                    }
                    String key = account.getCountry() + postfix;
                    Account[] index = country_by_name_status_1_not_premium.get(key);
                    Integer idx = country_by_name_status_1_not_premium_idx_num.get(key);
                    index[idx] = account;
                    idx++;
                    country_by_name_status_1_not_premium_idx_num.put(key, idx);
                }
                if (account.getSex() == Constants.F) {
                    status_1_f_not_premium[index_status_1_f_not_premium.incrementAndGet()] = account;
                } else {
                    status_1_m_not_premium[index_status_1_m_not_premium.incrementAndGet()] = account;
                }
            }
            if (account.getSex() == Constants.F) {
                premium_2_f[index_premium_2_f.incrementAndGet()] = account;
            } else {
                premium_2_m[index_premium_2_m.incrementAndGet()] = account;
            }
            premium_2[index_premium_2.incrementAndGet()] = account;
        } else {
            if (account.getStatus() == Constants.STATUS1) {
                if (account.getCountry() != null) {
                    String postfix = Constants.F;
                    if (account.getSex() == Constants.M) {
                        postfix = Constants.M;
                    }
                    String key = account.getCountry() + postfix;
                    Account[] index = country_by_name_status_1_not_premium.get(key);
                    Integer idx = country_by_name_status_1_not_premium_idx_num.get(key);
                    index[idx] = account;
                    idx++;
                    country_by_name_status_1_not_premium_idx_num.put(key, idx);
                }
                if (account.getSex() == Constants.F) {
                    status_1_f_not_premium[index_status_1_f_not_premium.incrementAndGet()] = account;
                } else {
                    status_1_m_not_premium[index_status_1_m_not_premium.incrementAndGet()] = account;
                }
            }
            if (account.getSex() == Constants.F) {
                premium_3_f[index_premium_3_f.incrementAndGet()] = account;
            } else {
                premium_3_m[index_premium_3_m.incrementAndGet()] = account;
            }
            premium_3[index_premium_3.incrementAndGet()] = account;
        }
    }

    public static void updateYearIndex(Account account) {
        Calendar calendar = LocalPoolService.threadLocalCalendar.get();
        calendar.setTimeInMillis((long)account.getBirth() * 1000);
        Integer yearValue = calendar.get(Calendar.YEAR);
        Account[] index = Repository.year.get(yearValue);
        Integer idx = year_idx_num.get(yearValue);
        index[idx] = account;
        idx++;
        year_idx_num.put(yearValue,idx);
    }

    public static void updateEmailIndex(Account account) {
        String email = account.getEmail();
        String domain = email.substring(email.indexOf("@") + 1).intern();
        Account[] index = email_domain_by_name.get(domain);
        Integer idx = email_domain_by_name_idx_num.get(domain);
        index[idx] = account;
        idx++;
        email_domain_by_name_idx_num.put(domain,idx);
    }

    public static void updateFnameIndex(Account account) {
        Account[] index = fname_by_name.get(account.getFname());
        Integer idx;
        idx = fname_by_name_idx_num.get(account.getFname());
        index[idx] = account;
        idx++;
        fname_by_name_idx_num.put(account.getFname(),idx);
    }

    public static void updateSnameIndex(Account account) {
        Account[] index = sname_by_name.get(account.getSname());
        Integer idx;
        idx = sname_by_name_idx_num.get(account.getSname());
        index[idx] = account;
        idx++;
        sname_by_name_idx_num.put(account.getSname(),idx);
    }

    public static void updateCountryIndex(Account account) {
        Account[] index = country_by_name.get(account.getCountry());
        Integer idx;
        idx = country_by_name_idx_num.get(account.getCountry());
        index[idx] = account;
        idx++;
        country_by_name_idx_num.put(account.getCountry(),idx);
    }

    public static void updateCityIndex(Account account) {
        Account[] index = city_by_name.get(account.getCity());
        Integer idx;
        if (index != null) {
            idx = city_by_name_idx_num.get(account.getCity());
            index[idx] = account;
            idx++;
            city_by_name_idx_num.put(account.getCity(), idx);
        } else {
            index = new Account[100];
            idx = 0;
            index[idx] = account;
            city_by_name.put(account.getCity(),index);
            city_by_name_idx_num.put(account.getCity(), idx);
        }

    }

    public static void updateInterestIndex(Account account) {
        if (account.getInterests() != null) {
            for (String interest : account.getInterests()) {
                Account[] index = interests_by_name.get(interest);
                Integer idx = interests_by_name_idx_num.get(interest);
                index[idx] = account;
                idx++;
                interests_by_name_idx_num.put(interest,idx);
            }
        }
    }

    public static void updatePhoneIndex(Account account) {
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
    }


    public static void reSortIndex() {
        long start = new Date().getTime();
        System.out.println("Start reindex = " + start);

        Arrays.sort(list, idsComparator);

        Arrays.sort(list_f, idsComparator);
        Arrays.sort(list_m, idsComparator);

        Arrays.sort(premium_1, idsComparator);
        Arrays.sort(premium_2, idsComparator);
        Arrays.sort(premium_3, idsComparator);

        Arrays.sort(premium_1_f, idsComparator);
        Arrays.sort(premium_1_m, idsComparator);

        Arrays.sort(premium_2_f, idsComparator);
        Arrays.sort(premium_2_m, idsComparator);

        Arrays.sort(premium_3_f, idsComparator);
        Arrays.sort(premium_3_m, idsComparator);

        Arrays.sort(status_1_f_not_premium, idsComparator);
        Arrays.sort(status_1_f_not_premium, idsComparator);

        Arrays.sort(status_1, idsComparator);
        Arrays.sort(status_2, idsComparator);
        Arrays.sort(status_3, idsComparator);

        Arrays.sort(status_1_not, idsComparator);
        Arrays.sort(status_2_not, idsComparator);
        Arrays.sort(status_3_not, idsComparator);

        for(Map.Entry<String, Account[]> entry : phone_code_by_name.entrySet()) {
            Arrays.sort(entry.getValue(), idsComparator);
        }
        for(Map.Entry<String, Account[]> entry : interests_by_name.entrySet()) {
            Arrays.sort(entry.getValue(), idsComparator);
        }
        for(Map.Entry<String, Account[]> entry : city_by_name.entrySet()) {
            Arrays.sort(entry.getValue(), idsComparator);
        }
        for(Map.Entry<String, Account[]> entry : sname_by_name.entrySet()) {
            Arrays.sort(entry.getValue(), idsComparator);
        }
        for(Map.Entry<String, Account[]> entry : fname_by_name.entrySet()) {
            Arrays.sort(entry.getValue(), idsComparator);
        }
        for(Map.Entry<String, Account[]> entry : country_by_name.entrySet()) {
            Arrays.sort(entry.getValue(), idsComparator);
        }
        for(Map.Entry<String, Account[]> entry : country_by_name_status_1_not_premium.entrySet()) {
            Arrays.sort(entry.getValue(), idsComparator);
           // Utils.printIndexSize(entry.getValue(),"county count");
        }
        for(Map.Entry<String, Account[]> entry : email_domain_by_name.entrySet()) {
            Arrays.sort(entry.getValue(), idsComparator);
        }
        for(Map.Entry<Integer, Account[]> entry : year.entrySet()) {
            Arrays.sort(entry.getValue(), idsComparator);
        }

        GroupRepository.reSortIndex();
        System.out.println("end reindex = " + (new Date().getTime() - start) );
    }


    public static void resortIndexForStage() {
        queryCount.incrementAndGet();
        if (Repository.isRait) {
            if (queryCount.get() == Constants.END_2_PHASE_RAIT) {
                Repository.reSortIndex();
                initCache();

                Server.printCurrentMemoryUsage();
                System.gc();
                Server.printCurrentMemoryUsage();
            } else if (queryCount.get() == Constants.END_1_PHASE_RAIT) {
                clearSug();
                clearCache();

                Server.printCurrentMemoryUsage();
                System.gc();
                Server.printCurrentMemoryUsage();
            }
        } else {
            if (queryCount.get() == Constants.END_2_PHASE_TEST) {
                Repository.reSortIndex();
                initCache();

                Server.printCurrentMemoryUsage();
                System.gc();
                Server.printCurrentMemoryUsage();
            } else if (queryCount.get() == Constants.END_1_PHASE_TEST) {
                clearSug();
                clearCache();

                Server.printCurrentMemoryUsage();
                System.gc();
                Server.printCurrentMemoryUsage();
            }
        }
    }

    private static void clearSug() {
        for (Account account : Repository.ids) {
            if (account != null && account.getLikes() != null) {
                account.setLikes(null);
                account.setLikesTs(null);
            }
        }
    }

    private static void clearCache() {
        queryCache.clear();
        queryCache = null;

        queryCacheRec.clear();
        queryCacheRec = null;
    }

    private static void initCache() {
        queryCache = new THashMap<>(30_000,1);
        queryCacheRec = new THashMap<>(11_000,1);
        queryCacheSug = new THashMap<>(6_800,1);
    }


    /*public static RoaringBitmap getLikesBitMap(int[] likes) {
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
    }*/

}
