// controllers/riskController.js
const { User, Transaction } = require('../models');
const { sendPushNotification } = require('../services/notificationService');
const aiService = require('../services/aiService'); // เรียกตัวคำนวณกลับมา

exports.analyze = async (req, res) => {
    try {
        const user_id = req.user.user_id; 
        // รับทั้ง "คำตอบ" และ "ข้อมูลธุรกรรม"
        const { answers, amount, destination } = req.body;

        // 1. Validation
        if (!answers || !Array.isArray(answers) || !amount || !destination) {
            return res.status(400).json({ 
                error: 'Please provide answers (array), amount, and destination' 
            });
        }

        console.log(`Analyzing Transaction for User ${user_id}`);

        // 2. คำนวณความเสี่ยง (Logic เดิมของคุณ)
        // ผลลัพธ์ที่ได้จะเป็น object เช่น { risk_score: 80, level: 'HIGH', ... }
        const aiResult = aiService.analyzeRisk(answers);
        const risk_score = aiResult.risk_score;

        // 3. กำหนดสถานะ (Logic ใหม่)
        let status = 'normal';
        if (risk_score >= 80) {
            status = 'pending_approval';
        }

        // 4. บันทึก Transaction ลง Database (ใช้ score ที่คำนวณได้)
        const newTrans = await Transaction.create({
            user_id,
            amount,
            destination,
            risk_score,
            status
        });

        // 5. ถ้าเสี่ยงสูง -> แจ้งเตือนพ่อแม่
        if (status === 'pending_approval') {
            const child = await User.findByPk(user_id);
            if (child && child.family_id) {
                const parent = await User.findOne({
                    where: { family_id: child.family_id, role: 'parent' }
                });

                if (parent && parent.fcm_token) {
                    const title = "🚨 แจ้งเตือนธุรกรรมเสี่ยง!";
                    const body = `น้อง ${child.nickname} จะโอน ${amount} บาท (ความเสี่ยง ${risk_score}%) จากการตอบว่า: ${answers.join(', ')}`;
                    
                    const dataPayload = {
                        transaction_id: newTrans.transaction_id.toString(),
                        type: 'risk_alert',
                        risk_score: risk_score.toString()
                    };

                    await sendPushNotification(parent.fcm_token, title, body, dataPayload);
                }
            }
        }

        // ส่งผลกลับไป ทั้งผลวิเคราะห์ และ ข้อมูล Transaction
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





exports.respondToTransaction = async (req, res) => {
    try {
        // รับค่าจากพ่อแม่ (User ID จาก Token)
        const parent_id = req.user.user_id; 
        const { transaction_id, action } = req.body; // action: 'approve' หรือ 'reject'

        // 1. Validation
        if (!['approve', 'reject'].includes(action)) {
            return res.status(400).json({ error: 'Action must be approve or reject' });
        }

        // 2. หา Transaction ที่จะอนุมัติ
        const transaction = await Transaction.findByPk(transaction_id, {
            include: [{ model: User, as: 'user' }] // join เพื่อเอาข้อมูลลูก
        });

        if (!transaction) {
            return res.status(404).json({ error: 'Transaction not found' });
        }

        // 3. Security Check: คนกดต้องเป็นพ่อแม่ของเจ้าของ Transaction นี้จริงๆ
        // หาข้อมูลพ่อแม่
        const parent = await User.findByPk(parent_id);
        
        // เช็คว่าอยู่บ้านเดียวกันไหม?
        if (parent.family_id !== transaction.user.family_id) {
            return res.status(403).json({ error: 'You are not authorized to approve this transaction' });
        }

        // เช็คสถานะปัจจุบัน (ต้องเป็น pending เท่านั้นถึงจะกดได้)
        if (transaction.status !== 'pending_approval') {
            return res.status(400).json({ error: 'Transaction is not in pending state' });
        }

        // 4. อัปเดตสถานะ
        transaction.status = (action === 'approve') ? 'approved' : 'rejected';
        await transaction.save();

        console.log(`Parent ${parent.nickname} ${action} transaction ${transaction_id}`);

        // (Optional) ตรงนี้อาจจะยิง Noti กลับไปบอกลูกว่า "พ่ออนุมัติแล้วนะ" ก็ได้

        res.json({
            message: `Transaction ${action} successfully`,
            transaction
        });

    } catch (error) {
        console.error("Approval Error:", error);
        res.status(500).json({ error: 'Server error', details: error.message });
    }
};