package in.ashwanthkumar.gocd.slack.jsonapi;

import com.google.gson.JsonElement;
import com.thoughtworks.go.plugin.api.logging.Logger;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import in.ashwanthkumar.gocd.slack.util.Options;
import in.ashwanthkumar.utils.lang.option.Option;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static in.ashwanthkumar.utils.lang.StringUtils.isNotEmpty;

/**
 * Actual methods for contacting the remote server.
 */
public class Server {
    private Logger LOG = Logger.getLoggerFor(Server.class);

    // Contains authentication credentials, etc.
    private Rules mRules;
    private HttpConnectionUtil httpConnectionUtil;

    /**
     * Construct a new server object, using credentials from Rules.
     */
    public Server(Rules rules) {
        mRules = rules;
        httpConnectionUtil = new HttpConnectionUtil();
    }

    Server(Rules mRules, HttpConnectionUtil httpConnectionUtil) {
        this.mRules = mRules;
        this.httpConnectionUtil = httpConnectionUtil;
    }

    JsonElement getUrl(URL url, Option<String> acceptContent)
        throws IOException
    {
        LOG.info("Fetching " + url.toString());

        HttpURLConnection request = httpConnectionUtil.getConnection(url);

        // Add in our HTTP authorization credentials if we have them.
        if (isNotEmpty(mRules.getGoLogin()) && isNotEmpty(mRules.getGoPassword())) {
            String userpass = mRules.getGoLogin() + ":" + mRules.getGoPassword();
            String basicAuth = "Basic "
                    + DatatypeConverter.printBase64Binary(userpass.getBytes());
            request.setRequestProperty("Authorization", basicAuth);
        }

        if (acceptContent.isDefined()) {
            request.setRequestProperty("Accept", acceptContent.get());
        }

        request.connect();

        return httpConnectionUtil.responseToJson(request.getContent());
    }

    /**
     * Get the recent history of a pipeline.
     */
    public History getPipelineHistory(String pipelineName)
        throws MalformedURLException, IOException
    {
        URL url = new URL(String.format("%s/go/api/pipelines/%s/history",
                mRules.getGoServerHost(), pipelineName));
        JsonElement json = getUrl(url, Options.<String>empty());
        return httpConnectionUtil.convertResponse(json, History.class);
    }

    /**
     * Get a specific instance of a pipeline.
     */
    public Pipeline getPipelineInstance(String pipelineName, int pipelineCounter)
        throws MalformedURLException, IOException
    {
        URL url = new URL(String.format("%s/go/api/pipelines/%s/instance/%d",
                                        mRules.getGoServerHost(), pipelineName, pipelineCounter));
        JsonElement json = getUrl(url, Options.<String>empty());
        return httpConnectionUtil.convertResponse(json, Pipeline.class);
    }

    public JsonElement fetchPipelineConfig(String pipelineName) throws IOException {
        URL url = new URL(String.format("%s/go/api/admin/pipelines/%s",
                mRules.getGoServerHost(),
                pipelineName)
        );

        return getUrl(url, Option.option("application/vnd.go.cd.v1+json"));
    }

}
