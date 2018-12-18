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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Alikin E.A. on 13.12.18.
 */
public class Repository {



    public static volatile long currentTimeStamp = 0l;
    private static final String dataPath = "/tmp/data/";
    //private static final String dataPath = "/mnt/data/";
    private static String getPath(String fileName) {
        //return dataPath + fileName;
        return fileName;
    }

    private static final ObjectMapper mapper = new ObjectMapper();


   //public static final DB db = DBMaker.fileDB(dataPath + "/testMapDB.db").make();
    public static volatile List<String> fileNames = new ArrayList<>();


    public static void initData() {
        ///data = db.indexTreeList("myList", Serializer.STRING).createOrOpen();
        try {
            ZipFile zipFile = new ZipFile(dataPath + "data.zip");
            zipFile.getFileHeaders().stream().forEach(item -> {
                if (item != null) {
                    try {
                        FileHeader fileHeader = (FileHeader)item;
                        if (fileHeader.getFileName().contains("options")) {
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(fileHeader)))){
                                String timestamp = reader.lines().findFirst().get();
                                currentTimeStamp = new Long(timestamp + "000");
                                System.out.println("timestamp = " + currentTimeStamp);
                            }
                        }
                        if (fileHeader.getFileName().contains("accounts")) {
                            String newFilePath = getPath(fileHeader.getFileName());
                            fileNames.add(newFilePath);
                            System.out.println("fileName = " + newFilePath);

                            Path path = Paths.get(newFilePath);
                            try(BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"))){
                                for (Account account : mapper
                                        .readValue(zipFile.getInputStream(fileHeader), Accounts.class)
                                        .getAccounts()) {
                                    writer.write(mapper.writeValueAsString(account));
                                    writer.newLine();
                                }
                            }catch(IOException ex){
                                ex.printStackTrace();
                            }
                            System.gc();//¯\_(ツ)_/¯
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
