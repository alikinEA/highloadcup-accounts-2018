package app.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Alikin E.A. on 20.12.18.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LikeRequest {
    private Integer likee;
    private Integer ts;
    private Integer liker;
}
