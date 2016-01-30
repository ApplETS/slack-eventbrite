# Slack-Eventbrite
Uses Slack's webhooks and Eventbrite API to track orders from an Eventbrite's event.

## Deploying to Heroku

```sh
$ heroku create
$ git push heroku master
$ heroku open
```

You also have to set the following environment variables in Heroku :
* EVENT_ID : ID for the event you want to track (on Eventbrite)
* EVENTBRITE_TOKEN : Required token to use Eventbrite's REST API
* SLACK_WEBHOOK_URL : Your Slack's webhook payload URL
