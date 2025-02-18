import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.giveawaychess.*;
import com.giveawaychess.MoveRequest;

@RestController
@RequestMapping("/chess")
@CrossOrigin(origins = "*") // Allow frontend access
public class ChessController {
    private final ChessBoard chessBoard = new ChessBoard(null); // No UI needed for API

    @PostMapping("/move")
    public ResponseEntity<?> makeMove(@RequestBody MoveRequest move) {
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        Move gameMove = new Move(fromRow, fromCol, toRow, toCol, chessBoard.getPieceAt(fromRow, fromCol));
        boolean moveSuccessful = chessBoard.handleMove(gameMove, new GameManager(null, null));

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
    
    @GetMapping("/restart")
public ResponseEntity<Void> restartGame() {
    chessBoard.setUpPieces();
    return ResponseEntity.ok().build();
}
}
