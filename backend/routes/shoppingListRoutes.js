const router = require('express').Router();
const shoppingListController = require('../controllers/shoppingListController');
const { authenticateToken } = require('../middleware/authMiddleware');

router.get('/:userId', authenticateToken, shoppingListController.getShoppingList);
router.post('/', authenticateToken, shoppingListController.addShoppingItem);
router.post('/bulk', authenticateToken, shoppingListController.addBulkShoppingItems);
router.put('/:id', authenticateToken, shoppingListController.updateShoppingItem);
router.delete('/:id', authenticateToken, shoppingListController.deleteShoppingItem);

module.exports = router;
