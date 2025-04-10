package com.giveawaychess;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.giveawaychess.Piece.Color;
import com.giveawaychess.Piece.PieceType;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;


// Define the ChessBoard class
public class ChessBoard {
    private Piece[][] board = new Piece[8][8];
    private boolean gameOver = false;
    private AntichessUI ui; // Reference to the UI
    private BotLogic bot;
    private GameManager gameManager;

    Player blackPlayer;
    Player whitePlayer;

    Move lastMove;
    private boolean captureLeft;


    // Constructor that allows usage with or without UI
    public ChessBoard(AntichessUI ui, GameManager gameManager) {
        this.ui = ui;
        this.gameManager = gameManager;
        setUpPieces();
    }
    
    public ChessBoard() {
        this(null, null); // Calls the other constructor with no UI
    }

    // Method to set up the pieces
    public void setUpPieces() {
        // Set up white pieces
        board[0][0] = createPiece(Piece.PieceType.ROOK, Piece.Color.WHITE);
        board[0][1] = createPiece(Piece.PieceType.KNIGHT, Piece.Color.WHITE);
        board[0][2] = createPiece(Piece.PieceType.BISHOP, Piece.Color.WHITE);
        board[0][3] = createPiece(Piece.PieceType.QUEEN, Piece.Color.WHITE);
        board[0][4] = createPiece(Piece.PieceType.KING, Piece.Color.WHITE);
        board[0][5] = createPiece(Piece.PieceType.BISHOP, Piece.Color.WHITE);
        board[0][6] = createPiece(Piece.PieceType.KNIGHT, Piece.Color.WHITE);
        board[0][7] = createPiece(Piece.PieceType.ROOK, Piece.Color.WHITE);

        // Set up white pawns
        for (int i = 0; i < 8; i++) {
            board[1][i] = createPiece(Piece.PieceType.PAWN, Piece.Color.WHITE);
        }

        // Set up black pieces
        board[7][0] = createPiece(Piece.PieceType.ROOK, Piece.Color.BLACK);
        board[7][1] = createPiece(Piece.PieceType.KNIGHT, Piece.Color.BLACK);
        board[7][2] = createPiece(Piece.PieceType.BISHOP, Piece.Color.BLACK);
        board[7][3] = createPiece(Piece.PieceType.QUEEN, Piece.Color.BLACK);
        board[7][4] = createPiece(Piece.PieceType.KING, Piece.Color.BLACK);
        board[7][5] = createPiece(Piece.PieceType.BISHOP, Piece.Color.BLACK);
        board[7][6] = createPiece(Piece.PieceType.KNIGHT, Piece.Color.BLACK);
        board[7][7] = createPiece(Piece.PieceType.ROOK, Piece.Color.BLACK);

        // Set up black pawns
        for (int i = 0; i < 8; i++) {
            board[6][i] = createPiece(Piece.PieceType.PAWN, Piece.Color.BLACK);
        }
    }

    public Piece[][] getBoard() {
        return board;
    }

    public List<int[]> getValidMoves(int row, int col) {
        Piece piece = board[row][col];
        if (piece == null) return new ArrayList<>();
    
        List<int[]> moves = new ArrayList<>();
        List<Move> potentialMoves = piece.generatePotentialMoves(row, col, board);
    
        for (Move move : potentialMoves) {
            if (isValidMove(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol())) {
                moves.add(new int[]{move.getToRow(), move.getToCol()});
            }
        }
    
        return moves;
    }    

    public boolean canCaptureEnPassant(int row, int col) {
        Move lastMove = getLastMove();
        if (lastMove == null || !lastMove.isPawnMove()) {
            // System.out.println("lastMove is null or isn't a pawn move");
            return false;
        }
    
        Piece movedPawn = board[lastMove.endRow][lastMove.endCol];
        if (movedPawn == null) {
            // System.out.println("movedPawn is null");
            return false; // Avoid NullPointerException
        }
    
        if (movedPawn.getColor() != gameManager.getCurrentPlayer().getColor() && Math.abs(lastMove.startRow - lastMove.endRow) == 2) {
            // System.out.println("Passes First Test");
            if (lastMove.endRow == row) {
                if (lastMove.endCol == col + 1 || lastMove.endCol == col - 1) {
                    if (lastMove.endCol == col - 1) {
                        captureLeft = true;
                    } else {
                        captureLeft = false;
                    }
                    return true; // En passant is allowed
                }
                
            }
        }
    
        return false;
    }
    

    public boolean isGameOver() {
        return gameOver;
    }

    private boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < board.length && col >= 0 && col < board[0].length;
    }
    

    // public boolean isValidMove(int startRow, int startCol, int endRow, int endCol, GameManager gameManager) {
    //     Piece piece = board[startRow][startCol];
    //     if (piece != null && piece.getType() == Piece.PieceType.PAWN) {
    //         return piece.canMovePawn(startRow, startCol, endRow, endCol, board, gameManager.isNewbieMode());
    //     }
    //     return piece != null && piece.canMove(startRow, startCol, endRow, endCol, board);
    // }

    public boolean isValidMove(int startRow, int startCol, int endRow, int endCol) {
        Piece piece = board[startRow][startCol];
    
        if (piece == null || !isWithinBounds(startRow, startCol) || !isWithinBounds(endRow, endCol)) {
            return false;
        }
    
        boolean hasCapture = hasMandatoryCapture(gameManager.getCurrentPlayer().getColor(), board);
    
        // Directly use piece's movement logic
        if (!piece.canMove(startRow, startCol, endRow, endCol, board)) {
            return false;
        }
    
        // If capturing is mandatory, enforce it
        return !hasCapture || isCaptureMove(startRow, startCol, endRow, endCol);
    }
    

    public boolean hasMandatoryCapture(Piece.Color currentPlayerColor, Piece[][] board) { 
        // System.out.println("Checking mandatory captures for " + currentPlayerColor);
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == currentPlayerColor) {
                    if (canCapture(piece, row, col, board)) {
                        // System.out.println("Capture possible for " + piece.getType() + " at (" + row + "," + col + ")");
                        return true;  // If any capture is possible, return true
                    }
                }
            }
        }
        return false;  // No captures found
    }

    private boolean canCapture(Piece piece, int startRow, int startCol, Piece[][] board) {
        // Check all possible moves for this piece and see if any involve capturing
        for (int endRow = 0; endRow < board.length; endRow++) {
            for (int endCol = 0; endCol < board[endRow].length; endCol++) {
                // The piece can only capture if there is an opponent's piece at the target location    
                    if (board[endRow][endCol] != null && board[endRow][endCol].getColor() != piece.getColor()) {
                        if (piece.canMove(startRow, startCol, endRow, endCol, board)) {
                            // System.out.println("Can move from " + startRow + "," + startCol + " to capture " + endRow + "," + endCol);
                        // System.out.println("Capture available for piece at (" + startRow + ", " + startCol + ")");
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public boolean isCaptureMove(int startRow, int startCol, int endRow, int endCol) {
        // if (!isWithinBounds(startRow, startCol) || !isWithinBounds(endRow, endCol)) return false;
        Piece startPiece = board[startRow][startCol];
        Piece endPiece = board[endRow][endCol];
    
        // Must be a legal move to count as a capture
        if (startPiece == null || !startPiece.canMove(startRow, startCol, endRow, endCol, board)) {
            return false;
        }
    
        return (endPiece != null && startPiece.getColor() != endPiece.getColor())
               || isEnPassantMove(startRow, startCol, endRow, endCol);
    }

    private void checkPawnPromotion(int endRow, int endCol, boolean randomPromotion) {
        Piece piece = board[endRow][endCol];
        if (piece.getType() == Piece.PieceType.PAWN && 
            ((piece.getColor() == Piece.Color.WHITE && endRow == 7) || 
            (piece.getColor() == Piece.Color.BLACK && endRow == 0))) {
    
            if (randomPromotion) {
                makeRandomPromotionMove(endRow, endCol, piece.getColor());
            } else {
                promotePawn(endRow, endCol, piece.getColor());
            }
        }
    }
    
    
    private void promotePawn(int row, int col, Piece.Color color) {
        Piece newPiece;
    // Display promotion options to the user
    String[] options = {"Queen", "Rook", "Bishop", "Knight", "King"};
    String choice = (String) JOptionPane.showInputDialog(
        null,
        "Choose piece for promotion:",
        "Pawn Promotion",
        JOptionPane.PLAIN_MESSAGE,
        null,
        options,
        options[0]
    );

    if (choice != null) {
        switch (choice) {
            case "Queen":
                newPiece = createPiece(Piece.PieceType.QUEEN, (color));
                break;
            case "Rook":
                newPiece = createPiece(Piece.PieceType.ROOK,(color));
                break;
            case "Bishop":
                newPiece = createPiece(Piece.PieceType.BISHOP, (color));
                break;
            case "Knight":
                newPiece = createPiece(Piece.PieceType.KNIGHT, (color));
                break;
            case "King":
                newPiece = createPiece(Piece.PieceType.KING, (color));
                break;
            default:
                newPiece = createPiece(Piece.PieceType.QUEEN, (color)); // Default to Queen if no valid choice
                break;
        }
    } else {
        newPiece = createPiece(Piece.PieceType.QUEEN, (color)); // Default to Queen if no choice is made
    }
        board[row][col] = newPiece;
    }

    private void makeRandomPromotionMove(int row, int col, Piece.Color color) {
        Piece newPiece;
        Piece[] promotionOptions = {createPiece(Piece.PieceType.QUEEN, (color)), createPiece(Piece.PieceType.ROOK,(color)), createPiece(Piece.PieceType.BISHOP,(color)), createPiece(Piece.PieceType.KNIGHT,(color)), createPiece(Piece.PieceType.KING,(color))};
        Random random = new Random();
        newPiece = promotionOptions[random.nextInt(promotionOptions.length)];
        board[row][col] = newPiece;
    }
    

    public boolean hasValidMove(Piece.Color playerColor) {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == playerColor) {
                    for (int endRow = 0; endRow < board.length; endRow++) {
                        for (int endCol = 0; endCol < board[endRow].length; endCol++) {
                            if (isValidMove(row, col, endRow, endCol)) {
                                return true;  // Player has at least one valid move
                            }
                        }
                    }
                }
            }
        }
        return false;  // No valid moves found
    }

    public boolean hasPieces(Piece.Color playerColor) {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == playerColor) {
                    return true;  // The player still has pieces left
                }
            }
        }
        return false;  // No pieces left for the player
    }

    public void checkGameEnd(boolean isSimulation) {
        if (ui != null) {
            blackPlayer = ui.blackPlayer;
            whitePlayer = ui.whitePlayer;
        }

        if (!hasValidMove(gameManager.getCurrentPlayer().getColor())) {
            Player winner = (gameManager.getCurrentPlayer().getColor() == Piece.Color.WHITE) ? blackPlayer : whitePlayer;
            System.out.println("Game over: No valid moves left for " + gameManager.getCurrentPlayer().getColor());
            if (!isSimulation && ui != null) {
                ui.gameWon(winner, false);
            }
            return;
        }
    
        if (!hasPieces(gameManager.getCurrentPlayer().getColor())) {
            Player winner = (gameManager.getCurrentPlayer().getColor() == Piece.Color.WHITE) ? blackPlayer : whitePlayer;
            System.out.println("Game over: No pieces left for " + gameManager.getCurrentPlayer().getColor());
            if (!isSimulation && ui != null) {
                ui.gameWon(winner, false);
            }
            return;
        }
    }

    public void printTurn() {
        System.out.println("It's " + ((gameManager.getCurrentPlayer().getColor() == Piece.Color.WHITE) ? "White" : "Black") + "'s turn.");
    }

    public Piece.Color getCurrentPlayer() {
        return gameManager.getCurrentPlayer().getColor();
    }
    
    public void startGame() {
        gameOver = false;
        if (ui != null) {
            ui.updateBoard(board);
        }
    }

    public boolean handleMove(Move move, GameManager gameManager, boolean isSimulation) {
        int startRow = move.getFromRow();
        int startCol = move.getFromCol();
        int endRow = move.getToRow();
        int endCol = move.getToCol();
        Piece piece = board[startRow][startCol];
    
        if (gameOver) {
            System.out.println("Game is over. No more moves allowed.");
            return false;
        }
    
        if (piece != null && piece.getColor() == gameManager.getCurrentPlayer().getColor() && isValidMove(startRow, startCol, endRow, endCol)) {
            Piece captured = board[endRow][endCol];
            move.setCapturedPiece(captured);

    
            if (isEnPassantMove(startRow, startCol, endRow, endCol)) {
                move.setWasEnPassant(true);
            }
            
            board[endRow][endCol] = piece;
            board[startRow][startCol] = null;
            

            recordMove(startRow, startCol, endRow, endCol, piece);
            lastMove = move;
    
            if (gameManager.getCurrentPlayer().isBot()) {
                checkPawnPromotion(endRow, endCol, true);
            } else {
                checkPawnPromotion(endRow, endCol, false);
            }
    
            gameManager.switchTurn();

            if (!isSimulation) {
                checkGameEnd(false);
            }
    
            if (!isSimulation && ui != null) {
                SwingUtilities.invokeLater(() -> ui.updateBoard(board));
            }
    
            return true;
        }
    
        return false;
    }

    // Print the board
    public void printBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board[row][col] == null) {
                    System.out.print("[    ] ");
                } else {
                    System.out.print("[" + board[row][col].getType().toString().charAt(0) + " " + board[row][col].getColor().toString().charAt(0) + "] ");
                }
            }
            System.out.println();
        }
    }


private List<Move> moveHistory = new ArrayList<>();

    // Method to return the last move made
    public Move getLastMove() {
        if (moveHistory.isEmpty()) {
            return null; // No moves have been made yet
        }
        return moveHistory.get(moveHistory.size() - 1);
    }

    public void undoMove(Move lastMove) {
        if (lastMove == null) return;
    
        // Get the move details
        int startRow = lastMove.getFromRow();
        int startCol = lastMove.getFromCol();
        int endRow = lastMove.getToRow();
        int endCol = lastMove.getToCol();
        Piece movedPiece = lastMove.getMovedPiece();
        Piece capturedPiece = lastMove.getCapturedPiece(); // Store what was captured
    
        // Move the piece back to its original position
        board[startRow][startCol] = movedPiece;
        board[endRow][endCol] = capturedPiece; // Restore captured piece (if any)
    
        // If the moved piece was a pawn that had been promoted, revert it back to a pawn
        if (lastMove.wasPromotion()) {
            board[startRow][startCol] = createPiece(PieceType.PAWN, movedPiece.getColor());
        }
    
        // Restore turn
        if (gameManager.getCurrentPlayer().getColor() == Piece.Color.WHITE) {
            gameManager.setCurrentPlayer(Piece.Color.BLACK);
        } else {
            gameManager.setCurrentPlayer(Piece.Color.WHITE);
        }
    }
    

    // Add this method to record a move after it's successfully made
    private void recordMove(int startRow, int startCol, int endRow, int endCol, Piece piece) {
    Piece capturedPiece = board[endRow][endCol]; // Get the captured piece (if any)
    
    // Check if this move results in a pawn promotion
    boolean promotion = (piece.getType() == Piece.PieceType.PAWN && 
                         ((piece.getColor() == Piece.Color.WHITE && endRow == 7) || 
                          (piece.getColor() == Piece.Color.BLACK && endRow == 0)));

        Move move = new Move(startRow, startCol, endRow, endCol, piece);
        moveHistory.add(move);
    }

    public Piece getPieceAt(int row, int col) {
        if (row < 0 || row >= 8 || col < 0 || col >= 8) {
            throw new IllegalArgumentException("Row and column must be between 0 and 7.");
        }
        return board[row][col]; // Return the piece at the specified row and column
    }


    
    /**
     * Sets up a custom position on the chessboard.
     * @param customPositions A list of pieces and their positions.
     */
    public void setupCustomPosition() {
        // Clear the current board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = null;
            }
        }

        // Set up white pawns
        for (int i = 0; i < 1; i++) {
            board[6][i] = createPiece(Piece.PieceType.PAWN, Piece.Color.WHITE);
        }

        // Set up black pawns
        for (int i = 0; i < 1; i++) {
            board[1][i] = createPiece(Piece.PieceType.PAWN, Piece.Color.BLACK);
        }
        printBoard();
    }

    public Piece[][] getBoardArray() {
        return board;
    }

    public boolean isPieceHanging(int row, int col) {
        Piece piece = getPieceAt(row, col);
        if (piece == null) return false; // No piece to check
    
        Piece.Color opponentColor = (piece.getColor() == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
        List<Move> opponentMoves = getAllValidMoves(opponentColor);
    
        for (Move move : opponentMoves) {
            if (move.getToRow() == row && move.getToCol() == col) {
                return true; // Opponent can capture this piece
            }
        }
        return false;
    }

    private List<Move> getAllValidMoves(Piece.Color opponentColor) {
        // System.out.println("getAllValidMovesCalled");
        List<Move> allValidMoves = new ArrayList<>();
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = getPieceAt(row, col);
                
                if (piece != null && piece.getColor() == opponentColor) {
                    List<int[]> validMoves = getValidMoves(row, col);
    
                    for (int[] move : validMoves) {
                        int endRow = move[0];
                        int endCol = move[1];
                        allValidMoves.add(new Move(row, col, endRow, endCol, piece)); 
                    }
                }
            }
        }
        
        return allValidMoves;
    }

    public int countPieces(Piece.Color playerColor) {
        int count = 0;
    
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = getPieceAt(row, col);
                if (piece != null && piece.getColor() == playerColor) {
                    count++;
                }
            }
        }
        return count;
    }

    public void restoreBoardState(Piece[][] storedBoard, Piece.Color storedPlayer, GameManager gameManager, boolean wasGameOver) {
        this.board = deepCopyBoard(storedBoard);
        this.gameOver = wasGameOver;
    
        if (gameOver) {
            if (ui != null) {
                ui.gameWon((storedPlayer == Piece.Color.WHITE) ? ui.blackPlayer : ui.whitePlayer, false);
            }
        }
        gameManager.setCurrentPlayer(storedPlayer);
    }
    
    
    
    
    public Piece[][] deepCopyBoard(Piece[][] original) {
        Piece[][] copy = new Piece[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (original[row][col] != null) {
                    Piece cloned = new Piece(original[row][col].getType(), original[row][col].getColor());
                    cloned.setChessBoard(this); // 👈 Ensure proper context
                    copy[row][col] = cloned;
                }
            }
        }
        return copy;
    }

    public boolean isEnPassantMove(int startRow, int startCol, int endRow, int endCol) {
         // Ensure move is diagonal
    if (Math.abs(startCol - endCol) != 1) return false;

    // Check if en passant is allowed at this square
    return canCaptureEnPassant(startRow, startCol) &&
           endRow == getLastMove().getToRow() + (board[startRow][startCol].getColor() == Piece.Color.WHITE ? 1 : -1);
    }

    public Piece.Color getWinner() {
        if (!isGameOver()) return null;
    
        Piece.Color currentColor = getCurrentPlayer();
    
        boolean noPieces = !hasPieces(currentColor);
        boolean noMoves = !hasValidMove(currentColor);
    
        if (noPieces || noMoves) {
            return (currentColor == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
        }
    
        return null; // Shouldn't happen, but fallback
    }
    
    public GameManager getGameManager() {
        return this.gameManager;
    }

    private Piece createPiece(Piece.PieceType type, Piece.Color color) {
        Piece piece = new Piece(type, color);
        piece.setChessBoard(this);
        return piece;
    }
        
}