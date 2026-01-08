exports.analyzeRisk = (answers) => {
    // answers คือ array ของคำตอบ 5 ข้อจากหน้าบ้าน
    // ตัวอย่าง: ["เบอร์แปลก", "ไม่รู้จัก", "ตำรวจ", "ให้โอนเงินตรวจสอบ", "ด่วนมากห้ามบอกใคร"]
    
    let score = 0;
    let reasons = [];

    // รวมคำตอบทั้งหมดเป็นข้อความเดียวเพื่อง่ายต่อการหา keyword
    const combinedText = answers.join(' ');

    // --- Logic การให้คะแนน (Mock AI) ---
    
    // 1. เช็คเรื่องเงิน (สำคัญสุด)
    if (combinedText.includes('โอน') || combinedText.includes('บัญชี') || combinedText.includes('เงิน')) {
        score += 40;
        reasons.push('มีการขอให้ทำธุรกรรมทางการเงิน');
    }

    // 2. เช็คการแอบอ้าง
    if (combinedText.includes('ตำรวจ') || combinedText.includes('ศาล') || combinedText.includes('ไปรษณีย์')) {
        score += 30;
        reasons.push('มีการอ้างเป็นเจ้าหน้าที่รัฐ');
    }

    // 3. เช็คความเร่งด่วน/ความลับ
    if (combinedText.includes('ด่วน') || combinedText.includes('ห้ามบอก') || combinedText.includes('ความลับ')) {
        score += 30;
        reasons.push('มีการเร่งรัดหรือขอให้ปิดบังข้อมูล');
    }

    // --- สรุปผล ---
    let level = 'LOW';
    if (score >= 80) level = 'HIGH';
    else if (score >= 40) level = 'MEDIUM';

    // ถ้าไม่มีเหตุผลเลย (score 0)
    if (reasons.length === 0) {
        reasons.push('ไม่พบความเสี่ยงที่ชัดเจนจากข้อมูลเบื้องต้น');
    }

    return {
        risk_score: score,
        level: level,
        reasons: reasons
    };
};