package com.giveawaychess;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.giveawaychess.BotLogic.BotType;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AntichessUI {
    private JButton[][] boardButtons; // 8x8 array of buttons representing the board
    private int[] selectedSquare = null; // To store the selected square (piece to move)
    private JTable moveHistoryTable; // Table for move history
    private DefaultTableModel tableModel; // Model for the move history table   
    private boolean isWhiteTurn = true; // Track whose turn it is
    private boolean isBoardFlipped = false;
    private JPanel boardPanel;
    private boolean isBotGame = false;
    private JButton restartButton;
    private boolean showLegalMoves = true;

    private JLabel player1Avatar;
    private JLabel player2Avatar;
    private JLabel dialogueLabel;
    private JPanel dialoguePanel;

    private Map<String, String> messageOptions;

    Player whitePlayer;
    Player blackPlayer;
    Player player;
    
    private ChessBoard board;
    private GameManager gameManager;

    private BotType botType;
    private Piece.Color pieceColor;

    // Constructor to set up the UI
    public AntichessUI() {
        setupDialogueSystem();
        initializeUI(); // Create and set up the GUI
        
        this.gameManager = new GameManager(); // Initialize GameManager
        this.board = new ChessBoard(this, gameManager);
        this.player = new Player(Piece.Color.WHITE, false, board, null, gameManager);

        // Prompt user for game mode
        String[] options = {"Play against Bot", "Play against Human"};
        int choice = JOptionPane.showOptionDialog(null, "Choose your opponent:", "Game Setup",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == 0) { // Bot game
            isBotGame = true;
            String[] botOptions = {"Play vs Randy", "Play vs Darwin", "Play vs Virgil", "Play vs Levi", "Play vs Mark"};
            int botChoice = JOptionPane.showOptionDialog(null, "Choose who to play:", "Bot Game Setup",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, botOptions, botOptions[0]);
            if (botChoice == 0) {
                botType = BotType.RANDOM;
            } else if (botChoice == 1) {
                botType = BotType.AGGRESSIVE;
            } else if (botChoice == 2) {
                botType = BotType.DEFENSIVE;
            } else if (botChoice == 3) {
                botType = BotType.SACRIFICIAL;
            } else if (botChoice == 4) {
                botType = BotType.SWEATY;
            }

            String[] botColorOptions = {"Play as White", "Play as Black"};
            int botColorChoice = JOptionPane.showOptionDialog(null, "Choose your side:", 
                    (botChoice == 0 ? "Randy Bot Game Setup" : "Darwin Bot Game Setup"),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, botColorOptions, botColorOptions[0]);
            pieceColor = (botColorChoice == 0) ? Piece.Color.WHITE : Piece.Color.BLACK;

            if (pieceColor == Piece.Color.WHITE) {
                whitePlayer = new Player(Piece.Color.WHITE, false, board, null, gameManager);
                blackPlayer = new Player(Piece.Color.BLACK, true, board, botType, gameManager);
                blackPlayer.setUI(this);
                flipBoard();
            } else {
                whitePlayer = new Player(Piece.Color.WHITE, true, board, botType, gameManager);
                blackPlayer = new Player(Piece.Color.BLACK, false, board, null, gameManager);
                whitePlayer.setUI(this);
            }
        } else { // Human vs Human
            whitePlayer = new Player(Piece.Color.WHITE, false, board, null, gameManager);
            blackPlayer = new Player(Piece.Color.BLACK, false, board, null, gameManager);
            flipBoard();
        }

        // Set up GameManager and start the game
        System.out.println("Bot Type Selected: " + botType);
        gameManager.setPlayers(whitePlayer, blackPlayer);
        whitePlayer.setGameManager(gameManager);
        blackPlayer.setGameManager(gameManager);
        board.startGame();
        if (whitePlayer.isBot()) {
            whitePlayer.makeBotMove(board.getBoard());
        }


    // Set up the custom position on the board when I want to
        // board.setupCustomPosition();
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

        JPanel topPanel = new JPanel(new FlowLayout());

        // Load avatars
        player1Avatar = createAvatarLabel("/images/profiles/crackedChess.png");
        player2Avatar = createAvatarLabel("/images/profiles/sidewaysChess.png");

        // Dialogue box
        dialogueLabel = new JLabel("Welcome to Giveaway Chess!");
        dialogueLabel.setFont(new Font("Arial", Font.BOLD, 16));

        dialoguePanel = new JPanel();
        dialoguePanel.add(dialogueLabel);

        // Add elements to top panel
        topPanel.add(player1Avatar);
        topPanel.add(dialoguePanel);
        topPanel.add(player2Avatar);

        // Add to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);

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
            // System.out.println("Board Flipped");
            flipBoard();
        });

        // Add Restart Button
        restartButton = new JButton("Restart Game");
        restartButton.addActionListener(e -> resetGame());

        // Add Help Button
        JButton helpButton = new JButton("Rules");
        helpButton.addActionListener(e -> showHelpDialog());

        // Add Check Valid Move Button
        JButton checkValidMoveButton = new JButton("Capture?");
        checkValidMoveButton.addActionListener(e -> checkMandatoryCapture());

        // Add Toggle Legal Moves Button
        JButton toggleLegalMovesButton = new JButton("Toggle Legal Moves");
        toggleLegalMovesButton.addActionListener(e -> toggleLegalMoves());

        // Create a panel for buttons at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(flipButton);
        buttonPanel.add(restartButton);
        buttonPanel.add(helpButton);
        buttonPanel.add(toggleLegalMovesButton);
        buttonPanel.add(checkValidMoveButton);

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
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

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
            if (showLegalMoves == true) {
                highlightMoves(validMoves);
            }
            if (movingPiece == null) {
                // System.out.println("No piece at this square");
                selectedSquare = null;
            }
            else if (isWhiteTurn ? targetPiece.getColor() == Piece.Color.BLACK : targetPiece.getColor() == Piece.Color.WHITE) {
                selectedSquare = null;
            }
        } else {
            Piece targetedPiece = board.getPieceAt(row, col);
            if (selectedSquare == null || selectedSquare.length < 2) {
                System.err.println("Error: selectedSquare is not properly initialized.");
            }
            // Second click: attempt to move the piece
            Piece movingPiece = board.getPieceAt(selectedSquare[0], selectedSquare[1]);
            if (targetedPiece != null && movingPiece.getColor() == targetedPiece.getColor()) {
                selectedSquare = null;
                selectedSquare = new int[]{row, col};
                List<int[]> validMoves = board.getValidMoves(row, col);
                // System.out.println(validMoves);
                if (showLegalMoves == true) {
                    highlightMoves(validMoves);
                }
                return;
            }
            System.out.println("Piece String: " + movingPiece.toString());
            Move move = new Move(selectedSquare[0], selectedSquare[1], row, col, movingPiece); // Declare the move variable
            boolean moveSuccessful = board.handleMove(move, gameManager, false);
            // System.out.println("Move Attempted");
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

    private void setupDialogueSystem() {
        // Predefined messages
        messageOptions = new HashMap<>();
        messageOptions.put("move", "Your move!");
        messageOptions.put("capture", "Nice capture!");
        messageOptions.put("botThinking", "The bot is thinking...");
        messageOptions.put("check", "Check!");
        messageOptions.put("win", "Congratulations, you win!");
    }


    private void flipBoard() {
        isBoardFlipped = !isBoardFlipped;
        // System.out.println("Board Flipped is " + isBoardFlipped);
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
                Piece piece = board[row][col];
                if (piece != null) {
                    // Construct the key using the piece's type and color
                    String key = piece.getColor().toString().toLowerCase() + "_" + piece.getType().toString().toLowerCase();
                    ImageIcon icon = pieceImages.get(key);
                    
                    if (icon != null) {
                        boardButtons[row][col].setIcon(scaleImageIcon(icon, boardButtons[row][col].getWidth(), boardButtons[row][col].getHeight()));
                    } else {
                        // Log a warning if the image is missing for the given key
                        System.err.println("Missing image for key: " + key);
                        boardButtons[row][col].setIcon(null);
                    }
                } else {
                    // Clear the icon for empty squares
                    boardButtons[row][col].setIcon(null);
                }
            }
        }
    }
    

private HashMap<String, ImageIcon> pieceImages = new HashMap<>();

    private void loadImages() {

        // Base folder for images
        String basePath = "/images/";

        // Piece type and color mappings
        String[] colors = {"white", "black"};
        String[] pieceTypes = {"pawn", "rook", "knight", "bishop", "queen", "king"};

        for (String color : colors) {
            for (String pieceType : pieceTypes) {
                // Construct the resource path
                String filePath = basePath + color + "/" + pieceType + ".png";
    
                // Load the image using getResource
                URL imageUrl = getClass().getResource(filePath);
    
                if (imageUrl == null) {
                    System.err.println("Image not found: " + filePath);
                    continue; // Skip missing images to avoid crashing
                }
    
                // Load and store the ImageIcon
                ImageIcon icon = new ImageIcon(imageUrl);
                pieceImages.put(color + "_" + pieceType, scaleImageIcon(icon, 64, 64));
            }
        }
    }
    



    private ImageIcon scaleImageIcon(ImageIcon icon, int width, int height) {
        if (icon == null) {
            System.err.println("Icon is null. Cannot scale.");
            return null; // Or return a default placeholder image if available.
        }
    
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive integers.");
        }
    
        Image img = icon.getImage();
        if (img == null) {
            System.err.println("Image from icon is null. Cannot scale.");
            return null;
        }
    
        // Smooth scaling
        Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    
        // Return a new ImageIcon with the scaled image
        return new ImageIcon(scaledImg);
    }



    public void resetBoardColors() {
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
    
    private void toggleLegalMoves() {
        showLegalMoves = !showLegalMoves;
        if (!showLegalMoves) {
            resetBoardColors();
        }
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
                whitePlayer.makeBotMove(boardArray);
            } else {
                if (!isBotGame) {
                    flipBoard();
                }
            }
        } else if (!isWhiteTurn) {
            if (blackPlayer.isBot()) {
                blackPlayer.makeBotMove(boardArray);
                // System.out.println(isWhiteTurn);
            } else {
                if (!isBotGame) {
                    flipBoard();
                }
                // Wait for human input
            }
        }
    }
    private void resetGame() {
        // Reset game-related state
        this.board = new ChessBoard(this, gameManager);
        this.player = new Player(Piece.Color.WHITE, false, board, null, gameManager);
    
        // Reset players
        this.whitePlayer = new Player(Piece.Color.WHITE, false, board, null, gameManager);
        this.blackPlayer = new Player(Piece.Color.BLACK, false, board, null, gameManager);
    
        // Start the game again
        this.gameManager = new GameManager();
        // Assign players to gameManager
        gameManager.setPlayers(whitePlayer, blackPlayer);
        whitePlayer.setGameManager(gameManager);
        blackPlayer.setGameManager(gameManager);
    
        board.startGame();

        // Clear the history table
        tableModel.setRowCount(0);
    
        // Reset the board and flip back to defaults
        updateBoard(board.getBoard());
    }

    private void restartGame() {
        // Close the current game window properly
        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(boardPanel);
        if (currentFrame != null) {
            currentFrame.dispose();
        }
        SwingUtilities.invokeLater(AntichessUI::new);
    
        // Reset game state variables
        isWhiteTurn = true; // Ensure White starts
        selectedSquare = null; // Reset selected square
        isBoardFlipped = false; // Reset board orientation
        isBotGame = false; // Ensure correct bot behavior
    
        // Reinitialize board and players
        board = new ChessBoard(this, gameManager);
        whitePlayer = new Player(Piece.Color.WHITE, false, board, null, gameManager);
        blackPlayer = new Player(Piece.Color.BLACK, false, board, null, gameManager);
    
        // Reinitialize GameManager
        gameManager = new GameManager();
        // Assign players to gameManager
        gameManager.setPlayers(whitePlayer, blackPlayer);
        whitePlayer.setGameManager(gameManager);
        blackPlayer.setGameManager(gameManager);
    
        // Ensure UI resets correctly
        tableModel.setRowCount(0); // Clear move history
        updateBoard(board.getBoard()); // Refresh UI with new board
    
        // Start the game and ensure movement works
        board.startGame();
    } 

    public void gameWon(Player winner, boolean isSimulation) {
        if (isSimulation) return;  // Don't trigger UI animations if the game ended in a bot simulation
    
        String winnerText = winner.getColor() == Piece.Color.WHITE ? "Black Wins!" : "White Wins!";
        
        JLabel winnerLabel = new JLabel(winnerText, SwingConstants.CENTER);
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 40));
        winnerLabel.setForeground(Color.RED);
        winnerLabel.setOpaque(true);
        winnerLabel.setBackground(new Color(255, 255, 255, 200));
    
        JPanel overlayPanel = new JPanel();
        overlayPanel.setLayout(new BorderLayout());
        overlayPanel.add(winnerLabel, BorderLayout.CENTER);
        overlayPanel.setOpaque(false);
    
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(boardPanel);
        frame.getLayeredPane().add(overlayPanel, JLayeredPane.POPUP_LAYER);
        overlayPanel.setBounds(0, 0, frame.getWidth(), frame.getHeight());
    
        startConfettiAnimation(frame);
    
        Timer restartTimer = new Timer(5000, e -> resetGame());
        restartTimer.setRepeats(false);
        restartTimer.start();
    }    

    private void startConfettiAnimation(JFrame frame) {
        JPanel confettiPanel = new JPanel() {
            List<Color> colors = List.of(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.PINK);
            List<int[]> confetti = new ArrayList<>();
            Random rand = new Random();
    
            {
                // Generate random confetti positions
                for (int i = 0; i < 100; i++) {
                    confetti.add(new int[]{rand.nextInt(frame.getWidth()), rand.nextInt(frame.getHeight()), rand.nextInt(10) + 5});
                }
    
                // Timer to update confetti movement
                Timer timer = new Timer(50, e -> {
                    for (int[] c : confetti) {
                        c[1] += rand.nextInt(5) + 2; // Move confetti down
                        if (c[1] > frame.getHeight()) {
                            c[1] = -10; // Reset to top when reaching bottom
                        }
                    }
                    repaint();
                });
                timer.start();
            }
    
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (int[] c : confetti) {
                    g.setColor(colors.get(rand.nextInt(colors.size())));
                    g.fillOval(c[0], c[1], c[2], c[2]);
                }
            }
        };
    
        confettiPanel.setOpaque(false);
        confettiPanel.setBounds(0, 0, frame.getWidth(), frame.getHeight());
    
        frame.getLayeredPane().add(confettiPanel, JLayeredPane.POPUP_LAYER);
    }
    
    private void showHelpDialog() {
        String rules = "Giveaway Chess Rules:\n" +
                "1. The goal is to lose all your pieces or be unable to move.\n" +
                "2. Capturing is mandatory if a capture move is available.\n" +
                "3. The king has no special significance; there is no check or checkmate.\n" +
                "4. Pawn promotion works as usual, but queening is often a disadvantage.\n" +
                "5. If a player has no legal moves, they win the game.";
        
        JOptionPane.showMessageDialog(null, rules, "Giveaway Chess Rules", JOptionPane.INFORMATION_MESSAGE);
    }

    private JLabel createAvatarLabel(String imagePath) {
        ImageIcon icon = new ImageIcon(imagePath);
        Image scaledImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        return new JLabel(new ImageIcon(scaledImage));
    }

    // Update dialogue message dynamically
    public void updateDialogue(String key) {
        if (messageOptions.containsKey(key)) {
            dialogueLabel.setText(messageOptions.get(key));
        } else {
            dialogueLabel.setText("Unknown action.");
        }
    }

    private void checkMandatoryCapture() {
        boolean hasMandatoryCapture = board.hasMandatoryCapture(gameManager.getCurrentPlayer().getColor(), board.getBoard());
        
        if (hasMandatoryCapture) {
            JOptionPane.showMessageDialog(null, "You have at least one mandatory capture!", "Move Check", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "No capture moves available!", "Move Check", JOptionPane.WARNING_MESSAGE);
        }
    }
}
