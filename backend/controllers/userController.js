const bcrypt = require('bcryptjs');
const pool = require('../config/db');

exports.getUser = async (req, res, next) => {
    try {
        const [rows] = await pool.query(
            'SELECT id, name, email, phone, role, profile_image, created_at FROM users WHERE id = ?',
            [req.params.id]
        );
        if (rows.length === 0) {
            return res.status(404).json({ success: false, message: 'User not found.' });
        }
        res.json({ success: true, data: rows[0] });
    } catch (err) {
        next(err);
    }
};

exports.updateUser = async (req, res, next) => {
    try {
        const targetId = Number(req.params.id);
        if (req.user.id !== targetId && req.user.role !== 'admin') {
            return res.status(403).json({ success: false, message: 'Forbidden.' });
        }

        const { name, phone, profile_image, role } = req.body;
        if (!name || !phone) {
            return res.status(400).json({ success: false, message: 'Name and phone are required.' });
        }

        const safeRole = req.user.role === 'admin' && (role === 'admin' || role === 'user') ? role : null;
        if (safeRole) {
            await pool.query(
                'UPDATE users SET name = ?, phone = ?, profile_image = ?, role = ? WHERE id = ?',
                [name.trim(), phone.trim(), profile_image || '', safeRole, targetId]
            );
        } else {
            await pool.query(
                'UPDATE users SET name = ?, phone = ?, profile_image = ? WHERE id = ?',
                [name.trim(), phone.trim(), profile_image || '', targetId]
            );
        }

        const [rows] = await pool.query(
            'SELECT id, name, email, phone, role, profile_image, created_at FROM users WHERE id = ?',
            [targetId]
        );
        res.json({ success: true, message: 'Profile updated.', data: rows[0] });
    } catch (err) {
        next(err);
    }
};

exports.changePassword = async (req, res, next) => {
    try {
        const targetId = Number(req.params.id);
        if (req.user.id !== targetId && req.user.role !== 'admin') {
            return res.status(403).json({ success: false, message: 'Forbidden.' });
        }

        const { current_password, new_password } = req.body;
        if (!current_password || !new_password || new_password.length < 6) {
            return res.status(400).json({ success: false, message: 'Valid current and new password (min 6 chars) required.' });
        }

        const [rows] = await pool.query('SELECT password FROM users WHERE id = ?', [targetId]);
        if (rows.length === 0) {
            return res.status(404).json({ success: false, message: 'User not found.' });
        }

        const valid = await bcrypt.compare(current_password, rows[0].password);
        if (!valid) {
            return res.status(401).json({ success: false, message: 'Current password is incorrect.' });
        }

        const hashed = await bcrypt.hash(new_password, 10);
        await pool.query('UPDATE users SET password = ? WHERE id = ?', [hashed, targetId]);
        res.json({ success: true, message: 'Password updated successfully.' });
    } catch (err) {
        next(err);
    }
};

exports.deleteUser = async (req, res, next) => {
    try {
        const targetId = Number(req.params.id);
        if (req.user.id !== targetId && req.user.role !== 'admin') {
            return res.status(403).json({ success: false, message: 'Forbidden.' });
        }
        await pool.query('DELETE FROM users WHERE id = ?', [targetId]);
        res.json({ success: true, message: 'User deleted.' });
    } catch (err) {
        next(err);
    }
};
