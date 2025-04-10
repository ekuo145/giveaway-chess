package com.giveawaychess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BotLogic {
    private ChessBoard board;
    private GameManager gameManager;
    private BotProfile profile;

    public BotType botType;  // Declare botType variable

    public enum BotType {
        AGGRESSIVE, DEFENSIVE, RANDOM, SACRIFICIAL, HYBRID, SWEATY
    }

    // Constructor to initialize botType and board
    public BotLogic(ChessBoard board, GameManager gameManager, BotType botType) {
        this.board = board;
        this.gameManager = gameManager;

        this.botType = botType;
    }

    public BotLogic(ChessBoard board, GameManager gameManager, BotProfile profile) {
        this.board = board;
        this.gameManager = gameManager;
        this.profile = profile;

        this.botType = BotType.RANDOM;
    }

    public Move getMove() {
        // System.out.println("Turn: " + gameManager.getTurnNumber() + " | Color: " + gameManager.getCurrentPlayer().getColor());

        List<Move> possibleMoves = getAllValidMoves(getBotColor());
        if (possibleMoves.isEmpty()) {
            System.out.println("BotLogic: No valid moves found.");
            return null;
        }

        if (profile != null && "Randomizer".equals(profile.wildCard)) {
            return getRandomMove(board, getBotColor());
        }

        // Handle "Skip Every 5th Turn"
        if (profile != null && "Skip Every 5th Turn".equals(profile.wildCard) && shouldSkipTurn()) {
            // System.out.println("Skipping turn (wild card active)");
            return null;  // Treat this as passing the turn
        }

        // Handle "Two-Second Decision Limit"
        if (profile != null && "Two-Second Decision Limit".equals(profile.wildCard)) {
            return getMoveWithTimeout(3, 2000);  // 2 seconds
        }

        if (profile != null) {
                return getCustomBotMove(3);
        }
        return switch (botType) {
            case AGGRESSIVE -> getMostAggressiveMove(board, getBotColor());
            case DEFENSIVE -> getDefensiveMove(board, getBotColor(), 3);
            case RANDOM -> getRandomMove(board, getBotColor());
            case SACRIFICIAL -> getSacrificialMove(board, getBotColor(), 3);
            case HYBRID -> getAdaptiveMove(board, getBotColor());
            case SWEATY -> getBestMove(board, getBotColor(), 3);
        };
    }

    public Move getCustomBotMove(int depth) {
        Piece.Color botColor = getBotColor();
        List<Move> legalMoves = getAllValidMoves(botColor);

        if (profile != null) {
            if (profile.wildCard.contains("Shortened Lookahead")) {
                depth = 2;
            }
        }
    
        if (legalMoves.isEmpty() || board.isGameOver()) {
            return null;
        }
    
        // Backup current board state
        Piece[][] storedBoard = board.deepCopyBoard(board.getBoardArray());
        Piece.Color storedPlayer = board.getCurrentPlayer();
        boolean wasGameOver = board.isGameOver();
    
        Move bestMove = null;
        int bestEval = Integer.MAX_VALUE;
    
        for (Move move : legalMoves) {
            board.handleMove(move, gameManager, true);
            int eval = evaluateCustomMove(board, botColor, move, depth);
            board.undoMove(move);
    
            if (eval < bestEval) {
                bestEval = eval;
                bestMove = move;
            }
        }
    
        // Restore original state
        board.restoreBoardState(storedBoard, storedPlayer, gameManager, wasGameOver);
        return bestMove;
    }

    private int evaluateCustomMove(ChessBoard board, Piece.Color color, Move move, int depth) {
        // Select appropriate evaluation strategy
        boolean usePositional = profile.forcedMoveStrategy.contains("Maximize positional advantage even when forced");
        boolean createOpponentForces = profile.forcedMoveStrategy.contains("Try to create more forced moves for the opponent");
    
        int score = usePositional
            ? evaluateBoardDefensive(board, color)  // treat as a positional play
            : minimaxAlphaBeta(board, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, true, color);  // normal eval
    
        // ----- CAPTURE PRIORITIZATION -----
        if (move.getCapturedPiece() != null) {
            if (profile.capturePrioritization.contains("Prefer capturing higher-valued pieces")) {
                score -= getPieceValue(move.getCapturedPiece()) * 3;
            }
            if (profile.capturePrioritization.contains("Prefer capturing to maximize mobility")) {
                int mobilityGain = evaluateMobility(color) - getAllValidMoves(color).size();
                score -= mobilityGain * 2;  // encourage moves that increase options
            }
            if (profile.capturePrioritization.contains("Capture only when forced") &&
                !board.hasMandatoryCapture(color, board.getBoardArray())) {
                score += 25;  // discourage voluntary captures
            }
        }
    
        // ----- PAWN BEHAVIOR -----
        if (move.getMovedPiece().getType() == Piece.PieceType.PAWN) {
            int rowDelta = Math.abs(move.getToRow() - move.getFromRow());
    
            if (profile.pawnBehavior.contains("Prefer pushing pawns early")) {
                int turnNumber = gameManager.getTurnNumber();  // Assumes you track turn count
                if (turnNumber < 10) score -= rowDelta * 2;  // push early
            }
    
            if (profile.pawnBehavior.contains("Delay pawn moves for later")) {
                int turnNumber = gameManager.getTurnNumber();
                if (turnNumber < 10) score += 10;  // discourage early pawn use
            }
    
            if (profile.pawnBehavior.contains("Prioritize promoting pawns") &&
                move.getToRow() == (color == Piece.Color.WHITE ? 0 : 7)) {
                score -= 30;  // strongly reward potential promotion
            }
        }
    
        // ----- FORCED MOVE STRATEGY -----
        if (getAllValidMoves(color).size() == 1) {
            if (profile.forcedMoveStrategy.contains("Find the move that reduces material fastest")) {
                score -= countMaterial(color);  // reward material loss
            } else if (createOpponentForces) {
                Piece.Color opponent = (color == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
                int forcedMoves = getAllValidMoves(opponent).size();
                score -= (32 - forcedMoves);  // reward reducing their options
            }
        }
    
        return score;
    }
    
    private int countMaterial(Piece.Color color) {
        int total = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = board.getPieceAt(row, col);
                if (p != null && p.getColor() == color) {
                    total += getPieceValue(p);
                }
            }
        }
        return total;
    }
    
    

    private Piece.Color getBotColor() {
        return gameManager.getCurrentPlayer().getColor();
    }    

    private int evaluateBoard(ChessBoard board, Piece.Color playerColor) {
        if (board.isGameOver() && board.getWinner() != playerColor) {
            return Integer.MAX_VALUE; // Bot loses = worst possible outcome
        }
    
        int botScore = 0;
        int opponentScore = 0;
    
        Piece.Color opponentColor = (playerColor == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
    
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece != null) {
                    if (piece.getColor() == playerColor) {
                        botScore += getPieceValue(piece);
                    } else if (piece.getColor() == opponentColor) {
                        opponentScore += getPieceValue(piece);
                    }
                }
            }
        }
    
        int score = botScore - opponentScore;
    
        List<Move> botMoves = getAllValidMoves(playerColor);
        for (Move move : botMoves) {
            if (board.isCaptureMove(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol())) {
                score -= 1;
            }
        }
    
        return score;
    }
    

    int evaluateMobility(Piece.Color color) {
        return getAllValidMoves(color).size();
    }

    public List<Move> getAllValidMoves(Piece.Color playerColor) {
        List<Move> allValidMoves = new ArrayList<>();
        List<Move> queenMoves = new ArrayList<>();
        boolean mustCapture = board.hasMandatoryCapture(playerColor, board.getBoardArray());
    
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
    
                if (piece != null && piece.getColor() == playerColor) {
                    List<int[]> rawMoves = board.getValidMoves(row, col);
                    for (int[] move : rawMoves) {
                        int endRow = move[0];
                        int endCol = move[1];
    
                        if (!board.isValidMove(row, col, endRow, endCol)) continue;
                        if (mustCapture && !board.isCaptureMove(row, col, endRow, endCol)) continue;
    
                        Move m = new Move(row, col, endRow, endCol, piece);
                        if (profile != null && "No Queen Moves".equals(profile.wildCard) && piece.getType() == Piece.PieceType.QUEEN) {
                            queenMoves.add(m);
                        } else {
                            allValidMoves.add(m);
                        }
                    }
                }
            }
        }
    
        // 👇 If no non-queen moves, allow queen moves
        if (allValidMoves.isEmpty()) {
            return queenMoves;
        }
    
        return allValidMoves;
    }
    
      
    // Assign values (lower is better)
    private int getPieceValue(Piece piece) {
        if (profile != null && profile.pieceValues != null) {
            String key = piece.getType().name().charAt(0) + piece.getType().name().substring(1).toLowerCase();
            Integer value = profile.pieceValues.get(key);
            if (value != null) return value;
        }
        return switch (piece.getType()) {
            case PAWN -> 1;
            case KNIGHT, BISHOP -> 3;
            case ROOK -> 5;
            case QUEEN -> 9;
            default -> 0;
        };
    }

    private int minimax(ChessBoard board, int depth, boolean isMaximizing, Piece.Color playerColor, boolean isDefensive) {
        if (depth == 0 || board.isGameOver()) {
            return isDefensive ? evaluateBoardDefensive(board, playerColor) : evaluateBoard(board, playerColor);
        }
    
        List<Move> legalMoves = getAllValidMoves(playerColor);
    
        if (isMaximizing) {  // Opponent’s turn (tries to keep pieces in defensive mode)
            int maxEval = Integer.MIN_VALUE;
            for (Move move : legalMoves) {
                board.handleMove(move, gameManager, true);
                int eval = minimax(board, depth - 1, false, playerColor, isDefensive);
                board.undoMove(move);
                maxEval = Math.max(maxEval, eval);
            }
            return maxEval;
        } else {  // Bot’s turn (tries to lose pieces in normal mode)
            int minEval = Integer.MAX_VALUE;
            for (Move move : legalMoves) {
                board.handleMove(move, gameManager, true);
                int eval = minimax(board, depth - 1, true, playerColor, isDefensive);
                board.undoMove(move);
                minEval = Math.min(minEval, eval);
            }
            return minEval;
        }
    }

    private int minimaxAlphaBeta(ChessBoard board, int depth, int alpha, int beta, boolean isMaximizing, Piece.Color playerColor) {
        // System.out.println("Minimax Alpha Beta Called, Depth: " + depth);
        if (depth == 0 || board.isGameOver()) {
            return evaluateBoard(board, playerColor);
        }
        
        List<Move> legalMoves = getAllValidMoves(playerColor);
        // System.out.println("legal moves are: " + getAllValidMoves(playerColor));
    
        if (isMaximizing) {  // Opponent’s turn
            // System.out.println("Minimax Alpha Beta Maximizing Called");
            int maxEval = Integer.MIN_VALUE;
            for (Move move : legalMoves) {
                board.handleMove(move, gameManager, true);
                int eval = minimaxAlphaBeta(board, depth - 1, alpha, beta, false, playerColor);
                board.undoMove(move);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;  // Prune
            }
            return maxEval;
        } else {  // Bot’s turn
            // System.out.println("Minimax Alpha Beta Else Called");
            int minEval = Integer.MAX_VALUE;
            for (Move move : legalMoves) {
                board.handleMove(move, gameManager, true);
                int eval = minimaxAlphaBeta(board, depth - 1, alpha, beta, true, playerColor);
                board.undoMove(move);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;  // Prune
            }
            return minEval; 
        }
        }

    public Move getBestMove(ChessBoard board, Piece.Color playerColor, int depth) {
        // **Store board state before simulation**
        Piece[][] storedBoard = board.deepCopyBoard(board.getBoardArray());
        Piece.Color storedPlayer = board.getCurrentPlayer();
        boolean wasGameOver = board.isGameOver(); // Store the game-over state

        List<Move> legalMoves = getAllValidMoves(playerColor);
        // System.out.println("Number of legal moves found: " + legalMoves.size());
        if (legalMoves.isEmpty() || wasGameOver) {
            return null; // If game is over, bot should not return any move
        }

        Move bestMove = null;
        int bestEval = Integer.MAX_VALUE;  // Bot wants the lowest score

        for (Move move : legalMoves) {
            board.handleMove(move, gameManager, true);

            boolean wasCapture = move.getCapturedPiece() != null || move.wasEnPassant();

            int eval = minimaxAlphaBeta(board, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, true, playerColor);

            if (wasCapture) {
                eval -= 5;
            }

            board.undoMove(move);        
        
            if (eval < bestEval) {
                bestEval = eval;
                bestMove = move;
            }
        }
        

        // **Restore board state after simulation, preserving game-over state**
        board.restoreBoardState(storedBoard, storedPlayer, gameManager, wasGameOver); // Marking as simulation

        // System.out.println("Best Move found: " + bestMove);

        return bestMove;
    }
    

    public Move getMostAggressiveMove(ChessBoard board, Piece.Color playerColor) {
        List<Move> legalMoves = getAllValidMoves(playerColor);
        // if (legalMoves.isEmpty()) {
        //     System.out.println("BotLogic: No valid moves found.");
        //     return null;
        // } else {
        //     System.out.println("List of Valid Moves " + legalMoves);
        // }
        //Logic is completely off here
        Move bestMove = null;
        int maxCaptureValue = -1;
    
        for (Move move : legalMoves) {
            if (board.isValidMove(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol())) {
                int pieceValue = getPieceValue(board.getPieceAt(move.getFromRow(), move.getFromCol()));
                // System.out.print("Move " + move + " has a piece value of " + pieceValue);
                if (pieceValue > maxCaptureValue) {
                    maxCaptureValue = pieceValue;
                    bestMove = move;
                }
            }
        }
        return bestMove;
    }

    private int evaluateBoardDefensive(ChessBoard board, Piece.Color playerColor) {
        if (board.isGameOver() && board.getWinner() != playerColor) {
            return Integer.MIN_VALUE; // Defensive bot strongly avoids losing
        }
    
        int score = 0;
    
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece != null && piece.getColor() == playerColor) {
                    score += getPieceValue(piece);
                    if (board.isPieceHanging(row, col)) {
                        score += 2;  // Extra penalty for vulnerable pieces
                    }
                }
            }
        }
    
        return score;
    }
    

    public Move getDefensiveMove(ChessBoard board, Piece.Color playerColor, int depth) {
        List<Move> legalMoves = getAllValidMoves(playerColor);
        Move bestMove = null;
        int bestEval = Integer.MIN_VALUE; // Defensive bot wants the highest board score

        Piece[][] storedBoard = board.deepCopyBoard(board.getBoardArray());
        Piece.Color storedPlayer = board.getCurrentPlayer();
        boolean wasGameOver = board.isGameOver(); // Store the game-over state
    
        for (Move move : legalMoves) {
            board.handleMove(move, gameManager, true);
            int eval = minimax(board, depth - 1, false, playerColor, true);
            board.undoMove(move);
    
            if (eval > bestEval) {
                bestEval = eval;
                bestMove = move;
            }
        }

        board.restoreBoardState(storedBoard, storedPlayer, gameManager, wasGameOver); // Marking as simulation
        return bestMove;
    }
    
    

    public Move getRandomMove(ChessBoard board, Piece.Color playerColor) {
        List<Move> legalMoves = getAllValidMoves(playerColor);
        if (legalMoves.isEmpty()) return null;
    
        Collections.shuffle(legalMoves);
        for (Move move : legalMoves) {
            if (board.isValidMove(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol())) {
                return move;
            }
        }
        return legalMoves.get(0);
    }

    private int minimaxSacrificial(ChessBoard board, int depth, int alpha, int beta, boolean isMaximizing, Piece.Color playerColor) {
        if (depth == 0 || board.isGameOver()) {
            return evaluateBoard(board, playerColor);
        }
    
        List<Move> legalMoves = getAllValidMoves(playerColor);
        legalMoves.sort((m1, m2) -> board.isCaptureMove(m2.getFromRow(), m2.getFromCol(), m2.getToRow(), m2.getToCol()) ? 1 : -1);
    
        int bestEval = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
    
        for (Move move : legalMoves) {
            board.handleMove(move, gameManager, true);
            int eval = minimaxSacrificial(board, depth - 1, alpha, beta, !isMaximizing, playerColor);
            board.undoMove(move);
    
            if (isMaximizing) {
                bestEval = Math.max(bestEval, eval);
                alpha = Math.max(alpha, eval);
            } else {
                bestEval = Math.min(bestEval, eval);
                beta = Math.min(beta, eval);
            }
            if (beta <= alpha) break;
        }
        return bestEval;
    }

    public Move getSacrificialMove(ChessBoard board, Piece.Color playerColor, int depth) {
        List<Move> legalMoves = getAllValidMoves(playerColor);
        Move bestMove = null;
        int bestEval = Integer.MAX_VALUE; // Sacrificial bot wants the lowest score (to lose pieces)

        Piece[][] storedBoard = board.deepCopyBoard(board.getBoardArray());
        Piece.Color storedPlayer = board.getCurrentPlayer();
        boolean wasGameOver = board.isGameOver(); // Store the game-over state

        for (Move move : legalMoves) {
            board.handleMove(move, gameManager, true);
            int eval = minimaxSacrificial(board, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, playerColor);
            board.undoMove(move);
            if (eval < bestEval) {
                bestEval = eval;
                bestMove = move;
            }
        }

        board.restoreBoardState(storedBoard, storedPlayer, gameManager, wasGameOver); // Marking as simulation
        return bestMove;
    }
    

    public Move getAdaptiveMove(ChessBoard board, Piece.Color playerColor) {
        int pieceCount = board.countPieces(playerColor);
        if (pieceCount > 10) {
            return getMostAggressiveMove(board, playerColor);
        } else if (pieceCount < 5) {
            return getBestMove(board, playerColor, 3);
        } else {
            return getRandomMove(board, playerColor);
        }
    }

    public interface BotSearchFunction {
        Move run(ChessBoard board);
    }
    
    public static Move simulateWithRollback(ChessBoard board, GameManager gameManager, BotSearchFunction logic) {
        Piece[][] storedBoard = board.deepCopyBoard(board.getBoardArray());
        Piece.Color storedPlayer = board.getCurrentPlayer();
        boolean wasGameOver = board.isGameOver();
    
        Move result = logic.run(board);
        board.restoreBoardState(storedBoard, storedPlayer, gameManager, wasGameOver);
        return result;
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public Move getMoveWithTimeout(int depth, int timeoutMillis) {
        final Move[] result = new Move[1];
        Thread worker = new Thread(() -> {
            result[0] = getCustomBotMove(depth);  // Run your usual logic here
        });
    
        worker.start();
        try {
            worker.join(timeoutMillis);
            if (worker.isAlive()) {
                worker.interrupt(); // Cancel if still running
                System.out.println("Move timed out. Picking random legal move.");
                return getRandomMove(board, getBotColor()); // Fallback if timeout
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    
        return result[0] != null ? result[0] : getRandomMove(board, getBotColor());
    }     

    public boolean shouldSkipTurn() {    
        int turn = gameManager.getTurnNumber();
        Piece.Color botColor = getBotColor();
        // Shift skip pattern based on bot color
    int skipOffset = (botColor == Piece.Color.WHITE) ? 4 : 5;

    return profile != null &&
    "Skip Every 5th Turn".equals(profile.wildCard) &&
    turn == skipOffset ||
    (turn > skipOffset && (turn - skipOffset) % 5 == 0) &&
    gameManager.getCurrentPlayer().getColor() == botColor;
    }    
    
}
