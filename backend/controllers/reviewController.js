const pool = require('../config/db');

exports.getRecipeReviews = async (req, res, next) => {
    try {
        const [rows] = await pool.query(
            `SELECT rv.*, u.name AS user_name, u.profile_image AS user_image
             FROM reviews rv
             JOIN users u ON rv.user_id = u.id
             WHERE rv.recipe_id = ?
             ORDER BY rv.created_at DESC`,
            [Number(req.params.recipeId)]
        );
        res.json({ success: true, data: rows });
    } catch (err) {
        next(err);
    }
};

exports.getUserReviews = async (req, res, next) => {
    try {
        const [rows] = await pool.query(
            `SELECT rv.*, u.name AS user_name, r.name AS recipe_name
             FROM reviews rv
             JOIN users u ON rv.user_id = u.id
             JOIN recipes r ON rv.recipe_id = r.id
             WHERE rv.user_id = ?
             ORDER BY rv.created_at DESC`,
            [Number(req.params.userId)]
        );
        res.json({ success: true, data: rows });
    } catch (err) {
        next(err);
    }
};

exports.addReview = async (req, res, next) => {
    try {
        const userId = req.user ? req.user.id : Number(req.body.user_id);
        const { recipe_id, rating, review_text } = req.body;
        const numRating = Number(rating);
        if (!userId || !recipe_id || isNaN(numRating) || numRating < 1 || numRating > 5) {
            return res.status(400).json({ success: false, message: 'Valid recipe_id and rating (1-5) required.' });
        }

        const [result] = await pool.query(
            'INSERT INTO reviews (user_id, recipe_id, rating, review_text) VALUES (?, ?, ?, ?)',
            [userId, Number(recipe_id), numRating, review_text || '']
        );
        const [rows] = await pool.query('SELECT * FROM reviews WHERE id = ?', [result.insertId]);
        res.status(201).json({ success: true, message: 'Review added.', data: rows[0] });
    } catch (err) {
        next(err);
    }
};

exports.updateReview = async (req, res, next) => {
    try {
        const reviewId = Number(req.params.id);
        const [existing] = await pool.query('SELECT user_id FROM reviews WHERE id = ?', [reviewId]);
        if (existing.length === 0) {
            return res.status(404).json({ success: false, message: 'Review not found.' });
        }
        if (existing[0].user_id !== req.user.id && req.user.role !== 'admin') {
            return res.status(403).json({ success: false, message: 'Forbidden.' });
        }

        const { rating, review_text } = req.body;
        const numRating = Math.min(5, Math.max(1, Number(rating) || 5));
        await pool.query(
            'UPDATE reviews SET rating = ?, review_text = ? WHERE id = ?',
            [numRating, review_text || '', reviewId]
        );
        const [rows] = await pool.query('SELECT * FROM reviews WHERE id = ?', [reviewId]);
        res.json({ success: true, message: 'Review updated.', data: rows[0] });
    } catch (err) {
        next(err);
    }
};

exports.deleteReview = async (req, res, next) => {
    try {
        const reviewId = Number(req.params.id);
        const [existing] = await pool.query('SELECT user_id FROM reviews WHERE id = ?', [reviewId]);
        if (existing.length === 0) {
            return res.status(404).json({ success: false, message: 'Review not found.' });
        }
        if (existing[0].user_id !== req.user.id && req.user.role !== 'admin') {
            return res.status(403).json({ success: false, message: 'Forbidden.' });
        }
        await pool.query('DELETE FROM reviews WHERE id = ?', [reviewId]);
        res.json({ success: true, message: 'Review deleted.' });
    } catch (err) {
        next(err);
    }
};
