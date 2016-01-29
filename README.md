# slack-eventbrite
Uses Slack's webhooks and EventBrite API to track orders from an EventBrite's event.

## Deploying to Heroku

```sh
$ heroku create
$ git push heroku master
$ heroku open
```

You also have to set the following environment variables in heroku :
* EVENT_ID : ID for the event you want to track (on EventBrite)
* EVENTBRITE_TOKEN : Required token to use EventBrite's REST API
* SLACK_WEBHOOK_URL : Your Slack's webhook payload URL
