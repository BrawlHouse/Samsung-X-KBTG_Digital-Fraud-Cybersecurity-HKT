const jwt = require('jsonwebtoken');

module.exports = (req, res, next) => {
    try {
        // 1. เช็คว่ามี Header ส่งมาไหม (ป้องกัน Error กรณีไม่มี Header เลย)
        if (!req.headers.authorization) {
            return res.status(401).json({ error: 'Authentication failed: No token provided' });
        }

        // 2. ดึง Token (รูปแบบ: Bearer <token>)
        const token = req.headers.authorization.split(' ')[1];
        
        if (!token) {
            return res.status(401).json({ error: 'Authentication failed: Malformed token' });
        }

        // 3. ตรวจสอบความถูกต้อง (Verify)
        const decodedToken = jwt.verify(token, process.env.JWT_SECRET);

        // 4. ฝากข้อมูล User ไว้ใน req
        req.user = { 
            user_id: decodedToken.user_id,
            role: decodedToken.role,
            family_id: decodedToken.family_id 
        };

        next(); 
    } catch (error) {
        return res.status(401).json({ error: 'Authentication failed: Invalid token' });
    }
};