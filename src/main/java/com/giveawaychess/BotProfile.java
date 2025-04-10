package com.giveawaychess;

import java.util.List;
import java.util.Map;

public class BotProfile {
    public String botName;
    public String authorName;
    public Map<String, Integer> pieceValues;
    public List<String> capturePrioritization;
    public List<String> pawnBehavior;
    public List<String> forcedMoveStrategy;
    public String wildCard;

    // Optional utility methods to interact with this data
}
