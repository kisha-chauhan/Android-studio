const bcrypt = require('bcryptjs');
const pool = require('../config/db');

exports.getDashboardStats = async (req, res, next) => {
    try {
        const [[users]] = await pool.query('SELECT COUNT(*) AS count FROM users');
        const [[recipes]] = await pool.query('SELECT COUNT(*) AS count FROM recipes');
        const [[categories]] = await pool.query('SELECT COUNT(*) AS count FROM categories');
        const [[reviews]] = await pool.query('SELECT COUNT(*) AS count FROM reviews');
        const [[favorites]] = await pool.query('SELECT COUNT(*) AS count FROM favorites');

        res.json({
            success: true,
            data: {
                total_users: users.count,
                total_recipes: recipes.count,
                total_categories: categories.count,
                total_reviews: reviews.count,
                total_favorites: favorites.count
            }
        });
    } catch (err) {
        next(err);
    }
};

exports.getAllUsers = async (req, res, next) => {
    try {
        const [rows] = await pool.query(
            'SELECT id, name, email, phone, role, profile_image, created_at FROM users ORDER BY id DESC'
        );
        res.json({ success: true, data: rows });
    } catch (err) {
        next(err);
    }
};

exports.createUser = async (req, res, next) => {
    try {
        const { name, email, phone, password, role } = req.body;
        if (!name || !email || !phone || !password) {
            return res.status(400).json({ success: false, message: 'Name, email, phone, and password required.' });
        }
        const [existing] = await pool.query('SELECT id FROM users WHERE email = ?', [email.trim().toLowerCase()]);
        if (existing.length > 0) {
            return res.status(409).json({ success: false, message: 'Email already exists.' });
        }
        const hashed = await bcrypt.hash(password, 10);
        const safeRole = role === 'admin' ? 'admin' : 'user';
        const [result] = await pool.query(
            'INSERT INTO users (name, email, phone, password, role) VALUES (?, ?, ?, ?, ?)',
            [name.trim(), email.trim().toLowerCase(), phone.trim(), hashed, safeRole]
        );
        const [rows] = await pool.query(
            'SELECT id, name, email, phone, role, profile_image, created_at FROM users WHERE id = ?',
            [result.insertId]
        );
        res.status(201).json({ success: true, message: 'User created.', data: rows[0] });
    } catch (err) {
        next(err);
    }
};

exports.getAllReviews = async (req, res, next) => {
    try {
        const [rows] = await pool.query(
            `SELECT rv.*, u.name AS user_name, r.name AS recipe_name
             FROM reviews rv
             JOIN users u ON rv.user_id = u.id
             JOIN recipes r ON rv.recipe_id = r.id
             ORDER BY rv.created_at DESC`
        );
        res.json({ success: true, data: rows });
    } catch (err) {
        next(err);
    }
};
