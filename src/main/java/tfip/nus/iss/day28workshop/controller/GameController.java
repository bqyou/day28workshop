package tfip.nus.iss.day28workshop.controller;

import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import tfip.nus.iss.day28workshop.Utils;
import tfip.nus.iss.day28workshop.model.Game;
import tfip.nus.iss.day28workshop.repo.GameRepo;

@RestController
@RequestMapping
public class GameController {

    @Autowired
    private GameRepo gameRepo;

    @GetMapping(path = "/game/{gid}/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> findGameByGID(@PathVariable Integer gid) {
        if (gameRepo.findGameById(gid).size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("GID not found");
        }
        Document d = gameRepo.findGameById(gid).get(0);
        Game g = Utils.toGame(d);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Utils.toJson(g).toString());
    }

    @GetMapping(path = "/review/{cid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> findCommentByCID(@PathVariable String cid) {
        if (gameRepo.findCommentById(cid).size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Comment not found");
        }
        Document d = gameRepo.findCommentById(cid).get(0);

        return ResponseEntity.status(HttpStatus.OK)
                .body(d.toJson());
    }

    @GetMapping(path = "/games/{order}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> listGames(@PathVariable String order) {
        Boolean high;
        if (order.equalsIgnoreCase("lowest")) {
            high = false;
        } else if ((order.equalsIgnoreCase("highest"))) {
            high = true;
        } else
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("bad request");

        List<JsonObject> games = gameRepo.listCommentByRating(high)
                .stream()
                .map(v -> Utils.toGameList(v))
                .toList();

        Date now = new Date();

        JsonObject resp = Json.createObjectBuilder()
                .add("rating", order)
                .add("games", Utils.toJsonArray(games))
                .add("timestamp", now.toString())
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .body(resp.toString());
    }
}
