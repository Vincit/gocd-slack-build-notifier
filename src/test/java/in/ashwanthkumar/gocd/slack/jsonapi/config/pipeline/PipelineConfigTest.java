package in.ashwanthkumar.gocd.slack.jsonapi.config.pipeline;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PipelineConfigTest {

    @Test
    public void shouldReturnEnvVarFromPipeline() {
        PipelineConfig pipelineConfig = getPipelineConfig();

        assertThat(pipelineConfig.getStageEnvVar("stage", "var5").get(), is("value_p5"));
    }

    @Test
    public void shouldReturnEnvVarFromStage() {
        PipelineConfig pipelineConfig = getPipelineConfig();

        assertThat(pipelineConfig.getStageEnvVar("stage", "var4").get(), is("value_s4"));
    }

    @Test
    public void shouldReturnEnvVarFromStageWhenNoPipelineLevelConfigs() {
        PipelineConfig pipelineConfig = getPipelineConfigWithOnlyStageConfigs();

        assertThat(pipelineConfig.getStageEnvVar("stage", "var4").get(), is("value_s4"));
    }

    @Test
    public void shouldOverridePipelineVarWithStageVar() {
        PipelineConfig pipelineConfig = getPipelineConfig();

        assertThat(pipelineConfig.getStageEnvVar("stage", "var3").get(), is("value_s3_other"));
    }


    @Test
    public void shouldWorkEvenWhenEnvVarsNull() {
        PipelineConfig pipelineConfig = new PipelineConfig()
                .setName("pipeline")
                .setStages(Arrays.asList(
                        new StageConfig()
                                .setName("stage")
                ));

        assertThat(pipelineConfig.getStageEnvVar("stage", "var1").isEmpty(), is(true));

    }


    private PipelineConfig getPipelineConfig() {
        Map<String, String> pipelineEnvVars = new HashMap<String, String>();
        pipelineEnvVars.put("var1", "value_p1");
        pipelineEnvVars.put("var5", "value_p5");
        pipelineEnvVars.put("var3", "value_p3");

        Map<String, String> stageEnvVars = new HashMap<String, String>();
        stageEnvVars.put("var1", "value_s1");
        stageEnvVars.put("var2", "value_s2");
        stageEnvVars.put("var3", "value_s3_other");
        stageEnvVars.put("var4", "value_s4");

        return new PipelineConfig()
                .setName("pipeline")
                .setEnvironmentVariables(pipelineEnvVars)
                .setStages(Arrays.asList(
                        new StageConfig()
                                .setName("stage")
                                .setEnvironmentVariables(stageEnvVars)
                ));
    }

    private PipelineConfig getPipelineConfigWithOnlyStageConfigs() {
        Map<String, String> stageEnvVars = new HashMap<String, String>();
        stageEnvVars.put("var1", "value_s1");
        stageEnvVars.put("var2", "value_s2");
        stageEnvVars.put("var3", "value_s3_other");
        stageEnvVars.put("var4", "value_s4");

        return new PipelineConfig()
                .setName("pipeline")
                .setStages(Arrays.asList(
                        new StageConfig()
                                .setName("stage")
                                .setEnvironmentVariables(stageEnvVars)
                ));
    }


}