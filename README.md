# gocd-slack-build-notifier
Slack based GoCD build notifier

![Demo](images/gocd-slack-notifier-demo.png)

## Setup
Download jar from releases & place it in /plugins/external & restart Go Server.

## Plugin Configuration

|Config|Description|
|------|-----------|
|Server Host|FQDN of the Go Server. All links on the slack channel will be relative to this host.|
|Webhook URL|Slack webhook URL|
|Display Name|Name of the messages poster.|
|Icon URL|URL to icon to show with the messges.|
|Default Channel|Default channel to post messages. Prefix with `#` for channels and `@` for users.|
|Admin Username|Login for a Go user who is authorized to access the REST API.|
|Admin Password|Password for the user specified above.|

## Pipeline and Stage Configuration

Slack configuration is done via pipeline and/or stage environment variables.

|Config|Description|
|------|------------|
|GO_SLACK_CHANNEL|Channel to post the messages. Prefix with `#` for channels and `@` for users.|
|GO_SLACK_STATUSES|State of the pipeline at which we should send a notification. You can provide multiple values separated by pipe (`|`) symbol. Valid values are passed, failed, cancelled, building, fixed, broken or all.|
|GO_SLACK_STAGES|Regex to match the stage name.|

## License

http://www.apache.org/licenses/LICENSE-2.0
