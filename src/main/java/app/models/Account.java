package app.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

/**
 * Created by Alikin E.A. on 13.12.18.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Account implements Comparable<Account>{

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

    // Two Employees are equal if their IDs are equal
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id == account.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Account account) {
        return this.getId() - account.getId();
    }

}
