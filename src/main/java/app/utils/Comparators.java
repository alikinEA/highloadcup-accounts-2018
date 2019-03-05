package app.utils;

import app.models.Account;

import java.util.Comparator;

/**
 * Created by Alikin E.A. on 05.03.19.
 */
public class Comparators {

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

}
