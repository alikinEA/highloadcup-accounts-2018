package app.service;

import app.models.Account;
import app.models.AccountC;
import app.utils.Comparators;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Alikin E.A. on 05.03.19.
 */
public class LocalPoolService {

    public static ReadWriteLock lock = new ReentrantReadWriteLock();

    public static ThreadLocal<TreeSet<AccountC>> recommendedResult =
            new ThreadLocal<>() {
                @Override
                protected TreeSet<AccountC> initialValue() {
                    return new TreeSet<>(Comparators.recComparator);
                }

                @Override
                public TreeSet<AccountC> get() {
                    TreeSet<AccountC> b = super.get();
                    b.clear();
                    return b;
                }
            };

    public static ThreadLocal<Calendar> threadLocalCalendar =

            new ThreadLocal<>() {
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


    public static ThreadLocal<Set<Account>> threadLocalAccounts =
            new ThreadLocal<>() {
                @Override
                protected Set<Account> initialValue() {
                    return new LinkedHashSet<>();
                }

                @Override
                public Set<Account> get() {
                    Set<Account> b = super.get();
                    b.clear();
                    return b;
                }
            };

    public static ThreadLocal<StringBuilder> threadLocalBuilder =
            new ThreadLocal<>() {
                @Override
                protected StringBuilder initialValue() {
                    return new StringBuilder();
                }

                @Override
                public StringBuilder get() {
                    StringBuilder b = super.get();
                    b.setLength(0); // clear/reset the buffer
                    return b;
                }

            };
}
