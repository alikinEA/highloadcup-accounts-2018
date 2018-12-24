package app.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

/**
 * Created by Alikin E.A. on 13.12.18.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    private String email;
    private Integer id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String sex;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fname;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> interests;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Premium premium;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String phone;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Like> likes;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Integer> likesArr;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer birth;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String city;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String country;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer joined;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String sname;
}
