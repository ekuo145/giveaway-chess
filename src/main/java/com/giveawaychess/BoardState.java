package com.giveawaychess;

public class BoardState {
    private Piece[][] board;
    public BoardState(Piece[][] board) {
        this.board = board;
    }

    @Override
    public String toString() {
        String result = "{\n";
        for (int i=0; i<8; i++) {
            for (int j=0; j<8; j++) {
            result += board[i][j].toString() + ",\n";
            }
        }
        result += "}";
        return result;
    }
}
