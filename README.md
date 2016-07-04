# gocd-slack-build-notifier
Slack based GoCD build notifier

## Setup
Download jar from releases & place it in /plugins/external & restart Go Server.

## Configuration
All configurations are in [HOCON](https://github.com/typesafehub/config) format. Plugin searches for the configuration file in the following order

1. File defined by the environment variable `GO_NOTIFY_CONF`.
2. `go_notify.conf` at the user's home directory. Typically it's the `go` user's home directory (`/var/go`).
3. `go_notify.conf` present at the `CRUISE_SERVER_DIR` environment variable location.

Minimalistic configuration would be something like
```hocon
gocd.slack {
  login = "someuser"
  password = "somepassword"
  server-host = "http://localhost:8153/"
  api-server-host = "http://localhost:8153/"
  webhookUrl = "https://hooks.slack.com/services/...."

  # optional fields
  channel = "#build"
  slackDisplayName = "gocd-slack-bot"
  slackUserIconURL = "http://example.com/slack-bot.png"
  displayMaterialChanges = true
  proxy {
    hostname = "localhost"
    port = "5555"
    type = "socks" # acceptable values are http / socks
  }
}
```
- `login` - Login for a Go user who is authorized to access the REST API.
- `password` - Password for the user specified above. You might want to create a less privileged user for this plugin.
- `server-host` - FQDN of the Go Server. All links on the slack channel will be relative to this host.
- `api-server-host` - This is an optional attribute. Set this field to localhost so server will use this endpoint to get `PipelineHistory` and `PipelineInstance`  
- `webhookUrl` - Slack Webhook URL
- `channel` - Override the default channel where we should send the notifications in slack. You can also give a value starting with `@` to send it to any specific user.
- `displayMaterialChanges` - Display material changes in the notification (git revisions for example). Defaults to true, set to false if you want to hide.
- `proxy` - Specify proxy related settings for the plugin.
  - `proxy.hostname` - Proxy Host
  - `proxy.port` - Proxy Port
  - `proxy.type` - `socks` or `http` are the only accepted values.

## Pipeline Rules
By default the plugin pushes a note about all failed stages across all pipelines to Slack. You have fine grain control over this operation.
```hocon
gocd.slack {
  server-host = "http://localhost:8153/"
  webhookUrl = "https://hooks.slack.com/services/...."

  pipelines = [{
    name = "gocd-slack-build"
    stage = "build"
    state = "failed|passed"
    channel = "#oss-build-group"
    owners = ["ashwanthkumar"]
  },
  {
    name = ".*"
    stage = ".*"
    state = "failed"
  }]
}
```
`gocd.slack.pipelines` contains all the rules for the go-server. It is a list of rules (see below for what the parameters mean) for various pipelines. The plugin will pick the first matching pipeline rule from the pipelines collection above, so your most specific rule should be first, with the most generic rule at the bottom.
- `name` - Regex to match the pipeline name
- `stage` - Regex to match the stage name
- `state` - State of the pipeline at which we should send a notification. You can provide multiple values separated by pipe (`|`) symbol. Valid values are passed, failed, cancelled, building, fixed, broken or all.
- `channel` - (Optional) channel where we should send the slack notification. This setting for a rule overrides the global setting
- `owners` - (Optional) list of slack user handles who must be tagged in the message upon notifications

## Screenshots
<img src="https://raw.githubusercontent.com/ashwanthkumar/gocd-slack-build-notifier/master/images/gocd-slack-notifier-demo-with-changes.png" width="400"/>
<img src="https://raw.githubusercontent.com/ashwanthkumar/gocd-slack-build-notifier/master/images/gocd-slack-notifier-demo.png" width="400"/>

## Environment Variable Based Configuration

Pipeline and stage specific configurations can also be done via pipeline level environment variables. This makes
it possible to let pipeline admins to configure the notifications as they wish without modifying the configuration
file and restarting the server. The environment variables based configurations override the configuration file options
if any of the environment variables has been set for the pipeline.

- `GO_SLACK_STAGES` - Stage regex, works same as the configuration file option.
- `GO_SLACK_CHANNEL` - (Optional) Channel to send the message. (If this is set, `GO_SLACK_USER` must not be set)
- `GO_SLACK_USER` - (Optional) User to send the message. (If this is set, `GO_SLACK_CHANNEL` must not be set)
- `GO_SLACK_STATUSES` - (Optional) Statuses to, works same as the configuration file option. This will
                        override any configuration file status configuration.
- `GO_SLACK_STATUSES_<stage name>` - (Optional) Stage specific status option. For instance if the stage name is `buildStage`,
                                     then configuration name is `GO_SLACK_STAGES_buildStage`.
                                     If the stage name contains dashes, they are removed. For instance
                                     if the stage name is `build-stage` then the configuration name is
                                     `GO_SLACK_STAGES_buildstage`. This will override any configuration file and
                                     pipeline environment variable level configuration.

## License

http://www.apache.org/licenses/LICENSE-2.0
