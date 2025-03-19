package com.giveawaychess;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class BotConfigGenerator {
    
    public static void saveBotConfig(String botName, String authorName, 
                                     Map<String, Integer> pieceValues, 
                                     List<String> capturePreferences, 
                                     List<String> pawnBehavior, 
                                     List<String> forcedMoveStrategy, 
                                     String wildCard) {
        try {
            // Create a JSON object
            JSONObject botConfig = new JSONObject();
            
            // Bot Information
            botConfig.put("botName", botName);
            botConfig.put("authorName", authorName);

            // Piece Values
            JSONObject pieceValuesJSON = new JSONObject(pieceValues);
            botConfig.put("pieceValues", pieceValuesJSON);

            // Strategic Incentives
            botConfig.put("capturePrioritization", new JSONArray(capturePreferences));
            botConfig.put("pawnBehavior", new JSONArray(pawnBehavior));
            botConfig.put("forcedMoveStrategy", new JSONArray(forcedMoveStrategy));

            // Wild Card
            botConfig.put("wildCard", wildCard);

            // Define file path using bot name
            String fileName = "bots/" + botName.replaceAll("\\s+", "_") + ".json"; // Save in "bots" directory
            
            // Ensure the directory exists
            Files.createDirectories(Paths.get("bots"));

            // Write JSON to a file
            try (FileWriter file = new FileWriter(fileName)) {
                file.write(botConfig.toString(4)); // Indented output for readability
            }

            System.out.println("Bot configuration saved: " + fileName);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Example bot setup
        Map<String, Integer> pieceValues = Map.of(
            "Pawn", -1, 
            "Knight", -3, 
            "Bishop", -3, 
            "Rook", -5, 
            "Queen", -9, 
            "King", 0
        );

        List<String> capturePreferences = List.of("Prefer capturing higher-valued pieces");
        List<String> pawnBehavior = List.of("Prefer pushing pawns early");
        List<String> forcedMoveStrategy = List.of("Find the move that reduces material fastest");
        String wildCard = "No Queen Moves";

        // Save multiple bot configurations
        saveBotConfig("AggressiveBot", "JohnDoe", pieceValues, capturePreferences, pawnBehavior, forcedMoveStrategy, wildCard);
        saveBotConfig("DefensiveBot", "JaneDoe", pieceValues, List.of("Capture only when forced"), List.of("Delay pawn moves for later"), List.of("Try to create more forced moves"), "Skip Every 5th Turn");
    }
}
