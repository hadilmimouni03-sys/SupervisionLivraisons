/**
 * Dashboard controller.
 *
 * Provides aggregate statistics requested in the spec:
 *   - delivery counts per driver and per state
 *   - delivery counts per client and per state
 */
const db = require('../config/database');

/**
 * GET /api/dashboard/by-driver
 * Optional query: dateFrom, dateTo
 */
async function byDriver(req, res) {
    const { dateFrom, dateTo } = req.query;
    const conds = [];
    const args  = [];
    if (dateFrom) { args.push(dateFrom); conds.push(`l.dateliv >= $${args.length}`); }
    if (dateTo)   { args.push(dateTo);   conds.push(`l.dateliv <= $${args.length}`); }
    const where = conds.length ? `WHERE ${conds.join(' AND ')}` : '';

    try {
        const sql = `
            SELECT p.idpers AS livreur_id,
                   (p.prenompers || ' ' || p.nompers) AS livreur_nom,
                   l.etatliv,
                   COUNT(*)::int AS nb
            FROM LivraisonCom l
            JOIN Personnel p ON p.idpers = l.livreur
            ${where}
            GROUP BY p.idpers, p.prenompers, p.nompers, l.etatliv
            ORDER BY livreur_nom, l.etatliv
        `;
        const r = await db.query(sql, args);
        res.json(r.rows);
    } catch (err) {
        console.error('[dashboard] byDriver error:', err);
        res.status(500).json({ error: 'Server error' });
    }
}

/**
 * GET /api/dashboard/by-client
 * Optional query: dateFrom, dateTo
 */
async function byClient(req, res) {
    const { dateFrom, dateTo } = req.query;
    const conds = [];
    const args  = [];
    if (dateFrom) { args.push(dateFrom); conds.push(`l.dateliv >= $${args.length}`); }
    if (dateTo)   { args.push(dateTo);   conds.push(`l.dateliv <= $${args.length}`); }
    const where = conds.length ? `WHERE ${conds.join(' AND ')}` : '';

    try {
        const sql = `
            SELECT cl.noclt,
                   (cl.prenomclt || ' ' || cl.nomclt) AS client_nom,
                   l.etatliv,
                   COUNT(*)::int AS nb
            FROM LivraisonCom l
            JOIN Commandes c ON c.nocde = l.nocde
            JOIN Clients  cl ON cl.noclt = c.noclt
            ${where}
            GROUP BY cl.noclt, cl.prenomclt, cl.nomclt, l.etatliv
            ORDER BY client_nom, l.etatliv
        `;
        const r = await db.query(sql, args);
        res.json(r.rows);
    } catch (err) {
        console.error('[dashboard] byClient error:', err);
        res.status(500).json({ error: 'Server error' });
    }
}

module.exports = { byDriver, byClient };
