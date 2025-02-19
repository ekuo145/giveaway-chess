package com.giveawaychess;

import java.util.ArrayList;
import java.util.List;

public class Piece {
    private ChessBoard board1;
    // Enum for piece type
    public enum PieceType {
        KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN;
    }

    // Enum for piece color
    public enum Color {
        WHITE, BLACK;
    }

    private PieceType type;
    private Color color;

    // Constructor for creating a piece
    public Piece(PieceType type, Color color) {
        this.type = type;
        this.color = color;
    }

    // Getters for type and color
    public PieceType getType() {
        return type;
    }

    public Color getColor() {
        return color;
    }

    public String getSymbol() {
        switch (type) {
            case PAWN:
                return (color == Color.WHITE) ? "P" : "p";
            case ROOK:
                return (color == Color.WHITE) ? "R" : "r";
            case KNIGHT:
                return (color == Color.WHITE) ? "N" : "n";
            case BISHOP:
                return (color == Color.WHITE) ? "B" : "b";
            case QUEEN:
                return (color == Color.WHITE) ? "Q" : "q";
            case KING:
                return (color == Color.WHITE) ? "K" : "k";
            default:
                return "?";
        }
    }

    @Override
    public String toString() {
        return "{ \"color\": \"" + color + "\", \"type\": \"" + type + "\"}"; 
    }

    // Method to check if the piece can move from (startRow, startCol) to (endRow, endCol)
    public boolean canMove(int startRow, int startCol, int endRow, int endCol, Piece[][] board) {
        switch (type) {
            case KING:
                return canMoveKing(startRow, startCol, endRow, endCol);
            case QUEEN:
                return canMoveQueen(startRow, startCol, endRow, endCol, board);
            case ROOK:
                return canMoveRook(startRow, startCol, endRow, endCol, board);
            case BISHOP:
                return canMoveBishop(startRow, startCol, endRow, endCol, board);
            case KNIGHT:
                return canMoveKnight(startRow, startCol, endRow, endCol);
            case PAWN:
                return canMovePawn(startRow, startCol, endRow, endCol, board);
            default:
                return false;
        }
    }

    // King moves one square in any direction
    public boolean canMoveKing(int startRow, int startCol, int endRow, int endCol) {
        return Math.abs(startRow - endRow) <= 1 && Math.abs(startCol - endCol) <= 1;
    }

    // Queen moves like both a rook and a bishop
    public boolean canMoveQueen(int startRow, int startCol, int endRow, int endCol, Piece[][] board) {
        return canMoveRook(startRow, startCol, endRow, endCol, board) || canMoveBishop(startRow, startCol, endRow, endCol, board);
    }

    // Rook moves in straight lines (either row or column must be the same)
    public boolean canMoveRook(int startRow, int startCol, int endRow, int endCol, Piece[][] board) {
        if (startRow != endRow && startCol != endCol) return false;
        // Check if path is clear (no pieces between start and end)
        if (startRow == endRow) {  // Horizontal movement
            int minCol = Math.min(startCol, endCol);
            int maxCol = Math.max(startCol, endCol);
            for (int col = minCol + 1; col < maxCol; col++) {
                if (board[startRow][col] != null) return false;
            }
        } else {  // Vertical movement
            int minRow = Math.min(startRow, endRow);
            int maxRow = Math.max(startRow, endRow);
            for (int row = minRow + 1; row < maxRow; row++) {
                if (board[row][startCol] != null) return false;
            }
        }
        return true;
    }

    // Bishop moves diagonally
    public boolean canMoveBishop(int startRow, int startCol, int endRow, int endCol, Piece[][] board) {
        if (Math.abs(startRow - endRow) == 0 || Math.abs(startCol - endCol) == 0) return false;
        if (Math.abs(startRow - endRow) != Math.abs(startCol - endCol)) return false;
        // Check if path is clear
        int rowDirection = (endRow > startRow) ? 1 : -1;
        int colDirection = (endCol > startCol) ? 1 : -1;
        int row = startRow + rowDirection, col = startCol + colDirection;
        while (row != endRow && col != endCol) {
            if (board[row][col] != null) return false;
            row += rowDirection;
            col += colDirection;
        }
        return true;
    }

    // Knight moves in an "L" shape
    public boolean canMoveKnight(int startRow, int startCol, int endRow, int endCol) {
        int rowDiff = Math.abs(startRow - endRow);
        int colDiff = Math.abs(startCol - endCol);
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    public boolean canMovePawn(int startRow, int startCol, int endRow, int endCol, Piece[][] board) {
        int direction = (color == Color.WHITE) ? 1 : -1;  // White pawns move up (1), Black pawns move down (-1)
    
        if (endRow < 0 || endRow >= board.length) {
            // System.out.println("Invalid move: target row is out of bounds.");
            return false;
        }
       
        // Moving forward (not capturing)
        if (startCol == endCol) {
            if (board[endRow][endCol] == null) {  // Only move forward if the target square is empty
                // One square forward move
                if (startRow + direction == endRow) {
                    // System.out.println("One-square move is valid.");
                    return true;
                }
                // Two squares forward move
                else if ((startRow == 1 && color == Color.WHITE) || (startRow == 6 && color == Color.BLACK)) {
                    int intermediateRow = startRow + direction;
                    if (startRow + 2 * direction == endRow && board[intermediateRow][startCol] == null) {
                        // System.out.println("Two-square move is valid.");
                        return true;
                     } 
                     //else {
                    //     System.out.println("Invalid two-square move: end row is not correct.");
                    // }
                } 
                // else {
                //     System.out.println("Invalid two-square move: path is blocked or out of bounds.");
                // }
             } 
        }
        
        // Capturing diagonally
        else if (Math.abs(startCol - endCol) == 1 && startRow + direction == endRow) {
            if (board[endRow][endCol] != null && board[endRow][endCol].getColor() != this.color) {
                // System.out.println("Valid Capture");
                return true;
            } 
            // En passant capture
        if (board1 != null) {
            Move lastMove =  board1.getLastMove();
            if (board[endRow][endCol] == null && lastMove != null) {
                int lastMoveStartRow = lastMove.getFromRow();
                int lastMoveStartCol = lastMove.getFromCol();
                int lastMoveEndRow = lastMove.getToRow();
                int lastMoveEndCol = lastMove.getToCol();
                if (board[lastMoveEndRow][lastMoveEndCol] != null && board[lastMoveEndRow][lastMoveEndCol].getType() == PieceType.PAWN) {
                    if (Math.abs(lastMoveStartRow - lastMoveEndRow) == 2 && lastMoveEndRow == startRow && lastMoveEndCol == endCol) {
                        return true;
                    }
                }
            } else {
                // System.out.println("No valid capture.");
            }
            }
        }
        return false;
    }

public List<Move> generatePotentialMoves(int row, int col, Piece[][] board) {
    List<Move> potentialMoves = new ArrayList<>();
    
    switch (this.type) {
        case KING:
            generateKingMoves(row, col, board, potentialMoves);
            break;
        case QUEEN:
            generateQueenMoves(row, col, board, potentialMoves);
            break;
        case ROOK:
            generateRookMoves(row, col, board, potentialMoves);
            break;
        case BISHOP:
            generateBishopMoves(row, col, board, potentialMoves);
            break;
        case KNIGHT:
            generateKnightMoves(row, col, board, potentialMoves);
            break;
        case PAWN:
            generatePawnMoves(row, col, board, potentialMoves);
            break;
    }
    
    return potentialMoves;
}
private void generateKingMoves(int row, int col, Piece[][] board, List<Move> potentialMoves) {
    int[] rowOffsets = {-1, -1, -1, 0, 0, 1, 1, 1};
    int[] colOffsets = {-1, 0, 1, -1, 1, -1, 0, 1};
    
    for (int i = 0; i < rowOffsets.length; i++) {
        int newRow = row + rowOffsets[i];
        int newCol = col + colOffsets[i];
        if (isWithinBounds(newRow, newCol)) {
            if (board[newRow][newCol] == null || board[newRow][newCol].getColor() != this.color) {
                potentialMoves.add(new Move(row, col, newRow, newCol, this));
            }
        }
    }
}

private void generateQueenMoves(int row, int col, Piece[][] board, List<Move> potentialMoves) {
    generateRookMoves(row, col, board, potentialMoves);
    generateBishopMoves(row, col, board, potentialMoves);
}

private void generateRookMoves(int row, int col, Piece[][] board, List<Move> potentialMoves) {
    int[] rowOffsets = {0, 0, 1, -1};
    int[] colOffsets = {1, -1, 0, 0};
    
    for (int i = 0; i < rowOffsets.length; i++) {
        int newRow = row;
        int newCol = col;
        
        while (true) {
            newRow += rowOffsets[i];
            newCol += colOffsets[i];
            if (!isWithinBounds(newRow, newCol)) break;
            
            if (board[newRow][newCol] == null) {
                potentialMoves.add(new Move(row, col, newRow, newCol, this));
            } else {
                if (board[newRow][newCol].getColor() != this.color) {
                    potentialMoves.add(new Move(row, col, newRow, newCol, this));
                }
                break;
            }
        }
    }
}

private void generateBishopMoves(int row, int col, Piece[][] board, List<Move> potentialMoves) {
    int[] rowOffsets = {1, 1, -1, -1};
    int[] colOffsets = {1, -1, 1, -1};
    
    for (int i = 0; i < rowOffsets.length; i++) {
        int newRow = row;
        int newCol = col;
        
        while (true) {
            newRow += rowOffsets[i];
            newCol += colOffsets[i];
            if (!isWithinBounds(newRow, newCol)) break;
            
            if (board[newRow][newCol] == null) {
                potentialMoves.add(new Move(row, col, newRow, newCol, this));
            } else {
                if (board[newRow][newCol].getColor() != this.color) {
                    potentialMoves.add(new Move(row, col, newRow, newCol, this));
                }
                break;
            }
        }
    }
}

private void generateKnightMoves(int row, int col, Piece[][] board, List<Move> potentialMoves) {
    int[] rowOffsets = {-2, -2, -1, -1, 1, 1, 2, 2};
    int[] colOffsets = {-1, 1, -2, 2, -2, 2, -1, 1};
    
    for (int i = 0; i < rowOffsets.length; i++) {
        int newRow = row + rowOffsets[i];
        int newCol = col + colOffsets[i];
        if (isWithinBounds(newRow, newCol)) {
            if (board[newRow][newCol] == null || board[newRow][newCol].getColor() != this.color) {
                potentialMoves.add(new Move(row, col, newRow, newCol, this));
            }
        }
    }
}

private void generatePawnMoves(int row, int col, Piece[][] board, List<Move> potentialMoves) {
    int direction = (color == Color.WHITE) ? 1 : -1;
    
    // Move forward
    int forwardRow = row + direction;
    if (isWithinBounds(forwardRow, col) && board[forwardRow][col] == null) {
        potentialMoves.add(new Move(row, col, forwardRow, col, this));
        // Two squares forward (only from the starting position)
        if ((row == 1 && color == Color.WHITE) || (row == 6 && color == Color.BLACK)) {
            int twoForwardRow = row + 2 * direction;
            if (board[twoForwardRow][col] == null) {
                potentialMoves.add(new Move(row, col, twoForwardRow, col, this));
            }
        }
    }
    
    // Capture diagonally
    for (int offset : new int[]{-1, 1}) {
        int captureCol = col + offset;
        if (isWithinBounds(forwardRow, captureCol) && board[forwardRow][captureCol] != null 
            && board[forwardRow][captureCol].getColor() != this.color) {
            potentialMoves.add(new Move(row, col, forwardRow, captureCol, this));
        }
    }
    
    // En passant can be handled separately if necessary
}

private boolean isWithinBounds(int row, int col) {
    return row >= 0 && row < 8 && col >= 0 && col < 8;
}

}