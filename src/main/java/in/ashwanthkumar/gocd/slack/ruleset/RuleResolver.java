package in.ashwanthkumar.gocd.slack.ruleset;

import com.thoughtworks.go.plugin.api.logging.Logger;
import in.ashwanthkumar.gocd.slack.jsonapi.Server;
import in.ashwanthkumar.gocd.slack.jsonapi.config.pipeline.PipelineConfig;
import in.ashwanthkumar.gocd.slack.util.StatusUtils;
import in.ashwanthkumar.utils.lang.option.Option;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

public class RuleResolver {

    private static Logger LOGGER = Logger.getLoggerFor(RuleResolver.class);

    private Server server;

    public RuleResolver(Server server) {
        this.server = server;
    }

    public Rules resolvePipelineRule(Rules defaultRules, String currentPipeline, String currentStage)
            throws URISyntaxException, IOException
    {
        PipelineConfig pipelineConfig = server.fetchPipelineConfig(currentPipeline);

        Option<String> slackChannel = pipelineConfig.getStageEnvVar(currentStage, "GO_SLACK_CHANNEL");
        Option<String> buildStatuses = pipelineConfig.getStageEnvVar(currentStage, "GO_SLACK_STATUSES");
        Option<String> stageRegex = pipelineConfig.getStageEnvVar(currentStage, "GO_SLACK_STAGES");


        PipelineRule pipelineRule = new PipelineRule()
                .setNameRegex(currentPipeline)
                .setStageRegex(stageRegex.getOrElse(currentStage))
                .setStatus(StatusUtils.statusStringToSet(buildStatuses.getOrElse(null)))
                .setChannel(slackChannel.getOrElse(null));

        LOGGER.info(String.format("Resolved rules for pipeline %s: %s", currentPipeline, pipelineRule.toString()));

        return new Rules()
                .setGoPassword(defaultRules.getGoPassword())
                .setGoLogin(defaultRules.getGoLogin())
                .setGoServerHost(defaultRules.getGoServerHost())
                .setWebHookUrl(defaultRules.getWebHookUrl())
                .setSlackDisplayName(defaultRules.getSlackDisplayName())
                .setSlackUserIcon(defaultRules.getSlackUserIcon())
                .setEnabled(defaultRules.isEnabled())
                .setSlackChannel(slackChannel.getOrElse(null))
                .setPipelineRules(Collections.singletonList(pipelineRule));
    }

}
