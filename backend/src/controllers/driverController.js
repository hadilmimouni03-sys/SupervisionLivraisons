/**
 * Driver and Client lookup controller.
 * Used by the Controller dashboard to populate filter dropdowns.
 */
const db = require('../config/database');

/** GET /api/drivers */
async function listDrivers(req, res) {
    try {
        const r = await db.query(
            `SELECT p.idpers AS id, (p.prenompers || ' ' || p.nompers) AS nom
             FROM Personnel p
             JOIN Postes po ON po.codeposte = p.codeposte
             WHERE po.libelle = 'Livreur'
             ORDER BY p.nompers`
        );
        res.json(r.rows);
    } catch (err) {
        console.error('[driver] list error:', err);
        res.status(500).json({ error: 'Server error' });
    }
}

/** GET /api/clients */
async function listClients(req, res) {
    try {
        const r = await db.query(
            `SELECT noclt AS id, (prenomclt || ' ' || nomclt) AS nom
             FROM Clients ORDER BY nomclt`
        );
        res.json(r.rows);
    } catch (err) {
        console.error('[client] list error:', err);
        res.status(500).json({ error: 'Server error' });
    }
}

module.exports = { listDrivers, listClients };
