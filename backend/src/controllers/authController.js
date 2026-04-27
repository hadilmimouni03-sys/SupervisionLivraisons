/**
 * Authentication controller.
 *
 * Verifies Personnel credentials against the DB and returns a JWT
 * containing the user's id, login and role label (Postes.libelle).
 */
const bcrypt = require('bcryptjs');
const jwt    = require('jsonwebtoken');
const db     = require('../config/database');

async function login(req, res) {
    const { login, password } = req.body || {};
    if (!login || !password) {
        return res.status(400).json({ error: 'login and password are required' });
    }

    try {
        const result = await db.query(
            `SELECT p.idpers, p.nompers, p.prenompers, p.login, p.motp,
                    po.libelle AS role
             FROM Personnel p
             LEFT JOIN Postes po ON po.codeposte = p.codeposte
             WHERE p.login = $1`,
            [login]
        );

        if (result.rowCount === 0) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }

        const user = result.rows[0];
        const ok   = await bcrypt.compare(password, user.motp);
        if (!ok) return res.status(401).json({ error: 'Invalid credentials' });

        const token = jwt.sign(
            { id: user.idpers, login: user.login, role: user.role },
            process.env.JWT_SECRET || 'dev_secret',
            { expiresIn: process.env.JWT_EXPIRES_IN || '12h' }
        );

        return res.json({
            token,
            user: {
                id: user.idpers,
                nom: user.nompers,
                prenom: user.prenompers,
                login: user.login,
                role: user.role,
            },
        });
    } catch (err) {
        console.error('[auth] login error:', err);
        return res.status(500).json({ error: 'Server error' });
    }
}

module.exports = { login };
