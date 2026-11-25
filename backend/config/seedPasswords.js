const bcrypt = require('bcryptjs');
const pool = require('./db');

async function ensureSeedUserPasswords() {
    try {
        const [rows] = await pool.query(
            "SELECT id, email, password FROM users WHERE email IN ('admin@smartrecipe.com', 'aarav@smartrecipe.com', 'priya@smartrecipe.com')"
        );
        for (const user of rows) {
            const targetPlain = user.email === 'admin@smartrecipe.com' ? 'Admin@123' : 'User@123';
            const matches = await bcrypt.compare(targetPlain, user.password).catch(() => false);
            const isPlaceholder = user.password.includes('wK8x9p1zQ2vJ5Z3vX8J2me') ||
                user.password.includes('N9qo8uLOickgx2ZMRZoMye');
            if (!matches && isPlaceholder) {
                const hashed = await bcrypt.hash(targetPlain, 10);
                await pool.query('UPDATE users SET password = ? WHERE id = ?', [hashed, user.id]);
            }
        }
    } catch (err) {
        console.warn('Seed password check skipped:', err.message);
    }
}

module.exports = { ensureSeedUserPasswords };
