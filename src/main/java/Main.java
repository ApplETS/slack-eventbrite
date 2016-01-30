import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.*;

import static spark.Spark.*;

public class Main {

    /**
     * You have to set the following environment variables in your server, for example heroku,
     * to link Slack and the EventBrite's event you want to track
     * <p>
     * EVENT_ID : ID for the event you want to track
     * EVENTBRITE_TOKEN : Required token to use EventBrite's API
     * SLACK_WEBHOOK_URL : Your Slack's webhook payload URL
     * </p>
     *
     * @param args
     */
    public static void main(String[] args) {

        port(Integer.valueOf(System.getenv("PORT")));
        staticFileLocation("/public");

//        post("attendees", (req, res) -> {
//            return null;
//
//        });

        post("/hello", (req, res) -> {

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://www.eventbriteapi.com/v3/events/" +
                            System.getenv(Constants.EVENT_ID) +
                            "/ticket_classes/?token=" +
                            System.getenv(Constants.EVENTBRITE_TOKEN))
                    .get()
                    .addHeader("cache-control", "no-cache")
                    .build();

            Response response = client.newCall(request).execute();

            JsonElement rootElement = new JsonParser().parse(response.body().string());
            JsonObject rootObject = rootElement.getAsJsonObject();
            JsonArray ticketClasses = rootObject.getAsJsonArray("ticket_classes");

            String text = "";

            for (JsonElement ticketClassElement : ticketClasses) {
                JsonObject ticketClass = ticketClassElement.getAsJsonObject();
                text += ticketClass.get("name").getAsString() + "\n";
                text += ticketClass.get("quantity_sold").getAsInt() + " / " +
                        ticketClass.get("quantity_total").getAsInt() + "\n";
            }

            client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\"text\": \"" + text + "\"}");
            request = new Request.Builder()
                    .url(System.getenv(Constants.SLACK_WEBHOOK_URL))
                    .post(body)
                    .addHeader("content-type", "application/json")
                    .addHeader("cache-control", "no-cache")
                    .build();

            response = client.newCall(request).execute();

            return text;
        });
    }

}
