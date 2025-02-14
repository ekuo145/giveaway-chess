const express = require('express');
const app = express();
const port = 5500;

app.get('/', (req, res) => {
  res.send('Hello World!');
});

app.listen(port, () => {
  console.log(`Server is running on http://localhost:${port}`);
});

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
    const basePath = isLiveServer ? "images" : "/giveaway-chess Maven Webapp/images";

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
    } 

    function updateBoard(boardState) {
    const board = document.querySelector(".chessboard");
    board.innerHTML = ""; // Clear previous board

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

            board.appendChild(square);
        }
    }
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
        if (toSquare.firstChild && toSquare.firstChild.dataset.color === selectedPiece.dataset.color) {
            return false;
        }
        return true; // Basic move validation for now
    }

    function resetSelection() {
        selectedSquare = null;
        selectedPiece = null;
    }
});


function makeMove(from, to) {
    fetch('/chess/move', {
        method: 'POST',
        body: JSON.stringify({
            fromRow: parseInt(from[1]) - 1,
            fromCol: from.charCodeAt(0) - 97, // Convert 'a'->0, 'b'->1, etc.
            toRow: parseInt(to[1]) - 1,
            toCol: to.charCodeAt(0) - 97
        }),
        headers: { 'Content-Type': 'application/json' }
    })
    .then(response => response.json())
    .then(data => updateBoard(data))
    .catch(error => console.error("Move error:", error));
}

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



// Restart game
document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("restartButton").addEventListener("click", function () {
        fetch('/chess/restart', { method: 'GET' })  
            .then(() => fetchBoardState())
            .catch(error => console.error("Error restarting game:", error));
    });
});


