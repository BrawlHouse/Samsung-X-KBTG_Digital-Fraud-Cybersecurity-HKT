const { Family, User, sequelize } = require('../models');
const generateInviteCode = require('../services/codeGenerator');

exports.createFamily = async (req, res) => {
    try {
        const user_id = req.user.user_id;

        // สุ่มรหัส Invite (วนลูปเช็คเพื่อกันซ้ำแบบ 100%)
        let invite_code;
        let isUnique = false;
        while (!isUnique) {
            invite_code = generateInviteCode();
            const existing = await Family.findOne({ where: { invite_code } });
            if (!existing) isUnique = true;
        }

        // สร้าง Family
        const newFamily = await Family.create({
            invite_code: invite_code
        });

        // อัปเดต User คนที่สร้าง ให้เข้าไปอยู่ในบ้านนี้เลย
        await User.update(
            { family_id: newFamily.family_id },
            { where: { user_id: user_id } }
        );

        res.status(201).json({
            message: 'Family created successfully',
            family_id: newFamily.family_id,
            invite_code: newFamily.invite_code, // <--- ส่งอันนี้ให้ frontend เอาไปโชว์
            created_at: newFamily.created_at || newFamily.createdAt,
            updated_at: newFamily.updated_at || newFamily.updatedAt
        });

    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};


exports.joinFamily = async (req, res) => {
    try {
        const user_id = req.user.user_id;
        const { invite_code } = req.body; // รับโค้ดที่ user กรอกมา

        // หาบ้านจากโค้ด
        const family = await Family.findOne({ where: { invite_code } });

        if (!family) {
            return res.status(404).json({ error: 'Invalid invite code' });
        }

        // อัปเดต User ให้เข้าบ้านนั้น
        await User.update(
            { family_id: family.family_id },
            { where: { user_id: user_id } }
        );

        res.json({
            message: 'Joined family successfully',
            family: family
        });

    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};


// ดูสมาชิกในครอบครัว
exports.getMembers = async (req, res) => {
    try {
        const userId = req.user.user_id;

        // 2. ⚠️ สำคัญ: Query User ใหม่จาก Database เพื่อเอา family_id ล่าสุด!
        // (เพราะ family_id ใน Token อาจจะเก่า ถ้าเพิ่งกด Join มา)
        const user = await User.findByPk(userId);

        if (!user || !user.family_id) {
            return res.status(400).json({ error: 'You are not in a family' });
        }

        const familyId = user.family_id; // ใช้ค่าสดๆ จาก DB

        if (!familyId) {
            return res.status(400).json({ error: 'You are not in a family' });
        }

        const family = await Family.findByPk(familyId);
        if (!family) {
            return res.status(404).json({ error: 'Family not found' });
        }

        const members = await User.findAll({
            where: { family_id: familyId },
            attributes: [
                'user_id',
                'nickname',
                'role',
                'device_id',
                'email',
                'bank_account_number'
            ]
        });

        res.status(200).json({
            family_id: familyId,
            invite_code: family.invite_code,
            members
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Internal Server Error' });
    }
}


// ลบสมาชิกออกจากครอบครัว
exports.removeMember = async (req, res) => {
    try {
        // ✅ แก้: ไม่ต้องรับ familyId จาก params แล้ว ใช้จาก Token เลย
        const requesterFamilyId = req.user.family_id; 
        const requesterId = req.user.user_id; 
        const requesterRole = req.user.role;
        
        const targetUserId = parseInt(req.params.user_id); // รับแค่ ID คนที่จะโดนลบ

        if (!requesterFamilyId) {
            return res.status(400).json({ error: 'You are not in a family' });
        }

        const targetUser = await User.findByPk(targetUserId);
        if (!targetUser) {
            return res.status(404).json({ error: 'User not found' });
        }

        // เช็คว่าคนที่จะโดนลบ อยู่บ้านเดียวกับเราไหม
        if (targetUser.family_id !== requesterFamilyId) {
            return res.status(403).json({ error: 'Cannot remove user from another family' });
        }

        // เช็คสิทธิ์ (Logic เดิม: ลบตัวเองได้ หรือ Child ลบคนอื่นได้)
        const isSelfRemove = requesterId === targetUserId;
        const isGuardian = requesterRole === 'child'; 

        if (!isSelfRemove && !isGuardian) {
            return res.status(403).json({ 
                error: 'Only guardian (child) can remove other members' 
            });
        }

        await targetUser.update({ family_id: null });

        res.status(200).json({
            message: 'Member removed successfully'
        });

    } catch (err) {
        console.error("Remove Member Error:", err);
        res.status(500).json({ error: 'Internal Server Error' });
    }
};



exports.getFamilyChildren = async (req, res) => {
  try {
    // 1. แกะ ID คนเรียกมาจาก JWT (Middleware ทำให้แล้ว)
    const myId = req.user.id; 
    
    // 2. หาข้อมูลของคนเรียกก่อน เพื่อเอา family_id
    // (สมมติใช้ Sequelize หรือ ORM อื่นๆ)
    const me = await User.findByPk(myId);

    if (!me || !me.family_id) {
      return res.status(404).json({ error: "User or Family not found" });
    }

    const myFamilyId = me.family_id;

    // 3. Query หาพี่น้อง/ลูกหลาน ตามเงื่อนไข
    // เงื่อนไข: family เดียวกัน + เป็น CHILD + ไม่ใช่ตัวฉันเอง
    const children = await User.findAll({
      where: {
        family_id: myFamilyId,     // 1. บ้านเดียวกัน
        role: 'child',             // 2. เอาเฉพาะเด็ก (ไม่เอา Parent)
        id: { [Op.ne]: myId }      // 3. id ต้องไม่เท่ากับ (Not Equal) ฉัน
      },
      attributes: ['user_id', 
        'nickname', 
        'email', 
        'role',] 
    });

    // 4. ส่ง response
    return res.json({
      family_id: myFamilyId,
      users: children
    });

  } catch (error) {
    console.error(error);
    return res.status(500).json({ error: "Server Error" });
  }
};