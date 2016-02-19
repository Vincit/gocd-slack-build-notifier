package in.ashwanthkumar.gocd.slack.ruleset;

import in.ashwanthkumar.gocd.slack.jsonapi.Server;
import in.ashwanthkumar.gocd.slack.jsonapi.config.pipeline.PipelineConfig;
import in.ashwanthkumar.gocd.slack.jsonapi.config.pipeline.StageConfig;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RuleResolverTest {

    public static final String GO_SLACK_CHANNEL = "GO_SLACK_CHANNEL";
    public static final String GO_SLACK_STATUSES = "GO_SLACK_STATUSES";
    public static final String GO_SLACK_STAGES = "GO_SLACK_STAGES";

    @Test
    public void shouldFetchRules() throws Exception {
        Server server = mock(Server.class);
        RuleResolver resolver = new RuleResolver(server);

        when(server.fetchPipelineConfig("pipeline"))
                .thenReturn(new PipelineConfig()
                        .setName("pipeline")
                        .addEnvVar(GO_SLACK_CHANNEL, "test-channel")
                        .addEnvVar(GO_SLACK_STATUSES, "failed|broken|fixed|building")
                        .addEnvVar(GO_SLACK_STAGES, "stage.*")
                        .addStage(new StageConfig()
                                .setName("stage")));

        Rules defaultRules = new Rules()
                .setGoServerHost("http://localhost");
        Rules rules = resolver.resolvePipelineRule(defaultRules, "pipeline", "stage");

        assertThat(rules, notNullValue());
        assertThat(rules.getPipelineRules().size(), is(1));

        PipelineRule pipelineRule = rules.getPipelineRules().get(0);

        assertThat(pipelineRule.getStatus().size(), is(4));

        PipelineRule foundRules = rules.find("pipeline", "stage", "failed").get();
        assertThat(foundRules.getChannel(), is("test-channel"));
        assertThat(foundRules.getNameRegex(), is("pipeline"));
        assertThat(foundRules.getStageRegex(), is("stage.*"));
    }

    @Test
    public void shouldFetchRulesFromStage() throws Exception {
        Server server = mock(Server.class);
        RuleResolver resolver = new RuleResolver(server);

        when(server.fetchPipelineConfig("pipeline"))
                .thenReturn(new PipelineConfig()
                        .setName("pipeline")
                        .addStage(new StageConfig()
                                .setName("stage")
                                .addEnvVar(GO_SLACK_CHANNEL, "test-channel")
                                .addEnvVar(GO_SLACK_STATUSES, "fixed|broken")
                                .addEnvVar(GO_SLACK_STAGES, "stage.*"))
                );

        Rules defaultRules = new Rules()
                .setGoServerHost("http://localhost");
        Rules rules = resolver.resolvePipelineRule(defaultRules, "pipeline", "stage");

        assertThat(rules, notNullValue());
        assertThat(rules.getPipelineRules().size(), is(1));

        PipelineRule pipelineRule = rules.getPipelineRules().get(0);

        assertThat(pipelineRule.getStatus().size(), is(2));

    }

    @Test
    public void shouldFetchRulesFromStageOverPipeline() throws Exception {
        Server server = mock(Server.class);
        RuleResolver resolver = new RuleResolver(server);

        when(server.fetchPipelineConfig("pipeline"))
                .thenReturn(new PipelineConfig()
                        .setName("pipeline")
                        .addEnvVar(GO_SLACK_CHANNEL, "test-channel")
                        .addEnvVar(GO_SLACK_STATUSES, "failed|broken|fixed|building")
                        .addEnvVar(GO_SLACK_STAGES, "stage.*")
                        .addStage(new StageConfig()
                                .setName("stage")
                        ).addStage(new StageConfig()
                                .setName("other-stage")
                                .addEnvVar(GO_SLACK_CHANNEL, "other-channel")
                                .addEnvVar(GO_SLACK_STATUSES, "fixed|broken")
                                .addEnvVar(GO_SLACK_STAGES, "other-stage.*"))
                );

        Rules defaultRules = new Rules().setGoServerHost("http://localhost");
        {
            Rules rules1 = resolver.resolvePipelineRule(defaultRules, "pipeline", "stage");

            assertThat(rules1, notNullValue());
            assertThat(rules1.getPipelineRules().size(), is(1));

            PipelineRule pipelineRule1 = rules1.getPipelineRules().get(0);
            assertThat(pipelineRule1.getStatus().size(), is(4));
        }

        {
            Rules rules2 = resolver.resolvePipelineRule(defaultRules, "pipeline", "other-stage");

            assertThat(rules2, notNullValue());
            assertThat(rules2.getPipelineRules().size(), is(1));

            PipelineRule pipelineRule2 = rules2.getPipelineRules().get(0);
            assertThat(pipelineRule2.getStatus().size(), is(2));
        }
    }

    @Test
    public void shouldFetchRulesDisabledForStage() throws Exception {
        Server server = mock(Server.class);
        RuleResolver resolver = new RuleResolver(server);

        when(server.fetchPipelineConfig("pipeline"))
                .thenReturn(new PipelineConfig()
                        .setName("pipeline")
                        .addEnvVar(GO_SLACK_CHANNEL, "test-channel")
                        .addEnvVar(GO_SLACK_STATUSES, "failed|broken|fixed|building")
                        .addEnvVar(GO_SLACK_STAGES, "other-stage")
                        .addStage(new StageConfig()
                                .setName("stage")));

        Rules defaultRules = new Rules()
                .setGoServerHost("http://localhost");
        Rules rules = resolver.resolvePipelineRule(defaultRules, "pipeline", "current-stage");

        assertThat(rules, notNullValue());

        assertThat(rules.find("pipeline", "current-stage", "failed").isDefined(), is(false));
        assertThat(rules.find("pipeline", "other-stage", "failed").isDefined(), is(true));
    }

    @Test
    public void shouldFetchRulesDisabledForPipeline() throws Exception {
        Server server = mock(Server.class);
        RuleResolver resolver = new RuleResolver(server);

        when(server.fetchPipelineConfig("pipeline"))
                .thenReturn(new PipelineConfig()
                        .setName("pipeline")
                        .addStage(new StageConfig()
                                .setName("stage")));

        Rules defaultRules = new Rules()
                .setGoServerHost("http://localhost");
        Rules rules = resolver.resolvePipelineRule(defaultRules, "pipeline", "current-stage");

        assertThat(rules, notNullValue());

        assertThat(rules.find("pipeline", "current-stage", "failed").isDefined(), is(false));
        assertThat(rules.find("pipeline", "other-stage", "failed").isDefined(), is(false));
    }

    @Test
    public void shouldFetchRulesNoStatuses() throws Exception {
        Server server = mock(Server.class);
        RuleResolver resolver = new RuleResolver(server);

        when(server.fetchPipelineConfig("pipeline"))
                .thenReturn(new PipelineConfig()
                        .setName("pipeline")
                        .addEnvVar(GO_SLACK_CHANNEL, "test-channel")
                        .addEnvVar(GO_SLACK_STATUSES, "")
                        .addEnvVar(GO_SLACK_STAGES, "stage.*")
                        .addStage(new StageConfig()
                                .setName("stage")));

        Rules defaultRules = new Rules()
                .setGoServerHost("http://localhost");
        Rules rules = resolver.resolvePipelineRule(defaultRules, "pipeline", "defaultStage");

        assertThat(rules, notNullValue());

        assertThat(rules.find("pipeline", "defaultStage", "failed").isDefined(), is(false));
        assertThat(rules.find("pipeline", "other-stage", "failed").isDefined(), is(false));
    }

}