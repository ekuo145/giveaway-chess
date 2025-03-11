import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.giveawaychess.*;
import com.giveawaychess.BotLogic.BotType;

@RestController
@RequestMapping("/chess")
@CrossOrigin(origins = "*") // Allow frontend access
public class ChessController {
    private final ChessBoard chessBoard = new ChessBoard(null); // No UI needed for API
    private GameAntichess game;

    @GetMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}
   
    @PostMapping("/move")
    public ResponseEntity<?> makeMove(@RequestBody MoveRequest move) {
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        Move gameMove = new Move(fromRow, fromCol, toRow, toCol, chessBoard.getPieceAt(fromRow, fromCol));
        boolean moveSuccessful = chessBoard.handleMove(gameMove, new GameManager());

        if (moveSuccessful) {
            return ResponseEntity.ok(chessBoard.getBoard());  // ✅ Move was valid, return updated board
        } else {
            return ResponseEntity.badRequest().body("Invalid move");  // ❌ Move was illegal
        }
    }

    // Get current board state
    @GetMapping("/state")
    public ResponseEntity<Piece[][]> getBoardState() {
        return ResponseEntity.ok(chessBoard.getBoard());
    }

    @PostMapping("/startGame")
    public String startGame(@RequestParam(required = false, defaultValue = "false") boolean playAgainstBot,
                            @RequestParam(required = false, defaultValue = "RANDOM") String botType,
                            @RequestParam(required = false, defaultValue = "WHITE") String playerColor) {
        
        // Initialize game with given parameters
        BotType selectedBotType = BotType.valueOf(botType.toUpperCase());
        Piece.Color color = Piece.Color.valueOf(playerColor.toUpperCase());

        game = new GameAntichess(playAgainstBot, selectedBotType, color);
        
        return "{\"message\": \"Game started successfully\"}";
    }
    
    @GetMapping("/restart")
public ResponseEntity<Void> restartGame() {
    chessBoard.setUpPieces();
    return ResponseEntity.ok().build();
}

    @GetMapping("/validMove")
    public ResponseEntity<?> isValidMove(@RequestBody MoveRequest move) {
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();
        boolean moveSuccessful = chessBoard.isValidMove(fromRow, fromCol, toRow, toCol);

        if (moveSuccessful) {
            return ResponseEntity.ok(chessBoard.getBoard());  // ✅ Move was valid, return updated board
        } else {
            return ResponseEntity.badRequest().body("Invalid move");  // ❌ Move was illegal
        }
    }
}
