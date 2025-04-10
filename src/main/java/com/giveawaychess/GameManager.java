package com.giveawaychess;

public class GameManager {
    private Player whitePlayer;
    private Player blackPlayer;
    private Player currentPlayer;

    private int turnNumber = 0;

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

    public int getTurnNumber() {
        return turnNumber;
    }
    
    public void incrementTurnNumber() {
        turnNumber++;
        // System.out.println("✅ Turn number incremented to: " + turnNumber);
    }

    public void incrementIfWhiteBot(Player currentPlayer) {
        if (currentPlayer.getColor() == Piece.Color.WHITE) {
            incrementTurnNumber();
        }
    }
    
    public void resetTurnNumber() {
        turnNumber = 0;
    }
}


