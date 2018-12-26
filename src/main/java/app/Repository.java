package app;

import app.models.Account;
import app.models.Accounts;
import app.models.Like;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Created by Alikin E.A. on 13.12.18.
 */
public class Repository {

    static volatile long currentTimeStamp = 0l;
    static volatile long currentTimeStamp2 = 0l;
    static volatile boolean isRait = false;
    private static final String dataPath = "/tmp/data/";
    //private static final String dataPath = "/mnt/data/";

    private static final ObjectMapper mapper = new ObjectMapper();
    //public static final List<String> fileNames = new ArrayList<>();

    private static final List<String> availableNames =
            Arrays.asList("accounts_130.json"
                    /*,"accounts_49.json"
                    ,"accounts_48.json"
                    ,"accounts_47.json"
                    ,"accounts_46.json"
                    ,"accounts_45.json"
                    ,"accounts_44.json"
                    ,"accounts_43.json"
                    ,"accounts_42.json"
                    ,"accounts_41.json"
                    ,"accounts_40.json"*/
                    );
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
    static final ReadWriteLock lock = new ReentrantReadWriteLock(true);
    //static final ConcurrentHashMap<String,Object> ids = new ConcurrentHashMap<>();
    //static final ConcurrentHashMap<String,Object> emails = new ConcurrentHashMap<>();
    //static final ConcurrentSkipListSet<Account> list = new ConcurrentSkipListSet<>(Comparator.comparing(Account::getId).reversed());


    //static final ConcurrentHashMap<String,Object> cityDir = new ConcurrentHashMap<>();
    //static final ConcurrentHashMap<String,Object> countryDir = new ConcurrentHashMap<>();
    //static final ConcurrentHashMap<String,Object> interestDir = new ConcurrentHashMap<>();



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
                /*if (isRait) {
                    for (int i = 1; i < 51; i++) {
                        fileNames.add("accounts_" + i + ".json");
                    }
                } else {
                    fileNames.add("accounts_1.json");
                }
                System.out.println("fileName count = " + fileNames.size());*/
                System.out.println("isRait = " + isRait);
                System.out.println("external timestamp = " + currentTimeStamp);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ZipFile zipFile = new ZipFile(dataPath + "data.zip");
            lock.writeLock().lock();
            zipFile.getFileHeaders().stream().forEach(item -> {
                if (item != null) {
                    try {
                        FileHeader fileHeader = (FileHeader)item;
                        if (fileHeader.getFileName().contains("accounts")) {
                                System.out.println(fileHeader.getFileName());
                                List<Account> accounts = mapper
                                        .readValue(zipFile.getInputStream(fileHeader), Accounts.class)
                                        .getAccounts();
                                for (Account account : accounts) {
                                    emails.put(account.getEmail(),PRESENT);
                                    ids.put(String.valueOf(account.getId()), PRESENT);
                                    if (isRait && availableNames.contains(fileHeader.getFileName())) {
                                        if (account.getLikes() != null) {
                                            account.setLikesArr(account.getLikes().stream().map(Like::getId).collect(Collectors.toList()));
                                            account.setLikes(null);
                                        }
                                        account.setJoined(null);
                                        list.add(account);
                                        /*if (account.getSex().equals(Service.F) && account.getStatus().equals(Service.STATUS1)) {
                                            list_f_status1.add(account);
                                        }*/
                                    } else if (!isRait && availableNamesTest.contains(fileHeader.getFileName())) {
                                        if (account.getLikes() != null) {
                                            account.setLikesArr(account.getLikes().stream().map(Like::getId).collect(Collectors.toList()));
                                            account.setLikes(null);
                                        }
                                        account.setJoined(null);
                                        list.add(account);
                                        /*if (account.getSex().equals(Service.F) && account.getStatus().equals(Service.STATUS1)) {
                                            list_f_status1.add(account);
                                        }*/
                                    }
                                }
                                accounts = null;
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
