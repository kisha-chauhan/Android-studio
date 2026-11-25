const pool = require('../config/db');

function buildRecipeSelectQuery(viewerUserId) {
    const uid = Number(viewerUserId) || 0;
    return `
        SELECT 
            r.id, r.user_id, r.category_id, r.name, r.description, r.image,
            r.preparation_time, r.cooking_time, r.servings, r.instructions, r.created_at,
            c.name AS category_name,
            u.name AS author_name,
            COALESCE(ROUND(AVG(rv.rating), 1), 0) AS average_rating,
            COUNT(DISTINCT rv.id) AS review_count,
            CASE WHEN EXISTS (
                SELECT 1 FROM favorites f WHERE f.recipe_id = r.id AND f.user_id = ${pool.escape(uid)}
            ) THEN TRUE ELSE FALSE END AS is_favorite
        FROM recipes r
        LEFT JOIN categories c ON r.category_id = c.id
        LEFT JOIN users u ON r.user_id = u.id
        LEFT JOIN reviews rv ON r.id = rv.recipe_id
    `;
}

async function attachIngredientsToRecipes(recipes) {
    if (!recipes || recipes.length === 0) return recipes;
    const ids = recipes.map(r => r.id);
    const [ingRows] = await pool.query(
        'SELECT id, recipe_id, ingredient_name, quantity, unit FROM ingredients WHERE recipe_id IN (?)',
        [ids]
    );
    const map = new Map();
    for (const ing of ingRows) {
        if (!map.has(ing.recipe_id)) map.set(ing.recipe_id, []);
        map.get(ing.recipe_id).push(ing);
    }
    for (const r of recipes) {
        r.is_favorite = Boolean(r.is_favorite);
        r.average_rating = Number(r.average_rating) || 0;
        r.ingredients = map.get(r.id) || [];
    }
    return recipes;
}

module.exports = { buildRecipeSelectQuery, attachIngredientsToRecipes };
