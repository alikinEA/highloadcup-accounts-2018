package app.models;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Alikin E.A. on 15.06.18.
 */
@Getter
@Setter
@AllArgsConstructor
public class Result {
    private byte[] content;
    private HttpResponseStatus status;
}
