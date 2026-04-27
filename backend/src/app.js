/**
 * Express application entry point.
 *
 * Wires up middleware, the /api router, and a Socket.IO server bound to the
 * same HTTP listener. The Socket.IO instance is exposed via app.set('io', io)
 * so controllers can push events without importing the singleton directly.
 */
require('dotenv').config();

const http    = require('http');
const express = require('express');
const cors    = require('cors');
const morgan  = require('morgan');

const routes        = require('./routes');
const socketService = require('./services/socketService');

const app = express();

app.use(cors());
app.use(express.json());
app.use(morgan('dev'));

app.get('/health', (_req, res) => res.json({ ok: true }));
app.use('/api', routes);

app.use((err, _req, res, _next) => {
    console.error('[app] unhandled error:', err);
    res.status(500).json({ error: 'Internal server error' });
});

const server = http.createServer(app);
const io     = socketService.attach(server);
app.set('io', io);

const PORT = parseInt(process.env.PORT || '3000', 10);
server.listen(PORT, () => {
    console.log(`[app] Supervision Livraisons API listening on :${PORT}`);
});
