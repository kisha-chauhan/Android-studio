const pool = require('../config/db');

exports.getAllCategories = async (req, res, next) => {
    try {
        const [rows] = await pool.query(
            `SELECT c.*, COUNT(r.id) AS recipe_count
             FROM categories c
             LEFT JOIN recipes r ON c.id = r.category_id
             GROUP BY c.id
             ORDER BY c.id ASC`
        );
        res.json({ success: true, data: rows });
    } catch (err) {
        next(err);
    }
};

exports.createCategory = async (req, res, next) => {
    try {
        const { name, description, image } = req.body;
        if (!name || !name.trim()) {
            return res.status(400).json({ success: false, message: 'Category name is required.' });
        }
        const [result] = await pool.query(
            'INSERT INTO categories (name, description, image) VALUES (?, ?, ?)',
            [name.trim(), description || '', image || '']
        );
        const [rows] = await pool.query('SELECT * FROM categories WHERE id = ?', [result.insertId]);
        res.status(201).json({ success: true, message: 'Category created.', data: rows[0] });
    } catch (err) {
        next(err);
    }
};

exports.updateCategory = async (req, res, next) => {
    try {
        const { name, description, image } = req.body;
        if (!name || !name.trim()) {
            return res.status(400).json({ success: false, message: 'Category name is required.' });
        }
        await pool.query(
            'UPDATE categories SET name = ?, description = ?, image = ? WHERE id = ?',
            [name.trim(), description || '', image || '', req.params.id]
        );
        const [rows] = await pool.query('SELECT * FROM categories WHERE id = ?', [req.params.id]);
        res.json({ success: true, message: 'Category updated.', data: rows[0] });
    } catch (err) {
        next(err);
    }
};

exports.deleteCategory = async (req, res, next) => {
    try {
        await pool.query('DELETE FROM categories WHERE id = ?', [req.params.id]);
        res.json({ success: true, message: 'Category deleted.' });
    } catch (err) {
        if (err.code === 'ER_ROW_IS_REFERENCED_2') {
            return res.status(409).json({
                success: false,
                message: 'Cannot delete category that still has recipes assigned.'
            });
        }
        next(err);
    }
};
