/**
 * Socket.IO bootstrap.
 *
 * Authenticates the socket handshake with the same JWT used for HTTP, then
 * subscribes the socket to two rooms:
 *   - role:<Controleur|Livreur>   for role-wide broadcasts
 *   - user:<idpers>               for direct messages
 */
const { Server } = require('socket.io');
const jwt        = require('jsonwebtoken');

function attach(httpServer) {
    const io = new Server(httpServer, { cors: { origin: '*' } });

    io.use((socket, next) => {
        const token = socket.handshake.auth?.token;
        if (!token) return next(new Error('Missing token'));
        try {
            socket.user = jwt.verify(token, process.env.JWT_SECRET || 'dev_secret');
            next();
        } catch (e) {
            next(new Error('Invalid token'));
        }
    });

    io.on('connection', (socket) => {
        const { id, role } = socket.user;
        socket.join(`user:${id}`);
        socket.join(`role:${role}`);
        console.log(`[socket] connected: user=${id} role=${role}`);

        socket.on('disconnect', () => {
            console.log(`[socket] disconnected: user=${id}`);
        });
    });

    return io;
}

module.exports = { attach };
