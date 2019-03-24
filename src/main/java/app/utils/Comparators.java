package app.utils;

import app.models.Account;
import app.models.GroupObj;

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

    public static final Comparator groupComparatorR = (Comparator<GroupObj>) (o1, o2) -> {
        if (o1.getCount().get() != o2.getCount().get()) {
            return o2.getCount().get() - o1.getCount().get();
        } else {
            return o2.getName().compareTo(o1.getName());
        }
    };

    public static final Comparator groupComparatorN = (Comparator<GroupObj>) (o1, o2) -> {
        if (o1.getCount().get() != o2.getCount().get()) {
            return o1.getCount().get() - o2.getCount().get();
        } else {
            return o1.getName().compareTo(o2.getName());
        }
    };
}
