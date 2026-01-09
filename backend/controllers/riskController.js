const { User, Transaction, sequelize } = require('../models');
const { sendPushNotification } = require('../services/notificationService');
const { Op } = require('sequelize');

// --- Helper Function: เช็ค Keyword ---
function calculateSimpleRisk(answers) {
    let riskScore = 0;
    let reasons = [];

    // Mapping ตามคำถามใหม่:
    // answers[0] : ผู้ติดต่อ (Who) - Input อิสระ
    // answers[1] : ความสัมพันธ์ (Relationship) -> "รู้จักเป็นการส่วนตัว", "ไม่แน่ใจ", "ไม่รู้จัก"
    // answers[2] : อาชีพ (Profession) - Input อิสระ
    // answers[3] : สิ่งที่ร้องขอ (Action) -> "โอนเงิน", "ให้รหัสหรือข้อมูลส่วนตัว", "ติดตั้งแอปพลิเคชัน", "อื่น ๆ"
    // answers[4] : แรงกดดัน (Pressure) -> "มี", "ไม่มี"

    // Helper: Normalize text function (ลบ space, lowercase) เพื่อให้เช็คง่ายขึ้น
    const normalize = (str) => (str || "").toLowerCase().trim();

    const who = normalize(answers[0]);
    const relationship = normalize(answers[1]);
    const profession = normalize(answers[2]);
    const action = normalize(answers[3]);
    const pressure = normalize(answers[4]);

    // --- 1. วิเคราะห์ "ผู้ติดต่อ" (Who) & "ความสัมพันธ์" (Relationship) ---
    // ถ้าตอบว่าเป็นระบบอัตโนมัติ หรือ Admin
    const suspiciousWho = ["ระบบ", "admin", "แอดมิน", "เจ้าหน้าที่", "call center", "sms", "ข้อความ"];
    if (suspiciousWho.some(kw => who.includes(kw))) {
        riskScore += 20;
        reasons.push(`ผู้ติดต่อระบุว่าเป็น ${who} ซึ่งมักเป็นช่องทางของมิจฉาชีพ`);
    }

    // 1. ความสัมพันธ์ (Relationship)
    if (relationship.includes("ไม่รู้จัก")) {
        riskScore += 40;
        reasons.push("การติดต่อจากบุคคลที่ไม่รู้จัก");
    } else if (relationship.includes("ไม่แน่ใจ")) {
        riskScore += 20;
        reasons.push("ไม่แน่ใจในความสัมพันธ์ของผู้ติดต่อ");
    }

    // --- 2. วิเคราะห์ "อาชีพ/หน่วยงาน" (Profession) ---
    // เพิ่ม Keyword ครอบคลุม Brand ขนส่ง, E-commerce, หน่วยงานรัฐ, ยูทิลิตี้
    const dangerKeywords = [
        // หน่วยงานรัฐ/กฎหมาย
        "ตำรวจ", "ศาล", "dsi", "สรรพากร", "ปปง", "กสทช", "คดี", "ฟอกเงิน",
        // ธนาคาร/การเงิน
        "ธนาคาร", "แบงก์", "กู้", "สินเชื่อ", "บัตรเครดิต", "อายัด",
        // ขนส่ง
        "ไปรษณีย์", "fedex", "kerry", "flash", "dhl", "พัสดุ", "ตกค้าง", "ตีกลับ",
        // ผู้ให้บริการ/ค่ายมือถือ/เน็ต
        "ais", "true", "dtac", "nt", "tot", "3bb", "การไฟฟ้า", "การประปา",
        // E-commerce/Social
        "tiktok", "shopee", "lazada", "facebook", "line", "instagram", "รางวัล"
    ];

    if (dangerKeywords.some(kw => profession.includes(kw))) {
        riskScore += 30;
        reasons.push(`มีการแอบอ้างชื่อหน่วยงาน/องค์กร (${profession})`);
    }

    // --- 3. วิเคราะห์ "สิ่งที่ร้องขอ" (Action) ---

    // A. ติดตั้งแอป/ลิงก์ (High Risk)
    const installKeywords = ["ติดตั้ง", "โหลด", "install", "apk", "แอป", "แอพ", "ลิงก์", "link", "เว็บไซต์", "www"];
    if (installKeywords.some(kw => action.includes(kw))) {
        riskScore += 50;
        reasons.push("มีการส่งลิงก์หรือให้ติดตั้งโปรแกรมแปลกปลอม");
    }

    // B. เรื่องเงิน (Transfer/Loan)
    const moneyKeywords = ["โอน", "เงิน", "บัญชี", "ลงทุน", "ออม", "ปันผล", "กำไร", "บาท", "ค่าปรับ", "ภาษี"];
    if (moneyKeywords.some(kw => action.includes(kw))) {
        riskScore += 30;
        reasons.push("มีเนื้อหาเกี่ยวกับการโอนเงินหรือการลงทุน");
    }

    // C. ข้อมูลส่วนตัว (Privacy)
    const privacyKeywords = ["รหัส", "password", "otp", "บัตรประชาชน", "เลขบัญชี", "ที่อยู่", "ยืนยันตัวตน", "สแกนหน้า"];
    if (privacyKeywords.some(kw => action.includes(kw))) {
        riskScore += 40;
        reasons.push("มีการขอข้อมูลส่วนตัวหรือรหัสผ่าน");
    }

    // D. ให้แอดไลน์ (Add Line) - พบบ่อยในมิจฉาชีพ
    if (action.includes("line") || action.includes("ไลน์") || action.includes("id")) {
        riskScore += 20;
        reasons.push("มีการพยายามดึงเข้าสนทนาทาง Line");
    }

    // --- 4. แรงกดดัน (Pressure) ---
    if (pressure === "มี") {
        riskScore += 30;
        reasons.push("มีการสร้างสถานการณ์เร่งด่วนหรือข่มขู่");
    }

    // Cap คะแนนสูงสุดที่ 100
    if (riskScore > 100) riskScore = 100;

    return { score: riskScore, reasons: reasons };
}

// ---------------------------------------------------
// 1. Analyze (วิเคราะห์ความเสี่ยง - เรียกโดยพ่อแม่)
// ---------------------------------------------------
exports.analyze = async (req, res) => {
    try {
        const elderly_id = req.user.user_id;
        const answers = req.body.answers;

        // Validation
        if (!answers || !Array.isArray(answers)) {
            return res.status(400).json({ error: 'Invalid answers format' });
        }

        // คำนวณความเสี่ยง
        const analysis = calculateSimpleRisk(answers);
        const risk_score = analysis.score;

        console.log(`Risk Score: ${risk_score} (User: ${elderly_id})`);

        // บันทึก Transaction
        let status = 'allow';
        if (risk_score >= 80) {
            status = 'waiting';
            const targetUser = await User.findByPk(elderly_id);
            if (!targetUser) {
                return res.status(404).json({ error: "User not found" });
            }

            targetUser.status = status;
            await targetUser.save();
        }

        const newTrans = await Transaction.create({
            user_id: elderly_id,
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
                    message: JSON.stringify(answers).replaceAll('"', ''), // array ต้องแปลงเป็น string ก่อนส่งผ่าน FCM
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
        if (!['allow', 'reject'].includes(action)) {
            await t.rollback();
            return res.status(400).json({ error: 'Action must be allow or reject' });
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
        const newStatus = (action === 'allow') ? 'allow' : 'reject';
        transaction.status = newStatus;
        await transaction.save({ transaction: t });

        // 5. Update User Status (แม่)
        if (transaction.user) {
            const userStatus = (action === 'allow') ? 'allow' : 'reject';
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
        if (action === 'allow' || action === 'reject' && transaction.user) {
            const TIMEOUT_MINUTES = 5;
            const targetUserId = transaction.user.user_id;

            console.log(`⏳ Timer started: Will reset User ${targetUserId} to normal in ${TIMEOUT_MINUTES} mins.`);

            setTimeout(async () => {
                try {
                    // ต้อง Query ใหม่เพื่อดูสถานะล่าสุด (เผื่อแม่กดจบไปเองแล้ว)
                    const userCheck = await User.findByPk(targetUserId);

                    // ถ้ายังเป็น allow อยู่ แปลว่าหมดเวลาแล้วแม่ยังไม่จบงาน -> ระบบตัดจบให้
                    if (userCheck && userCheck.status === 'allow' && userCheck.status === 'reject') {
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
            const title = action === 'allow' ? "✅ อนุมัติแล้ว" : "❌ รายการถูกระงับ";
            const body = action === 'allow'
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

        // C. เปลี่ยน status จาก table users
        

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