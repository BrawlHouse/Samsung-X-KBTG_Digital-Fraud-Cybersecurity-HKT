const { Family, User, sequelize } = require('../models');

// 1. สร้างครอบครัวใหม่ (POST /family)
exports.createFamily = async (req, res) => {
    const t = await sequelize.transaction();
    try {
        const userId = req.user.user_id; // ได้มาจาก Middleware

        // สร้าง Family ใหม่
        const newFamily = await Family.create({}, { transaction: t });

        // อัปเดต User คนที่สร้าง ให้เป็นหัวหน้าครอบครัว (หรือแค่มี family_id)
        await User.update(
            { family_id: newFamily.family_id },
            { where: { user_id: userId }, transaction: t }
        );

        await t.commit();

        res.status(201).json({
            message: 'Family created successfully',
            family_id: newFamily.family_id
        });

    } catch (error) {
        await t.rollback();
        res.status(500).json({ error: 'Failed to create family', details: error.message });
    }
};

// 2. ดูสมาชิกในครอบครัว (GET /family/members)
exports.getMembers = async (req, res) => {
    try {
        // ดึง family_id จาก User ปัจจุบัน (ต้องไป query ใหม่เผื่อ token เก่าไม่อัปเดต)
        const currentUser = await User.findByPk(req.user.user_id);
        const familyId = currentUser.family_id;

        if (!familyId) {
            return res.status(400).json({ error: 'You are not in a family yet' });
        }

        // หา User ทุกคนที่มี family_id เดียวกัน
        const members = await User.findAll({
            where: { family_id: familyId },
            attributes: ['user_id', 'nickname', 'role', 'device_id'] // เลือกเอาแค่ข้อมูลที่จำเป็น
        });

        res.json({
            family_id: familyId,
            members: members
        });

    } catch (error) {
        res.status(500).json({ error: 'Failed to fetch members', details: error.message });
    }
};

// 3. เพิ่มสมาชิกเข้าครอบครัว (POST /family/members)
exports.addMember = async (req, res) => {
    try {
        // รับ user_id ของคนที่จะดึงเข้า, และ family_id เป้าหมาย
        const { user_id, family_id } = req.body; 

        if (!user_id || !family_id) {
            return res.status(400).json({ error: 'Please provide user_id and family_id' });
        }

        // อัปเดต User คนนั้นให้มี family_id
        await User.update(
            { family_id: family_id },
            { where: { user_id: user_id } }
        );

        res.json({ message: `User ${user_id} added to family ${family_id} successfully` });

    } catch (error) {
        res.status(500).json({ error: 'Failed to add member', details: error.message });
    }
};