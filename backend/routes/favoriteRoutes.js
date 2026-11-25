const router = require('express').Router();
const favoriteController = require('../controllers/favoriteController');
const { authenticateToken } = require('../middleware/authMiddleware');

router.post('/', authenticateToken, favoriteController.toggleFavorite);
router.get('/:userId', authenticateToken, favoriteController.getFavoritesByUser);
router.delete('/:id', authenticateToken, favoriteController.deleteFavorite);

module.exports = router;
