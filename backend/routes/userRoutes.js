const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');

// POST http://localhost:3000/users
router.post('/', userController.register);

module.exports = router;