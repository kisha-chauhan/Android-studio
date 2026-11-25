const router = require('express').Router();
const reviewController = require('../controllers/reviewController');
const { authenticateToken } = require('../middleware/authMiddleware');

router.get('/recipe/:recipeId', reviewController.getRecipeReviews);
router.get('/user/:userId', authenticateToken, reviewController.getUserReviews);
router.post('/', authenticateToken, reviewController.addReview);
router.put('/:id', authenticateToken, reviewController.updateReview);
router.delete('/:id', authenticateToken, reviewController.deleteReview);

module.exports = router;
