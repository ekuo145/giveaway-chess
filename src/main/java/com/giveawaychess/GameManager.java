package com.giveawaychess;

public class GameManager {
    private Player whitePlayer;
    private Player blackPlayer;
    private Piece.Color turnColor;

    public GameManager(Player whitePlayer, Player blackPlayer) {
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.turnColor = Piece.Color.WHITE; // White starts
    }

    public Player getCurrentPlayer() {
        return (turnColor == Piece.Color.WHITE) ? whitePlayer : blackPlayer;
    }

    public void switchTurn() {
        turnColor = (turnColor == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
    }
}

