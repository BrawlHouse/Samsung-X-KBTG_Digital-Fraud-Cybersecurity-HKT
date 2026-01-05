const { Family, User, sequelize } = require('../models');

async function createFamily(req, res) {
    const t = await sequelize.transaction();
    try {
        const userId = req.user.user_id;

        const user = await User.findByPk(userId);
        if (user.family_id) {
            await t.rollback();
            return res.status(400).json({ error: 'User already belongs to a family' });
        }

        const family = await Family.create({}, { transaction: t });

        await user.update(
            { family_id: family.family_id },
            { transaction: t }
        );

        await t.commit();
        res.status(201).json({
            message: 'Family created successfully',
            family_id: family.family_id
        });
    } catch (err) {
        await t.rollback();
        res.status(500).json({ error: 'Internal Server Error' });
    }
}

// ดู Family ของ User
async function getFamilyInfo(req, res) {
    try {
        const { user_id: userId } = req.params;

        const user = await User.findByPk(userId);
        if (!user) return res.status(404).json({ error: 'User not found' });
        if (!user.family_id) return res.status(404).json({ error: 'User has no family' });

        res.json({
            user_id: user.user_id,
            family_id: user.family_id
        });
    } catch (err) {
        res.status(500).json({ error: 'Internal Server Error' });
    }
}

// ดูสมาชิกในครอบครัว
async function getMembers(req, res) {
    try {
        const { family_id: familyId } = req.params;

        const members = await User.findAll({
            where: { family_id: familyId },
            attributes: ['user_id', 'nickname', 'role', 'device_id']
        });

        res.json({
            family_id: familyId,
            members
        });
    } catch (err) {
        res.status(500).json({ error: 'Internal Server Error' });
    }
}

// เพิ่มสมาชิกเข้าครอบครัว
async function addMember(req, res) {
    try {
        const { family_id: familyId } = req.params;
        const { user_id: userId } = req.body;

        if (!userId) {
            return res.status(400).json({ error: 'Please provide user_id' });
        }

        const user = await User.findByPk(userId);
        if (!user) return res.status(404).json({ error: 'User not found' });
        if (user.family_id) {
            return res.status(400).json({ error: 'User already belongs to a family' });
        }

        await user.update({ family_id: familyId });

        res.json({ message: 'User added to family successfully' });
    } catch (err) {
        res.status(500).json({ error: 'Internal Server Error' });
    }
}

// ลบสมาชิกออกจากครอบครัว
async function removeMember(req, res) {
    try {
        const { family_id: familyId, user_id: userId } = req.params;

        const user = await User.findByPk(userId);
        if (!user) return res.status(404).json({ error: 'User not found' });

        if (user.family_id != familyId) {
            return res.status(400).json({ error: 'User is not in this family' });
        }

        await user.update({ family_id: null });

        res.json({ message: 'Member removed successfully' });
    } catch (err) {
        res.status(500).json({ error: 'Internal Server Error' });
    }
}


module.exports = {
    createFamily,
    addMember,
    removeMember,
    getFamilyInfo,
    getMembers
};