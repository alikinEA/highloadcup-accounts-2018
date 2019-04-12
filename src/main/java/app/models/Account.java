package app.models;

import lombok.Getter;
import lombok.Setter;
import org.roaringbitmap.RoaringBitmap;

/**
 * Created by Alikin E.A. on 13.12.18.
 */
@Getter
@Setter
public class Account {

    private int id = -1;
    private String email;
    private String sex;
    private String fname;
    private String[] interests;
    private String status;
    private int start;
    private int finish;
    private String phone;
    private int[] likes;
    //private int[] likesTs;
    private int birth;
    private String city;
    private String country;
    private String sname;
   // private RoaringBitmap interestBitmap;
    //private RoaringBitmap likesBitmap;

}
