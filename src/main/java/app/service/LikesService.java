package app.service;

import app.Repository.Repository;
import app.models.Account;
import app.models.Constants;
import app.server.ServerHandler;
import app.utils.Comparators;
import app.utils.Utils;
import com.jsoniter.JsonIterator;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Alikin E.A. on 2019-03-17.
 */
public class LikesService {

    public static DefaultFullHttpResponse handleLikes(FullHttpRequest req) {
        try {
            ByteBuf buf = req.content();
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            Any likesRequestAny = JsonIterator.deserialize(bytes);
            List<Any> likesListAny = likesRequestAny.get(Constants.LIKES).asList();

            for (Any any : likesListAny) {
                Any valueTs = any.get(Constants.TS);
                if (!ValueType.NUMBER.equals(valueTs.valueType())) {
                    return ServerHandler.BAD_REQUEST_R;
                }
                Any value = any.get(Constants.LIKEE);
                int likeeId;
                if (!ValueType.NUMBER.equals(value.valueType())) {
                    return ServerHandler.BAD_REQUEST_R;
                } else {
                    likeeId = value.toInt();
                    if (likeeId > Repository.MAX_ID) {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                    if (Repository.ids[likeeId] == null) {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                }
                value = any.get(Constants.LIKER);
                if (!ValueType.NUMBER.equals(value.valueType())) {
                    return ServerHandler.BAD_REQUEST_R;
                } else {
                    int id = value.toInt();
                    if (id > Repository.MAX_ID) {
                        return ServerHandler.BAD_REQUEST_R;
                    }
                    Account liker = Repository.ids[id];
                    if (liker == null) {
                        return ServerHandler.BAD_REQUEST_R;
                    } else {
                        LocalPoolService.lock.writeLock().lock();
                        try {
                            Account[] invertLikesArr = Repository.likeInvert.get(likeeId);
                            if (invertLikesArr == null) {
                                invertLikesArr = new Account[1];
                                invertLikesArr[0] = liker;
                                Repository.likeInvert.put(likeeId, invertLikesArr);
                            } else {
                                invertLikesArr = Arrays.copyOf(invertLikesArr, invertLikesArr.length + 1);
                                invertLikesArr[invertLikesArr.length - 1] = liker;
                                Arrays.sort(invertLikesArr, Comparators.idsComparator);
                                Repository.likeInvert.put(likeeId, invertLikesArr);
                            }
                            /*if (liker.getLikes() != null) {
                                int[] arrNewLikes = Arrays.copyOf(liker.getLikes(), liker.getLikes().length + 1);
                                arrNewLikes[arrNewLikes.length - 1] = likeeId;
                                liker.setLikes(arrNewLikes);

                                int[] arrNewLikesTs = Arrays.copyOf(liker.getLikesTs(), liker.getLikesTs().length + 1);
                                arrNewLikesTs[arrNewLikesTs.length - 1] = valueTs.toInt();
                                liker.setLikesTs(arrNewLikesTs);
                                Utils.quickSortForLikes(liker.getLikes(),liker.getLikesTs(),0,liker.getLikes().length -1 );
                            } else {
                                int[] arrNewLikes = new int[1];
                                arrNewLikes[0] = likeeId;
                                liker.setLikes(arrNewLikes);

                                int[] arrNewLikesTs = new int[1];
                                arrNewLikesTs[0] = valueTs.toInt();
                                liker.setLikesTs(arrNewLikesTs);
                            }*/
                        } finally {
                            LocalPoolService.lock.writeLock().unlock();
                        }
                    }
                }
            }
            return ServerHandler.ACCEPTED_R;
        } catch (Exception e) {
            e.printStackTrace();
            return ServerHandler.BAD_REQUEST_R;
        }
    }
}
