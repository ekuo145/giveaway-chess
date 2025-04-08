package com.giveawaychess;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.Timer;

import com.giveawaychess.AntichessUI.DialogueKey;

public class Player {
    private Piece.Color color;
    private ChessBoard chessBoard;
    private boolean isBot; 
    private GameManager gameManager;
    private AntichessUI ui;
    private BotLogic botLogic; 

    private Piece.Color turnColor = Piece.Color.WHITE;

    public Player(Piece.Color color, boolean isBot, ChessBoard chessBoard, BotLogic.BotType botType, GameManager gameManager) {
        this.color = color;
        this.isBot = isBot;
        this.chessBoard = chessBoard; // Initialize chessBoard
        this.gameManager = gameManager;
        if (isBot) {
            this.botLogic = new BotLogic(chessBoard, gameManager, botType);
        }
    }

    public Piece.Color getTurnColor() {
        return turnColor;
    }

    public Piece.Color getColor() {
        return color;
    }



    public void setChessBoard(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
        if (isBot && botLogic != null) {
            botLogic = new BotLogic(chessBoard, gameManager, botLogic.botType);
        }
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    // Setter for ui
    public void setUI(AntichessUI ui) {
        this.ui = ui;
    }

    public List<Move> getLegalMoves() {
        if (chessBoard == null) {
            throw new IllegalStateException("ChessBoard is not initialized for the player.");
        }

        List<Move> legalMoves = new ArrayList<>();
        
        // Iterate through the board to find all pieces of the current player
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Piece piece = chessBoard.getPieceAt(row, col); // Assuming `chessBoard` has this method
                if (piece != null && piece.getColor() == this.color) {
                    // Get all potential moves for this piece and validate them
                    List<Move> candidateMoves = piece.generatePotentialMoves(row, col, chessBoard.getBoard());
                    for (Move move : candidateMoves) {
                        int startRow = move.getFromRow();
                        int startCol = move.getFromCol();
                        int endRow = move.getToRow();
                        int endCol = move.getToCol();
                        if (chessBoard.isValidMove(startRow, startCol, endRow, endCol)) { // Efficient validation for each move
                            legalMoves.add(move);
                        }
                    }
                }
            }
        }
        return legalMoves;
    }
    


    public boolean isBot() {
        return isBot;
    }

    /**
     * Switches the turn to the other player.
     */
    public void switchTurn() {
        turnColor = (turnColor == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;;
    }

    /**
     * Makes a random move for the bot.
     */
    public void makeRandomMove(Piece[][] board) {
        Random rand = new Random();
    
        // Get all legal moves for the current player
        List<Move> possibleMoves = getLegalMoves();
    
        // If there are valid moves, randomly select one and execute it
        if (!possibleMoves.isEmpty()) {
            Move randomMove = possibleMoves.get(rand.nextInt(possibleMoves.size()));
            // System.out.println("Bot selected move: " + randomMove.getFromRow() + ", " + randomMove.getFromCol() + " to " + randomMove.getToRow() + ", " + randomMove.getToCol());
            chessBoard.handleMove(randomMove, gameManager, false);
            ui.addMoveToHistory(randomMove.getFromRow(), randomMove.getFromCol(), randomMove.getToRow(), randomMove.getToCol());
        } else {
            System.out.println("No legal moves available for the bot.");
        }
    }    

    public void makeBotMove(Piece[][] board) {
        if (!isBot || botLogic == null) return;
    
        // Show bot personality's thinking line
        String thinkingMessage = DialogueKey.getBotMessage(botLogic.botType, DialogueKey.THINKING);
        if (ui != null) {
            ui.updateBotDialogue(thinkingMessage);
        }
    
        // Simulate bot "thinking..."
        Timer timer = new Timer(400, e -> {
            Move botMove = botLogic.getMove();
    
            if (botMove != null) {
                boolean wasCapture = chessBoard.getPieceAt(botMove.getToRow(), botMove.getToCol()) != null;
    
                chessBoard.handleMove(botMove, gameManager, false);
                ui.addMoveToHistory(botMove.getFromRow(), botMove.getFromCol(), botMove.getToRow(), botMove.getToCol());
                ui.updateBoard(board);
                ui.getBoardPanel().revalidate();
                ui.getBoardPanel().repaint();

    
                // Determine post-move dialogue
                DialogueKey responseKey;
                if (chessBoard.isGameOver()) {
                    responseKey = (chessBoard.getWinner() == this.color)
                            ? DialogueKey.VICTORY
                            : DialogueKey.DEFEAT;
                } else if (wasCapture) {
                    responseKey = DialogueKey.CAPTURE;
                } else {
                    responseKey = DialogueKey.NEUTRAL;
                }
    
                String response = DialogueKey.getBotMessage(botLogic.botType, responseKey);
                ui.updateBotDialogue(response);
                } else {
                System.out.println("Bot has no valid moves.");
            }
        });
    
        timer.setRepeats(false);
        timer.start();
    }
    
}