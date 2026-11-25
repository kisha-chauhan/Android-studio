const router = require('express').Router();
const upload = require('../middleware/uploadMiddleware');
const { authenticateToken } = require('../middleware/authMiddleware');

router.post('/', authenticateToken, upload.single('image'), (req, res) => {
    if (!req.file) {
        return res.status(400).json({ success: false, message: 'No image file uploaded.' });
    }
    const url = `/uploads/${req.file.filename}`;
    res.status(201).json({
        success: true,
        message: 'Image uploaded successfully.',
        data: { url }
    });
});

module.exports = router;
