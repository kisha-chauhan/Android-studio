const pool = require('../config/db');
const { buildRecipeSelectQuery, attachIngredientsToRecipes } = require('../models/recipeHelper');

exports.toggleFavorite = async (req, res, next) => {
    try {
        const userId = req.user ? req.user.id : Number(req.body.user_id);
        const recipeId = Number(req.body.recipe_id);
        if (!userId || !recipeId) {
            return res.status(400).json({ success: false, message: 'user_id and recipe_id are required.' });
        }

        const [existing] = await pool.query(
            'SELECT id FROM favorites WHERE user_id = ? AND recipe_id = ?',
            [userId, recipeId]
        );

        if (existing.length > 0) {
            await pool.query('DELETE FROM favorites WHERE id = ?', [existing[0].id]);
            return res.json({
                success: true,
                message: 'Removed from favorites.',
                data: { is_favorite: false }
            });
        }

        const [result] = await pool.query(
            'INSERT INTO favorites (user_id, recipe_id) VALUES (?, ?)',
            [userId, recipeId]
        );
        res.status(201).json({
            success: true,
            message: 'Added to favorites.',
            data: { id: result.insertId, is_favorite: true }
        });
    } catch (err) {
        next(err);
    }
};

exports.getFavoritesByUser = async (req, res, next) => {
    try {
        const userId = Number(req.params.userId);
        const [favRows] = await pool.query(
            'SELECT id, user_id, recipe_id, created_at FROM favorites WHERE user_id = ? ORDER BY created_at DESC',
            [userId]
        );
        if (favRows.length === 0) {
            return res.json({ success: true, data: [] });
        }

        const recipeIds = favRows.map(f => f.recipe_id);
        const sql = `${buildRecipeSelectQuery(userId)} WHERE r.id IN (?) GROUP BY r.id`;
        const [recipes] = await pool.query(sql, [recipeIds]);
        await attachIngredientsToRecipes(recipes);

        const recipeMap = new Map(recipes.map(r => [r.id, r]));
        const payload = favRows.map(f => ({
            ...f,
            recipe: recipeMap.get(f.recipe_id) || null
        }));

        res.json({ success: true, data: payload });
    } catch (err) {
        next(err);
    }
};

exports.deleteFavorite = async (req, res, next) => {
    try {
        await pool.query('DELETE FROM favorites WHERE id = ?', [req.params.id]);
        res.json({ success: true, message: 'Favorite removed.' });
    } catch (err) {
        next(err);
    }
};
