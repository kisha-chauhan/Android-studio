const router = require('express').Router();
const userController = require('../controllers/userController');
const { authenticateToken } = require('../middleware/authMiddleware');

router.get('/:id', authenticateToken, userController.getUser);
router.put('/:id', authenticateToken, userController.updateUser);
router.put('/:id/password', authenticateToken, userController.changePassword);
router.delete('/:id', authenticateToken, userController.deleteUser);

module.exports = router;
