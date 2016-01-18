package in.ashwanthkumar.gocd.slack.jsonapi.config.pipeline;

import com.google.gson.annotations.SerializedName;
import in.ashwanthkumar.gocd.slack.util.Options;
import in.ashwanthkumar.utils.lang.option.Option;

import java.util.HashMap;
import java.util.Map;

public class StageConfig {

    @SerializedName("name")
    private String name;

    @SerializedName("environment_variables")
    private Map<String, String> environmentVariables;

    public StageConfig() {
    }

    public StageConfig setName(String name) {
        this.name = name;
        return this;
    }

    public StageConfig setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getEnvironmentVariable(String key) {
        return environmentVariables.get(key);
    }

    Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public StageConfig addEnvVar(String key, String value) {
        if (this.environmentVariables == null) {
            this.environmentVariables = new HashMap<String, String>();
        }

        this.environmentVariables.put(key, value);
        return this;
    }

    public Option<String> getStageEnvVar(String key) {
        if (environmentVariables == null) {
            return Options.empty();
        }

        return Option.option(environmentVariables.get(key));
    }
}
