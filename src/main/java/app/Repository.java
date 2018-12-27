package app;

import app.models.Account;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Alikin E.A. on 13.12.18.
 */
public class Repository {

    static volatile long currentTimeStamp = 0l;
    static volatile long currentTimeStamp2 = 0l;
    static volatile boolean isRait = false;
    private static final String dataPath = "/tmp/data/";
    //private static final String dataPath = "/mnt/data/";


    private static final List<String> availableNames =
            Arrays.asList("accounts_130.json");
    private static final List<String> availableNamesTest =
            Arrays.asList("accounts_1.json"
                    ,"accounts_2.json"
                    ,"accounts_3.json"

            );
    private static final int elementCount = availableNames.size() * 10_000 + 11_000;

    static final Object PRESENT = new Object();
    static final Map<String,Object> ids = Collections.synchronizedMap(new HashMap<>(elementCount));
    static final Map<String,Object> emails = Collections.synchronizedMap(new HashMap<>(elementCount));
    static final NavigableSet<Account> list = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    //static final NavigableSet<Account> list_f_status1 = new TreeSet<>(Comparator.comparing(Account::getId).reversed());
    static final ReadWriteLock lock = new ReentrantReadWriteLock();
    //static final ConcurrentHashMap<String,Object> ids = new ConcurrentHashMap<>();
    //static final ConcurrentHashMap<String,Object> emails = new ConcurrentHashMap<>();
    //static final ConcurrentSkipListSet<Account> list = new ConcurrentSkipListSet<>(Comparator.comparing(Account::getId).reversed());

    public static void initData() {
        try {
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
            lock.writeLock().lock();
            zipFile.getFileHeaders().forEach(item -> {
                if (item != null) {
                    try {
                        FileHeader fileHeader = (FileHeader)item;
                        if (fileHeader.getFileName().contains("accounts")) {
                                Any json = JsonIterator.deserialize(Utils.readBytes(zipFile.getInputStream(fileHeader)));
                            for (Any accountAny : json.get("accounts").asList()) {
                                Account account = Utils.anyToAccount(accountAny);
                                if (account == null) {
                                    System.out.println("invalid field");
                                    System.out.println(accountAny.toString());
                                    throw new RuntimeException("invalid field");
                                }
                                emails.put(account.getEmail(),PRESENT);
                                ids.put(String.valueOf(account.getId()), PRESENT);
                                if (isRait && availableNames.contains(fileHeader.getFileName())) {
                                    list.add(account);
                                }
                                if (!isRait && availableNamesTest.contains(fileHeader.getFileName())) {
                                    list.add(account);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            lock.writeLock().unlock();
            System.out.println("list ids = " + ids.size());
            System.out.println("list emails = " + emails.size());
            System.out.println("list size = " + list.size());
            System.gc();//¯\_(ツ)_/¯
            for (int i = 0; i < 1000; i++) {
                Service.handleFilterv2("/accounts/filter/?sex_eq=f&birth_lt=642144352&limit=16&city_any=Роттеростан,Белосинки,Зеленобург,Светлокенск&country_eq=Индания&status_neq=свободны");
            }
            System.gc();//¯\_(ツ)_/¯
            System.out.println("End ");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
