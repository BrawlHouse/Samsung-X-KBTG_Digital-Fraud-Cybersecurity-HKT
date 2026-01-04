// middlewares/authMiddleware.js
const jwt = require('jsonwebtoken');

module.exports = (req, res, next) => {
    try {
        // 1. ดึง Token จาก Header (รูปแบบ: Bearer <token>)
        const token = req.headers.authorization.split(' ')[1];
        
        if (!token) {
            return res.status(401).json({ error: 'Authentication failed: No token provided' });
        }

        // 2. ตรวจสอบความถูกต้องของ Token
        const decodedToken = jwt.verify(token, process.env.JWT_SECRET);

        // 3. ฝากข้อมูล User ไว้ใน req เพื่อให้ Controller ใช้งานต่อได้
        req.user = { 
            user_id: decodedToken.user_id,
            role: decodedToken.role,
            family_id: decodedToken.family_id 
        };

        next(); // ผ่านด่านได้ ไปทำต่อที่ Controller
    } catch (error) {
        return res.status(401).json({ error: 'Authentication failed: Invalid token' });
    }
};