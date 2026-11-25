const pool = require('../config/db');

exports.getShoppingList = async (req, res, next) => {
    try {
        const [rows] = await pool.query(
            "SELECT * FROM shopping_list WHERE user_id = ? ORDER BY status = 'purchased' ASC, id DESC",
            [Number(req.params.userId)]
        );
        res.json({ success: true, data: rows });
    } catch (err) {
        next(err);
    }
};

exports.addShoppingItem = async (req, res, next) => {
    try {
        const userId = req.user ? req.user.id : Number(req.body.user_id);
        const { item_name, quantity, unit, status } = req.body;
        if (!userId || !item_name) {
            return res.status(400).json({ success: false, message: 'Item name is required.' });
        }
        const [result] = await pool.query(
            'INSERT INTO shopping_list (user_id, item_name, quantity, unit, status) VALUES (?, ?, ?, ?, ?)',
            [userId, item_name.trim(), String(quantity || '1'), String(unit || 'pcs'), status === 'purchased' ? 'purchased' : 'pending']
        );
        const [rows] = await pool.query('SELECT * FROM shopping_list WHERE id = ?', [result.insertId]);
        res.status(201).json({ success: true, message: 'Item added.', data: rows[0] });
    } catch (err) {
        next(err);
    }
};

exports.addBulkShoppingItems = async (req, res, next) => {
    try {
        const userId = req.user ? req.user.id : Number(req.body.user_id);
        const { items } = req.body;
        if (!userId || !Array.isArray(items) || items.length === 0) {
            return res.status(400).json({ success: false, message: 'Items list is required.' });
        }
        const values = items
            .filter(i => i.item_name && i.item_name.trim())
            .map(i => [userId, i.item_name.trim(), String(i.quantity || '1'), String(i.unit || 'pcs'), 'pending']);

        if (values.length > 0) {
            await pool.query(
                'INSERT INTO shopping_list (user_id, item_name, quantity, unit, status) VALUES ?',
                [values]
            );
        }
        res.status(201).json({ success: true, message: 'Ingredients added to shopping list.' });
    } catch (err) {
        next(err);
    }
};

exports.updateShoppingItem = async (req, res, next) => {
    try {
        const { item_name, quantity, unit, status } = req.body;
        await pool.query(
            'UPDATE shopping_list SET item_name = ?, quantity = ?, unit = ?, status = ? WHERE id = ?',
            [item_name.trim(), String(quantity || '1'), String(unit || 'pcs'), status === 'purchased' ? 'purchased' : 'pending', req.params.id]
        );
        const [rows] = await pool.query('SELECT * FROM shopping_list WHERE id = ?', [req.params.id]);
        res.json({ success: true, message: 'Item updated.', data: rows[0] });
    } catch (err) {
        next(err);
    }
};

exports.deleteShoppingItem = async (req, res, next) => {
    try {
        await pool.query('DELETE FROM shopping_list WHERE id = ?', [req.params.id]);
        res.json({ success: true, message: 'Item deleted.' });
    } catch (err) {
        next(err);
    }
};
