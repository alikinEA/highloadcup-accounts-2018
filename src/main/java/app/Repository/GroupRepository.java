package app.Repository;

import app.models.Account;
import app.models.GroupObj;
import app.utils.Utils;

import java.util.LinkedList;

import static app.utils.Comparators.groupComparatorN;
import static app.utils.Comparators.groupComparatorR;

/**
 * Created by Alikin E.A. on 2019-03-23.
 */
public class GroupRepository {

    public static final LinkedList<GroupObj> city_gr_n = new LinkedList<>();
    public static final LinkedList<GroupObj> city_gr_r = new LinkedList<>();

    public static final LinkedList<GroupObj> country_gr_n = new LinkedList<>();
    public static final LinkedList<GroupObj> country_gr_r = new LinkedList<>();

    public static final LinkedList<GroupObj> sex_gr_n = new LinkedList<>();
    public static final LinkedList<GroupObj> sex_gr_r = new LinkedList<>();

    public static final LinkedList<GroupObj> status_gr_n = new LinkedList<>();
    public static final LinkedList<GroupObj> status_gr_r = new LinkedList<>();


    public static void insertGroupIndex(Account account) {
        Utils.insertStrToIndexGr(account.getCity(),city_gr_n);
        Utils.insertStrToIndexGr(account.getCountry(),country_gr_n);
        Utils.insertStrToIndexGr(account.getSex(),sex_gr_n);
        Utils.insertStrToIndexGr(account.getStatus(),status_gr_n);
    }

    public static void reSortIndex() {
        city_gr_n.sort(groupComparatorN);
        city_gr_r.clear();
        city_gr_r.addAll(city_gr_n);
        city_gr_r.sort(groupComparatorR);

        country_gr_n.sort(groupComparatorN);
        country_gr_r.clear();
        country_gr_r.addAll(country_gr_n);
        country_gr_r.sort(groupComparatorR);

        sex_gr_n.sort(groupComparatorN);
        sex_gr_r.clear();
        sex_gr_r.addAll(sex_gr_n);
        sex_gr_r.sort(groupComparatorR);

        status_gr_n.sort(groupComparatorN);
        status_gr_r.clear();
        status_gr_r.addAll(status_gr_n);
        status_gr_r.sort(groupComparatorR);
    }


}
