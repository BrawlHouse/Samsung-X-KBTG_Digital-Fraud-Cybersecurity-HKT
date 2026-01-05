const { User, Device, sequelize } = require('../models');
const jwt = require('jsonwebtoken');


exports.register = async (req, res) => {
    const t = await sequelize.transaction();

    try {
        const { nickname, device_id, role } = req.body;

        if (!nickname || !device_id || !role) {
            return res.status(400).json({ error: 'Please provide nickname, device_id, and role' });
        }

        const [device] = await Device.findOrCreate({
            where: { device_id: device_id },
            defaults: { device_id: device_id },
            transaction: t
        });

        let user = await User.findOne({ where: { device_id: device_id }, transaction: t });

        if (!user) {
            user = await User.create({ nickname, role, device_id }, { transaction: t });
        } else {
            user.nickname = nickname;
            user.role = role;
            await user.save({ transaction: t });
        }

        await t.commit();
        
        const token = jwt.sign(
            { user_id: user.user_id, role: user.role, family_id: user.family_id },
            process.env.JWT_SECRET,
            { expiresIn: '365d' }
        );

        res.status(201).json({
            message: 'User registered successfully',
            user: {
                user_id: user.user_id,
                nickname: user.nickname,
                role: user.role,
                family_id: user.family_id,
                device_id: user.device_id
            },
            token: token
        });

    } catch (error) {
        await t.rollback();
        console.error(error);
        res.status(500).json({ error: 'Registration failed', details: error.message });
    }
};

// (NEW) ลบ User ออกจากระบบ (DELETE /users)
exports.deleteUser = async (req, res) => {
    try {
        const user_id = req.params.user_id;

        if (!user_id) {
            return res.status(400).json({ error: 'Please provide user_id' });
        }

        // ป้องกันไม่ให้ User คนอื่นมาลบ (Optional: เช็คว่าคนลบคือตัวเองหรือ Admin)
        // แต่ในที่นี้ทำตามโจทย์ก่อน
        const user = await User.findByPk(user_id);

        if (!user) {
            return res.status(404).json({ error: 'User not found' });
        }

        await user.destroy();

        res.json({ message: `User ${user_id} deleted successfully` });

    } catch (error) {
        res.status(500).json({ error: 'Internal Server Error', details: error.message });
    }
};

exports.getUserInfo = async (req, res) => {
    try {
        // รับค่าจาก query หรือ body ก็ได้
        const user_id = req.params.user_id;

        if (!user_id) {
            return res.status(400).json({ error: 'Please provide user_id' });
        }

        const user = await User.findByPk(user_id, {
            // เลือกเฉพาะฟิลด์ที่ต้องการ
            attributes: ['user_id', 'nickname', 'created_at', 'device_id'] 
        });

        if (!user) {
            return res.status(404).json({ error: 'User not found' });
        }

        res.json(user);

    } catch (error) {
        res.status(500).json({ error: 'Internal Server Error', details: error.message });
    }
};



exports.updateFcmToken = async (req, res) => {
    try {
        const { token } = req.body;
        const user_id = req.user.user_id; // ได้มาจาก authMiddleware

        if (!token) {
            return res.status(400).json({ error: 'Device token is required' });
        }

        // อัปเดตลง Database
        await User.update({ fcm_token: token }, {
            where: { user_id: user_id }
        });

        res.json({ message: 'Device token updated successfully' });

    } catch (error) {
        console.error('Update Token Error:', error);
        res.status(500).json({ error: 'Server error', details: error.message });
    }
};