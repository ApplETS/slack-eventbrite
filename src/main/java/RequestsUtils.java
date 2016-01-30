import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import model.Attendee;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gnut3ll4 on 29/01/16.
 */
public class RequestsUtils {

    public static ArrayList<Attendee> getAttendees(int pageNumber, ArrayList<Attendee> attendees) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://www.eventbriteapi.com/v3/events/" +
                        System.getenv(Constants.EVENT_ID) +
                        "/attendees/?page=" + pageNumber +
                        "&token=" + System.getenv(Constants.EVENTBRITE_TOKEN))
                .get()
                .build();

        Response response = client.newCall(request).execute();


        JsonElement rootElement = new JsonParser().parse(response.body().string());
        JsonObject rootObject = rootElement.getAsJsonObject();
        JsonObject pagination = rootObject.getAsJsonObject("pagination");
        int pageCount = pagination.get("page_count").getAsInt();

        JsonElement attendeesObject = rootObject.getAsJsonArray("attendees");

        attendees.addAll(new Gson().fromJson(attendeesObject,
                new TypeToken<List<Attendee>>() {
                }.getType()));

        if (pageNumber < pageCount) {
            attendees.addAll(getAttendees(pageNumber + 1, attendees));
        }

        return attendees;
    }
}
