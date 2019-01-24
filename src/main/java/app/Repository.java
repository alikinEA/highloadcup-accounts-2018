package app;

import app.models.Account;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Alikin E.A. on 13.12.18.
 */
public class Repository {

    static volatile long currentTimeStamp = 0l;
    static volatile Long currentTimeStamp2 = 0l;
    static volatile boolean isRait = false;

    private static final String dataPath = "/tmp/data/";
    //private static final String dataPath = "/mnt/data/";

    public static ThreadLocal<Calendar> threadLocalCalendar =
            new ThreadLocal<Calendar>() {
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

    static final Object PRESENT = new Object();
    static final Account PRESENT_AC = new Account();
    static final Account[] ids = new Account[2_000_000];
    static final Map<String,Object> emails = new HashMap<>(elementCount);

    static final Map<Integer,TreeSet<Account>> interests_count = new HashMap<>();

    static final TreeSet<Account> phone_not_null = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> phone_null = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> city_not_null = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> country_not_null = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> sname_not_null = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> fname_not_null = new TreeSet<>(Comparator.comparing(Account::getId).reversed());

    static final Map<String,TreeSet<Account>> email_domain = new HashMap<>();
    static final Map<String,TreeSet<Account>> phone_code = new HashMap<>();
    static final Map<String,TreeSet<Account>> city = new HashMap<>();
    static final Map<String,TreeSet<Account>> country = new HashMap<>();
    static final Map<String,TreeSet<Account>> fname = new HashMap<>();
    static final Map<String,TreeSet<Account>> sname = new HashMap<>();

    static final Map<Integer,TreeSet<Account>> year = new HashMap<>();

    static final TreeSet<Account> premium_1 = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> premium_2 = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> premium_3 = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_m = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_f = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_status_1 = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_status_2 = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_status_3 = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_status_1_not = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_status_2_not = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_status_3_not = new TreeSet<>(Comparator.comparing(Account::getId).reversed());

    static final TreeSet<Account> list_status_1_f = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_status_2_f = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_status_3_f = new TreeSet<>(Comparator.comparing(Account::getId).reversed());

    static final TreeSet<Account> list_status_1_m = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_status_2_m = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_status_3_m = new TreeSet<>(Comparator.comparing(Account::getId).reversed());


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
            for (int i = fileCount; i > 0; i--) {
                ZipFile zipFile = new ZipFile(dataPath + "data.zip");
                FileHeader fileHeader = zipFile.getFileHeader("accounts_" + i + ".json");
                if (fileHeader.getFileName().contains("accounts")) {
                   // System.out.println("file= " + fileHeader.getFileName() + ",time = " + new Date().getTime());
                    try (InputStream inputStream = zipFile.getInputStream(fileHeader)) {
                        List<Any> json = JsonIterator.deserialize(Utils.readBytes(inputStream)).get("accounts").asList();
                        for (Any accountAny : json) {
                            Account account = Utils.anyToAccount(accountAny,false);
                            emails.put(account.getEmail(),PRESENT);
                            if (!isRait || i > 130 - 31) {
                                list.add(account);
                                ids[account.getId()] = account;
                            } else {
                                ids[account.getId()] = PRESENT_AC;
                            }
                        }
                        json = null;
                    }
                }
                System.gc();
            }

            System.out.println("list size = " + list.size());
            System.out.println("list ids size = " + ids.length);
            System.out.println("list emails size = " + emails.size());

            for (int i = 0; i < 11; i++) {
                interests_count.put(i,new TreeSet<>(Comparator.comparing(Account::getId).reversed()));
            }
            for (Account account : list) {
                insertToIndex(account);
            }
            System.out.println("list premium_1 size = " + premium_1.size());
            System.out.println("list premium_2 size = " + premium_2.size());
            System.out.println("list premium_3 size = " + premium_3.size());

            System.out.println("warm up start = " + (new Date().getTime() - start));
            if (isRait) {
                for (int i = 0; i < 100; i++) {
                    Service.handleFilterv2("/accounts/filter/?sex_eq=f&birth_lt=642144352&limit=16&city_any=Роттеростан,Белосинки,Зеленобург,Светлокенск&country_eq=Индания&status_neq=свободны");
                }
            } else {
                for (int i = 0; i < 1_000; i++) {
                    Service.handleFilterv2("/accounts/filter/?sex_eq=f&birth_lt=642144352&limit=16&city_any=Роттеростан,Белосинки,Зеленобург,Светлокенск&country_eq=Индания&status_neq=свободны");
                }
            }

            System.gc();//¯\_(ツ)_/¯
            System.out.println("End like Set" + (new Date().getTime() - start));
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            Service.lock.writeLock().unlock();
        }

    }

    public static void insertToIndex(Account account) {
        String email = account.getEmail();
        String domain = email.substring(email.indexOf("@") + 1).intern();
        TreeSet<Account> domainIndex = email_domain.get(domain);
        if (domainIndex == null) {
            domainIndex = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
            domainIndex.add(account);
            email_domain.put(domain,domainIndex);
        } else {
            domainIndex.add(account);
        }

        if (account.getInterests() != null && account.getInterests().size() > 0) {
            for (int size = account.getInterests().size(); size > 0; size--) {
                TreeSet<Account> interestCountIndex = interests_count.get(size);
                interestCountIndex.add(account);
            }
        }
        if (account.getPhone() != null) {
            phone_not_null.add(account);
            String code = account.getPhone()
                    .substring(account.getPhone().indexOf("(") + 1
                            , account.getPhone().indexOf(")")).intern();
            TreeSet<Account> codeIndex = phone_code.get(code);
            if (codeIndex == null) {
                codeIndex = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                codeIndex.add(account);
                phone_code.put(code,codeIndex);
            } else {
                codeIndex.add(account);
            }
        } else {
            phone_null.add(account);
        }
        if (account.getStart() != 0) {
            if (currentTimeStamp2 < account.getFinish()
                    && currentTimeStamp2 > account.getStart()) {
                premium_1.add(account);
            }
            premium_2.add(account);
        } else {
            premium_3.add(account);
        }
        TreeSet<Account> list = Repository.sname.get(account.getSname());
        if (list != null) {
            list.add(account);
        } else {
            list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
            list.add(account);
            Repository.sname.put(account.getSname(),list);
        }
        list = Repository.fname.get(account.getFname());
        if (list != null) {
            list.add(account);
        } else {
            list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
            list.add(account);
            Repository.fname.put(account.getFname(),list);

        }
        list = Repository.city.get(account.getCity());
        if (list != null) {
            list.add(account);
        } else {
            list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
            list.add(account);
            Repository.city.put(account.getCity(),list);

        }
        if (account.getCity() != null) {
            Repository.city_not_null.add(account);
        }
        if (account.getCountry() != null) {
            Repository.country_not_null.add(account);
        }
        if (account.getSname() != null) {
            Repository.sname_not_null.add(account);
        }
        if (account.getFname() != null) {
            Repository.fname_not_null.add(account);
        }
        list = Repository.country.get(account.getCountry());
        if (list != null) {
            list.add(account);
        } else {
            list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
            list.add(account);
            Repository.country.put(account.getCountry(),list);
        }

        if (account.getSex().equals(Service.M)) {
            list = Repository.city.get(account.getCity() + "_m");
            if (list != null) {
                list.add(account);
            } else {
                list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                list.add(account);
                Repository.city.put(account.getCity() + "_m", list);
            }
            list = Repository.country.get(account.getCountry() + "_m");
            if (list != null) {
                list.add(account);
            } else {
                list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                list.add(account);
                Repository.country.put(account.getCountry() + "_m", list);
            }
        }
        if (account.getSex().equals(Service.F)) {
            list = Repository.city.get(account.getCity() + "_f");
            if (list != null) {
                list.add(account);
            } else {
                list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                list.add(account);
                Repository.city.put(account.getCity()+ "_f",list);
            }
            list = Repository.country.get(account.getCountry() + "_f");
            if (list != null) {
                list.add(account);
            } else {
                list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                list.add(account);
                Repository.country.put(account.getCountry()+ "_f",list);
            }
        }
        if (account.getSex().equals(Service.M)) {
            Repository.list_m.add(account);
        } else {
            Repository.list_f.add(account);
        }
        if (account.getStatus().equals(Service.STATUS1)) {
            Repository.list_status_1.add(account);
            Repository.list_status_2_not.add(account);
            Repository.list_status_3_not.add(account);
        } else if (account.getStatus().equals(Service.STATUS2)) {
            Repository.list_status_2.add(account);
            Repository.list_status_1_not.add(account);
            Repository.list_status_3_not.add(account);
        } else {
            Repository.list_status_3.add(account);
            Repository.list_status_2_not.add(account);
            Repository.list_status_1_not.add(account);
        }

        if (account.getSex().equals(Service.M)
                && account.getStatus().equals(Service.STATUS1)) {
            Repository.list_status_1_m.add(account);
        }

        if (account.getSex().equals(Service.M)
                && account.getStatus().equals(Service.STATUS2)) {
            Repository.list_status_2_m.add(account);
        }

        if (account.getSex().equals(Service.M)
                && account.getStatus().equals(Service.STATUS3)) {
            Repository.list_status_3_m.add(account);
        }

        if (account.getSex().equals(Service.F)
                && account.getStatus().equals(Service.STATUS1)) {
            Repository.list_status_1_f.add(account);
        }

        if (account.getSex().equals(Service.F)
                && account.getStatus().equals(Service.STATUS2)) {
            Repository.list_status_2_f.add(account);
        }

        if (account.getSex().equals(Service.F)
                && account.getStatus().equals(Service.STATUS3)) {
            Repository.list_status_3_f.add(account);
        }

        Calendar calendar = threadLocalCalendar.get();
        calendar.setTimeInMillis((long)account.getBirth() * 1000);
        Integer yearValue = calendar.get(Calendar.YEAR);
        list = Repository.year.get(yearValue);
        if (list != null) {
            list.add(account);
        } else {
            list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
            list.add(account);
            Repository.year.put(yearValue,list);
        }

    }



}
