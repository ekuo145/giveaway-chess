package com.giveawaychess;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class BotProfileLoader {
    public static BotProfile loadBotProfile(String filePath) throws IOException {
        String content = Files.readString(Paths.get(filePath));
        JSONObject json = new JSONObject(content);

        BotProfile profile = new BotProfile();
        profile.botName = json.getString("botName");
        profile.authorName = json.getString("authorName");

        JSONObject values = json.getJSONObject("pieceValues");
        Map<String, Integer> pieceValues = new HashMap<>();
        for (String key : values.keySet()) {
            pieceValues.put(key, values.getInt(key));
        }
        profile.pieceValues = pieceValues;

        profile.capturePrioritization = toList(json.getJSONArray("capturePrioritization"));
        profile.pawnBehavior = toList(json.getJSONArray("pawnBehavior"));
        profile.forcedMoveStrategy = toList(json.getJSONArray("forcedMoveStrategy"));
        profile.wildCard = json.getString("wildCard");

        return profile;
    }

    private static List<String> toList(JSONArray arr) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            list.add(arr.getString(i));
        }
        return list;
    }
}
