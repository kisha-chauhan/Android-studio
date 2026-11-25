const pool = require('../config/db');
const { buildRecipeSelectQuery, attachIngredientsToRecipes } = require('../models/recipeHelper');

exports.getRecipes = async (req, res, next) => {
    try {
        const { sort, user_id, author_id } = req.query;
        let sql = buildRecipeSelectQuery(user_id);
        const params = [];

        if (author_id) {
            sql += ' WHERE r.user_id = ? ';
            params.push(Number(author_id));
        }

        sql += ' GROUP BY r.id ';
        sql += sort === 'popular'
            ? ' ORDER BY average_rating DESC, review_count DESC, r.id DESC '
            : ' ORDER BY r.created_at DESC, r.id DESC ';

        const [rows] = await pool.query(sql, params);
        await attachIngredientsToRecipes(rows);
        res.json({ success: true, data: rows });
    } catch (err) {
        next(err);
    }
};

exports.getRecipeById = async (req, res, next) => {
    try {
        const { user_id } = req.query;
        const sql = `${buildRecipeSelectQuery(user_id)} WHERE r.id = ? GROUP BY r.id`;
        const [rows] = await pool.query(sql, [req.params.id]);
        if (rows.length === 0) {
            return res.status(404).json({ success: false, message: 'Recipe not found.' });
        }
        await attachIngredientsToRecipes(rows);
        res.json({ success: true, data: rows[0] });
    } catch (err) {
        next(err);
    }
};

exports.searchRecipes = async (req, res, next) => {
    try {
        const { q, category_id, ingredient, user_id } = req.query;
        let sql = buildRecipeSelectQuery(user_id);
        const conditions = [];
        const params = [];

        if (category_id) {
            conditions.push('r.category_id = ?');
            params.push(Number(category_id));
        }
        if (q && q.trim()) {
            const like = `%${q.trim()}%`;
            conditions.push(
                `(r.name LIKE ? OR c.name LIKE ? OR EXISTS (
                    SELECT 1 FROM ingredients ing WHERE ing.recipe_id = r.id AND ing.ingredient_name LIKE ?
                ))`
            );
            params.push(like, like, like);
        }
        if (ingredient && ingredient.trim()) {
            conditions.push(
                `EXISTS (SELECT 1 FROM ingredients ing WHERE ing.recipe_id = r.id AND ing.ingredient_name LIKE ?)`
            );
            params.push(`%${ingredient.trim()}%`);
        }

        if (conditions.length > 0) {
            sql += ' WHERE ' + conditions.join(' AND ');
        }
        sql += ' GROUP BY r.id ORDER BY average_rating DESC, r.id DESC';

        const [rows] = await pool.query(sql, params);
        await attachIngredientsToRecipes(rows);
        res.json({ success: true, data: rows });
    } catch (err) {
        next(err);
    }
};

exports.getRecipesByCategory = async (req, res, next) => {
    try {
        const { user_id } = req.query;
        const sql = `${buildRecipeSelectQuery(user_id)} WHERE r.category_id = ? GROUP BY r.id ORDER BY r.id DESC`;
        const [rows] = await pool.query(sql, [req.params.categoryId]);
        await attachIngredientsToRecipes(rows);
        res.json({ success: true, data: rows });
    } catch (err) {
        next(err);
    }
};

exports.getRecipesByIngredients = async (req, res, next) => {
    try {
        const { ingredients, user_id } = req.body;
        if (!Array.isArray(ingredients) || ingredients.length === 0) {
            return res.status(400).json({ success: false, message: 'Provide at least one ingredient.' });
        }

        const available = ingredients.map(i => String(i).trim().toLowerCase()).filter(Boolean);
        const sql = `${buildRecipeSelectQuery(user_id)} GROUP BY r.id`;
        const [recipes] = await pool.query(sql);
        await attachIngredientsToRecipes(recipes);

        const matched = [];
        for (const recipe of recipes) {
            const recipeIngs = recipe.ingredients || [];
            if (recipeIngs.length === 0) continue;

            const missing = [];
            let matchCount = 0;

            for (const ing of recipeIngs) {
                const ingLower = (ing.ingredient_name || '').trim().toLowerCase();
                const isAvailable = available.some(
                    avail => ingLower.includes(avail) || avail.includes(ingLower)
                );
                if (isAvailable) {
                    matchCount++;
                } else {
                    missing.push(ing.ingredient_name);
                }
            }

            if (matchCount > 0) {
                recipe.matched_count = matchCount;
                recipe.total_ingredients = recipeIngs.length;
                recipe.match_percentage = Math.round((matchCount / recipeIngs.length) * 100);
                recipe.missing_ingredients = missing;
                matched.push(recipe);
            }
        }

        matched.sort((a, b) => b.match_percentage - a.match_percentage || b.matched_count - a.matched_count);
        res.json({ success: true, data: matched });
    } catch (err) {
        next(err);
    }
};

exports.createRecipe = async (req, res, next) => {
    const conn = await pool.getConnection();
    try {
        const {
            user_id, category_id, name, description, image,
            preparation_time, cooking_time, servings, instructions, ingredients
        } = req.body;

        const ownerId = req.user ? req.user.id : Number(user_id);
        if (!ownerId || !category_id || !name || !instructions) {
            conn.release();
            return res.status(400).json({ success: false, message: 'Name, category, and instructions are required.' });
        }

        await conn.beginTransaction();
        const [result] = await conn.query(
            `INSERT INTO recipes (user_id, category_id, name, description, image, preparation_time, cooking_time, servings, instructions)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
            [
                ownerId,
                Number(category_id),
                name.trim(),
                description || '',
                image || '',
                Number(preparation_time) || 10,
                Number(cooking_time) || 20,
                Number(servings) || 2,
                instructions.trim()
            ]
        );

        const recipeId = result.insertId;
        if (Array.isArray(ingredients) && ingredients.length > 0) {
            const values = ingredients
                .filter(i => i.ingredient_name && i.ingredient_name.trim())
                .map(i => [recipeId, i.ingredient_name.trim(), String(i.quantity || '1'), String(i.unit || 'pcs')]);
            if (values.length > 0) {
                await conn.query(
                    'INSERT INTO ingredients (recipe_id, ingredient_name, quantity, unit) VALUES ?',
                    [values]
                );
            }
        }

        await conn.commit();
        conn.release();

        const [rows] = await pool.query(`${buildRecipeSelectQuery(ownerId)} WHERE r.id = ? GROUP BY r.id`, [recipeId]);
        await attachIngredientsToRecipes(rows);
        res.status(201).json({ success: true, message: 'Recipe created.', data: rows[0] });
    } catch (err) {
        await conn.rollback();
        conn.release();
        next(err);
    }
};

exports.updateRecipe = async (req, res, next) => {
    const conn = await pool.getConnection();
    try {
        const recipeId = Number(req.params.id);
        const [existing] = await conn.query('SELECT user_id FROM recipes WHERE id = ?', [recipeId]);
        if (existing.length === 0) {
            conn.release();
            return res.status(404).json({ success: false, message: 'Recipe not found.' });
        }
        if (existing[0].user_id !== req.user.id && req.user.role !== 'admin') {
            conn.release();
            return res.status(403).json({ success: false, message: 'Only the recipe owner or admin can edit.' });
        }

        const {
            category_id, name, description, image,
            preparation_time, cooking_time, servings, instructions, ingredients
        } = req.body;

        await conn.beginTransaction();
        await conn.query(
            `UPDATE recipes SET category_id = ?, name = ?, description = ?, image = ?,
             preparation_time = ?, cooking_time = ?, servings = ?, instructions = ? WHERE id = ?`,
            [
                Number(category_id),
                name.trim(),
                description || '',
                image || '',
                Number(preparation_time) || 10,
                Number(cooking_time) || 20,
                Number(servings) || 2,
                instructions.trim(),
                recipeId
            ]
        );

        if (Array.isArray(ingredients)) {
            await conn.query('DELETE FROM ingredients WHERE recipe_id = ?', [recipeId]);
            const values = ingredients
                .filter(i => i.ingredient_name && i.ingredient_name.trim())
                .map(i => [recipeId, i.ingredient_name.trim(), String(i.quantity || '1'), String(i.unit || 'pcs')]);
            if (values.length > 0) {
                await conn.query(
                    'INSERT INTO ingredients (recipe_id, ingredient_name, quantity, unit) VALUES ?',
                    [values]
                );
            }
        }

        await conn.commit();
        conn.release();

        const [rows] = await pool.query(`${buildRecipeSelectQuery(req.user.id)} WHERE r.id = ? GROUP BY r.id`, [recipeId]);
        await attachIngredientsToRecipes(rows);
        res.json({ success: true, message: 'Recipe updated.', data: rows[0] });
    } catch (err) {
        await conn.rollback();
        conn.release();
        next(err);
    }
};

exports.deleteRecipe = async (req, res, next) => {
    try {
        const recipeId = Number(req.params.id);
        const [existing] = await pool.query('SELECT user_id FROM recipes WHERE id = ?', [recipeId]);
        if (existing.length === 0) {
            return res.status(404).json({ success: false, message: 'Recipe not found.' });
        }
        if (existing[0].user_id !== req.user.id && req.user.role !== 'admin') {
            return res.status(403).json({ success: false, message: 'Only the recipe owner or admin can delete.' });
        }
        await pool.query('DELETE FROM recipes WHERE id = ?', [recipeId]);
        res.json({ success: true, message: 'Recipe deleted.' });
    } catch (err) {
        next(err);
    }
};
