const router = require('express').Router();
const recipeController = require('../controllers/recipeController');
const { authenticateToken, optionalAuth } = require('../middleware/authMiddleware');

router.get('/search', optionalAuth, recipeController.searchRecipes);
router.get('/category/:categoryId', optionalAuth, recipeController.getRecipesByCategory);
router.post('/by-ingredients', optionalAuth, recipeController.getRecipesByIngredients);

router.get('/', optionalAuth, recipeController.getRecipes);
router.get('/:id', optionalAuth, recipeController.getRecipeById);
router.post('/', authenticateToken, recipeController.createRecipe);
router.put('/:id', authenticateToken, recipeController.updateRecipe);
router.delete('/:id', authenticateToken, recipeController.deleteRecipe);

module.exports = router;
