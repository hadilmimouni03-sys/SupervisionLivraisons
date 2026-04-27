/**
 * Messaging controller.
 *
 * Two flows are supported:
 *   - Controller -> Driver(s): info messages (broadcast or targeted).
 *   - Driver    -> Controllers: emergency messages tied to a specific order.
 *
 * Messages are persisted in the Messages table AND pushed in real time
 * through Socket.IO rooms (see services/socketService.js).
 */
const db = require('../config/database');

/** POST /api/messages/info  (Controller -> Driver) */
async function sendInfo(req, res) {
    const { receiver_id, content } = req.body || {};
    if (!content) return res.status(400).json({ error: 'content is required' });

    try {
        const r = await db.query(
            `INSERT INTO Messages (sender_id, receiver_id, type, content)
             VALUES ($1, $2, 'INFO', $3)
             RETURNING id, sender_id, receiver_id, type, content, created_at`,
            [req.user.id, receiver_id || null, content]
        );

        const msg = r.rows[0];
        const io  = req.app.get('io');
        if (io) {
            if (receiver_id) io.to(`user:${receiver_id}`).emit('message:new', msg);
            else             io.to('role:Livreur').emit('message:new', msg);
        }
        res.status(201).json(msg);
    } catch (err) {
        console.error('[message] sendInfo error:', err);
        res.status(500).json({ error: 'Server error' });
    }
}

/** POST /api/messages/urgence  (Driver -> Controllers) */
async function sendUrgence(req, res) {
    const { nocde, content } = req.body || {};
    if (!nocde || !content) {
        return res.status(400).json({ error: 'nocde and content are required' });
    }

    try {
        // Enrich the message with the client contact, as required by the spec.
        const ctx = await db.query(
            `SELECT cl.telclt, (cl.prenomclt || ' ' || cl.nomclt) AS client_nom
             FROM Commandes c JOIN Clients cl ON cl.noclt = c.noclt
             WHERE c.nocde = $1`,
            [nocde]
        );
        const clientInfo = ctx.rows[0]
            ? ` [Client ${ctx.rows[0].client_nom} - ${ctx.rows[0].telclt}]`
            : '';

        const r = await db.query(
            `INSERT INTO Messages (sender_id, nocde, type, content)
             VALUES ($1, $2, 'URGENCE', $3)
             RETURNING id, sender_id, nocde, type, content, created_at`,
            [req.user.id, nocde, content + clientInfo]
        );
        const msg = r.rows[0];

        const io = req.app.get('io');
        if (io) io.to('role:Controleur').emit('message:new', msg);

        res.status(201).json(msg);
    } catch (err) {
        console.error('[message] sendUrgence error:', err);
        res.status(500).json({ error: 'Server error' });
    }
}

/** GET /api/messages  (inbox of authenticated user) */
async function inbox(req, res) {
    try {
        const sql = req.user.role === 'Controleur'
            ? `SELECT m.*, (p.prenompers || ' ' || p.nompers) AS sender_nom
               FROM Messages m
               JOIN Personnel p ON p.idpers = m.sender_id
               WHERE m.type = 'URGENCE' OR m.receiver_id = $1
               ORDER BY m.created_at DESC LIMIT 100`
            : `SELECT m.*, (p.prenompers || ' ' || p.nompers) AS sender_nom
               FROM Messages m
               JOIN Personnel p ON p.idpers = m.sender_id
               WHERE m.type = 'INFO'
                 AND (m.receiver_id IS NULL OR m.receiver_id = $1)
               ORDER BY m.created_at DESC LIMIT 100`;
        const r = await db.query(sql, [req.user.id]);
        res.json(r.rows);
    } catch (err) {
        console.error('[message] inbox error:', err);
        res.status(500).json({ error: 'Server error' });
    }
}

module.exports = { sendInfo, sendUrgence, inbox };
