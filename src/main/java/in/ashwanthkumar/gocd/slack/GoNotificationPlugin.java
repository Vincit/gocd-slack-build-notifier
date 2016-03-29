package in.ashwanthkumar.gocd.slack;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import in.ashwanthkumar.gocd.slack.jsonapi.Server;
import in.ashwanthkumar.gocd.slack.ruleset.RuleResolver;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import in.ashwanthkumar.gocd.slack.ruleset.RulesReader;
import in.ashwanthkumar.gocd.slack.util.JSONUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static in.ashwanthkumar.gocd.slack.util.GoFieldUtils.createField;
import static in.ashwanthkumar.gocd.slack.util.GoRequestFactory.createGoApiRequest;
import static in.ashwanthkumar.utils.lang.StringUtils.isEmpty;
import static java.util.Arrays.asList;

@Extension
public class GoNotificationPlugin implements GoPlugin {
    private static Logger LOGGER = Logger.getLoggerFor(GoNotificationPlugin.class);

    public static final String EXTENSION_TYPE = "notification";
    private static final List<String> goSupportedVersions = asList("1.0");

    public static final String REQUEST_NOTIFICATIONS_INTERESTED_IN = "notifications-interested-in";
    public static final String REQUEST_STAGE_STATUS = "stage-status";
    public static final String REQUEST_GET_CONFIGURATION = "go.plugin-settings.get-configuration";
    public static final String REQUEST_GET_VIEW = "go.plugin-settings.get-view";
    public static final String REQUEST_VALIDATE_CONFIGURATION = "go.plugin-settings.validate-configuration";

    public static final int SUCCESS_RESPONSE_CODE = 200;
    public static final int INTERNAL_ERROR_RESPONSE_CODE = 500;

    public static final String GET_PLUGIN_SETTINGS = "go.processor.plugin-settings.get";
    public static final String GO_NOTIFY_CONFIGURATION = "go_notify.conf";

    private Rules fileRules;
    private GoApplicationAccessor goApplicationAccessor;

    public GoNotificationPlugin() {
        String userHome = System.getProperty("user.home");
        File pluginConfig = new File(userHome + File.separator + GO_NOTIFY_CONFIGURATION);
        if (!pluginConfig.exists()) {
            throw new RuntimeException(String.format("%s file is not found in %s", GO_NOTIFY_CONFIGURATION, userHome));
        }
        fileRules = RulesReader.read(pluginConfig);
    }

    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        this.goApplicationAccessor = goApplicationAccessor;
    }

    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) {
        String requestName = goPluginApiRequest.requestName();
        if (requestName.equals(REQUEST_NOTIFICATIONS_INTERESTED_IN)) {
            return handleNotificationsInterestedIn();
        } else if (requestName.equals(REQUEST_STAGE_STATUS)) {
            return handleStageNotification(goPluginApiRequest);
        } else if (requestName.equals(REQUEST_GET_VIEW)) {
            return handleRequestGetView();
        } else if (requestName.equals(REQUEST_VALIDATE_CONFIGURATION)) {
            return handleValidateConfig(goPluginApiRequest.requestBody());
        } else if (requestName.equals(REQUEST_GET_CONFIGURATION)) {
            return handleRequestGetConfiguration();
        }
        return renderJSON(404, null);
    }

    private GoPluginApiResponse handleValidateConfig(String requestBody) {
        List<Object> response = Arrays.asList();
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }


    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier(EXTENSION_TYPE, goSupportedVersions);
    }


    private GoPluginApiResponse handleRequestGetView() {
        Map<String, Object> response = new HashMap<String, Object>();

        try {
            String template = IOUtils.toString(getClass().getResourceAsStream("/views/config.template.html"), "UTF-8");
            response.put("template", template);
        } catch (IOException e) {
            response.put("error", "Can't load view template");
            return renderJSON(INTERNAL_ERROR_RESPONSE_CODE, response);
        }


        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleRequestGetConfiguration() {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("server-url-external", createField("External GoCD Server URL", null, false, true, false, "0"));
        response.put("webhook-url", createField("Webhook URL", null, true, false, false, "1"));
        response.put("display-name", createField("Display Name", null, false, true, false, "2"));
        response.put("icon-url", createField("Icon URL", null, false, false, false, "3"));
        response.put("default-channel", createField("Default Channel", null, false, false, false, "5"));
        response.put("admin-username", createField("Admin Username", null, false, false, false, "6"));
        response.put("admin-password", createField("Admin Password", null, false, false, true, "7"));
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleNotificationsInterestedIn() {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("notifications", Arrays.asList(REQUEST_STAGE_STATUS));
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleStageNotification(GoPluginApiRequest goPluginApiRequest) {
        GoNotificationMessage message = parseNotificationMessage(goPluginApiRequest);
        int responseCode = SUCCESS_RESPONSE_CODE;

        Map<String, Object> response = new HashMap<String, Object>();
        List<String> messages = new ArrayList<String>();
        try {
            RuleResolver resolver = getRuleResolver();

            Rules filePipelineRules = resolver.resolvePipelineRule(fileRules, message.getPipelineName(), message.getStageName());

            response.put("status", "success");
            LOGGER.info(message.fullyQualifiedJobName() + " has " + message.getStageState() + "/" + message.getStageResult());
            filePipelineRules.resolvePipelineListener().notify(message);
        } catch (Exception e) {
            LOGGER.info(message.fullyQualifiedJobName() + " failed with error", e);
            responseCode = INTERNAL_ERROR_RESPONSE_CODE;
            response.put("status", "failure");
            if (!isEmpty(e.getMessage())) {
                messages.add(e.getMessage());
            }
        }

        if (!messages.isEmpty()) {
            response.put("messages", messages);
        }
        return renderJSON(responseCode, response);
    }

    private GoNotificationMessage parseNotificationMessage(GoPluginApiRequest goPluginApiRequest) {
        return new GsonBuilder().create().fromJson(goPluginApiRequest.requestBody(), GoNotificationMessage.class);
    }

    private GoPluginApiResponse renderJSON(final int responseCode, Object response) {
        final String json = response == null ? null : new GsonBuilder().disableHtmlEscaping().create().toJson(response);
        return new GoPluginApiResponse() {
            @Override
            public int responseCode() {
                return responseCode;
            }

            @Override
            public Map<String, String> responseHeaders() {
                return null;
            }

            @Override
            public String responseBody() {
                return json;
            }
        };
    }

    private RuleResolver getRuleResolver() {
        Rules configuration = getPluginConfiguration();
        RuleResolver resolver;
        if (configuration.isUseConfigFile()) {
            resolver = new RuleResolver(new Server(fileRules));
        } else {
            resolver = new RuleResolver(new Server(configuration));
        }
        return resolver;
    }

    private Rules getPluginConfiguration() {
        Map<String, Object> responseBodyMap = getRulesFromGo();
        return new Rules()
                .setEnabled(false) // FIXME: Really false?
                .setUseConfigFile(Boolean.valueOf((String)responseBodyMap.get("useconfigfile")))
                .setGoLogin((String)responseBodyMap.get("adminusername"))
                .setGoPassword((String)responseBodyMap.get("adminpassword"))
                .setWebHookUrl((String)responseBodyMap.get("webhookurl"))
                .setSlackDisplayName((String)responseBodyMap.get("displayname"))
                .setSlackUserIcon((String)responseBodyMap.get("iconurl"))
                .setGoServerHost((String)responseBodyMap.get("serverhost"));
    }

    private Map<String, Object> getRulesFromGo() {
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("plugin-id", "slack.notifier");
        GoApiResponse response = goApplicationAccessor.submit(createGoApiRequest(GET_PLUGIN_SETTINGS, JSONUtils.toJSON(requestMap)));

        return response.responseBody() == null ?
                new HashMap<String, Object>() :
                (Map<String, Object>) JSONUtils.fromJSON(response.responseBody());
    }
}
