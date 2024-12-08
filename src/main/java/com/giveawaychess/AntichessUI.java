package com.giveawaychess;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AntichessUI {
    private JButton[][] boardButtons; // 8x8 array of buttons representing the board
    private int[] selectedSquare = null; // To store the selected square (piece to move)
    private JTable moveHistoryTable; // Table for move history
    private DefaultTableModel tableModel; // Model for the move history table   
    private boolean isWhiteTurn = true; // Track whose turn it is
    private boolean isBoardFlipped = false;
    private JPanel boardPanel;

    Player whitePlayer;
    Player blackPlayer;
    Player player;
    
    private ChessBoard board;
    private GameManager gameManager;

    // Constructor to set up the UI
    public AntichessUI() {
        initializeUI(); // Create and set up the GUI
        
        this.board = new ChessBoard(this);
        this.player = new Player(Piece.Color.WHITE, false, board);

        // Ask the user if they want to play against a bot or another human
        String[] options = {"Play against Bot", "Play against Human"};
        int choice = JOptionPane.showOptionDialog(
            null,
            "Choose your opponent:",
            "Game Setup",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
        );

        if (choice == 0) {
            // Play against Bot
            String[] botOptions = {"Play as White", "Play as Black"};
            int botChoice = JOptionPane.showOptionDialog(
                null,
                "Choose your side:",
                "Bot Game Setup",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                botOptions,
                botOptions[0]
            );

            if (botChoice == 0) {
                // Play as White against Black Bot
                this.whitePlayer = new Player(Piece.Color.WHITE, false, board);  // Human player (White)
                this.blackPlayer = new Player(Piece.Color.BLACK, true, board);   // Bot player (Black)
                blackPlayer.setUI(AntichessUI.this);
                flipBoard();
            } else {
                // Play as Black against White Bot
                this.whitePlayer = new Player(Piece.Color.WHITE, true, board);   // Bot player (White)
                this.blackPlayer = new Player(Piece.Color.BLACK, false, board);  // Human player (Black)
                whitePlayer.setUI(AntichessUI.this);
            }
        } else {
            // Play against Human
            this.whitePlayer = new Player(Piece.Color.WHITE, false, board);  // Human player (White)
            this.blackPlayer = new Player(Piece.Color.BLACK, false, board);  // Human player (Black)
            flipBoard();
        }

        this.gameManager = new GameManager(whitePlayer, blackPlayer); // Initialize GameManager
        
        whitePlayer.setGameManager(gameManager); // Set GameManager for white player
        blackPlayer.setGameManager(gameManager); // Set GameManager for black player


    // Set up the custom position on the board when I want to
        // board.setupCustomPosition();

        
        board.startGame(); // Start the game
        if (whitePlayer.isBot()) {
            whitePlayer.makeRandomMove(board.getBoard());
        }
    }

    // Method to initialize the UI
    private void initializeUI() {
        JFrame frame = new JFrame("Antichess");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800); // Adjust to fit the board and labels snugly
        frame.setLayout(new BorderLayout());

        
        JPanel mainPanel = new JPanel(new BorderLayout()); // Use BorderLayout for main panel
        this.boardPanel = new JPanel(new GridLayout(9,9));
        // Create board buttons and set up action listeners
        boardButtons = new JButton[8][8];

        // Add the top-left empty corner
        boardPanel.add(new JLabel("")); // Top-left corner is empty

        char[] columns = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        for (char column : columns) {
            JLabel colLabel = new JLabel(Character.toString(column), SwingConstants.CENTER);
            colLabel.setPreferredSize(new Dimension(80, 80)); // Adjust to match button size
            boardPanel.add(colLabel); // Add to the top row
        }

        loadImages();

         // Add the flip button
         JButton flipButton = new JButton("Flip Board");
        flipButton.addActionListener(e -> {
            System.out.println("Board Flipped");
            flipBoard();
        });
        
        
        // Add row labels and board buttons
        for (int row = 0; row < 8; row++) {
            // Add row label on the left side
            JLabel rowLabel = new JLabel(Integer.toString(1 + row), SwingConstants.CENTER); // Row label (reverse order)
            rowLabel.setPreferredSize(new Dimension(80, 80)); // Adjust size to match the board buttons
            boardPanel.add(rowLabel);

            for (int col = 0; col < 8; col++) {
                JButton button = new JButton();
                boardButtons[row][col] = button;

                if ((row + col) % 2 == 0) {
                    button.setBackground(Color.GRAY); // Dark square
                } else {
                    button.setBackground(Color.WHITE);// Light square
                    
                }

                button.setOpaque(true);
                button.setBorderPainted(false);
                button.setPreferredSize(new Dimension(80, 80)); // Make sure the buttons are square and compact

                // Add action listener to each button to handle user clicks
                final int currentRow = row;
                final int currentCol = col;
                button.addActionListener(e -> handleBoardClick(currentRow, currentCol));

                boardPanel.add(button);
            }
        }

        // Create and set up the move history table
        String[] columnNames = {"White", "Black"};
        tableModel = new DefaultTableModel(columnNames, 0);
        moveHistoryTable = new JTable(tableModel);

        moveHistoryTable.setPreferredScrollableViewportSize(new Dimension(150, 400)); // Adjust the width for a narrower look
        moveHistoryTable.setFillsViewportHeight(true); // Table fills the height of its viewport

        // Adjust column widths to make the table narrower
        moveHistoryTable.getColumnModel().getColumn(0).setPreferredWidth(70); // Width for White's moves
        moveHistoryTable.getColumnModel().getColumn(1).setPreferredWidth(70); // Width for Black's moves

        JScrollPane scrollPane = new JScrollPane(moveHistoryTable);

        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.EAST);
        mainPanel.add(flipButton, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    // Method to handle board button clicks
    private void handleBoardClick(int row, int col) {
        if (selectedSquare == null) {
            Piece movingPiece = board.getPieceAt(row, col);
            // System.out.println("Piece Selected");
            selectedSquare = new int[]{row, col};
            Piece targetPiece = board.getPieceAt(row, col);
            List<int[]> validMoves = board.getValidMoves(row, col);
            // System.out.println(validMoves);
            highlightMoves(validMoves);
            if (movingPiece == null) {
                // System.out.println("No piece at this square");
                selectedSquare = null;
            }
            else if (isWhiteTurn ? targetPiece.getColor() == Piece.Color.BLACK : targetPiece.getColor() == Piece.Color.WHITE) {
                selectedSquare = null;
            }
        } else {
            if (selectedSquare == null || selectedSquare.length < 2) {
                System.err.println("Error: selectedSquare is not properly initialized.");
            }
            // Second click: attempt to move the piece
            Piece movingPiece = board.getPieceAt(selectedSquare[0], selectedSquare[1]);
            Move move = new Move(selectedSquare[0], selectedSquare[1], row, col, movingPiece); // Declare the move variable
            boolean moveSuccessful = board.handleMove(move, gameManager);
            System.out.println("Move Attempted");
            if (moveSuccessful) {
                addMoveToHistory(selectedSquare[0], selectedSquare[1], row, col);
                selectedSquare = null; // Reset after a successful move
                onMoveMade();
            } else {
                // Handle invalid move (optional feedback to the user)
                selectedSquare = null; // Reset after an invalid attempt
                System.out.println("Move Not Successful");
            }
            resetBoardColors();
        }
    }


    private void flipBoard() {
        isBoardFlipped = !isBoardFlipped;
        boardPanel.removeAll(); // Clear the boardPanel for re-layout
        
        // Re-add the top-left empty corner
        boardPanel.add(new JLabel("")); // Top-left corner is empty

        char[] columns = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        int[] rows = {1, 2, 3, 4, 5, 6, 7, 8};
        int[] flippedRows = {8, 7, 6, 5, 4, 3, 2, 1};

        JButton[][] flippedButtons = new JButton[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                // Flip the button positions
                flippedButtons[row][col] = boardButtons[7 - row][7 - col];
            }
        }

        // Add column labels
        for (char column : columns) {
            JLabel colLabel = new JLabel(Character.toString(column), SwingConstants.CENTER);
            colLabel.setPreferredSize(new Dimension(80, 80)); // Adjust to match button size
            boardPanel.add(colLabel); // Add to the top row
        }

        // Re-add row labels and board buttons in flipped order
        for (int row = 0; row < 8; row++) {
            if (isBoardFlipped) {
                JLabel rowLabel = new JLabel(Integer.toString(flippedRows[row]), SwingConstants.CENTER); // Row label
                rowLabel.setPreferredSize(new Dimension(80, 80)); // Adjust size to match the board buttons
                boardPanel.add(rowLabel);
            } else {
                // Add row label on the left side
                JLabel rowLabel = new JLabel(Integer.toString(rows[row]), SwingConstants.CENTER); // Row label
                rowLabel.setPreferredSize(new Dimension(80, 80)); // Adjust size to match the board buttons
                boardPanel.add(rowLabel);
            }

            for (int col = 0; col < 8; col++) {
                int displayRow = isBoardFlipped ? 7 - row : row;
                boardPanel.add(boardButtons[displayRow][col]); // Add the button in flipped order
            }
        }
    
        // Update the board colors to ensure correct pattern after flip
        resetBoardColors();
    
        // Refresh the UI to display the changes
        boardPanel.revalidate();
        boardPanel.repaint();
    }

    // Method to update the board buttons based on the current state of the chessboard
    public void updateBoard(Piece[][] board) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];;
                 if (piece != null) {
                        int index = piece.getType().ordinal() + (piece.getColor() == Piece.Color.WHITE ? 0 : 6); 
                        boardButtons[row][col].setIcon(scaleImageIcon(pieceImages[index], boardButtons[row][col].getWidth(), boardButtons[row][col].getHeight()));
                } else {
                        boardButtons[row][col].setIcon(null); // Clear icon for empty squares
                }
            }
        }
    }

    private ImageIcon[] pieceImages = new ImageIcon[12];

    private void loadImages() {
        pieceImages[0] = new ImageIcon("/Users/ekuo25/MyScripts/SSP_Anti-Chess/WhiteKing.png");
        pieceImages[1] = new ImageIcon("/Users/ekuo25/MyScripts/SSP_Anti-Chess/WhiteQueen.png");
        pieceImages[2] = new ImageIcon("/Users/ekuo25/MyScripts/SSP_Anti-Chess/WhiteRook.png");
        pieceImages[3] = new ImageIcon("/Users/ekuo25/MyScripts/SSP_Anti-Chess/WhiteBishop.png");
        pieceImages[4] = new ImageIcon("/Users/ekuo25/MyScripts/SSP_Anti-Chess/WhiteKnight.png");      
        pieceImages[5] = new ImageIcon("/Users/ekuo25/MyScripts/SSP_Anti-Chess/WhitePawn.png");
        pieceImages[6] = new ImageIcon("/Users/ekuo25/MyScripts/SSP_Anti-Chess/BlackKing.png");
        pieceImages[7] = new ImageIcon("/Users/ekuo25/MyScripts/SSP_Anti-Chess/BlackQueen.png");
        pieceImages[8] = new ImageIcon("/Users/ekuo25/MyScripts/SSP_Anti-Chess/BlackRook.png");
        pieceImages[9] = new ImageIcon("/Users/ekuo25/MyScripts/SSP_Anti-Chess/BlackBishop.png");
        pieceImages[10] = new ImageIcon("/Users/ekuo25/MyScripts/SSP_Anti-Chess/BlackKnight.png");
        pieceImages[11] = new ImageIcon("/Users/ekuo25/MyScripts/SSP_Anti-Chess/BlackPawn.png");
    }

    private ImageIcon scaleImageIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImg);
    }



    public void resetBoardColors() {
        if (isBoardFlipped) {
            for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                // Reset to default colors (e.g., white and gray for a chessboard pattern)
                if ((row + col) % 2 == 0) {
                    boardButtons[row][col].setBackground(Color.WHITE);
                } else {
                    boardButtons[row][col].setBackground(Color.GRAY);
                }
            }
        }
        } else {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    // Reset to default colors (e.g., white and gray for a chessboard pattern)
                    if ((row + col) % 2 == 0) {
                        boardButtons[row][col].setBackground(Color.GRAY);
                    } else {
                        boardButtons[row][col].setBackground(Color.WHITE);
                    }
                }
            }
        }
        
    }

    public void addMoveToHistory(int startRow, int startCol, int endRow, int endCol) {
        char[] columns = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        Piece movingPiece = board.getPieceAt(startRow, startCol);
        Piece targetPiece = board.getPieceAt(endRow, endCol); // Piece at the destination square
        StringBuilder move = new StringBuilder();
        

        if (movingPiece != null && movingPiece.getType() != Piece.PieceType.PAWN) {
            switch (movingPiece.getType()) {
                case KING:
                    move.append("K");
                    break;
                case QUEEN:
                    move.append("Q");
                    break;
                case ROOK:
                    move.append("R");
                    break;
                case BISHOP:
                    move.append("B");
                    break;
                case KNIGHT:
                    move.append("N");
                    break;
                default:
                    break;
            }
        }

        // If a capture is made, add "x" to the move notation
        //Target Piece is never null, moving piece is always null
        if (movingPiece != null) {
            move.append(columns[startCol]).append(1 + startRow).append("x"); // "x" for capture
        }
        else {
            move.append(columns[startCol]).append(1 + startRow).append("->");
    }

        // Continue with the move destination notation
        move.append(columns[endCol]).append(1 + endRow);
        // Add the move to the appropriate column (White or Black)
        if (isWhiteTurn) {
            tableModel.addRow(new Object[]{move, ""}); // Add move to White's column
        } else {
            int lastRow = tableModel.getRowCount() - 1;
            tableModel.setValueAt(move.toString(), lastRow, 1);
        }   
        isWhiteTurn = !isWhiteTurn; // Toggle turn
    }

    public void highlightMoves(List<int[]> validMoves) {
        // Reset the board first
        resetBoardColors();
        // System.out.println("Board Colors Reset");
        
        // Highlight the valid moves
        for (int[] move : validMoves) {
            int row = move[0];
            int col = move[1];
            boardButtons[row][col].setBackground(Color.GREEN); // Use green for valid moves
            // System.out.println("Background Colors set to Green");
        }
    }

    public void onMoveMade() {
        if (this.board == null) {
            System.err.println("Error: ChessBoard is not initialized. AntiChessLocation");
            return;
        }

        // Update the UI and game state after a move is made
        Piece [][] boardArray = this.board.getBoard();
        // System.out.println("Move Made");
        player.switchTurn();
        if (isWhiteTurn) {
            if (whitePlayer.isBot()) {
                whitePlayer.makeRandomMove(boardArray);
            } else {
                // Wait for human input
            }
        } else if (!isWhiteTurn) {
            if (blackPlayer.isBot()) {
                blackPlayer.makeRandomMove(boardArray);
                // System.out.println(isWhiteTurn);
            } else {
                // Wait for human input
            }
        }
    }
}
