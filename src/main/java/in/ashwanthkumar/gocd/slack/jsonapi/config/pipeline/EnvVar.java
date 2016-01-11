package in.ashwanthkumar.gocd.slack.jsonapi.config.pipeline;

import com.google.gson.annotations.SerializedName;

public class EnvVar {

    @SerializedName("secure")
    private boolean secure;
    @SerializedName("name")
    private String name;
    @SerializedName("value")
    private String value;

    public EnvVar() {
    }

    public EnvVar(boolean secure, String name, String value) {
        this.secure = secure;
        this.name = name;
        this.value = value;
    }

    public EnvVar(String name, String value) {
        this.secure = false;
        this.name = name;
        this.value = value;
    }

    public boolean isSecure() {
        return secure;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
