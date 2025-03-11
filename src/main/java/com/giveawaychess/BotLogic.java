package com.giveawaychess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BotLogic {
    private ChessBoard board;
    private GameManager gameManager;

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

    public Move getMove() {
        List<Move> possibleMoves = getAllValidMoves(getBotColor());
        if (possibleMoves.isEmpty()) {
            System.out.println("BotLogic: No valid moves found.");
            return null;
        }
        
        switch (botType) {
            case AGGRESSIVE:
                return getMostAggressiveMove(board, getBotColor()); // Ensure method exists
            case DEFENSIVE:
                return getDefensiveMove(board, getBotColor(), 3);
            case RANDOM:
                return getRandomMove(board, getBotColor());
            case SACRIFICIAL:
                return getSacrificialMove(board, getBotColor(), 3);
            case HYBRID:
                return getAdaptiveMove(board, getBotColor());
            case SWEATY:
                return getBestMove(board, getBotColor(), 2);
            default:
                return getRandomMove(board, getBotColor());
        }
    }

    private Piece.Color getBotColor() {
        return gameManager.getCurrentPlayer().getColor();
    }    

    private int evaluateBoard(ChessBoard board, Piece.Color playerColor) {
        int score = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece != null && piece.getColor() == playerColor) {
                    score += getPieceValue(piece);
                }
            }
        }

        Piece.Color opponentColor = (playerColor == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
            List<Move> opponentMoves = getAllValidMoves(opponentColor);

            for (Move move : opponentMoves) {
                if (board.isCaptureMove(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol())) {
                    score -= 1; 
                }
            }


        return score;
    }

    public List<Move> getAllValidMoves(Piece.Color opponentColor) {
        // System.out.println("getAllValidMovesCalled");
        List<Move> allValidMoves = new ArrayList<>();
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
                
                if (piece != null && piece.getColor() == opponentColor) {
                    List<int[]> validMoves = board.getValidMoves(row, col);
    
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
    
    
    
    // Assign values (lower is better)
    private int getPieceValue(Piece piece) {
        switch (piece.getType()) {
            case PAWN: return 1;
            case KNIGHT: return 3;
            case BISHOP: return 3;
            case ROOK: return 5;
            case QUEEN: return 9;
            default: return 0;
        }
    }

    private int minimax(ChessBoard board, int depth, boolean isMaximizing, Piece.Color playerColor, boolean isDefensive) {
        if (depth == 0 || board.isGameOver()) {
            return isDefensive ? evaluateBoardDefensive(board, playerColor) : evaluateBoard(board, playerColor);
        }
    
        List<Move> legalMoves = getAllValidMoves(playerColor);
    
        if (isMaximizing) {  // Opponent’s turn (tries to keep pieces in defensive mode)
            int maxEval = Integer.MIN_VALUE;
            for (Move move : legalMoves) {
                board.handleMove(move, gameManager);
                int eval = minimax(board, depth - 1, false, playerColor, isDefensive);
                board.undoMove(move);
                maxEval = Math.max(maxEval, eval);
            }
            return maxEval;
        } else {  // Bot’s turn (tries to lose pieces in normal mode)
            int minEval = Integer.MAX_VALUE;
            for (Move move : legalMoves) {
                board.handleMove(move, gameManager);
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
                board.handleMove(move, gameManager);
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
                board.handleMove(move, gameManager);
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
        Piece.Color gameManagerPlayer = gameManager.getCurrentPlayer().getColor();
        // System.out.println("At the start of BestMove, Game Manager thinks it is " + gameManagerPlayer);
    
        List<Move> legalMoves = getAllValidMoves(playerColor);
        Move bestMove = null;
        int bestEval = Integer.MAX_VALUE;  // Bot wants the lowest score
    
        for (Move move : legalMoves) {
            board.handleMove(move, gameManager);
            int eval = minimaxAlphaBeta(board, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, true, playerColor);
            board.undoMove(move);  // Undo the move after evaluation
    
            if (board.isCaptureMove(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol())) {
                eval -= 5;  // Extra reward for moves that force captures
            }
    
            if (eval < bestEval) {
                bestEval = eval;
                bestMove = move;
            }
        }
    
        // **Restore board state after simulation**
        // System.out.println("At the middle of BestMove, Game Manager thinks it is " + gameManagerPlayer);
        board.restoreBoardState(storedBoard, storedPlayer, gameManager);

        System.out.println("bot has found best move");
        // System.out.println("Stored Player is " + storedPlayer);
        // System.out.println("At the end of BestMove, Game Manager thinks it is " + gameManagerPlayer);
    
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
    
        for (Move move : legalMoves) {
            board.handleMove(move, gameManager);
            int eval = minimax(board, depth - 1, false, playerColor, true); // Now passes `true` for defensive eval
            board.undoMove(move);
    
            if (eval > bestEval) {
                bestEval = eval;
                bestMove = move;
            }
        }

        board.restoreBoardState(storedBoard, storedPlayer, gameManager);
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
            board.handleMove(move, gameManager);
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

        for (Move move : legalMoves) {
            board.handleMove(move, gameManager);
            int eval = minimaxSacrificial(board, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, playerColor);
            board.undoMove(move);
    
            if (eval < bestEval) {
                bestEval = eval;
                bestMove = move;
            }
        }

        board.restoreBoardState(storedBoard, storedPlayer, gameManager);
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
    
}
