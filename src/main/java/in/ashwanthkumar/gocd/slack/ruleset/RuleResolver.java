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

    public static final String GO_SLACK_CHANNEL = "GO_SLACK_CHANNEL";
    public static final String GO_SLACK_USER = "GO_SLACK_USER";
    public static final String GO_SLACK_STATUSES = "GO_SLACK_STATUSES";
    public static final String GO_SLACK_STAGES = "GO_SLACK_STAGES";
    private static Logger LOGGER = Logger.getLoggerFor(RuleResolver.class);

    private Server server;

    public RuleResolver(Server server) {
        this.server = server;
    }

    public Rules resolvePipelineRule(Rules defaultRules, String currentPipeline, String currentStage)
            throws URISyntaxException, IOException
    {
        PipelineConfig pipelineConfig = server.fetchPipelineConfig(currentPipeline);

        Option<String> slackChannel = pipelineConfig.getStageEnvVar(currentStage, GO_SLACK_CHANNEL);
        Option<String> slackUser = pipelineConfig.getStageEnvVar(currentStage, GO_SLACK_USER);
        Option<String> buildStatuses = pipelineConfig.getStageEnvVar(currentStage, GO_SLACK_STATUSES);
        Option<String> stageRegex = pipelineConfig.getStageEnvVar(currentStage, GO_SLACK_STAGES);
        Option<String> stageBuildStatuses = pipelineConfig.getStageEnvVar(currentStage, getStageBuildStatusKey(currentStage));

        String statuses = stageBuildStatuses.getOrElse(buildStatuses.getOrElse(null));
        String targetChannel = resolveTargetChannel(slackChannel, slackUser);

        PipelineRule pipelineRule = new PipelineRule()
                .setNameRegex(currentPipeline)
                .setStageRegex(stageRegex.getOrElse(currentStage))
                .setStatus(StatusUtils.statusStringToSet(statuses))
                .setChannel(targetChannel);

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

    private String resolveTargetChannel(Option<String> slackChannel, Option<String> slackUser) {
        if (slackChannel.isDefined() && slackUser.isDefined()) {
            throw new IllegalArgumentException(
                    String.format("Only %s or %s may be defined, not both", GO_SLACK_CHANNEL, GO_SLACK_USER)
            );
        } else if (slackChannel.isDefined()) {
            return "#" + slackChannel.get();
        } else if (slackUser.isDefined()) {
            return "@" + slackUser.get();
        } else {
            return null;
        }
    }

    private String getStageBuildStatusKey(String stageName) {
        return String.format("%s_%s", GO_SLACK_STATUSES, removeUnsupportedEnvVarChars(stageName));
    }

    private String removeUnsupportedEnvVarChars(String stageName) {
        return stageName.replaceAll("-", "");
    }

}
