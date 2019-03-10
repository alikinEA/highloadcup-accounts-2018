package app.service;

import app.models.Account;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Alikin E.A. on 05.03.19.
 */
public class LocalPool {

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