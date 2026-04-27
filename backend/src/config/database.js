/**
 * PostgreSQL connection pool.
 *
 * Reads connection parameters from environment variables (see .env.example).
 * A single shared {@link pg.Pool} is exported and reused across the app.
 */
const { Pool } = require('pg');

const pool = new Pool({
    host:     process.env.PGHOST     || 'localhost',
    port:     parseInt(process.env.PGPORT || '5432', 10),
    database: process.env.PGDATABASE || 'BDG_LivraisonCom_25',
    user:     process.env.PGUSER     || 'postgres',
    password: process.env.PGPASSWORD || 'postgres',
    max: 20,
    idleTimeoutMillis: 30000,
});

pool.on('error', (err) => {
    console.error('[pg] Unexpected pool error:', err);
});

module.exports = {
    pool,
    query: (text, params) => pool.query(text, params),
};
