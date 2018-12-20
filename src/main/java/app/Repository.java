package app;

import app.models.Account;
import app.models.Accounts;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Alikin E.A. on 13.12.18.
 */
public class Repository {

    static volatile long currentTimeStamp = 0l;
    static volatile long currentTimeStamp2 = 0l;
    static volatile boolean isRait = false;
    private static final String dataPath = "/tmp/data/";
    //private static final String dataPath = "/mnt/data/";
    private static String getPath(String fileName) {
        //return dataPath + fileName;
        return fileName;
    }

    private static final ObjectMapper mapper = new ObjectMapper();


   //public static final DB db = DBMaker.fileDB(dataPath + "/testMapDB.db").make();
    public static final List<String> fileNames = new ArrayList<>();

    public static final Object PRESENT = new Object();
    public static final ConcurrentHashMap ids = new ConcurrentHashMap();
    public static final ConcurrentHashMap emails = new ConcurrentHashMap();

    public static void initData() {
        ///data = db.indexTreeList("myList", Serializer.STRING).createOrOpen();
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataPath + "options.txt")))){
                String timestamp = reader.readLine();
                currentTimeStamp = new Long(timestamp + "000");
                currentTimeStamp2 = new Long(timestamp);
                String flag = reader.readLine();
                if (Integer.parseInt(flag) == 1) {
                    isRait = true;
                }
                if (isRait) {
                    for (int i = 1; i < 51; i++) {
                        fileNames.add("accounts_" + i + ".json");
                    }
                } else {
                    fileNames.add("accounts_1.json");
                }
                System.out.println("fileName count = " + fileNames.size());
                System.out.println("isRait = " + isRait);
                System.out.println("external timestamp = " + currentTimeStamp);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ZipFile zipFile = new ZipFile(dataPath + "data.zip");
            zipFile.getFileHeaders().stream().forEach(item -> {
                if (item != null) {
                    try {
                        FileHeader fileHeader = (FileHeader)item;
                        /*if (fileHeader.getFileName().contains("options")) {
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(fileHeader)))){
                                String timestamp = reader.lines().findFirst().get();
                                currentTimeStamp = new Long(timestamp + "000");
                                currentTimeStamp2 = new Long(timestamp);
                                System.out.println("timestamp = " + currentTimeStamp);
                            }
                        }*/
                        if (fileHeader.getFileName().contains("accounts")) {
                            String newFilePath = getPath(fileHeader.getFileName());
                            Path path = Paths.get(newFilePath);
                            try(BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"))){
                                List<Account> accounts = mapper
                                        .readValue(zipFile.getInputStream(fileHeader), Accounts.class)
                                        .getAccounts();
                                accounts.sort(Comparator.comparingInt(Account::getId).reversed());
                                for (Account account : accounts) {
                                    writer.write(mapper.writeValueAsString(account));
                                    ids.put(String.valueOf(account.getId()),PRESENT);
                                    emails.put(account.getEmail(),PRESENT);
                                    writer.newLine();
                                }
                                accounts = null;
                            }catch(IOException ex){
                                ex.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("End unzip");
            System.gc();//¯\_(ツ)_/¯
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
