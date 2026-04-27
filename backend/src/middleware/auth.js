/**
 * JWT authentication and role-based authorization middleware.
 *
 * - {@link authenticate} verifies the Bearer token and attaches the decoded
 *   payload to req.user.
 * - {@link requireRole} returns a middleware that allows only the given role
 *   labels (e.g. 'Controleur', 'Livreur').
 */
const jwt = require('jsonwebtoken');

function authenticate(req, res, next) {
    const header = req.headers.authorization || '';
    const token  = header.startsWith('Bearer ') ? header.slice(7) : null;
    if (!token) return res.status(401).json({ error: 'Missing token' });

    try {
        req.user = jwt.verify(token, process.env.JWT_SECRET || 'dev_secret');
        next();
    } catch (err) {
        return res.status(401).json({ error: 'Invalid or expired token' });
    }
}

function requireRole(...allowed) {
    return (req, res, next) => {
        if (!req.user || !allowed.includes(req.user.role)) {
            return res.status(403).json({ error: 'Forbidden' });
        }
        next();
    };
}

module.exports = { authenticate, requireRole };
