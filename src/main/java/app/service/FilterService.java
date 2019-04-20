package app.service;

import app.Repository.Repository;
import app.models.Account;
import app.models.Constants;
import app.server.ServerHandler;
import app.utils.Utils;
import io.netty.handler.codec.http.DefaultFullHttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Set;

import static app.Repository.Repository.currentTimeStamp2;

/**
 * Created by Alikin E.A. on 2019-03-17.
 */
public class FilterService {

    public static DefaultFullHttpResponse handleFilterv2(String uri) {
        LocalPoolService.lock.readLock().lock();
        try {

            String paramUrl = uri.substring(18);

            boolean emailPr = false;
            boolean sexPr = false;
            boolean fnamePr = false;
            boolean interestsPr = false;
            boolean statusPr = false;
            boolean premiumPr = false;
            boolean phonePr = false;
            boolean birthPr = false;
            boolean cityPr = false;
            boolean countryPr = false;
            boolean snamePr = false;
            boolean likesPr = false;

            String emailPrV = null;
            String fnamePrV = null;
            String interestsPrV = null;
            String statusPrV = null;
            String premiumPrV = null;
            String phonePrV = null;
            String birthPrV = null;
            String cityPrV = null;
            String countryPrV = null;
            String snamePrV = null;

            String emailV = null;
            String sexV = null;
            String fnameV = null;
            String interestsV = null;
            String statusV = null;
            String premiumV = null;
            String phoneV = null;
            String birthV = null;
            String cityV = null;
            String countryV = null;
            String snameV = null;
            String likesV = null;
            String queryId = null;


            int limit = 0;
            int i = 0;
            int j = 0;
            int paramCount = 0;

            do {
                j = paramUrl.indexOf('&', i);
                String param;
                if (j != -1) {
                    param = paramUrl.substring(i, j);
                } else {
                    param = paramUrl.substring(i);
                }
                i = j + 1;
                paramCount++;

                String valueParam = Utils.getValue(param).intern();
                String predicate = Utils.getPredicate(param).intern();
                if (param.charAt(0) == 'q' && param.charAt(1) == 'u') {
                    queryId = valueParam;
                    byte[] cachedQuery = Repository.queryCache.get(queryId);
                    if (cachedQuery != null) {
                        //System.out.println("from cache = " + queryCacheCount.incrementAndGet());
                        return ServerHandler.createOK(cachedQuery);
                    }
                }
                if (param.charAt(0) == 's' && param.charAt(1) == 'e') {
                    sexPr = true;
                    sexV = valueParam;
                    if (!predicate.equals(Constants.EQ_PR)) {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'e') {
                    emailPr = true;
                    emailV = valueParam;
                    if (predicate.equals(Constants.DOMAIN_PR)) {
                        emailPrV = Constants.DOMAIN_PR;
                    } else if (predicate.equals(Constants.LT_PR)) {
                        emailPrV = Constants.LT_PR;
                    } else if (predicate.equals(Constants.GT_PR)) {
                        emailPrV = Constants.GT_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 's' && param.charAt(1) == 't') {
                    statusPr = true;
                    statusV = valueParam;
                    if (predicate.equals(Constants.EQ_PR)) {
                        statusPrV = Constants.EQ_PR;
                    } else if (predicate.equals(Constants.NEQ_PR)) {
                        statusPrV = Constants.NEQ_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'f') {
                    fnamePr = true;
                    fnameV = valueParam;
                    if (predicate.equals(Constants.EQ_PR)) {
                        fnamePrV = Constants.EQ_PR;
                    } else if (predicate.equals(Constants.ANY_PR)) {
                        fnamePrV = Constants.ANY_PR;
                    } else if (predicate.equals(Constants.NULL_PR)) {
                        fnamePrV = Constants.NULL_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 's' && param.charAt(1) == 'n') {
                    snamePr = true;
                    snameV = valueParam;
                    if (predicate.equals(Constants.EQ_PR)) {
                        snamePrV = Constants.EQ_PR;
                    } else if (predicate.equals(Constants.STARTS_PR)) {
                        snamePrV = Constants.STARTS_PR;
                    } else if (predicate.equals(Constants.NULL_PR)) {
                        snamePrV = Constants.NULL_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'p' && param.charAt(1) == 'h') {
                    phonePr = true;
                    phoneV = valueParam;
                    if (predicate.equals(Constants.NULL_PR)) {
                        phonePrV = Constants.NULL_PR;
                    } else if (predicate.equals(Constants.CODE_PR)) {
                        phonePrV = Constants.CODE_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'c' && param.charAt(1) == 'o') {
                    countryPr = true;
                    countryV = valueParam;
                    if (predicate.equals(Constants.NULL_PR)) {
                        countryPrV = Constants.NULL_PR;
                    } else if (predicate.equals(Constants.EQ_PR)) {
                        countryPrV = Constants.EQ_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'c' && param.charAt(1) == 'i') {
                    cityPr = true;
                    cityV = valueParam;
                    if (predicate.equals(Constants.EQ_PR)) {
                        cityPrV = Constants.EQ_PR;
                    } else if (predicate.equals(Constants.ANY_PR)) {
                        cityPrV = Constants.ANY_PR;
                    } else if (predicate.equals(Constants.NULL_PR)) {
                        cityPrV = Constants.NULL_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'b') {
                    birthPr = true;
                    birthV = valueParam;
                    if (predicate.equals(Constants.YEAR_PR)) {
                        birthPrV = Constants.YEAR_PR;
                    } else if (predicate.equals(Constants.LT_PR)) {
                        birthPrV = Constants.LT_PR;
                    } else if (predicate.equals(Constants.GT_PR)) {
                        birthPrV = Constants.GT_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'i') {
                    interestsPr = true;
                    interestsV = valueParam;
                    if (predicate.equals(Constants.CONTAINS_PR)) {
                        interestsPrV = Constants.CONTAINS_PR;
                    } else if (predicate.equals(Constants.ANY_PR)) {
                        interestsPrV = Constants.ANY_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'l' && param.charAt(1) == 'i' && param.charAt(2) == 'k') {
                    likesPr = true;
                    if (predicate.equals(Constants.CONTAINS_PR)) {
                        likesV = valueParam;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.charAt(0) == 'p' && param.charAt(1) == 'r') {
                    premiumPr = true;
                    premiumV = valueParam;
                    if (predicate.equals(Constants.NULL_PR)) {
                        premiumPrV = Constants.NULL_PR;
                    } else if (predicate.equals(Constants.NOW_PR)) {
                        premiumPrV = Constants.NOW_PR;
                    } else {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }

                if (param.charAt(0) == 'l' && param.charAt(1) == 'i' && param.charAt(2) == 'm') {
                    if (!Character.isDigit(valueParam.charAt(0))) {
                        return ServerHandler.BAD_REQUEST_R;
                    } else {
                        limit = Integer.parseInt(valueParam);
                    }
                }

            } while (j >= 0);

            if (paramCount < 2) {
                return ServerHandler.BAD_REQUEST_R;
            }

            Set<Account> accounts = LocalPoolService.threadLocalAccounts.get();
            if (paramCount == 2) {
                for (Account account : Repository.list) {
                    if (accounts.size() == limit) {
                        byte[] body = Utils.accountToString(accounts
                                , sexPr
                                , fnamePr
                                , statusPr
                                , premiumPr
                                , phonePr
                                , birthPr
                                , cityPr
                                , countryPr
                                , snamePr);
                        Repository.queryCache.put(queryId, body);
                        return ServerHandler.createOK(body);
                    } else {
                        accounts.add(account);
                    }
                }
            }
            Integer year = null;
            String[] cityArr = null;
            String[] fnameArr = null;
            String[] interArr = null;
            int[] likesArr = null;
            if (birthPr) {
                year = Integer.parseInt(birthV);
            }
            if (cityPr) {
                if (cityPrV == Constants.ANY_PR) {
                    cityArr = Utils.tokenize(cityV, Constants.DELIM);
                }
            }
            if (fnamePr) {
                if (fnamePrV == Constants.ANY_PR) {
                    fnameArr = Utils.tokenize(fnameV, Constants.DELIM);
                }
            }
            if (interestsPr) {
                interArr = Utils.tokenize(interestsV, Constants.DELIM);
            }
            if (likesPr) {
                String[] likesArrStr = Utils.tokenize(likesV, Constants.DELIM);
                likesArr = new int[likesArrStr.length];
                int index = 0;
                for (String s : likesArrStr) {
                    likesArr[index] = Integer.valueOf(s);
                }
            }


            Account[] listForSearch = getIndexForFilter(interArr, interestsPrV
                    , phonePr, phonePrV, phoneV
                    , snamePr, snamePrV, snameV
                    , cityPr, cityPrV, cityV, sexV
                    , fnamePr, fnamePrV, fnameV
                    , countryPr, countryPrV, countryV
                    , premiumPr, premiumPrV, premiumV
                    , statusPr, statusPrV, statusV
                    , sexPr
                    , birthPr, birthPrV, year
                    , emailPr, emailPrV, emailV
                    , cityArr, fnameArr
                    , likesPr, likesArr
            );
            if (listForSearch == null) {
                return ServerHandler.OK_EMPTY_R;
            }

            for (Account account : listForSearch) {
                if (account == null) {
                    break;
                }
                //SEX ============================================
                if (sexPr) {
                    if (!account.getSex().equals(sexV)) {
                        continue;
                    }
                }
                //SEX ============================================

                //EMAIL ============================================
                if (emailPr) {
                    if (emailPrV == Constants.DOMAIN_PR) {
                        if (!account.getEmail().contains(emailV)) {
                            continue;
                        }
                    } else if (emailPrV == Constants.LT_PR) {
                        if (account.getEmail().compareTo(emailV) > 0) {
                            continue;
                        }
                    } else if (emailPrV == Constants.GT_PR) {
                        if (account.getEmail().compareTo(emailV) < 0) {
                            continue;
                        }
                    }
                }
                //EMAIL ============================================

                //STATUS ============================================
                if (statusPr) {
                    if (statusPrV == Constants.EQ_PR) {
                        if (!account.getStatus().equals(statusV)) {
                            continue;
                        }
                    } else if (statusPrV == Constants.NEQ_PR) {
                        if (account.getStatus().equals(statusV)) {
                            continue;
                        }
                    }
                }
                //STATUS ============================================


                //SNAME ============================================
                if (snamePr) {
                    if (snamePrV == Constants.EQ_PR) {
                        if (!snameV.equals(account.getSname())) {
                            continue;
                        }
                    } else if (snamePrV == Constants.NULL_PR) {
                        if (snameV == Constants.NULL_PR_VAL_ONE) {
                            if (account.getSname() != null) {
                                continue;
                            }
                        } else {
                            if (account.getSname() == null) {
                                continue;
                            }
                        }
                    } else if (snamePrV == Constants.STARTS_PR) {
                        if (account.getSname() != null) {
                            if (!account.getSname().startsWith(snameV)) {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                }
                //SNAME ============================================

                //PHONE ============================================
                if (phonePr) {

                    if (phonePrV == Constants.CODE_PR) {
                        if (account.getPhone() != null) {
                            if (!account.getPhone()
                                    .substring(account.getPhone().indexOf("(") + 1
                                            , account.getPhone().indexOf(")"))
                                    .equals(phoneV)) {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    } else if (phonePrV == Constants.NULL_PR) {
                        if (phoneV == Constants.NULL_PR_VAL_ONE) {
                            if (account.getPhone() != null) {
                                continue;
                            }
                        } else {
                            if (account.getPhone() == null) {
                                continue;
                            }
                        }
                    }
                }
                //PHONE ============================================


                //COUNTRY ============================================
                if (countryPr) {
                    if (countryPrV == Constants.EQ_PR) {
                        if (!countryV.equals(account.getCountry())) {
                            continue;
                        }
                    } else if (countryPrV == Constants.NULL_PR) {
                        if (countryV == Constants.NULL_PR_VAL_ONE) {
                            if (account.getCountry() != null) {
                                continue;
                            }
                        } else {
                            if (account.getCountry() == null) {
                                continue;
                            }
                        }
                    }
                }
                //COUNTRY ============================================


                //PREMIUM ============================================
                if (premiumPr) {
                    if (premiumPrV == Constants.NOW_PR) {
                        if (account.getStart() != 0) {
                            if (currentTimeStamp2 < account.getFinish()
                                    && currentTimeStamp2 > account.getStart()) {
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    } else if (premiumPrV == Constants.NULL_PR) {
                        if (premiumV == Constants.NULL_PR_VAL_ONE) {
                            if (account.getStart() != 0) {
                                continue;
                            }
                        } else {
                            if (account.getStart() == 0) {
                                continue;
                            }
                        }
                    }
                }
                //PREMIUM ============================================
                //BIRTH ============================================
                if (birthPr) {
                    if (birthPrV == Constants.YEAR_PR) {
                        Calendar calendar = LocalPoolService.threadLocalCalendar.get();
                        calendar.setTimeInMillis((long) account.getBirth() * 1000);
                        if (year != calendar.get(Calendar.YEAR)) {
                            continue;
                        }
                    } else if (birthPrV == Constants.LT_PR) {
                        if (account.getBirth() > year) {
                            continue;
                        }
                    } else if (birthPrV == Constants.GT_PR) {
                        if (account.getBirth() < year) {
                            continue;
                        }
                    }
                }
                //BIRTH ============================================

                //CITY ============================================
                if (cityPr) {

                    if (cityPrV == Constants.EQ_PR) {
                        if (!cityV.equals(account.getCity())) {
                            continue;
                        }
                    } else if (cityPrV == Constants.ANY_PR) {
                        boolean isValid = false;
                        for (String value : cityArr) {
                            if (value.equals(account.getCity())) {
                                isValid = true;
                                break;
                            }
                        }
                        if (!isValid) {
                            continue;
                        }
                    } else if (cityPrV == Constants.NULL_PR) {
                        if (cityV == Constants.NULL_PR_VAL_ONE) {
                            if (account.getCity() != null) {
                                continue;
                            }
                        } else {
                            if (account.getCity() == null) {
                                continue;
                            }
                        }
                    }
                }
                //CITY ============================================


                //FNAME ============================================
                if (fnamePr) {

                    if (fnamePrV == Constants.EQ_PR) {
                        if (!fnameV.equals(account.getFname())) {
                            continue;
                        }
                    } else if (fnamePrV == Constants.ANY_PR) {
                        boolean isValid = false;
                        for (String value : fnameArr) {
                            if (value.equals(account.getFname())) {
                                isValid = true;
                                break;
                            }
                        }
                        if (!isValid) {
                            continue;
                        }
                    } else if (fnamePrV == Constants.NULL_PR) {
                        if (fnameV == Constants.NULL_PR_VAL_ONE) {
                            if (account.getFname() != null) {
                                continue;
                            }
                        } else {
                            if (account.getFname() == null) {
                                continue;
                            }
                        }
                    }
                }
                //FNAME ============================================

                //INTERESTS ============================================
                if (interestsPr) {
                    if (account.getInterests() != null) {
                        if (interestsPrV == Constants.ANY_PR) {
                            boolean isValid = false;
                            for (String value : interArr) {
                                //todo упорядочить
                                if (Utils.contains(account.getInterests(), value)) {
                                    isValid = true;
                                    break;
                                }
                            }
                            if (!isValid) {
                                continue;
                            }
                        } else if (interestsPrV == Constants.CONTAINS_PR) {
                            //todo чекнуть битмапы
                            if (interArr.length <= account.getInterests().length) {
                                /*RoaringBitmap bitmapQ = Repository.getInterestBitMap(interArr);
                                bitmapQ.or(account.getInterestBitmap());
                                if (!bitmapQ.equals(account.getInterestBitmap())) {
                                    continue;
                                }*/
                                boolean isValid = true;
                                for (String value : interArr) {
                                    if (!Utils.contains(account.getInterests(), value)) {
                                        isValid = false;
                                        break;
                                    }
                                }
                                if (!isValid) {
                                    continue;
                                }
                            } else {
                                continue;
                            }
                        }

                    } else {
                        continue;
                    }
                }
                //INTERESTS ============================================

                accounts.add(account);
                if (accounts.size() == limit) {
                    break;
                }
            }
            if (accounts.size() == 0) {
                return ServerHandler.OK_EMPTY_R;
            }
            byte[] body = Utils.accountToString(accounts
                    , sexPr
                    , fnamePr
                    , statusPr
                    , premiumPr
                    , phonePr
                    , birthPr
                    , cityPr
                    , countryPr
                    , snamePr);
            Repository.queryCache.put(queryId, body);
            return ServerHandler.createOK(body);
        } catch (Exception e) {
            System.out.println(uri);
            e.printStackTrace();
            return ServerHandler.INTERNAL_ERROR_R;
        } finally {
            LocalPoolService.lock.readLock().unlock();
        }
    }

    private static Account[] getIndexForFilter(String[] interArr, String interestsPrV
            , boolean phonePr, String phonePrV, String phoneV
            , boolean snamePr, String snamePrV, String snameV
            , boolean cityPr, String cityPrV, String cityV, String sexV
            , boolean fnamePr, String fnamePrV, String fnameV
            , boolean countryPr, String countryPrV, String countryV
            , boolean premiumPr, String premiumPrV, String premiumV
            , boolean statusPr, String statusPrV, String statusV
            , boolean sexPr
            , boolean birthPr, String birthPrV, Integer year
            , boolean emailPr, String emailPrV, String emailV
            , String[] cityArr, String[] fnameArr, boolean likesPr, int[] likesArr) {
        Account[] resultIndex = Repository.list;

        if (likesPr) {
            return Repository.likeInvert.get(likesArr[likesArr.length - 1]);
        }

        if (snamePr) {
            if (snamePrV == Constants.NULL_PR) {
                if (snameV == Constants.NULL_PR_VAL_ONE) {
                    return Repository.sname_by_name.get(null);
                } else {
                    // resultIndex = compareIndex(Repository.sname_not_null,resultIndex);
                }
            } else if (snamePrV == Constants.EQ_PR) {
                return Repository.sname_by_name.get(snameV);
            }
        }

        if (cityPr) {
            if (cityPrV == Constants.ANY_PR) {
                if (cityArr.length == 1) {
                    return Repository.city_by_name.get(cityArr[0]);
                } else {
                    //return Repository.city_not_null;
                }
            } else {
                if (cityPrV == Constants.NULL_PR) {
                    if (cityV == Constants.NULL_PR_VAL_ONE) {
                        return Repository.city_by_name.get(null);
                    } else {
                        //return Repository.city_not_null;
                    }
                }
                if (cityPrV == Constants.EQ_PR) {
                    return Repository.city_by_name.get(cityV);
                }
            }
        }

        if (fnamePr) {
            if (fnamePrV == Constants.NULL_PR) {
                if (fnameV == Constants.NULL_PR_VAL_ONE) {
                    return Repository.fname_by_name.get(null);
                } else {
                    // resultIndex = compareIndex(Repository.fname_not_null,resultIndex);
                }
            } else if (fnamePrV == Constants.ANY_PR) {
                if (fnameArr.length == 1) {
                    return Repository.fname_by_name.get(fnameArr[0]);
                } else {
                    // resultIndex = compareIndex(Repository.fname_not_null, resultIndex);
                }
            } else if (fnamePrV == Constants.EQ_PR) {
                return Repository.fname_by_name.get(fnameV);
            }
        }

        if (phonePr) {
            if (phonePrV == Constants.CODE_PR) {
                return Repository.phone_code_by_name.get(phoneV);
            } else {
                if (phoneV == Constants.NULL_PR_VAL_ONE) {
                    // resultIndex = compareIndex(Repository.phone_null, resultIndex);
                } else {
                    //resultIndex = compareIndex(Repository.phone_not_null, resultIndex);
                }
            }
        }


        if (countryPr) {
            if (countryPrV == Constants.NULL_PR) {
                if (countryV == Constants.NULL_PR_VAL_ONE) {
                    return Repository.country_by_name.get(null);
                } else {
                    // return Repository.country_not_null;
                }
            }
            if (countryPrV == Constants.EQ_PR) {
                return Repository.country_by_name.get(countryV);
            }
        }

        if (interArr != null && interestsPrV == Constants.CONTAINS_PR) {
            return Repository.interests_by_name.get(interArr[0]);
        }
        if (birthPr) {
            if (birthPrV == Constants.YEAR_PR) {
                return Repository.year.get(year);
            } else {
                /*Account[] curInd = birth_idx_lt;
                if (birthPrV == GT_PR) {
                    curInd = birth_idx_gt;
                }
                int startPos = Utils.binarySearchStartPos(curInd,year,0,curInd.length - 1);
                Account[] newArr = new Account[curInd.length - startPos];
                System.arraycopy(curInd, 0, newArr, 0, curInd.length - startPos);
                Arrays.sort(newArr,Repository.idsComparator);
                return newArr;*/
            }
        }

        if (emailPr) {
            if (emailPrV == Constants.DOMAIN_PR) {
                return Repository.email_domain_by_name.get(emailV);
            }
        }

        if (premiumPr) {
            if (premiumPrV == Constants.NOW_PR) {
                if (sexPr) {
                    if (sexV == Constants.F) {
                        return Repository.premium_1_f;
                    } else {
                        return Repository.premium_1_m;
                    }
                } else {
                    return Repository.premium_1;
                }
            } else if (premiumPrV == Constants.NULL_PR) {
                if (premiumV == Constants.NULL_PR_VAL_ONE) {
                    if (sexPr) {
                        if (sexV == Constants.F) {
                            return Repository.premium_3_f;
                        } else {
                            return Repository.premium_3_m;
                        }
                    } else {
                        return Repository.premium_3;
                    }
                } else {
                    if (sexPr) {
                        if (sexV == Constants.F) {
                            return Repository.premium_2_f;
                        } else {
                            return Repository.premium_2_m;
                        }
                    } else {
                        return Repository.premium_2;
                    }
                }
            }
        }

        if (statusPr) {
            if (statusPrV == Constants.EQ_PR) {
                if (Constants.STATUS1.equals(statusV)) {
                    return Repository.status_1;
                }
                if (Constants.STATUS2.equals(statusV)) {
                    return Repository.status_2;
                }
                if (Constants.STATUS3.equals(statusV)) {
                    return Repository.status_3;
                }
            } else {
                if (statusV.equals(Constants.STATUS1)) {
                    return Repository.status_1_not;
                } else if (statusV.equals(Constants.STATUS2)) {
                    return Repository.status_2_not;
                } else {
                    return Repository.status_3_not;
                }
            }
        }

        if (sexPr) {
            if (Constants.F.equals(sexV)) {
                return Repository.list_f;
            }
            if (Constants.M.equals(sexV)) {
                return Repository.list_m;
            }
        }

        return resultIndex;

    }
}
