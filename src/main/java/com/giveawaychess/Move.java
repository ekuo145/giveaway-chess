package com.giveawaychess;

public class Move {
    int startRow;
    int startCol;
    int endRow;
    int endCol;
    Piece movedPiece;
    private Piece capturedPiece; // Store the piece that was captured
    private boolean promotion;

    private boolean wasEnPassant = false;

    public Move(int startRow, int startCol, int endRow, int endCol, Piece movedPiece) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.promotion = promotion;
    }

    public boolean isPawnMove() {
        if (movedPiece instanceof Piece) {
            if (movedPiece.getType() == Piece.PieceType.PAWN) {
                return true;
            }
        }
            return false;
    }

    public int getFromRow() {
        return startRow;
    }

    public int getFromCol() {
        return startCol;
    }

    public int getToRow() {
        return endRow;
    }

    public int getToCol() {
        return endCol;
    }

    public Piece getMovedPiece() {
        return movedPiece;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public boolean wasPromotion() {
        return promotion;
    }

    public boolean wasEnPassant() {
        return wasEnPassant;
    }

    public void setCapturedPiece(Piece capturedPiece) {
        this.capturedPiece = capturedPiece;
    }    

    public void setWasEnPassant(boolean wasEnPassant) {
        this.wasEnPassant = wasEnPassant;
    }
    
}