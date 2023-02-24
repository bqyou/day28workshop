package tfip.nus.iss.day28workshop;

import org.bson.Document;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import tfip.nus.iss.day28workshop.model.Game;
import tfip.nus.iss.day28workshop.model.GameList;

import static tfip.nus.iss.day28workshop.Constants.*;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Utils {

    public static JsonObject toJson(String str) {
        Reader reader = new StringReader(str);
        JsonReader jsonReader = Json.createReader(reader);
        return jsonReader.readObject();
    }

    public static Game toGame(Document d) {
        Game g = new Game();
        g.setGameId(d.getInteger(FIELD_GID));
        g.setName(d.getString(FIELD_NAME));
        g.setRank(d.getInteger(FIELD_RANK));
        g.setYear(d.getInteger(FIELD_YEAR));
        g.setAverage(d.getDouble("average"));
        g.setUsersRated(d.getInteger(FIELD_RATED));
        g.setUrl(d.getString(FIELD_URL));
        g.setThumbnail(d.getString(FIELD_IMAGE));
        List<String> list = new ArrayList<String>();
        for (Object f : d.get(FIELD_REVIEWS, List.class)) {
            list.add("/review/" + ((Document) f).getString(FIELD_CID));
        }
        g.setReviews(list);
        return g;
    }

    public static JsonObject toJson(Game g) {

        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
        g.getReviews().stream()
                .map(v -> v.toString())
                .forEach(v -> {
                    arrBuilder.add(v);
                });

        Date now = new Date();

        JsonObject json = Json.createObjectBuilder()
                .add(FIELD_GID, g.getGameId())
                .add(FIELD_NAME, g.getName())
                .add(FIELD_YEAR, g.getYear())
                .add(FIELD_RANK, g.getRank())
                .add(FIELD_AVERAGE, g.getAverage())
                .add(FIELD_RATED, g.getUsersRated())
                .add(FIELD_URL, g.getUrl())
                .add(FIELD_IMAGE, g.getThumbnail())
                .add(FIELD_REVIEWS, arrBuilder.build())
                .add("timestamp", now.toString())
                .build();
        return json;
    }

    public static JsonObject toGameList(Document d) {
        GameList g = new GameList();
        g.set_id(d.getInteger(FIELD_UNDERSCORE_ID));
        g.setName(d.getString(FIELD_NAME));
        g.setRating(d.getInteger(FIELD_RATING));
        g.setUser(d.getString(FIELD_USER));
        g.setComment(d.getString("comment"));
        g.setReviewId(d.getString("review_id"));
        System.out.println(g.get_id());

        return Json.createObjectBuilder()
                .add("_id", g.get_id())
                .add("name", g.getName())
                .add("rating", g.getRating())
                .add("user", g.getUser())
                .add("comment", g.getComment())
                .add("review_id", g.getReviewId())
                .build();
    }

    public static JsonArray toJsonArray(List<JsonObject> list) {
        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
        list.stream()
                .forEach(v -> {
                    arrBuilder.add(v);
                });
        return arrBuilder.build();
    }

}
