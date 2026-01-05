const aiService = require('../services/aiService');

exports.analyze = (req, res) => {
    try {
        const { answers } = req.body;

        // Validation: ต้องมีคำตอบมา
        if (!answers || !Array.isArray(answers)) {
            return res.status(400).json({ error: 'Please provide answers as an array' });
        }

        // เรียกใช้ AI Service
        const result = aiService.analyzeRisk(answers);

        // ส่งผลลัพธ์กลับทันที (Stateless API ไม่บันทึกลง DB ตามโจทย์)
        res.json(result);

    } catch (error) {
        res.status(500).json({ error: 'Analysis failed', details: error.message });
    }
};