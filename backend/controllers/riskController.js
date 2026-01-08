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
        if (risk_score >= 80) status = 'pending_approval'; // ถ้าเสี่ยงสูง ตีเป็น rejected ไว้ก่อนเลย (หรือ pending_approval)

        const newTrans = await Transaction.create({
            user_id: elderly_id,
            amount: amount || 0,
            destination: destination || "Unknown",
            risk_score,
            status
        });

        // ถ้าเสี่ยงสูง -> แจ้งเตือนทุกคนในครอบครัว
        // ถ้าเสี่ยงสูง -> แจ้งเตือนทุกคนในครอบครัว
// หลังจากสร้าง newTrans สำเร็จแล้ว
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
        const body = `${currentUser.nickname} กำลังทำรายการเสี่ยง (${risk_score}%)`;
        
        // <<< แก้ตรงนี้: เพิ่ม transaction_id >>>
        const payload = {
            action: 'risk_alert',
            transaction_id: newTrans.transaction_id.toString(),  // สำคัญมาก!
            risk_score: risk_score.toString(),
            reasons: analysis.reasons.join(", ")
        };

        for (const member of familyMembers) {
            if (member.fcm_token) {
                sendPushNotification(member.fcm_token, title, body, payload);
            }
        }
    }
}
        res.status(201).json({
            message: 'Analysis complete',
            ai_result: { // ใช้ชื่อ key ให้ตรงกับที่ Android รอรับ (ai_result)
                risk_score: risk_score, // ใช้ key ให้ตรง (risk_score)
                riskScore: risk_score,   // เผื่อไว้ทั้งสองแบบ
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
    try {
        const child_id = req.user.user_id;
        const { transaction_id, action } = req.body;

        if (!['approve', 'reject'].includes(action)) {
            return res.status(400).json({ error: 'Action must be approve or reject' });
        }

        const transaction = await Transaction.findByPk(transaction_id, {
            include: [{ model: User, as: 'user' }]
        });

        if (!transaction) {
            return res.status(404).json({ error: 'Transaction not found' });
        }

        // Update status
        transaction.status = (action === 'approve') ? 'approved' : 'rejected';
        await transaction.save();

        // แจ้งเตือนกลับไปหาเจ้าของรายการ (พ่อแม่)
        if (transaction.user && transaction.user.fcm_token) {
            const title = action === 'approve' ? "✅ อนุมัติแล้ว" : "❌ รายการถูกปฏิเสธ";
            const body = action === 'approve' 
                ? "ลูกหลานตรวจสอบแล้วว่าปลอดภัย" 
                : "ลูกหลานมองว่ามีความเสี่ยง จึงระงับรายการ";
            
            await sendPushNotification(transaction.user.fcm_token, title, body);
        }

        res.json({ message: `Transaction ${action} successfully`, transaction });

    } catch (error) {
        console.error("Respond Error:", error);
        res.status(500).json({ error: 'Server error', details: error.message });
    }
};