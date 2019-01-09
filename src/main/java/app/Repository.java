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


    private static final List<String> availableNames =
            Arrays.asList("accounts_130.json","accounts_129.json","accounts_128.json");
    private static final List<String> availableNamesTest =
            Arrays.asList("accounts_1.json"
                    ,"accounts_2.json"
                    ,"accounts_3.json"

            );
    private static final int elementCount = 1300_000  + 21_600;

    static final Object PRESENT = new Object();
    static final Account PRESENT_AC = new Account();
    static final Map<Integer,Account> ids = new HashMap<>(elementCount);
    static final Map<String,Object> emails = new HashMap<>(elementCount);
    static final Map<String,TreeSet<Account>> city = new HashMap<>();
    static final Map<String,TreeSet<Account>> country = new HashMap<>();
    static final TreeSet<Account> list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_m = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_f = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_status_1 = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_status_2 = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_status_3 = new TreeSet<>(Comparator.comparing(Account::getId).reversed());

    static final TreeSet<Account> list_status_1_f = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_status_2_f = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_status_3_f = new TreeSet<>(Comparator.comparing(Account::getId).reversed());

    static final TreeSet<Account> list_status_1_m = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_status_2_m = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final TreeSet<Account> list_status_3_m = new TreeSet<>(Comparator.comparing(Account::getId).reversed());

    //static final NavigableSet<Account> list_f_status1 = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    //static final ConcurrentHashMap<String,Object> ids = new ConcurrentHashMap<>();
    //static final ConcurrentHashMap<String,Object> emails = new ConcurrentHashMap<>();
    //static final ConcurrentSkipListSet<Account> list = new ConcurrentSkipListSet<>(Comparator.comparing(Account::getId).reversed());

    public static void initData() {
        long start = new Date().getTime();
        System.out.println("Start = " + start);
        try {
            Service.lock.writeLock().lock();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataPath + "options.txt")))){
                String timestamp = reader.readLine();
                currentTimeStamp = new Long(timestamp + "000");
                currentTimeStamp2 = new Long(timestamp);
                String flag = reader.readLine();
                if (Integer.parseInt(flag) == 1) {
                    isRait = true;
                }
                System.out.println("isRait = " + isRait);
                System.out.println("external timestamp = " + currentTimeStamp);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ZipFile zipFile = new ZipFile(dataPath + "data.zip");
            zipFile.getFileHeaders().forEach(item -> {
                if (item != null) {
                    try {
                        FileHeader fileHeader = (FileHeader)item;
                        if (fileHeader.getFileName().contains("accounts")) {
                            //System.out.println("file= " + fileHeader.getFileName() + ",time = " + new Date().getTime());
                            try (InputStream inputStream = zipFile.getInputStream(fileHeader)) {
                                List<Any> json = JsonIterator.deserialize(Utils.readBytes(inputStream)).get("accounts").asList();
                                for (Any accountAny : json) {
                                    Account account = Utils.anyToAccount(accountAny);
                                    emails.put(account.getEmail(),PRESENT);
                                    if ((isRait && availableNames.contains(fileHeader.getFileName()))
                                            || (!isRait && availableNamesTest.contains(fileHeader.getFileName()))) {
                                        list.add(account);
                                        ids.put(account.getId(), account);
                                    } else {
                                        ids.put(account.getId(), PRESENT_AC);
                                    }
                                }
                                json = null;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("list size = " + list.size());
            System.out.println("list ids = " + ids.size());
            for (Account account : list) {
                TreeSet<Account> list = Repository.city.get(account.getCity());
                if (list != null) {
                    list.add(account);
                } else {
                    list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                    list.add(account);
                    Repository.city.put(account.getCity(),list);
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
                    list_m.add(account);
                    list = Repository.city.get(account.getCity() + "_m");
                    if (list != null) {
                        list.add(account);
                    } else {
                        list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                        list.add(account);
                        Repository.city.put(account.getCity()+ "_m",list);
                    }
                    list = Repository.country.get(account.getCountry() + "_m");
                    if (list != null) {
                        list.add(account);
                    } else {
                        list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
                        list.add(account);
                        Repository.country.put(account.getCountry()+ "_m",list);
                    }
                } else {
                    list_f.add(account);
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
                if (account.getStatus().equals(Service.STATUS1)) {
                    list_status_1.add(account);
                } else if (account.getStatus().equals(Service.STATUS2)) {
                    list_status_2.add(account);
                } else {
                    list_status_3.add(account);
                }

                if (account.getSex().equals(Service.M)
                        && account.getStatus().equals(Service.STATUS1)) {
                    list_status_1_m.add(account);
                }

                if (account.getSex().equals(Service.M)
                        && account.getStatus().equals(Service.STATUS2)) {
                    list_status_2_m.add(account);
                }

                if (account.getSex().equals(Service.M)
                        && account.getStatus().equals(Service.STATUS3)) {
                    list_status_3_m.add(account);
                }

                if (account.getSex().equals(Service.F)
                        && account.getStatus().equals(Service.STATUS1)) {
                    list_status_1_f.add(account);
                }

                if (account.getSex().equals(Service.F)
                        && account.getStatus().equals(Service.STATUS2)) {
                    list_status_2_f.add(account);
                }

                if (account.getSex().equals(Service.F)
                        && account.getStatus().equals(Service.STATUS3)) {
                    list_status_3_f.add(account);
                }
            }
            System.out.println("list emails = " + emails.size());
            /*System.out.println("start warm up");
            for (int i = 0; i < 1000; i++) {
                Service.handleFilterv2("/accounts/filter/?sex_eq=f&birth_lt=642144352&limit=16&city_any=Роттеростан,Белосинки,Зеленобург,Светлокенск&country_eq=Индания&status_neq=свободны");
            }*/

            System.gc();//¯\_(ツ)_/¯
            System.out.println("End like Set" + (new Date().getTime() - start));
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            Service.lock.writeLock().unlock();
        }

    }

}
