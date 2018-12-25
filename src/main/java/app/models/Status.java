package app.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alikin E.A. on 25.12.18.
 */
public enum Status {
    @JsonProperty("свободны")
    STATUS1("свободны"),
    @JsonProperty("всё сложно")
    STATUS2("всё сложно"),
    @JsonProperty("заняты")
    STATUS3("заняты");

    public String status;

    Status(String status) {
        this.status = status;
    }

    private String getStatus() {
        return status;
    }

}
