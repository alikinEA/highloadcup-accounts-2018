package app.service;

import app.Repository.GroupRepository;
import app.Repository.Repository;
import app.models.Constants;
import app.models.GroupObj;
import app.server.ServerHandler;
import app.utils.Utils;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

/**
 * Created by Alikin E.A. on 2019-03-17.
 */
public class GroupService {

    public static DefaultFullHttpResponse handleGroup(FullHttpRequest req) {
        try {
            String countryKey = null;
            String cityKey = null;
            String statusKey = null;
            String sexKey = null;
            Integer limit = null;
            String order = null;

            String paramUrl = req.uri().substring(17);
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

                if (param.startsWith("query_id")) {
                    continue;
                }
                if (param.startsWith(Constants.LIMIT)) {
                    try {
                        limit = Integer.parseInt(Utils.getValue(param));
                        if (limit <= 0 || limit > 50) {
                            return ServerHandler.BAD_REQUEST_R;
                        }
                    } catch (Exception e) {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.startsWith("order")) {
                    order = Utils.getValue(param);
                    if (!order.equals("-1") && !order.equals("1")) {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                if (param.startsWith(Constants.KEYS)) {
                    String value = Utils.getValue(param);
                    String[] tokens = Utils.tokenize(value, Constants.DELIM);
                    for (String token : tokens) {
                        if (!Constants.SEX.equals(token)
                                && !Constants.STATUS.equals(token)
                                && !Constants.INTERESTS.equals(token)
                                && !Constants.COUNTRY.equals(token)
                                && !Constants.CITY.equals(token)) {
                            return ServerHandler.BAD_REQUEST_R;
                        }
                    }
                    if (value.equals(Constants.COUNTRY)) {
                        countryKey = value;
                    } else if (value.equals(Constants.CITY)) {
                        cityKey = value;
                    } else if (value.equals(Constants.SEX)) {
                        sexKey = value;
                    } else if (value.equals(Constants.STATUS)) {
                        statusKey = value;
                    }
                }
            }  while (j >= 0);

            if (order == null || limit == null) {
                return ServerHandler.BAD_REQUEST_R;
            }
            if (paramCount == 4) {
                if (cityKey != null) {
                    LinkedList<GroupObj> list = GroupRepository.city_gr_r;
                    if (order.charAt(0) == '1') {
                        list =  GroupRepository.city_gr_n;
                    }
                    return ServerHandler.createOK(Utils.groupToString(list, limit,Constants.CITY));
                } else if (countryKey != null) {
                    LinkedList<GroupObj> list = GroupRepository.country_gr_r;
                    if (order.charAt(0) == '1') {
                        list = GroupRepository.country_gr_n;
                    }
                    return ServerHandler.createOK(Utils.groupToString(list, limit, Constants.COUNTRY));
                } else if (sexKey != null) {
                    LinkedList<GroupObj> list = GroupRepository.sex_gr_r;
                    if (order.charAt(0) == '1') {
                        list = GroupRepository.sex_gr_n;
                    }
                    return ServerHandler.createOK(Utils.groupToString(list, limit, Constants.SEX));
                } else if (statusKey != null) {
                    LinkedList<GroupObj> list = GroupRepository.status_gr_r;
                    if (order.charAt(0) == '1') {
                        list = GroupRepository.status_gr_n;
                    }
                    //System.out.println(req.uri() + "===" + new String(Utils.groupToString(list, limit, Constants.STATUS)));
                    return ServerHandler.createOK(Utils.groupToString(list, limit, Constants.STATUS));
                }
            }
            /*if (phase1.get()) {
                if (t.length == 5) {
                    if (sex != null && countryKey != null) {
                        if (sex.equals(Service.F)) {
                            return ServerHandler.createOK(Utils.groupCSToString(Repository.country_f_gr, limit, order).getBytes(StandardCharsets.UTF_8));
                        }
                        if (sex.equals(Service.M)) {
                            return ServerHandler.createOK(Utils.groupCSToString(Repository.country_m_gr, limit, order).getBytes(StandardCharsets.UTF_8));
                        }
                    }
                    if (sex != null && cityKey != null) {
                        if (sex.equals(Service.F)) {
                            return ServerHandler.createOK(Utils.groupCiSToString(Repository.city_f_gr, limit, order).getBytes(StandardCharsets.UTF_8));
                        }
                        if (sex.equals(Service.M)) {
                            return ServerHandler.createOK(Utils.groupCiSToString(Repository.city_m_gr, limit, order).getBytes(StandardCharsets.UTF_8));
                        }
                    }
                }
                if (t.length == 4) {
                    if (sex == null) {
                        if (countryKey != null) {
                            return ServerHandler.createOK(Utils.groupCSToString(Repository.country_gr, limit, order).getBytes(StandardCharsets.UTF_8));
                        }
                        if (cityKey != null) {
                            return ServerHandler.createOK(Utils.groupCiSToString(Repository.city_gr, limit, order).getBytes(StandardCharsets.UTF_8));
                        }
                    }
                }
            }*/
            return ServerHandler.OK_EMPTY_GR_R;
        } catch (Exception e) {
            e.printStackTrace();
            return ServerHandler.NOT_FOUND_R;
        }
    }
}
