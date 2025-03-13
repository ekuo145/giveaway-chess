package com.giveawaychess;

import com.giveawaychess.BotLogic.BotType;

public class GameAntichess {
    private boolean isWhiteTurn = true;
    private boolean isBotGame = false;
    public Player whitePlayer;
    public Player blackPlayer;
    private Player player;
    private ChessBoard board;
    private GameManager gameManager;
    private BotType botType;
    private Piece.Color pieceColor;

    public GameAntichess(boolean playAgainstBot, BotType selectedBotType, Piece.Color playerColor) {
        this.board = new ChessBoard();
        this.gameManager = new GameManager();
        this.isBotGame = playAgainstBot;
        
        if (playAgainstBot) {
            this.botType = selectedBotType;
            this.pieceColor = playerColor;
            
            if (pieceColor == Piece.Color.WHITE) {
                whitePlayer = new Player(Piece.Color.WHITE, false, board, null, gameManager);
                blackPlayer = new Player(Piece.Color.BLACK, true, board, botType, gameManager);
            } else {
                whitePlayer = new Player(Piece.Color.WHITE, true, board, botType, gameManager);
                blackPlayer = new Player(Piece.Color.BLACK, false, board, null, gameManager);
            }
        } else {
            whitePlayer = new Player(Piece.Color.WHITE, false, board, null, gameManager);
            blackPlayer = new Player(Piece.Color.BLACK, false, board, null, gameManager);
        }
        
        gameManager.setPlayers(whitePlayer, blackPlayer);
        whitePlayer.setGameManager(gameManager);
        blackPlayer.setGameManager(gameManager);
        board.startGame();
        
        if (whitePlayer.isBot()) {
            whitePlayer.makeBotMove(board.getBoard());
        }
    }

    public boolean makeMove(int startRow, int startCol, int endRow, int endCol) {
        Piece movingPiece = board.getPieceAt(startRow, startCol);
        if (movingPiece == null) return false;
        
        Move move = new Move(startRow, startCol, endRow, endCol, movingPiece);
        boolean moveSuccessful = board.handleMove(move, gameManager, false);
        
        if (moveSuccessful) {
            isWhiteTurn = !isWhiteTurn;
            player.switchTurn();
            if (isWhiteTurn && whitePlayer.isBot()) {
                whitePlayer.makeBotMove(board.getBoard());
            } else if (!isWhiteTurn && blackPlayer.isBot()) {
                blackPlayer.makeBotMove(board.getBoard());
            }
            return true;
        }
        return false;
    }
    
    public void resetGame() {
        this.board = new ChessBoard();
        this.player = new Player(Piece.Color.WHITE, false, board, null, gameManager);
        this.whitePlayer = new Player(Piece.Color.WHITE, false, board, null, gameManager);
        this.blackPlayer = new Player(Piece.Color.BLACK, false, board, null, gameManager);
        this.gameManager = new GameManager();
        gameManager.setPlayers(whitePlayer, blackPlayer);
        whitePlayer.setGameManager(gameManager);
        blackPlayer.setGameManager(gameManager);
        board.startGame();
    }

    public boolean hasMandatoryCapture() {
        return board.hasMandatoryCapture(gameManager.getCurrentPlayer().getColor(), board.getBoard());
    }
}
