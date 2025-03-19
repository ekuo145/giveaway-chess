package com.giveawaychess;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class BotConfigReader {

    public static List<JSONObject> loadAllBotConfigs(String directoryPath) {
        List<JSONObject> botConfigs = new ArrayList<>();

        try {
            // List all JSON files in the directory
            DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(directoryPath), "*.json");
            for (Path entry : stream) {
                JSONObject botConfig = loadBotConfig(entry.toString());
                if (botConfig != null) {
                    botConfigs.add(botConfig);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return botConfigs;
    }

    public static JSONObject loadBotConfig(String filePath) {
        try {
            // Read JSON file
            FileReader reader = new FileReader(filePath);
            StringBuilder jsonText = new StringBuilder();
            int i;
            while ((i = reader.read()) != -1) {
                jsonText.append((char) i);
            }
            reader.close();

            // Parse JSON
            JSONObject botConfig = new JSONObject(jsonText.toString());
            return botConfig;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        List<JSONObject> bots = loadAllBotConfigs("bots/");
        
        for (JSONObject bot : bots) {
            System.out.println("\nLoaded Bot: " + bot.getString("botName"));
            System.out.println("Author: " + bot.getString("authorName"));
            System.out.println("Piece Values: " + bot.getJSONObject("pieceValues").toMap());
            System.out.println("Capture Prioritization: " + bot.getJSONArray("capturePrioritization").toList());
            System.out.println("Pawn Behavior: " + bot.getJSONArray("pawnBehavior").toList());
            System.out.println("Forced Move Strategy: " + bot.getJSONArray("forcedMoveStrategy").toList());
            System.out.println("Wild Card: " + bot.getString("wildCard"));
        }
    }
}
