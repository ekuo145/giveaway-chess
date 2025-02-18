const express = require('express');
const app = express();
const port = 5500;

app.use(express.static('public')); // Serve static frontend files from 'public' directory

app.get('/', (req, res) => {
  res.sendFile(__dirname + '/public/index.html'); // Serve the main HTML file
});

app.listen(port, () => {
  console.log(`Server is running on http://localhost:${port}`);
});
