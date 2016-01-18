package in.ashwanthkumar.gocd.slack.jsonapi.config.pipeline;

import com.google.gson.annotations.SerializedName;
import in.ashwanthkumar.gocd.slack.util.Options;
import in.ashwanthkumar.utils.lang.option.Option;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipelineConfig {

    @SerializedName("name")
    private String name;

    @SerializedName("params")
    private Map<String, String> params;

    @SerializedName("environment_variables")
    private Map<String, String> environmentVariables;

    @SerializedName("stages")
    private List<StageConfig> stages;

    public PipelineConfig() {
    }

    public PipelineConfig setName(String name) {
        this.name = name;
        return this;
    }

    public PipelineConfig setParams(Map<String, String> params) {
        this.params = params;
        return this;
    }

    public PipelineConfig setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
        return this;
    }

    public PipelineConfig setStages(List<StageConfig> stages) {
        this.stages = stages;
        return this;
    }

    public PipelineConfig addEnvVar(String key, String value) {
        if (this.environmentVariables == null) {
            this.environmentVariables = new HashMap<String, String>();
        }

        this.environmentVariables.put(key, value);
        return this;
    }

    public PipelineConfig addStage(StageConfig stage) {
        if (this.stages == null) {
            this.stages = new ArrayList<StageConfig>();
        }

        this.stages.add(stage);
        return this;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getEnvironmentVariable(String key) {
        return environmentVariables.get(key);
    }

    public List<StageConfig> getStages() {
        return stages;
    }

    public Option<String> getStageEnvVar(String stageName, String key) {
        Option<String> value = Options.empty();
        if (environmentVariables == null) {
            return value;
        }

        for (StageConfig stage : stages) {
            if (stageName.equals(stage.getName())) {
                value = stage.getStageEnvVar(key);
            }
        }

        return Option.option(value.getOrElse(environmentVariables.get(key)));
    }

}
