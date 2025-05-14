const express = require('express');
const cors = require('cors');
const { ZingMp3 } = require("zingmp3-api-full");

const app = express();
const port = 8088;

// Middleware
app.use(cors());
app.use(express.json());

// Error handler middleware
const errorHandler = (err, req, res, next) => {
    console.error(err.stack);
    res.status(500).json({ 
        error: 'Something went wrong!',
        message: err.message 
    });
};

// Routes

// Get Song Info
app.get('/api/song/:id', async (req, res, next) => {
    try {
        const data = await ZingMp3.getInfoSong(req.params.id);
        res.json(data);
    } catch (error) {
        next(error);
    }
});

// Get Song Streaming URL
app.get('/api/song/streamUrl/:id', async (req, res, next) => {
    try {
        const data = await ZingMp3.getSong(req.params.id);
        res.json(data);
    } catch (error) {
        next(error);
    }
});

// Search
app.get('/api/search', async (req, res, next) => {
    try {
        const { keyword } = req.query;
        if (!keyword) {
            return res.status(400).json({ error: 'Keyword is required' });
        }
        const data = await ZingMp3.search(keyword);
        res.json(data);
    } catch (error) {
        next(error);
    }
});

// Get Home Data
app.get('/api/home', async (req, res, next) => {
    try {
        const data = await ZingMp3.getHome();
        res.json(data);
    } catch (error) {
        next(error);
    }
});

// Get Chart Home
app.get('/api/chart-home', async (req, res, next) => {
    try {
        const data = await ZingMp3.getChartHome();
        res.json(data);
    } catch (error) {
        next(error);
    }
});

// Get Top 100
app.get('/api/top100', async (req, res, next) => {
    try {
        const data = await ZingMp3.getTop100();
        res.json(data);
    } catch (error) {
        next(error);
    }
});

// Get Playlist Details
app.get('/api/playlist/:id', async (req, res, next) => {
    try {
        const data = await ZingMp3.getDetailPlaylist(req.params.id);
        res.json(data);
    } catch (error) {
        next(error);
    }
});

// Get Artist Details
app.get('/api/artist/:name', async (req, res, next) => {
    try {
        const data = await ZingMp3.getArtist(req.params.name);
        res.json(data);
    } catch (error) {
        next(error);
    }
});

// Get Song Lyrics
app.get('/api/song/lyric/:id', async (req, res, next) => {
    try {
        const data = await ZingMp3.getLyric(req.params.id);
        res.json(data);
    } catch (error) {
        next(error);
    }
});

// Get List MV
app.get('/api/mv/list', async (req, res, next) => {
    try {
        const { id, page = 1, count = 15 } = req.query;
        const data = await ZingMp3.getListMV(id, page, count);
        res.json(data);
    } catch (error) {
        next(error);
    }
});

// Get Category MV
app.get('/api/mv/category', async (req, res, next) => {
    try {
        const { id } = req.query;
        const data = await ZingMp3.getCategoryMV(id);
        res.json(data);
    } catch (error) {
        next(error);
    }
});

// Get MV Info
app.get('/api/mv/:id', async (req, res, next) => {
    try {
        const data = await ZingMp3.getVideoMV(req.params.id);
        res.json(data);
    } catch (error) {
        next(error);
    }
});

// Register error handler
app.use(errorHandler);

// Start server
app.listen(port, () => {
    console.log(`ZingMP3 API Server is running on port ${port}`);
});
