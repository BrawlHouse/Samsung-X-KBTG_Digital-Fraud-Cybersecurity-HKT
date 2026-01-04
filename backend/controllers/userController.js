const { User, Device, sequelize } = require('../models');
const jwt = require('jsonwebtoken');

// ฟังก์ชันสร้าง Token
const generateToken = (user) => {
    return jwt.sign(
        { 
            user_id: user.user_id, 
            role: user.role, 
            family_id: user.family_id 
        },
        process.env.JWT_SECRET,
        { expiresIn: '365d' } // ให้ Token อายุ 1 ปีไปเลย (แอพมือถือไม่อยากให้หลุดบ่อย)
    );
};

exports.register = async (req, res) => {
    // ใช้ Transaction เพื่อความชัวร์ (ถ้าพังให้ Rollback ทั้งหมด)
    const t = await sequelize.transaction();

    try {
        const { nickname, device_id, role } = req.body;

        // 1. Validation: เช็คว่าส่งข้อมูลมาครบไหม
        if (!nickname || !device_id || !role) {
            return res.status(400).json({ error: 'Please provide nickname, device_id, and role' });
        }

        // 2. หาหรือสร้าง Device (Upsert)
        // ถ้ามี device_id นี้แล้วก็ใช้เลย ถ้ายังไม่มีก็สร้างใหม่
        const [device] = await Device.findOrCreate({
            where: { device_id: device_id },
            defaults: { device_id: device_id },
            transaction: t
        });

        // 3. สร้าง User ผูกกับ Device นั้น
        // เช็คก่อนว่า Device นี้เคยมี User หรือยัง (กัน Login ซ้ำ)
        let user = await User.findOne({ where: { device_id: device_id }, transaction: t });

        if (!user) {
            // ถ้ายังไม่มี User ให้สร้างใหม่
            user = await User.create({
                nickname,
                role, // 'parent' หรือ 'child'
                device_id
            }, { transaction: t });
        } else {
            // ถ้ามีแล้ว (เช่น ลบแอพลงใหม่) ให้อัปเดตชื่อใหม่
            user.nickname = nickname;
            user.role = role;
            await user.save({ transaction: t });
        }

        // Commit Transaction (บันทึกลง DB จริงๆ)
        await t.commit();

        // 4. สร้าง Token
        const token = generateToken(user);

        // 5. ส่งค่ากลับ
        res.status(201).json({
            message: 'User registered successfully',
            user: {
                user_id: user.user_id,
                nickname: user.nickname,
                role: user.role,
                family_id: user.family_id,
                device_id: user.device_id
            },
            token: token // **สำคัญมาก** Frontend ต้องเก็บอันนี้ไว้แปะ Header
        });

    } catch (error) {
        // ถ้า error ให้ยกเลิกการบันทึกทั้งหมด
        await t.rollback();
        console.error(error);
        res.status(500).json({ error: 'Registration failed', details: error.message });
    }
};