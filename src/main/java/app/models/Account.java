package app.models;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Alikin E.A. on 13.12.18.
 */
@Getter
@Setter
public class Account implements Comparable<Account>{

    private int id = -1;
    private String email;
    private String sex;
    private String fname;
    private Set<String> interests;
    private String status;
    private int start;
    private int finish;
    private String phone;
    //private Set<Integer> likesArr;
    private int birth;
    private String city;
    private String country;
    private String sname;

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
