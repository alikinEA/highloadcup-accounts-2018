package app.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Alikin E.A. on 26.01.19.
 */
@Getter
@Setter
@AllArgsConstructor
public class GroupObj {
    private final AtomicInteger count = new AtomicInteger(1);
    private final String name;

}
