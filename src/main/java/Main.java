import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.*;
import model.Answer;
import model.Attendee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

        get("/attendees", (req, res) -> {

            String text = "";

            if (req.queryParams("token").equals(System.getenv(Constants.SLACK_VERIFICATION_TOKEN))) {
                ArrayList<Attendee> attendees = RequestsUtils.getAttendees(1, new ArrayList<>());

                HashMap<String, HashMap<String, Integer>> mapQuestion = new HashMap<>();

                HashMap<String, Integer> mapAnswer;
                int answerCount;
                for (Attendee attendee : attendees) {
                    if (!attendee.getCancelled()) {
                        for (Answer answer : attendee.getAnswers()) {

                            if (answer.getType().equals("multiple_choice")) {

                                mapAnswer = mapQuestion.getOrDefault(answer.getQuestion(), new HashMap<>());
                                answerCount = mapAnswer.getOrDefault(answer.getAnswer(), 0);
                                mapAnswer.put(answer.getAnswer(), answerCount + 1);
                                mapQuestion.put(answer.getQuestion(), mapAnswer);
                            }
                        }
                    }
                }

                for (Map.Entry<String, HashMap<String, Integer>> mapQuestionEntry : mapQuestion.entrySet()) {
                    text += "*" + mapQuestionEntry.getKey() + "*\n";
                    for (Map.Entry<String, Integer> mapAnswerEntry : mapQuestionEntry.getValue().entrySet()) {
                        text += mapAnswerEntry.getKey() + " : " + mapAnswerEntry.getValue() + "\n";
                    }
                    text += "\n";
                }

                RequestsUtils.sendMessageToSlack(text);

                halt(200);
            } else {
                halt(401, "Not Authorized");
            }

            halt(501);
            return null;

        });

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

            RequestsUtils.sendMessageToSlack(text);
            return text;
        });
    }

}
