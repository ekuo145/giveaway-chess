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
    private static Piece.Color currentPlayer = Piece.Color.WHITE;
    private boolean gameOver = false;
    private AntichessUI ui; // Reference to the UI
    private BotLogic bot;


    Player blackPlayer;
    Player whitePlayer;

    Move lastMove;
    private boolean captureLeft;

    // Constructor initializes the board with pieces
    public ChessBoard(AntichessUI ui) {
        this.ui = ui;
        setUpPieces();
        ui.updateBoard(board);
    }

    // Method to set up the pieces
    public void setUpPieces() {
        // Set up white pieces
        board[0][0] = new Piece(Piece.PieceType.ROOK, Piece.Color.WHITE);
        board[0][1] = new Piece(Piece.PieceType.KNIGHT, Piece.Color.WHITE);
        board[0][2] = new Piece(Piece.PieceType.BISHOP, Piece.Color.WHITE);
        board[0][3] = new Piece(Piece.PieceType.QUEEN, Piece.Color.WHITE);
        board[0][4] = new Piece(Piece.PieceType.KING, Piece.Color.WHITE);
        board[0][5] = new Piece(Piece.PieceType.BISHOP, Piece.Color.WHITE);
        board[0][6] = new Piece(Piece.PieceType.KNIGHT, Piece.Color.WHITE);
        board[0][7] = new Piece(Piece.PieceType.ROOK, Piece.Color.WHITE);

        // Set up white pawns
        for (int i = 0; i < 8; i++) {
            board[1][i] = new Piece(Piece.PieceType.PAWN, Piece.Color.WHITE);
        }

        // Set up black pieces
        board[7][0] = new Piece(Piece.PieceType.ROOK, Piece.Color.BLACK);
        board[7][1] = new Piece(Piece.PieceType.KNIGHT, Piece.Color.BLACK);
        board[7][2] = new Piece(Piece.PieceType.BISHOP, Piece.Color.BLACK);
        board[7][3] = new Piece(Piece.PieceType.QUEEN, Piece.Color.BLACK);
        board[7][4] = new Piece(Piece.PieceType.KING, Piece.Color.BLACK);
        board[7][5] = new Piece(Piece.PieceType.BISHOP, Piece.Color.BLACK);
        board[7][6] = new Piece(Piece.PieceType.KNIGHT, Piece.Color.BLACK);
        board[7][7] = new Piece(Piece.PieceType.ROOK, Piece.Color.BLACK);

        // Set up black pawns
        for (int i = 0; i < 8; i++) {
            board[6][i] = new Piece(Piece.PieceType.PAWN, Piece.Color.BLACK);
        }
    }

    public Piece[][] getBoard() {
        return board;
    }

    public List<int[]> getValidMoves(int row, int col) {
        List<int[]> validMoves = new ArrayList<>();
        Piece piece = board[row][col];
        
        if (piece == null) {
            return validMoves; // No piece at this location
        }

        switch (piece.getType()) {
            case PAWN:
                validMoves = getPawnMoves(row, col, piece);
                break;
            case ROOK:
                validMoves = getRookMoves(row, col, piece);
                break;
            case KNIGHT:
                validMoves = getKnightMoves(row, col, piece);
                break;
            case BISHOP:
                validMoves = getBishopMoves(row, col, piece);
                break;
            case QUEEN:
                validMoves = getQueenMoves(row, col, piece);
                break;
            case KING:
                validMoves = getKingMoves(row, col, piece);
                break;
        }
        return validMoves;
    }

    private List<int[]> getPawnMoves(int row, int col, Piece piece) {
        List<int[]> moves = new ArrayList<>();
        int direction = piece.getColor() == Piece.Color.WHITE ? 1 : -1; // White pawns move up, black down

        // Normal move forward (one square)
        if (isValidMove(row, col, row + direction, col)) {
            moves.add(new int[]{row + direction, col});
        }

        // First move, two squares forward
        if ((piece.getColor() == Piece.Color.WHITE && row == 1) || (piece.getColor() == Piece.Color.BLACK && row == 6)) {
        // Pawns can move two squares if they are in their starting position
            if (isValidMove(row, col, row + 2 * direction, col)) {
            moves.add(new int[]{row + 2 * direction, col});
            }
        }

        // Capture diagonally
        if (isValidMove(row, col, row + direction, col - 1)) {
            moves.add(new int[]{row + direction, col - 1});
        }
        if (isValidMove(row, col, row + direction, col + 1)) {
            moves.add(new int[]{row + direction, col + 1});
        }

        // En passant capture (check if the previous move allows for en passant)
        if (canCaptureEnPassant(row, col)) {
            if (captureLeft) {
                moves.add(new int[]{row + direction, col - 1});//Add en passant move (to left)
            } else {
                moves.add(new int[]{row + direction, col + 1}); //Add en passant move (to right)
            }
        
        }

        // Add more complex pawn logic here (e.g., promotion, en passant, double move on first turn)

        return moves;
    }

    public boolean canCaptureEnPassant(int row, int col) {
        Move lastMove = getLastMove();
        if (lastMove == null || !lastMove.isPawnMove()) {
            System.out.println("lastMove is null or isn't a pawn move");
            return false;
        }
    
        Piece movedPawn = board[lastMove.endRow][lastMove.endCol];
        if (movedPawn == null) {
            System.out.println("movedPawn is null");
            return false; // Avoid NullPointerException
        }
    
        if (movedPawn.getColor() != currentPlayer && Math.abs(lastMove.startRow - lastMove.endRow) == 2) {
            System.out.println("Passes First Test");
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
    

    private List<int[]> getRookMoves(int row, int col, Piece piece) {
        List<int[]> moves = new ArrayList<>();
        
        // Directions: up, down, left, right
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    
        for (int[] direction : directions) {
            int newRow = row;
            int newCol = col;
            
            while (true) {
                newRow += direction[0];
                newCol += direction[1];
    
                if (!isWithinBounds(newRow, newCol)) {
                    break; // Out of bounds
                }
    
                // If the move is valid (not blocked by a piece of the same color)
                if (isValidMove(row, col, newRow, newCol)) {
                    moves.add(new int[]{newRow, newCol});
                    // Stop if we encounter an opponent's piece (we can capture it)
                    if (board[newRow][newCol] != null) {
                        break;
                    }
                }
            }
        }
    
        return moves;
    }
    

    private List<int[]> getKnightMoves(int row, int col, Piece piece) {
        List<int[]> moves = new ArrayList<>();
        
        // The possible knight move offsets
        int[][] knightMoves = {
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1}, // Vertical "L" moves
            {1, 2}, {1, -2}, {-1, 2}, {-1, -2}  // Horizontal "L" moves
        };
        
        // Iterate over all possible knight moves
        for (int[] move : knightMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
    
            // Check if the move is within the board bounds and is valid
            if (isWithinBounds(newRow, newCol) && isValidMove(row, col, newRow, newCol)) {
                moves.add(new int[]{newRow, newCol});
            }
        }
    
        return moves;
    }
    
    private List<int[]> getBishopMoves(int row, int col, Piece piece) {
        List<int[]> moves = new ArrayList<>();
        
        // Directions: top-left, top-right, bottom-left, bottom-right
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    
        for (int[] direction : directions) {
            int newRow = row;
            int newCol = col;
    
            while (true) {
                newRow += direction[0];
                newCol += direction[1];
    
                if (!isWithinBounds(newRow, newCol)) {
                    break; // Out of bounds
                }
    
                // If the move is valid (not blocked by a piece of the same color)
                if (isValidMove(row, col, newRow, newCol)) {
                    moves.add(new int[]{newRow, newCol});
                    // Stop if we encounter an opponent's piece (we can capture it)
                    if (board[newRow][newCol] != null) {
                        break;
                    }
                }
            }
        }
    
        return moves;
    }

    private List<int[]> getQueenMoves(int row, int col, Piece piece) {
        List<int[]> moves = new ArrayList<>();
    
        // Queen moves are a combination of rook and bishop moves
        moves.addAll(getRookMoves(row, col, piece)); // Add Rook's horizontal and vertical moves
        moves.addAll(getBishopMoves(row, col, piece)); // Add Bishop's diagonal moves
    
        return moves;
    }

    private List<int[]> getKingMoves(int row, int col, Piece piece) {
        List<int[]> moves = new ArrayList<>();
        
        // Directions: all 8 possible directions (vertical, horizontal, diagonal)
        int[][] kingMoves = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},  // Vertical and horizontal
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1} // Diagonal
        };
    
        for (int[] move : kingMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
    
            // Check if the move is within bounds and valid
            if (isWithinBounds(newRow, newCol) && isValidMove(row, col, newRow, newCol)) {
                moves.add(new int[]{newRow, newCol});
            }
        }
    
        return moves;
    }
    

    public boolean isGameOver() {
        return gameOver;
    }

    private boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < board.length && col >= 0 && col < board[0].length;
    }
    

    // Method to check if a move is valid based on the piece's movement rules
    public boolean isValidMove(int startRow, int startCol, int endRow, int endCol) {
        // Ensure the starting and ending positions are within the bounds of the board
        // Allow en passant capture
        if (isEnPassantMove(startRow, startCol, endRow, endCol)) {
            return true;
        }

        if (!isWithinBounds(startRow, startCol) || !isWithinBounds(endRow, endCol)) {
            // System.out.println("Move is out of bounds.");
            return false;
        }
    
        Piece piece = board[startRow][startCol];
        Piece capturedPiece = board[endRow][endCol];   

        if (piece != null && piece.canMove(startRow, startCol, endRow, endCol, board)) {
            boolean hasCapture = hasMandatoryCapture(currentPlayer, board);

            if (hasCapture && !isCaptureMove(startRow, startCol, endRow, endCol)) {
                // System.out.println("Capture is mandatory, but this move doesn't capture.");
                return false;
            }

            if (capturedPiece != null) {
                if (piece.getColor() == capturedPiece.getColor()) {
                    return false;
                }
            }
            return true;  // The move is valid
        }   

    return false;
    }

    
    

    public boolean hasMandatoryCapture(Piece.Color currentPlayerColor, Piece[][] board) { 
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == currentPlayerColor) {
                    if (canCapture(piece, row, col, board)) {
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
                        // System.out.println("Capture available for piece at (" + startRow + ", " + startCol + ")");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isCaptureMove(int startRow, int startCol, int endRow, int endCol) {
        Piece startPiece = board[startRow][startCol];
        Piece endPiece = board[endRow][endCol];
    
        // A capture move happens when the target square has an opponent's piece
        return endPiece != null && startPiece.getColor() != endPiece.getColor() || isEnPassantMove(startRow, startCol, endRow, endCol);
    }
    
    private void checkPawnPromotion(int endRow, int endCol) {
        Piece piece = board[endRow][endCol];
        if (piece instanceof Piece) {
            if (piece.getType() == Piece.PieceType.PAWN) {
            // System.out.println("Piece is a Pawn");
            // Check if the pawn has reached the last row (opposite side)
            if ((piece.getColor() == Piece.Color.WHITE && endRow == 7) || 
                (piece.getColor() == Piece.Color.BLACK && endRow == 0)) {
                    promotePawn(endRow, endCol, piece.getColor());
                }
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
                newPiece = new Piece(Piece.PieceType.QUEEN, (color));
                break;
            case "Rook":
                newPiece = new Piece(Piece.PieceType.ROOK,(color));
                break;
            case "Bishop":
                newPiece = new Piece(Piece.PieceType.BISHOP, (color));
                break;
            case "Knight":
                newPiece = new Piece(Piece.PieceType.KNIGHT, (color));
                break;
            case "King":
                newPiece = new Piece(Piece.PieceType.KING, (color));
                break;
            default:
                newPiece = new Piece(Piece.PieceType.QUEEN, (color)); // Default to Queen if no valid choice
                break;
        }
    } else {
        newPiece = new Piece(Piece.PieceType.QUEEN, (color)); // Default to Queen if no choice is made
    }
        board[row][col] = newPiece;
    }

    public void checkRandomPromotionMove(int endRow, int endCol, Piece.Color color) {
        Piece piece = board[endRow][endCol];
        if (piece instanceof Piece) {
            if (piece.getType() == Piece.PieceType.PAWN) {
            // System.out.println("Piece is a Pawn");
            // Check if the pawn has reached the last row (opposite side)
            if ((piece.getColor() == Piece.Color.WHITE && endRow == 7) || 
                (piece.getColor() == Piece.Color.BLACK && endRow == 0)) {
                    makeRandomPromotionMove(endRow, endCol, piece.getColor());
                }
            }
        }
    }

    private void makeRandomPromotionMove(int row, int col, Piece.Color color) {
        Piece newPiece;
        Piece[] promotionOptions = {new Piece(Piece.PieceType.QUEEN, (color)), new Piece(Piece.PieceType.ROOK,(color)), new Piece(Piece.PieceType.BISHOP,(color)), new Piece(Piece.PieceType.KNIGHT,(color)), new Piece(Piece.PieceType.KING,(color))};
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

    public void checkGameEnd() {
        // Check if the current player has any valid moves
        blackPlayer = ui.blackPlayer;
        whitePlayer = ui.whitePlayer;
        if (!hasValidMove(currentPlayer)) {
            System.out.println("Player " + (currentPlayer == Piece.Color.WHITE ? "White" : "Black") + " has no valid moves left!");
            System.out.println("Game over! " + (currentPlayer == Piece.Color.WHITE ? "White" : "Black") + " wins!");
            
            // Determine the winner (opposite of the current player)
            Player winner = (currentPlayer == Piece.Color.WHITE) ? blackPlayer : whitePlayer;
            ui.gameWon(winner);
            return;
        }
    
        // Check if the current player has any pieces left
        if (!hasPieces(currentPlayer)) {
            System.out.println("Player " + (currentPlayer == Piece.Color.WHITE ? "White" : "Black") + " has no pieces left!");
            System.out.println("Game over! " + (currentPlayer == Piece.Color.WHITE ? "White" : "Black") + " wins!");
            
            // Determine the winner (opposite of the current player)
            Player winner = (currentPlayer == Piece.Color.WHITE) ? blackPlayer : whitePlayer;
            ui.gameWon(winner);
            return;
        }
    }
    
    public void switchPlayer(GameManager gameManager) {
        currentPlayer = (currentPlayer == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
    
        // **Ensure gameManager updates correctly**
        if (gameManager.getCurrentPlayer().getColor() != currentPlayer) {
            gameManager.switchTurn();
        }
    }
    

    public static void printTurn() {
        System.out.println("It's " + (currentPlayer == Piece.Color.WHITE ? "White" : "Black") + "'s turn.");
    }

    
    
    public void startGame() {
        gameOver = false;
        ui.updateBoard(board);
    }
    
    public boolean handleMove(Move move, GameManager gameManager) {
        int startRow = move.getFromRow();
        int startCol = move.getFromCol();
        int endRow = move.getToRow();
        int endCol = move.getToCol();
        Piece piece = board[startRow][startCol];

        if (gameOver) {
            System.out.println("Game is over. No more moves allowed.");
            return false;
        }


        // Check if it's the current player's turn and if the move is valid
        if (piece != null && piece.getColor() == currentPlayer && isValidMove(startRow, startCol, endRow, endCol)) {
            
            board[endRow][endCol] = piece;  // Move the piece
            if (isEnPassantMove(startRow, startCol, endRow, endCol)) {
                board[endRow - 1][endCol] = null;
            }
            board[startRow][startCol] = null;  // Clear the original square

            // Record the move
            recordMove(startRow, startCol, endRow, endCol, piece);
            lastMove = move;

            // printBoard();
            if (gameManager.getCurrentPlayer().isBot()) {
                checkRandomPromotionMove(endRow, endCol, currentPlayer);;
            } else {
            checkPawnPromotion(endRow, endCol);
            }

            // Alternate between players
            gameManager.switchTurn();
            currentPlayer = gameManager.getCurrentPlayer().getColor();
            //System.out.println("Switching turn to " + gameManager.getCurrentPlayer().getColor().toString());
    
            // Check if the next player has valid moves or if the game should end
            checkGameEnd();

            // Update the UI after the move
            SwingUtilities.invokeLater(() -> ui.updateBoard(board));

            boolean hasCapture = hasMandatoryCapture(currentPlayer, board);
                if (hasCapture) {
            System.out.println("Next player has a mandatory capture.");
        }

        return true;
        }

        // System.out.println("Invalid move.");
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
            board[startRow][startCol] = new Piece(PieceType.PAWN, movedPiece.getColor());
        }
    
        // Restore turn
        currentPlayer = (currentPlayer == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
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
            board[6][i] = new Piece(Piece.PieceType.PAWN, Piece.Color.WHITE);
        }

        // Set up black pawns
        for (int i = 0; i < 1; i++) {
            board[1][i] = new Piece(Piece.PieceType.PAWN, Piece.Color.BLACK);
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

    public Piece.Color getCurrentPlayer() {
        return currentPlayer;
    }

    public void restoreBoardState(Piece[][] storedBoard, Piece.Color storedPlayer, GameManager gameManager) {
        this.board = deepCopyBoard(storedBoard);
        this.currentPlayer = storedPlayer;
    
        // **Ensure gameManager's current player is correct**
        if (gameManager.getCurrentPlayer().getColor() != storedPlayer) {
            gameManager.setCurrentPlayer(storedPlayer);
        }
    }
    
    
    public Piece[][] deepCopyBoard(Piece[][] original) {
        Piece[][] copy = new Piece[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (original[row][col] != null) {
                    copy[row][col] = new Piece(original[row][col].getType(), original[row][col].getColor());
                }
            }
        }
        return copy;
    }

    public boolean isEnPassantMove(int startRow, int startCol, int endRow, int endCol) {
        Move lastMove = getLastMove();
        if (lastMove == null || !lastMove.isPawnMove()) {
            return false;
        }
    
        Piece movedPawn = board[lastMove.getToRow()][lastMove.getToCol()];
        if (movedPawn == null || movedPawn.getType() != Piece.PieceType.PAWN) {
            return false;
        }
    
        if (movedPawn.getColor() != board[startRow][startCol].getColor().opposite() || Math.abs(lastMove.getFromRow() - lastMove.getToRow()) != 2) {
            return false;
        }
    
        if (lastMove.getToRow() == startRow && Math.abs(lastMove.getToCol() - startCol) == 1 && endRow == lastMove.getToRow() + (board[startRow][startCol].getColor() == Piece.Color.WHITE ? 1 : -1) && endCol == lastMove.getToCol()) {
            return true;
        }
    
        return false;
    }
    
}