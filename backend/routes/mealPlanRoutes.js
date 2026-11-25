const router = require('express').Router();
const mealPlanController = require('../controllers/mealPlanController');
const { authenticateToken } = require('../middleware/authMiddleware');

router.get('/:userId', authenticateToken, mealPlanController.getMealPlans);
router.post('/', authenticateToken, mealPlanController.createMealPlan);
router.put('/:id', authenticateToken, mealPlanController.updateMealPlan);
router.delete('/:id', authenticateToken, mealPlanController.deleteMealPlan);

module.exports = router;
