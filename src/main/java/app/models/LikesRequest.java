package app.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Created by Alikin E.A. on 20.12.18.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LikesRequest {
    private List<LikeRequest> likes;
}