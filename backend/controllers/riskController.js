// controllers/riskController.js
const { User, Transaction } = require('../models');
const { sendPushNotification } = require('../services/notificationService');
const aiService = require('../services/aiService'); // เรียกตัวคำนวณกลับมา

// ----------------------------------------------------------------------------
// 1. Analyze (วิเคราะห์ความเสี่ยง)
// ผู้เรียก: พ่อแม่/ผู้สูงอายุ (Role: 'parent')
// เป้าหมาย: ถ้าเสี่ยงสูง -> แจ้งเตือนลูกหลาน (Role: 'child')
// ----------------------------------------------------------------------------
exports.analyze = async (req, res) => {
    try {
        const elderly_id = req.user.user_id; // คนกดคือ Parent
        const { answers, amount, destination } = req.body;

        // 1. Validation
        if (!answers || !Array.isArray(answers) || !amount || !destination) {
            return res.status(400).json({ 
                error: 'Please provide answers (array), amount, and destination' 
            });
        }

        console.log(`Analyzing Transaction for Elderly (Parent) ID: ${elderly_id}`);

        // 2. AI วิเคราะห์
        const aiResult = await aiService.analyzeRisk(answers);
        const risk_score = aiResult.risk_score;

        // 3. กำหนดสถานะ
        let status = 'normal';
        if (risk_score >= 80) {
            status = 'pending_approval';
        }

        // 4. บันทึก Transaction
        const newTrans = await Transaction.create({
            user_id: elderly_id,
            amount,
            destination,
            risk_score,
            status
        });

        // 5. ถ้าเสี่ยงสูง -> แจ้งเตือน "ลูกหลาน" (child)
        if (status === 'pending_approval') {
            const elderlyUser = await User.findByPk(elderly_id);
            
            if (elderlyUser && elderlyUser.family_id) {
                // 🔍 หา "ลูก" ในครอบครัวเดียวกัน
                const childUser = await User.findOne({
                    where: { 
                        family_id: elderlyUser.family_id, 
                        role: 'child' // <--- แจ้งเตือนไปที่ Child
                    }
                });

                if (childUser && childUser.fcm_token) {
                    const title = "🚨 แจ้งเตือนพ่อแม่ทำรายการเสี่ยง!";
                    const body = `คุณพ่อ/แม่ (${elderlyUser.nickname}) กำลังจะโอนเงิน ${amount} บาท (ความเสี่ยง ${risk_score}%) โปรดตรวจสอบด่วน`;
                    
                    const dataPayload = {
                        transaction_id: newTrans.transaction_id.toString(),
                        type: 'risk_alert',
                        risk_score: risk_score.toString()
                    };

                    await sendPushNotification(childUser.fcm_token, title, body, dataPayload);
                    console.log(`Alert sent to Child: ${childUser.nickname}`);
                }
            }
        }

        res.status(201).json({
            message: 'Analysis complete',
            ai_result: aiResult,
            transaction: newTrans
        });

    } catch (error) {
        console.error("Risk Analysis Error:", error);
        res.status(500).json({ error: 'Server error', details: error.message });
    }
};

// ----------------------------------------------------------------------------
// 2. Respond (อนุมัติ/ระงับ)
// ผู้เรียก: ลูกหลาน (Role: 'child')
// เป้าหมาย: อนุมัติรายการของพ่อแม่
// ----------------------------------------------------------------------------
exports.respondToTransaction = async (req, res) => {
    try {
        const child_id = req.user.user_id; // คนกดคือ Child
        const { transaction_id, action } = req.body;

        if (!['approve', 'reject'].includes(action)) {
            return res.status(400).json({ error: 'Action must be approve or reject' });
        }

        // 1. หา Transaction + ข้อมูลพ่อแม่เจ้าของรายการ
        const transaction = await Transaction.findByPk(transaction_id, {
            include: [{ model: User, as: 'user' }] 
        });

        if (!transaction) {
            return res.status(404).json({ error: 'Transaction not found' });
        }

        // 2. Security Check
        const childUser = await User.findByPk(child_id);

        // เช็ค: คนกดต้องเป็น Child (ลูกหลาน)
        if (childUser.role !== 'child') {
            return res.status(403).json({ error: 'Only children can approve transactions' });
        }

        // เช็ค: ต้องอยู่บ้านเดียวกันกับพ่อแม่เจ้าของรายการ
        if (childUser.family_id !== transaction.user.family_id) {
            return res.status(403).json({ error: 'You are not authorized for this family' });
        }

        // เช็ค: ต้องรออนุมัติอยู่
        if (transaction.status !== 'pending_approval') {
            return res.status(400).json({ error: 'Transaction is not pending' });
        }

        // 3. อัปเดตสถานะ
        transaction.status = (action === 'approve') ? 'approved' : 'rejected';
        await transaction.save();

        console.log(`Child ${childUser.nickname} ${action} transaction ${transaction_id}`);

        // 4. แจ้งผลกลับไปที่เครื่องพ่อแม่
        if (transaction.user.fcm_token) {
            const title = action === 'approve' ? "✅ ลูกหลานอนุมัติแล้ว" : "❌ รายการถูกระงับ";
            const body = action === 'approve' 
               ? "ลูกตรวจสอบแล้วว่าปลอดภัย โอนได้เลยครับ" 
               : "ลูกเห็นว่าเสี่ยงเกินไป จึงขอยกเลิกรายการนี้นะครับ";
            
            await sendPushNotification(transaction.user.fcm_token, title, body);
        }

        res.json({
            message: `Transaction ${action} successfully`,
            transaction
        });

    } catch (error) {
        console.error("Approval Error:", error);
        res.status(500).json({ error: 'Server error', details: error.message });
    }
};