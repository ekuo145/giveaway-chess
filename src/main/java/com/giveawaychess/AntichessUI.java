package com.giveawaychess;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.giveawaychess.BotLogic.BotType;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private JLabel botDialogueLabel;

    Player whitePlayer;
    Player blackPlayer;
    
    private ChessBoard board;
    private GameManager gameManager;

    private BotType botType;
    private Piece.Color pieceColor;

    public enum DialogueKey {
        MOVE("Your move!"),
        CAPTURE("I'll take that."),
        VICTORY("GG!"),
        DEFEAT("You got me."),
        NEUTRAL("Hmm..."),
        THINKING("The bot is thinking...");
    
        private final String message;
    
        DialogueKey(String message) {
            this.message = message;
        }
    
        public String getMessage() {
            return message;
        }

        public static String getBotMessage(BotLogic.BotType bot, DialogueKey key) {
            return switch (bot) {
                case RANDOM -> switch (key) {
                    case THINKING -> "Uhh... this one?";
                    case CAPTURE -> "Aww man, I had to take that?";
                    case VICTORY -> "Wait... I won?!";
                    case DEFEAT -> "I was just messing around...";
                    case NEUTRAL -> "This looks fun!";
                    default -> throw new IllegalArgumentException("Unexpected value: " + key);
                };
                case AGGRESSIVE -> switch (key) {
                    case THINKING -> "Calculating your doom...";
                    case CAPTURE -> "I hate giving in to your bait...";
                    case VICTORY -> "Check... and eliminated.";
                    case DEFEAT -> "Tch. Just a misstep.";
                    case NEUTRAL -> "You're delaying the inevitable.";
                    default -> throw new IllegalArgumentException("Unexpected value: " + key);
                };
                case DEFENSIVE -> switch (key) {
                    case THINKING -> "Patience is a virtue...";
                    case CAPTURE -> "That was... unfortunate.";
                    case VICTORY -> "Balance is restored.";
                    case DEFEAT -> "Hmm. I'll learn from this.";
                    case NEUTRAL -> "Interesting tension...";
                    default -> throw new IllegalArgumentException("Unexpected value: " + key);
                };
                case SACRIFICIAL -> switch (key) {
                    case THINKING -> "What shall I give away next?";
                    case CAPTURE -> "Why am I the one taking things?!";
                    case VICTORY -> "I sacrificed everything... and won!";
                    case DEFEAT -> "I gave too much.";
                    case NEUTRAL -> "To give is to gain!";
                    default -> throw new IllegalArgumentException("Unexpected value: " + key);
                };
                case SWEATY -> switch (key) {
                    case THINKING -> "Optimizing sequence...";
                    case CAPTURE -> "That wasn't part of the plan...";
                    case VICTORY -> "Efficiency wins.";
                    case DEFEAT -> "Unacceptable. Logging blunder.";
                    case NEUTRAL -> "Input received.";
                    default -> throw new IllegalArgumentException("Unexpected value: " + key);
                };
                default -> throw new IllegalArgumentException("Unexpected value: " + bot);
            };
        }
    }

    // Constructor to set up the UI
    public AntichessUI() {
        this.gameManager = new GameManager();
        this.board = new ChessBoard(this, gameManager);
    
        // Ask game mode
        String[] options = {"Play against Bot", "Play against Human"};
        int choice = JOptionPane.showOptionDialog(null, "Choose your opponent:", "Game Setup",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    
        if (choice == 0) { // ✅ BOT GAME
            isBotGame = true;
    
            // 1. Choose bot
            showBotSelectionDialog(); // 👈 pops up the avatar/info dialog
    
            // 2. Choose color
            String[] botColorOptions = {"Play as White", "Play as Black"};
            int colorChoice = JOptionPane.showOptionDialog(null, "Choose your side:", "Color Selection",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, botColorOptions, botColorOptions[0]);
    
            pieceColor = (colorChoice == 0) ? Piece.Color.WHITE : Piece.Color.BLACK;
    
            if (pieceColor == Piece.Color.WHITE) {
                whitePlayer = new Player(Piece.Color.WHITE, false, board, null, gameManager);
                blackPlayer = new Player(Piece.Color.BLACK, true, board, botType, gameManager);
                blackPlayer.setUI(this);
            } else {
                whitePlayer = new Player(Piece.Color.WHITE, true, board, botType, gameManager);
                blackPlayer = new Player(Piece.Color.BLACK, false, board, null, gameManager);
                whitePlayer.setUI(this);
            }
    
        } else { // 👥 HUMAN vs HUMAN
            isBotGame = false;
            pieceColor = Piece.Color.WHITE; // Default UI color
            whitePlayer = new Player(Piece.Color.WHITE, false, board, null, gameManager);
            blackPlayer = new Player(Piece.Color.BLACK, false, board, null, gameManager);
        }
    
        // ✅ Init UI after logic is set
        initializeUI();
        updateBoard(board.getBoardArray());
    
        if (pieceColor == Piece.Color.BLACK) {
            flipBoard(); // Flip if playing black
        }
    
        gameManager.setPlayers(whitePlayer, blackPlayer);
        whitePlayer.setGameManager(gameManager);
        blackPlayer.setGameManager(gameManager);
        board.startGame();
    
        // Let bot move first if playing black
        if (whitePlayer.isBot()) {
            whitePlayer.makeBotMove(board.getBoardArray());
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


        JPanel topPanel = new JPanel(new BorderLayout());

        if (isBotGame) {
            String botDisplayName = switch (botType) {
                case RANDOM -> "Randy (800)";
                case AGGRESSIVE -> "Darwin (1200)";
                case DEFENSIVE -> "Virgil (1000)";
                case SACRIFICIAL -> "Levi (1100)";
                case SWEATY -> "Mark (1600)";
                default -> throw new IllegalArgumentException("Unexpected value: " + botType);
            };
        
            ImageIcon avatarIcon = new ImageIcon(getClass().getResource("/images/profiles/crackedChess.png")); // update path accordingly
            Image scaled = avatarIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            JLabel avatar = new JLabel(new ImageIcon(scaled));
            avatar.setPreferredSize(new Dimension(50, 50));
            avatar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
            JLabel botName = new JLabel(botDisplayName);
            botName.setFont(new Font("SansSerif", Font.BOLD, 16));
            botName.setForeground(Color.WHITE);
        
            botDialogueLabel = new JLabel("The bot is thinking...");
            botDialogueLabel.setForeground(Color.LIGHT_GRAY);
            botDialogueLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        
            JPanel botTextPanel = new JPanel();
            botTextPanel.setLayout(new BoxLayout(botTextPanel, BoxLayout.Y_AXIS));
            botTextPanel.setBackground(new Color(50, 50, 50));
            botTextPanel.add(botName);
            botTextPanel.add(botDialogueLabel);
        
            JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            botPanel.setBackground(new Color(50, 50, 50));
            botPanel.add(avatar);
            botPanel.add(botTextPanel);
        
            topPanel.add(botPanel, BorderLayout.WEST);
        }
        


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
        restartButton.addActionListener(e -> restartGame());

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
            int modelRow = 7 - row;  // Flip row index so white is at bottom
            // Add row label on the left side
            JLabel rowLabel = new JLabel(Integer.toString(8 - row), SwingConstants.CENTER); // Row label (reverse order)
            rowLabel.setPreferredSize(new Dimension(80, 80)); // Adjust size to match the board buttons
            boardPanel.add(rowLabel);

            for (int col = 0; col < 8; col++) {
                int modelCol = col;
                JButton button = new JButton();
                boardButtons[modelRow][modelCol] = button; // match internal position

                if ((modelRow + modelCol) % 2 == 0) {
                    button.setBackground(Color.GRAY);
                } else {
                    button.setBackground(Color.WHITE);
                }

                button.setOpaque(true);
                button.setBorderPainted(false);
                button.setPreferredSize(new Dimension(80, 80)); // Make sure the buttons are square and compact

                // Add action listener to each button to handle user clicks
                final int currentRow = modelRow;
                final int currentCol = modelCol;
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
            // System.out.println("Piece String: " + movingPiece.toString());
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

    private void flipBoard() {
        isBoardFlipped = !isBoardFlipped;
        boardPanel.removeAll(); // Clear the boardPanel for re-layout
    
        // Add top-left corner empty
        boardPanel.add(new JLabel(""));
    
        char[] columns = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
    
        // Flip column labels to match orientation
        for (int i = 0; i < 8; i++) {
            int colIndex = isBoardFlipped ? 7 - i : i;
            JLabel colLabel = new JLabel(Character.toString(columns[colIndex]), SwingConstants.CENTER);
            colLabel.setPreferredSize(new Dimension(80, 80));
            boardPanel.add(colLabel);
        }
    
        // Add board squares
        for (int row = 0; row < 8; row++) {
            int displayRow = isBoardFlipped ? row : 7 - row;
            int labelRow = isBoardFlipped ? row + 1 : 8 - row;
            JLabel rowLabel = new JLabel(Integer.toString(labelRow), SwingConstants.CENTER);
            rowLabel.setPreferredSize(new Dimension(80, 80));
            boardPanel.add(rowLabel);
    
            for (int col = 0; col < 8; col++) {
                int displayCol = isBoardFlipped ? 7 - col : col;
                boardPanel.add(boardButtons[displayRow][displayCol]);
            }
        }
    
        resetBoardColors();
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
        gameManager.getCurrentPlayer().switchTurn();
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
    
        Timer restartTimer = new Timer(5000, e -> restartGame());
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
        URL imageUrl = getClass().getResource(imagePath);
        if (imageUrl == null) {
            System.err.println("Avatar image not found: " + imagePath);
            return new JLabel();
        }
        ImageIcon icon = new ImageIcon(imageUrl);
        Image scaledImage = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        JLabel label = new JLabel(new ImageIcon(scaledImage));
        label.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        return label;
    }

    // Update dialogue message dynamically
    public void updateDialogue(DialogueKey key) {
        if (isBotGame && botDialogueLabel != null) {
            botDialogueLabel.setText(key.getMessage());
        }
    }    
    
    public void updateBotDialogue(String message) {
        if (isBotGame && botDialogueLabel != null) {
            botDialogueLabel.setText(message);
        }
    }

    private void checkMandatoryCapture() {
        boolean hasMandatoryCapture = board.hasMandatoryCapture(gameManager.getCurrentPlayer().getColor(), board.getBoard());
    
        if (hasMandatoryCapture) {
            highlightMandatoryCaptures();
            JOptionPane.showMessageDialog(null, "You have at least one mandatory capture! Red squares show capture options.", "Move Check", JOptionPane.INFORMATION_MESSAGE);
        } else {
            resetBoardColors();
            JOptionPane.showMessageDialog(null, "No capture moves available!", "Move Check", JOptionPane.WARNING_MESSAGE);
        }
    }
    

    public void highlightMandatoryCaptures() {
        resetBoardColors();
        Piece.Color currentColor = gameManager.getCurrentPlayer().getColor();
        Piece[][] currentBoard = board.getBoard();
    
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = currentBoard[row][col];
                if (piece != null && piece.getColor() == currentColor) {
                    List<int[]> validMoves = board.getValidMoves(row, col);
                    for (int[] move : validMoves) {
                        Piece target = currentBoard[move[0]][move[1]];
                        if (target != null && target.getColor() != currentColor) {
                            boardButtons[move[0]][move[1]].setBackground(Color.RED);
                            boardButtons[row][col].setBackground(new Color(255, 100, 100)); // light red for source
                        }
                    }
                }
            }
        }
    }
    
    public JPanel getBoardPanel() {
        return boardPanel;
    }
    
    private void showBotSelectionDialog() {
        JDialog botDialog = new JDialog((Frame) null, "Choose a Bot", true);
        botDialog.setLayout(new BorderLayout());
    
        String[] botNames = {"Mark", "Levi", "Virgil", "Darwin", "Randy"};
        BotLogic.BotType[] botTypes = {
            BotLogic.BotType.SWEATY,
            BotLogic.BotType.SACRIFICIAL,
            BotLogic.BotType.DEFENSIVE,
            BotLogic.BotType.AGGRESSIVE,
            BotLogic.BotType.RANDOM
        };
        String[] botDescriptions = {
            "🤖 Mark (⭐⭐⭐⭐⭐)\n\nCalculated, optimal, and serious. Tryhard mode: ON.",
            "🎭 Levi (⭐⭐)\n\nEmbraces chaos. Sacrifices recklessly, but cleverly.",
            "🛡️ Virgil (⭐⭐⭐)\n\nCareful and patient. Avoids risky trades.",
            "🔥 Darwin (⭐⭐⭐⭐)\n\nRuthless attacker. Will give anything to escape fast.",
            "🃏 Randy (⭐)\n\nPlays completely randomly — no logic, just vibes."
        };
        String[] avatarPaths = {
            "/images/profiles/mark.png",
            "/images/profiles/levi.png",
            "/images/profiles/virgil.png",
            "/images/profiles/darwin.png",
            "/images/profiles/randy.png"
        };
    
        JPanel listPanel = new JPanel(new GridLayout(botNames.length, 1, 10, 10));
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
    
        JLabel avatarPreview = new JLabel();
        avatarPreview.setHorizontalAlignment(SwingConstants.CENTER);
        avatarPreview.setPreferredSize(new Dimension(100, 100));
    
        JTextArea infoBox = new JTextArea();
        infoBox.setEditable(false);
        infoBox.setWrapStyleWord(true);
        infoBox.setLineWrap(true);
        infoBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        infoBox.setMargin(new Insets(10, 10, 10, 10));
    
        JButton playButton = new JButton("Play");
        playButton.setEnabled(false);
    
        final BotLogic.BotType[] selected = {null};
    
        for (int i = 0; i < botNames.length; i++) {
            String name = botNames[i];
            BotLogic.BotType type = botTypes[i];
            String description = botDescriptions[i];
            String avatarPath = avatarPaths[i];
    
            JButton botButton = new JButton(name);
            botButton.setHorizontalAlignment(SwingConstants.LEFT);
            botButton.addActionListener(e -> {
                selected[0] = type;
                playButton.setEnabled(true);
                infoBox.setText(description);
    
                ImageIcon icon = new ImageIcon(getClass().getResource(avatarPath));
                Image scaled = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                avatarPreview.setIcon(new ImageIcon(scaled));
            });
            listPanel.add(botButton);
        }
    
        rightPanel.add(avatarPreview, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(infoBox), BorderLayout.CENTER);
    
        playButton.addActionListener(e -> {
            if (selected[0] != null) {
                botType = selected[0];
                botDialog.dispose();
            }
        });
    
        botDialog.add(listPanel, BorderLayout.WEST);
        botDialog.add(rightPanel, BorderLayout.CENTER);
        botDialog.add(playButton, BorderLayout.SOUTH);
    
        botDialog.setSize(600, 350);
        botDialog.setLocationRelativeTo(null);
        botDialog.setVisible(true);
    }
    
}
