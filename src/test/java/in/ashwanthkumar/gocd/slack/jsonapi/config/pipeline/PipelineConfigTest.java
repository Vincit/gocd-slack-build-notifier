package in.ashwanthkumar.gocd.slack.jsonapi.config.pipeline;

import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PipelineConfigTest {

    @Test
    public void shouldReturnEnvVarFromPipeline() {
        PipelineConfig pipelineConfig = getPipelineConfig();

        assertThat(pipelineConfig.getStageEnvVar("stage", "var5").get(), is("value_p5"));
    }


    @Test
    public void shouldWorkEvenWhenEnvVarsNull() {
        PipelineConfig pipelineConfig = new PipelineConfig()
                .setName("pipeline");

        assertThat(pipelineConfig.getStageEnvVar("stage", "var1").isEmpty(), is(true));

    }

    @Test
    public void shouldParsePipelineConfig() {
        PipelineConfig config = new GsonBuilder().create().fromJson(PIPELINE_CONFIG, PipelineConfig.class);
        assertThat(config.getName(), is("BUILD"));
    }


    private PipelineConfig getPipelineConfig() {
        List<EnvVar> pipelineEnvVars = new ArrayList<EnvVar>();
        pipelineEnvVars.add(new EnvVar("var1", "value_p1"));
        pipelineEnvVars.add(new EnvVar("var5", "value_p5"));
        pipelineEnvVars.add(new EnvVar("var3", "value_p3"));

        List<EnvVar> stageEnvVars = new ArrayList<EnvVar>();
        stageEnvVars.add(new EnvVar("var1", "value_s1"));
        stageEnvVars.add(new EnvVar("var2", "value_s2"));
        stageEnvVars.add(new EnvVar("var3", "value_s3_other"));
        stageEnvVars.add(new EnvVar("var4", "value_s4"));

        return new PipelineConfig()
                .setName("pipeline")
                .setEnvironmentVariables(pipelineEnvVars);
    }

    private PipelineConfig getPipelineConfigWithOnlyStageConfigs() {
        List<EnvVar> stageEnvVars = new ArrayList<EnvVar>();
        stageEnvVars.add(new EnvVar("var1", "value_s1"));
        stageEnvVars.add(new EnvVar("var2", "value_s2"));
        stageEnvVars.add(new EnvVar("var3", "value_s3_other"));
        stageEnvVars.add(new EnvVar("var4", "value_s4"));

        return new PipelineConfig()
                .setName("pipeline");
    }


    private static final String PIPELINE_CONFIG = "{\n" +
            "    \"_links\": {\n" +
            "        \"self\": {\n" +
            "            \"href\": \"https://example.org/go/api/admin/pipelines/BUILD\"\n" +
            "        },\n" +
            "        \"doc\": {\n" +
            "            \"href\": \"http://api.go.cd/#pipeline_config\"\n" +
            "        },\n" +
            "        \"find\": {\n" +
            "            \"href\": \"https://example.org/go/api/admin/pipelines/:name\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"label_template\": \"${COUNT}\",\n" +
            "    \"enable_pipeline_locking\": false,\n" +
            "    \"name\": \"BUILD\",\n" +
            "    \"template\": \"build-and-test\",\n" +
            "    \"parameters\": [\n" +
            "    ],\n" +
            "    \"environment_variables\": [\n" +
            "        {\n" +
            "            \"secure\": false,\n" +
            "            \"name\": \"GO_SLACK_CHANNEL\",\n" +
            "            \"value\": \"#channel\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"secure\": false,\n" +
            "            \"name\": \"GO_SLACK_STATUSES\",\n" +
            "            \"value\": \"failed|fixed|broken\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"secure\": false,\n" +
            "            \"name\": \"GO_SLACK_STAGES\",\n" +
            "            \"value\": \".*\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"materials\": [\n" +
            "        {\n" +
            "            \"type\": \"git\",\n" +
            "            \"attributes\": {\n" +
            "                \"url\": \"ssh:example.org/repository.git\",\n" +
            "                \"destination\": \"project\",\n" +
            "                \"filter\": null,\n" +
            "                \"name\": \"project\",\n" +
            "                \"auto_update\": true,\n" +
            "                \"branch\": \"master\",\n" +
            "                \"submodule_folder\": null\n" +
            "            }\n" +
            "        }\n" +
            "    ],\n" +
            "    \"stages\": [],\n" +
            "    \"tracking_tool\": {\n" +
            "        \"type\": \"generic\",\n" +
            "        \"attributes\": {\n" +
            "            \"url_pattern\": \"https://bitbucket.org/bitbucket/main/issue/${ID}\",\n" +
            "            \"regex\": \"\\\\w ##(\\\\d+)\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"timer\": null\n" +
            "}";

}