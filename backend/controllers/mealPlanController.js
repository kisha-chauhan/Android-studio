const pool = require('../config/db');

exports.getMealPlans = async (req, res, next) => {
    try {
        const userId = Number(req.params.userId);
        const { date } = req.query;
        let sql = `
            SELECT mp.id, mp.user_id, mp.recipe_id, DATE_FORMAT(mp.meal_date, '%Y-%m-%d') AS meal_date,
                   mp.meal_type, r.name AS recipe_name, r.image AS recipe_image,
                   (r.preparation_time + r.cooking_time) AS cooking_time, c.name AS category_name
            FROM meal_plans mp
            JOIN recipes r ON mp.recipe_id = r.id
            LEFT JOIN categories c ON r.category_id = c.id
            WHERE mp.user_id = ?
        `;
        const params = [userId];
        if (date) {
            sql += ' AND mp.meal_date = ? ';
            params.push(date);
        }
        sql += " ORDER BY mp.meal_date ASC, FIELD(mp.meal_type, 'Breakfast', 'Lunch', 'Snacks', 'Dinner')";

        const [rows] = await pool.query(sql, params);
        res.json({ success: true, data: rows });
    } catch (err) {
        next(err);
    }
};

exports.createMealPlan = async (req, res, next) => {
    try {
        const userId = req.user ? req.user.id : Number(req.body.user_id);
        const { recipe_id, meal_date, meal_type } = req.body;
        if (!userId || !recipe_id || !meal_date || !meal_type) {
            return res.status(400).json({ success: false, message: 'All meal plan fields are required.' });
        }
        const [result] = await pool.query(
            'INSERT INTO meal_plans (user_id, recipe_id, meal_date, meal_type) VALUES (?, ?, ?, ?)',
            [userId, Number(recipe_id), meal_date, meal_type]
        );
        const [rows] = await pool.query(
            "SELECT id, user_id, recipe_id, DATE_FORMAT(meal_date, '%Y-%m-%d') AS meal_date, meal_type FROM meal_plans WHERE id = ?",
            [result.insertId]
        );
        res.status(201).json({ success: true, message: 'Meal planned.', data: rows[0] });
    } catch (err) {
        next(err);
    }
};

exports.updateMealPlan = async (req, res, next) => {
    try {
        const { recipe_id, meal_date, meal_type } = req.body;
        await pool.query(
            'UPDATE meal_plans SET recipe_id = ?, meal_date = ?, meal_type = ? WHERE id = ?',
            [Number(recipe_id), meal_date, meal_type, req.params.id]
        );
        const [rows] = await pool.query(
            "SELECT id, user_id, recipe_id, DATE_FORMAT(meal_date, '%Y-%m-%d') AS meal_date, meal_type FROM meal_plans WHERE id = ?",
            [req.params.id]
        );
        res.json({ success: true, message: 'Meal plan updated.', data: rows[0] });
    } catch (err) {
        next(err);
    }
};

exports.deleteMealPlan = async (req, res, next) => {
    try {
        await pool.query('DELETE FROM meal_plans WHERE id = ?', [req.params.id]);
        res.json({ success: true, message: 'Meal plan removed.' });
    } catch (err) {
        next(err);
    }
};
