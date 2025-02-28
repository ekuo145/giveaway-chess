package com.giveawaychess;

public class GameManager {
    private Player whitePlayer;
    private Player blackPlayer;
    private Player currentPlayer;

    public GameManager() {
        // Initially, players are null. They will be set after creation.
    }

    public void setPlayers(Player whitePlayer, Player blackPlayer) {
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.currentPlayer = whitePlayer; // White starts first
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Piece.Color color) {
        currentPlayer = (color == Piece.Color.WHITE) ? whitePlayer : blackPlayer;
    }

    public void switchTurn() {
        currentPlayer = (currentPlayer == whitePlayer) ? blackPlayer : whitePlayer;
    }
}


