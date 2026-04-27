/**
 * Delivery controller.
 *
 * Exposes the operations needed by the two user types:
 *
 *   Controller side:
 *     - {@link listDeliveries}   period + multi-criteria filters.
 *     - {@link todayDeliveries}  today's list with sort criteria.
 *
 *   Driver side:
 *     - {@link myTodayDeliveries} today's list for the authenticated driver.
 *     - {@link getDeliveryDetail} full details (client, address, items, total).
 *     - {@link updateDeliveryStatus} change etatliv (+ remarque if NON_LIVREE).
 */
const db = require('../config/database');

/* ------------------------------------------------------------------ */
/*  CONTROLLER ENDPOINTS                                              */
/* ------------------------------------------------------------------ */

/**
 * GET /api/deliveries
 * Query: dateFrom, dateTo, etat, livreur, noclt, nocde
 */
async function listDeliveries(req, res) {
    const { dateFrom, dateTo, etat, livreur, noclt, nocde } = req.query;
    const conds = [];
    const args  = [];

    if (dateFrom) { args.push(dateFrom); conds.push(`l.dateliv >= $${args.length}`); }
    if (dateTo)   { args.push(dateTo);   conds.push(`l.dateliv <= $${args.length}`); }
    if (etat)     { args.push(etat);     conds.push(`l.etatliv = $${args.length}`); }
    if (livreur)  { args.push(livreur);  conds.push(`l.livreur = $${args.length}`); }
    if (noclt)    { args.push(noclt);    conds.push(`c.noclt = $${args.length}`); }
    if (nocde)    { args.push(nocde);    conds.push(`l.nocde = $${args.length}`); }

    const where = conds.length ? `WHERE ${conds.join(' AND ')}` : '';

    try {
        const sql = `
            SELECT l.nocde, l.dateliv, l.etatliv, l.modepay, l.remarque,
                   p.idpers AS livreur_id,
                   (p.prenompers || ' ' || p.nompers) AS livreur_nom,
                   c.noclt, (cl.prenomclt || ' ' || cl.nomclt) AS client_nom,
                   cl.villeclt, cl.telclt,
                   COALESCE(SUM(lc.qtecde * a.prixV), 0) AS montant
            FROM LivraisonCom l
            JOIN Commandes c   ON c.nocde = l.nocde
            JOIN Clients  cl   ON cl.noclt = c.noclt
            JOIN Personnel p   ON p.idpers = l.livreur
            LEFT JOIN LigCdes lc ON lc.nocde = l.nocde
            LEFT JOIN Articles a ON a.refart = lc.refart
            ${where}
            GROUP BY l.nocde, l.dateliv, l.etatliv, l.modepay, l.remarque,
                     p.idpers, p.prenompers, p.nompers,
                     c.noclt, cl.prenomclt, cl.nomclt, cl.villeclt, cl.telclt
            ORDER BY l.dateliv DESC, l.nocde DESC
        `;
        const r = await db.query(sql, args);
        res.json(r.rows);
    } catch (err) {
        console.error('[delivery] list error:', err);
        res.status(500).json({ error: 'Server error' });
    }
}

/**
 * GET /api/deliveries/today
 * Query: sortBy = etatliv | livreur | client | nocde
 */
async function todayDeliveries(req, res) {
    const sortMap = {
        etatliv: 'l.etatliv',
        livreur: 'livreur_nom',
        client:  'client_nom',
        nocde:   'l.nocde',
    };
    const orderBy = sortMap[req.query.sortBy] || 'l.nocde';

    try {
        const sql = `
            SELECT l.nocde, l.dateliv, l.etatliv, l.modepay,
                   p.idpers AS livreur_id,
                   (p.prenompers || ' ' || p.nompers) AS livreur_nom,
                   c.noclt, (cl.prenomclt || ' ' || cl.nomclt) AS client_nom,
                   cl.villeclt, cl.telclt,
                   COALESCE(SUM(lc.qtecde * a.prixV), 0) AS montant
            FROM LivraisonCom l
            JOIN Commandes c ON c.nocde = l.nocde
            JOIN Clients  cl ON cl.noclt = c.noclt
            JOIN Personnel p ON p.idpers = l.livreur
            LEFT JOIN LigCdes lc ON lc.nocde = l.nocde
            LEFT JOIN Articles a ON a.refart = lc.refart
            WHERE l.dateliv = CURRENT_DATE
            GROUP BY l.nocde, l.dateliv, l.etatliv, l.modepay,
                     p.idpers, p.prenompers, p.nompers,
                     c.noclt, cl.prenomclt, cl.nomclt, cl.villeclt, cl.telclt
            ORDER BY ${orderBy} ASC
        `;
        const r = await db.query(sql);
        res.json(r.rows);
    } catch (err) {
        console.error('[delivery] today error:', err);
        res.status(500).json({ error: 'Server error' });
    }
}

/* ------------------------------------------------------------------ */
/*  DRIVER ENDPOINTS                                                  */
/* ------------------------------------------------------------------ */

/**
 * GET /api/deliveries/my-today
 * Driver-only. Returns today's deliveries for req.user.id ordered by nocde.
 */
async function myTodayDeliveries(req, res) {
    try {
        const sql = `
            SELECT l.nocde, l.dateliv, l.etatliv, l.modepay,
                   c.noclt, (cl.prenomclt || ' ' || cl.nomclt) AS client_nom,
                   cl.telclt, cl.villeclt,
                   ROW_NUMBER() OVER (ORDER BY l.nocde ASC) AS ordre
            FROM LivraisonCom l
            JOIN Commandes c ON c.nocde = l.nocde
            JOIN Clients  cl ON cl.noclt = c.noclt
            WHERE l.dateliv = CURRENT_DATE AND l.livreur = $1
            ORDER BY l.nocde ASC
        `;
        const r = await db.query(sql, [req.user.id]);
        res.json(r.rows);
    } catch (err) {
        console.error('[delivery] my-today error:', err);
        res.status(500).json({ error: 'Server error' });
    }
}

/** GET /api/deliveries/:nocde */
async function getDeliveryDetail(req, res) {
    const { nocde } = req.params;
    try {
        const head = await db.query(
            `SELECT l.nocde, l.dateliv, l.etatliv, l.modepay, l.remarque,
                    l.livreur AS livreur_id,
                    cl.noclt, cl.nomclt, cl.prenomclt, cl.adrclt, cl.villeclt,
                    cl.code_postal, cl.telclt, cl.adrmail
             FROM LivraisonCom l
             JOIN Commandes c ON c.nocde = l.nocde
             JOIN Clients  cl ON cl.noclt = c.noclt
             WHERE l.nocde = $1`,
            [nocde]
        );
        if (head.rowCount === 0) return res.status(404).json({ error: 'Not found' });

        const lines = await db.query(
            `SELECT lc.refart, a.designation, lc.qtecde, a.prixV,
                    (lc.qtecde * a.prixV) AS sous_total
             FROM LigCdes lc
             JOIN Articles a ON a.refart = lc.refart
             WHERE lc.nocde = $1`,
            [nocde]
        );

        const totals = lines.rows.reduce(
            (acc, l) => ({
                articles: acc.articles + parseInt(l.qtecde, 10),
                montant:  acc.montant  + parseFloat(l.sous_total),
            }),
            { articles: 0, montant: 0 }
        );

        const adresse = `${head.rows[0].adrclt || ''}, ${head.rows[0].villeclt || ''}`;
        const mapsUrl = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(adresse)}`;

        res.json({
            ...head.rows[0],
            articles: lines.rows,
            nb_articles: totals.articles,
            montant: totals.montant,
            maps_url: mapsUrl,
        });
    } catch (err) {
        console.error('[delivery] detail error:', err);
        res.status(500).json({ error: 'Server error' });
    }
}

/**
 * PATCH /api/deliveries/:nocde/status
 * Body: { etatliv, remarque? }
 *
 * remarque is required when etatliv === 'NON_LIVREE'.
 */
async function updateDeliveryStatus(req, res) {
    const { nocde } = req.params;
    const { etatliv, remarque } = req.body || {};

    const allowed = ['EN_ATTENTE', 'EN_COURS', 'LIVREE', 'NON_LIVREE'];
    if (!allowed.includes(etatliv)) {
        return res.status(400).json({ error: 'Invalid etatliv' });
    }
    if (etatliv === 'NON_LIVREE' && !remarque) {
        return res.status(400).json({ error: 'remarque is required for NON_LIVREE' });
    }

    try {
        // Drivers can only update their own deliveries.
        if (req.user.role === 'Livreur') {
            const own = await db.query(
                'SELECT 1 FROM LivraisonCom WHERE nocde = $1 AND livreur = $2',
                [nocde, req.user.id]
            );
            if (own.rowCount === 0) return res.status(403).json({ error: 'Not your delivery' });
        }

        const r = await db.query(
            `UPDATE LivraisonCom
             SET etatliv = $1, remarque = COALESCE($2, remarque)
             WHERE nocde = $3
             RETURNING nocde, etatliv, remarque`,
            [etatliv, remarque || null, nocde]
        );
        if (r.rowCount === 0) return res.status(404).json({ error: 'Not found' });

        // Notify connected controllers in real time.
        const io = req.app.get('io');
        if (io) io.to('role:Controleur').emit('delivery:updated', r.rows[0]);

        res.json(r.rows[0]);
    } catch (err) {
        console.error('[delivery] update error:', err);
        res.status(500).json({ error: 'Server error' });
    }
}

module.exports = {
    listDeliveries,
    todayDeliveries,
    myTodayDeliveries,
    getDeliveryDetail,
    updateDeliveryStatus,
};
