document.addEventListener("DOMContentLoaded", function () {
    const board = document.querySelector(".chessboard");
    let selectedSquare = null; 
    let selectedPiece = null;
    let currentTurn = "white";

    const pieceSetup = [
        "rook", "knight", "bishop", "queen", "king", "bishop", "knight", "rook",
        "pawn", "pawn", "pawn", "pawn", "pawn", "pawn", "pawn", "pawn",
        "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "",
        "pawn", "pawn", "pawn", "pawn", "pawn", "pawn", "pawn", "pawn",
        "rook", "knight", "bishop", "queen", "king", "bishop", "knight", "rook"
    ];

    const isLiveServer = window.location.hostname === "127.0.0.1";  
    const basePath = isLiveServer ? "images" : "/images";
    

    for (let row = 0; row < 8; row++) {
        for (let col = 0; col < 8; col++) {
            const square = document.createElement("div");
            square.classList.add("square", (row + col) % 2 === 0 ? "light" : "dark");
            square.dataset.row = row;
            square.dataset.col = col;

            const index = row * 8 + col;
            if (pieceSetup[index]) {
                const img = document.createElement("img");
                const color = row < 2 ? "black" : row > 5 ? "white" : "";

                if (color) {
                    img.src = `${basePath}/${color}/${pieceSetup[index]}.png`;
                    img.alt = `${color} ${pieceSetup[index]}`;
                    img.classList.add("piece");
                    img.dataset.color = color;
                    img.dataset.type = pieceSetup[index];
                    img.onerror = () => console.error("Image not found:", img.src);
                    square.appendChild(img);
                }
            }

            square.addEventListener("click", () => {
                if (selectedSquare && selectedPiece) {
                    if (isValidMove(selectedSquare, square)) {
                        movePiece(selectedSquare, square);
                        currentTurn = currentTurn === "white" ? "black" : "white";
                    } else {
                        resetSelection();
                    }
                } else if (square.firstChild && square.firstChild.dataset.color === currentTurn) {
                    selectPiece(square);
                }
            });

            board.appendChild(square);
        }
        startGame();
    } 

    function startGame(playAgainstBot = false, botType = "RANDOM", playerColor = "WHITE") {
        fetch(`/chess/startGame?playAgainstBot=${playAgainstBot}&botType=${botType}&playerColor=${playerColor}`, {
            method: "POST"
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json(); // Convert response to JSON
        })
        .then(data => console.log("Game started:", data))
        .catch(error => console.error("Error starting game", error));
    }
    
    

    function selectPiece(square) {
        if (selectedSquare) {
            selectedSquare.classList.remove("selected");
        }
        selectedSquare = square;
        selectedPiece = square.firstChild;
        square.classList.add("selected");
    }

    function movePiece(fromSquare, toSquare) {
        if (toSquare.firstChild) {
            toSquare.removeChild(toSquare.firstChild); // Capture logic
        }
        toSquare.appendChild(selectedPiece);
        fromSquare.classList.remove("selected");
        resetSelection();
    } 

    function isValidMove(fromSquare, toSquare) {
        fetch('/chess/validMove', {
            method: 'GET',
            body: JSON.stringify({
                fromRow: parseInt(from[1]) - 1,
                fromCol: from.charCodeAt(0) - 97,
                toRow: parseInt(to[1]) - 1,
                toCol: to.charCodeAt(0) - 97
            }),
            headers: { 'Content-Type': 'application/json' }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error("Invalid move!"); // If move is illegal, throw an error
            }
            return response.json();
        })
        .then(data => updateBoard(data)) // Only update the board if move is valid
        .catch(error => alert(error.message)); // Show an error popup if move is illegal
    }

    function resetSelection() {
        selectedSquare = null;
        selectedPiece = null;
    }
});

// Send move to backend
function makeMove(from, to) {
    fetch('/chess/move', {
        method: 'GET',
        body: JSON.stringify({
            fromRow: parseInt(from[1]) - 1,
            fromCol: from.charCodeAt(0) - 97,
            toRow: parseInt(to[1]) - 1,
            toCol: to.charCodeAt(0) - 97
        }),
        headers: { 'Content-Type': 'application/json' }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Invalid move!"); // If move is illegal, throw an error
        }
        return response.json();
    })
    .then(data => updateBoard(data)) // Only update the board if move is valid
    .catch(error => alert(error.message)); // Show an error popup if move is illegal
}

function updateBoard(boardState) {
    const board = document.querySelector(".chessboard");
    board.innerHTML = ""; // Clear the previous board

    for (let row = 0; row < 8; row++) {
        for (let col = 0; col < 8; col++) {
            const square = document.createElement("div");
            square.classList.add("square", (row + col) % 2 === 0 ? "light" : "dark");
            square.dataset.row = row;
            square.dataset.col = col;

            const piece = boardState[row][col];
            if (piece) {
                const img = document.createElement("img");
                img.src = `images/${piece.color.toLowerCase()}/${piece.type.toLowerCase()}.png`;
                img.alt = `${piece.color} ${piece.type}`;
                img.classList.add("piece");
                img.dataset.color = piece.color.toLowerCase();
                img.dataset.type = piece.type.toLowerCase();
                square.appendChild(img);
            }

            // Add event listener for move handling
            square.addEventListener("click", () => handleSquareClick(square));

            board.appendChild(square);
        }
    }
}


// Fetch current board state from backend
function fetchBoardState() {
    fetch('/chess/state')
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json(); // Convert response to JSON
        })
        .then(data => updateBoard(data))
        .catch(error => console.error("Error fetching board state:", error));
}

function handleSquareClick(square) {
    if (selectedSquare && selectedPiece) {
        // Prevent moving if no piece was originally selected
        makeMove(
            selectedSquare.dataset.col + (parseInt(selectedSquare.dataset.row) + 1),
            square.dataset.col + (parseInt(square.dataset.row) + 1)
        );
        resetSelection();
    } else if (square.firstChild && square.firstChild.classList.contains("piece")) {
        // Select the piece if it's the current player's turn
        if (square.firstChild.dataset.color === currentTurn) {
            selectPiece(square);
        }
    }
}


// Restart game
document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("restartButton").addEventListener("click", function () {
        fetch('/chess/restart', { method: 'GET' })  
            .then(() => fetchBoardState())
            .catch(error => console.error("Error restarting game:", error));
    });
});
