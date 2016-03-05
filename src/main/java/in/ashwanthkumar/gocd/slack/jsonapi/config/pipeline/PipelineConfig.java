package in.ashwanthkumar.gocd.slack.jsonapi.config.pipeline;

import com.google.gson.annotations.SerializedName;
import in.ashwanthkumar.gocd.slack.util.Options;
import in.ashwanthkumar.utils.lang.option.Option;

import java.util.*;

public class PipelineConfig {

    @SerializedName("name")
    private String name;

    @SerializedName("environment_variables")
    private List<EnvVar> environmentVariables;

    private Map<String, String> environmentVariablesAsMap;

    public PipelineConfig() {
    }

    public PipelineConfig setName(String name) {
        this.name = name;
        return this;
    }

    public PipelineConfig setEnvironmentVariables(List<EnvVar> environmentVariables) {
        this.environmentVariables = environmentVariables;
        return this;
    }

    public PipelineConfig addEnvVar(String key, String value) {
        if (this.environmentVariables == null) {
            this.environmentVariables = new ArrayList<EnvVar>();
        }

        this.environmentVariables.add(new EnvVar(false, key, value));
        return this;
    }

    public String getName() {
        return name;
    }

    public Option<String> getEnvVar(String key) {
        Option<String> value = Options.empty();

        Map<String, String> envVars = Option.option(getEnvironmentVariablesAsMap())
                .getOrElse(Collections.<String, String>emptyMap());

        return Option.option(value.getOrElse(envVars.get(key)));
    }

    Map<String, String> getEnvironmentVariablesAsMap() {
        if (environmentVariablesAsMap == null) {
            environmentVariablesAsMap = new HashMap<String, String>();
            if (environmentVariables != null) {
                for (EnvVar e : environmentVariables) {
                    environmentVariablesAsMap.put(e.getName(), e.getValue());
                }
            }
        }
        return environmentVariablesAsMap;
    }

    List<EnvVar> getEnvironmentVariables() {
        return environmentVariables;
    }

}
