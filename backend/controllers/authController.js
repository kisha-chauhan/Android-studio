const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const pool = require('../config/db');

function signToken(user) {
    return jwt.sign(
        { id: user.id, email: user.email, role: user.role },
        process.env.JWT_SECRET,
        { expiresIn: process.env.JWT_EXPIRES_IN || '7d' }
    );
}

exports.register = async (req, res, next) => {
    try {
        const { name, email, phone, password } = req.body;
        if (!name || !email || !phone || !password) {
            return res.status(400).json({ success: false, message: 'All fields are required.' });
        }
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email.trim())) {
            return res.status(400).json({ success: false, message: 'Invalid email address.' });
        }
        if (password.length < 6) {
            return res.status(400).json({ success: false, message: 'Password must be at least 6 characters.' });
        }

        const [existing] = await pool.query('SELECT id FROM users WHERE email = ?', [email.trim().toLowerCase()]);
        if (existing.length > 0) {
            return res.status(409).json({ success: false, message: 'Email is already registered.' });
        }

        const hashedPassword = await bcrypt.hash(password, 10);
        const [result] = await pool.query(
            'INSERT INTO users (name, email, phone, password, role) VALUES (?, ?, ?, ?, ?)',
            [name.trim(), email.trim().toLowerCase(), phone.trim(), hashedPassword, 'user']
        );

        const user = {
            id: result.insertId,
            name: name.trim(),
            email: email.trim().toLowerCase(),
            phone: phone.trim(),
            role: 'user',
            profile_image: ''
        };
        const token = signToken(user);

        res.status(201).json({
            success: true,
            message: 'Registration successful.',
            token,
            user
        });
    } catch (err) {
        next(err);
    }
};

exports.login = async (req, res, next) => {
    try {
        const { email, password } = req.body;
        if (!email || !password) {
            return res.status(400).json({ success: false, message: 'Email and password are required.' });
        }

        const [rows] = await pool.query(
            'SELECT id, name, email, phone, password, role, profile_image FROM users WHERE email = ?',
            [email.trim().toLowerCase()]
        );
        if (rows.length === 0) {
            return res.status(401).json({ success: false, message: 'Invalid email or password.' });
        }

        const userRow = rows[0];
        const isMatch = await bcrypt.compare(password, userRow.password);
        if (!isMatch) {
            return res.status(401).json({ success: false, message: 'Invalid email or password.' });
        }

        const user = {
            id: userRow.id,
            name: userRow.name,
            email: userRow.email,
            phone: userRow.phone,
            role: userRow.role,
            profile_image: userRow.profile_image || ''
        };
        const token = signToken(user);

        res.json({
            success: true,
            message: 'Login successful.',
            token,
            user
        });
    } catch (err) {
        next(err);
    }
};

exports.forgotPassword = async (req, res, next) => {
    try {
        const { email } = req.body;
        if (!email) {
            return res.status(400).json({ success: false, message: 'Email is required.' });
        }
        const [rows] = await pool.query('SELECT id FROM users WHERE email = ?', [email.trim().toLowerCase()]);
        if (rows.length === 0) {
            return res.status(404).json({ success: false, message: 'No account found with this email.' });
        }
        res.json({
            success: true,
            message: 'Password reset instructions have been prepared for your registered email. Contact admin or use Change Password once signed in.'
        });
    } catch (err) {
        next(err);
    }
};
