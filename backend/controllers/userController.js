const bcrypt = require('bcryptjs')
const jwt = require('jsonwebtoken');
const { User, Device, sequelize } = require('../models');


exports.register = async (req, res) => {
    // เริ่ม Transaction เพื่อความชัวร์ (ถ้าพังให้ Rollback หมด)
    const t = await sequelize.transaction(); 
    
    try {
        const { nickname, email, password, role, device_id, bank_account_number } = req.body;

        // 1. Validate Input
        if (!email || !password || !nickname || !role) {
            return res.status(400).json({ error: 'Please provide all required fields' });
        }

        // 2. Check duplicate email
        const existingUser = await User.findOne({ where: { email }, transaction: t });
        if (existingUser) {
            await t.rollback(); // ยกเลิก Transaction
            return res.status(400).json({ error: 'Email already exists' });
        }
        
        if (device_id) {
            await Device.findOrCreate({
                where: { device_id: device_id },
                defaults: { 
                    device_id: device_id,
                    register_at: new Date()
                },
                transaction: t
            });
        }

        // 3. Hash Password
        const hashedPassword = await bcrypt.hash(password, 10);

        // 4. Create User
        const newUser = await User.create({
            nickname,
            email,
            password: hashedPassword,
            role,
            device_id: device_id || null, // ถ้าไม่มีส่งมาให้เป็น null
            bank_account_number
        }, { transaction: t });

        // บันทึกทุกอย่างลง DB จริง
        await t.commit();

        res.status(201).json({ 
            message: 'User registered successfully',
            user: {
                user_id: newUser.user_id,
                nickname: newUser.nickname,
                email: newUser.email,
                role: newUser.role
            }
        });

    } catch (error) {
        await t.rollback(); // ย้อนกลับถ้า error
        console.error(error); // log ดู error ใน terminal
        res.status(500).json({ error: 'Registration failed', details: error.message });
    }
};


exports.login = async (req, res) => {
    try {
        const { email, password } = req.body;

        // 1. หา User จาก Email
        const user = await User.findOne({ where: { email } });
        if (!user) {
            return res.status(401).json({ error: 'Invalid email or password' });
        }

        // 2. ตรวจสอบรหัสผ่าน (เทียบสิ่งที่พิมพ์มา กับ Hash ใน DB)
        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            return res.status(401).json({ error: 'Invalid email or password' });
        }

        // 3. รหัสถูก -> แจก Token
        const token = jwt.sign(
            { 
                user_id: user.user_id, 
                role: user.role, 
                family_id: user.family_id 
            },
            process.env.JWT_SECRET,
            { expiresIn: '365d' }
        );

        res.json({
            message: 'Login successful',
            token,
            user: {
                user_id: user.user_id,
                email: user.email,
                nickname: user.nickname,
                role: user.role,
                family_id: user.family_id,
                bank_account_number: user.bank_account_number
            }
        });

    } catch (error) {
        res.status(500).json({ error: 'Login failed', details: error.message });
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
        const user_id = req.params.user_id;
        const user = await User.findByPk(user_id, {
            attributes: { exclude: ['password'] } 
        });

        if (!user) return res.status(404).json({ error: 'User not found' });

        res.json(user);
    } catch (error) {
        res.status(500).json({ error: error.message });
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