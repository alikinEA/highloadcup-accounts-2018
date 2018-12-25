package app.models;

/**
 * Created by Alikin E.A. on 25.12.18.
 */
public enum  Sex {
    m("m"),f("f");


    public String sex;

    Sex(String sex) {
        this.sex = sex;
    }

    private String getSex() {
        return sex;
    }
}
