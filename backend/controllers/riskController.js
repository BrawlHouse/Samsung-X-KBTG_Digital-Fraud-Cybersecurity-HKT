const { User, Transaction, sequelize } = require('../models');
const { sendPushNotification } = require('../services/notificationService');
const { Op } = require('sequelize');

// --- Helper Function: เช็ค Keyword ---
function calculateSimpleRisk(answers) {
    let riskScore = 0;
    let reasons = [];
    
    // answers[0] = Who, [1] = Relationship, [2] = Profession, [3] = Action, [4] = Urgency
    const relationship = answers[1] || "";
    const profession = answers[2] || "";
    const action = answers[3] || "";
    const urgency = answers[4] || "";

    // 1. ความสัมพันธ์
    if (relationship.includes("ไม่รู้จัก") || relationship.includes("ไม่แน่ใจ")) {
        riskScore += 30;
        reasons.push("ติดต่อจากคนไม่รู้จัก");
    }

    // 2. อาชีพ
    const dangerJobs = ["ตำรวจ", "ไปรษณีย์", "ศาล", "DSI", "สรรพากร", "คอลเซ็นเตอร์", "ธนาคาร", "เจ้าหน้าที่"];
    if (dangerJobs.some(job => profession.includes(job))) {
        riskScore += 40;
        reasons.push(`มีการแอบอ้างเป็น ${profession}`);
    }

    // 3. เรื่องเงิน
    if (action.includes("โอนเงิน") || action.includes("ข้อมูลส่วนตัว")) {
        riskScore += 20;
    }

    // 4. เร่งรีบ
    if (urgency.includes("มี") || urgency.includes("ข่มขู่") || urgency.includes("เร่ง")) {
        riskScore += 30;
        reasons.push("มีการสร้างความกดดัน/เร่งรีบ");
    }

    if (riskScore > 100) riskScore = 100;

    return { score: riskScore, reasons: reasons };
}

// ---------------------------------------------------
// 1. Analyze (วิเคราะห์ความเสี่ยง - เรียกโดยพ่อแม่)
// ---------------------------------------------------
exports.analyze = async (req, res) => {
    try {
        const elderly_id = req.user.user_id;
        const { answers, amount, destination } = req.body;

        // Validation
        if (!answers || !Array.isArray(answers)) {
            return res.status(400).json({ error: 'Invalid answers format' });
        }

        // คำนวณความเสี่ยง
        const analysis = calculateSimpleRisk(answers);
        const risk_score = analysis.score;

        console.log(`Risk Score: ${risk_score} (User: ${elderly_id})`);

        // บันทึก Transaction
        let status = 'normal';
        if (risk_score >= 80) status = 'waiting'; 

        const newTrans = await Transaction.create({
            user_id: elderly_id,
            amount: amount || 0,
            destination: destination || "Unknown",
            risk_score,
            status
        });

        // ถ้าเสี่ยงสูง -> แจ้งเตือนทุกคนในครอบครัว
        if (risk_score >= 80) {
            const currentUser = await User.findByPk(elderly_id);
            
            if (currentUser && currentUser.family_id) {
                const familyMembers = await User.findAll({
                    where: {
                        family_id: currentUser.family_id,
                        user_id: { [Op.ne]: elderly_id }
                    }
                });

                const title = "🚨 พบความเสี่ยงระดับสูง!";
                const body = `${currentUser.nickname} กำลังทำรายการเสี่ยง`;

                const payload = {
                    type: 'risk_alert',           
                    nickname: currentUser.nickname,
                    message: JSON.stringify(answers), // array ต้องแปลงเป็น string ก่อนส่งผ่าน FCM
                    
                    // แอบแถม transaction_id ไปด้วย (สำคัญมาก ไม่งั้นลูกกด Approve ไม่ได้)
                    transaction_id: newTrans.transaction_id.toString()
                };

                for (const member of familyMembers) {
                    if (member.fcm_token) {
                        // ส่ง payload ไปกับ notification
                        sendPushNotification(member.fcm_token, title, body, payload);
                    }
                }
            }
        }

        res.status(201).json({
            message: 'Analysis complete',
            ai_result: { 
                risk_score: risk_score,
                level: risk_score >= 80 ? 'HIGH' : 'LOW',
                reasons: analysis.reasons
            },
            transaction: newTrans
        });

    } catch (error) {
        console.error("Risk Analysis Error:", error);
        res.status(500).json({ error: 'Server error', details: error.message });
    }
};

// ---------------------------------------------------
// 2. Respond (อนุมัติ/ระงับ - เรียกโดยลูกหลาน)
// ---------------------------------------------------
exports.respondToTransaction = async (req, res) => {
    // 1. เริ่ม Database Transaction
    const t = await sequelize.transaction();

    try {
        const child_id = req.user.user_id; 
        const child_info = await User.findByPk(child_id); 
        
        const { transaction_id, action } = req.body;

        // Validation Input
        if (!['approve', 'reject'].includes(action)) {
            await t.rollback(); 
            return res.status(400).json({ error: 'Action must be approve or reject' });
        }

        // 2. ค้นหา Transaction + Lock (Pessimistic Locking)
        const transaction = await Transaction.findByPk(transaction_id, {
            lock: true, 
            transaction: t,
            include: [{ model: User, as: 'user' }] 
        });

        if (!transaction) {
            await t.rollback();
            return res.status(404).json({ error: 'Transaction not found' });
        }

        // 3. เช็ค Race Condition
        if (transaction.status !== 'waiting') {
            await t.rollback();
            return res.status(409).json({ 
                error: 'รายการนี้ถูกจัดการไปแล้วโดยสมาชิกอื่น',
                current_status: transaction.status
            });
        }

        // 4. Update Transaction Status
        const newStatus = (action === 'approve') ? 'allow' : 'rejected';
        transaction.status = newStatus;
        await transaction.save({ transaction: t });

        // 5. Update User Status (แม่)
        if (transaction.user) {
            const userStatus = (action === 'approve') ? 'allow' : 'normal';
            await User.update(
                { status: userStatus },
                { where: { user_id: transaction.user.user_id }, transaction: t }
            );
        }

        // 6. Commit Database (บันทึกข้อมูลทั้งหมดลง DB จริงๆ)
        await t.commit(); 

        // =========================================================
        // ✨ ส่วนที่เพิ่ม: Auto-Reset Status (Safety Net) ✨
        // =========================================================
        // ถ้าอนุมัติ (Approve) เราจะตั้งเวลา 5 นาที เพื่อดีดสถานะแม่กลับเป็น normal
        // ป้องกันกรณีแม่ลืมกดออก หรือแอปแม่ค้าง เดี๋ยวเครื่องจะ allow ยาว
        if (action === 'approve' && transaction.user) {
            const TIMEOUT_MINUTES = 5; 
            const targetUserId = transaction.user.user_id;

            console.log(`⏳ Timer started: Will reset User ${targetUserId} to normal in ${TIMEOUT_MINUTES} mins.`);

            setTimeout(async () => {
                try {
                    // ต้อง Query ใหม่เพื่อดูสถานะล่าสุด (เผื่อแม่กดจบไปเองแล้ว)
                    const userCheck = await User.findByPk(targetUserId);
                    
                    // ถ้ายังเป็น allow อยู่ แปลว่าหมดเวลาแล้วแม่ยังไม่จบงาน -> ระบบตัดจบให้
                    if (userCheck && userCheck.status === 'allow') {
                        await userCheck.update({ status: 'normal' });
                        console.log(`⏰ Auto-reset: User ${targetUserId} status force reset to normal.`);
                    } else {
                        console.log(`ℹ️ Auto-reset skipped: User ${targetUserId} is already ${userCheck.status}.`);
                    }
                } catch (err) {
                    console.error("Auto-reset error:", err);
                }
            }, TIMEOUT_MINUTES * 60 * 1000); // แปลงนาทีเป็น Milliseconds
        }
        // =========================================================


        // --- โซน Notification (ส่งหลังจาก Commit DB) ---

        // A. แจ้งเตือนกลับไปหา "แม่"
        if (transaction.user && transaction.user.fcm_token) {
            const title = action === 'approve' ? "✅ อนุมัติแล้ว" : "❌ รายการถูกระงับ";
            const body = action === 'approve' 
                ? `${child_info.nickname} อนุญาตให้ทำรายการได้` 
                : `${child_info.nickname} มองว่ามีความเสี่ยง จึงระงับรายการ`;
            
            const payload = {
                type: 'decision_result',
                action: action,
                approver: child_info.nickname || 'ลูกหลาน'
            };
            
            sendPushNotification(transaction.user.fcm_token, title, body, payload).catch(console.error);
        }

        // B. แจ้งเตือนหา "ลูกคนอื่น" (Sync หน้าจอ)
        if (child_info.family_id) {
            const siblings = await User.findAll({
                where: {
                    family_id: child_info.family_id,
                    user_id: { [Op.notIn]: [child_id, transaction.user.user_id] }
                }
            });

            for (const sibling of siblings) {
                if (sibling.fcm_token) {
                    const syncPayload = {
                        type: 'transaction_handled',
                        transaction_id: transaction_id.toString(),
                        status: newStatus,
                        handled_by: child_info.nickname
                    };
                    sendPushNotification(sibling.fcm_token, null, null, syncPayload).catch(console.error);
                }
            }
        }

        res.json({ 
            message: `Transaction ${action} successfully`, 
            transaction: transaction 
        });

    } catch (error) {
        if (t && !t.finished) await t.rollback(); 
        console.error("Respond Error:", error);
        res.status(500).json({ error: 'Server error', details: error.message });
    }
};