const OpenAI = require('openai'); // ต้อง install: npm install openai

// ควรเอา Key ไปไว้ใน .env
const openai = new OpenAI({
  apiKey: process.env.OPENAI_API_KEY, 
});

exports.analyzeMessage = async (req, res) => {
  try {
    const { text } = req.body;
    const input = text;

    // 1. Validate Input
    if (!input || typeof input !== 'string') {
      return res.status(400).json({ error: 'Input text is required' });
    }

    // 2. สร้าง Prompt เพื่อสั่ง AI
    // เทคนิค: สั่งให้ AI ตอบกลับมาเป็น JSON เท่านั้น เพื่อให้ Backend เอาไปใช้ง่ายๆ
    const systemPrompt = `
      You are a scam detection expert specialized in Thai SMS and messages.
      Analyze the given text and determine the probability of it being a scam or call center fraud.
      
      Return ONLY a JSON object with this structure (no markdown, no explanations outside JSON):
      {
        "percentage": <number 0-100>,
        "level": "<string: 'low', 'medium', 'high'>",
        "reason": "<short reason in Thai>"
      }

      Criteria:
      - High: Links to weird URLs (bit.ly without context), asking for money/transfer, claiming urgent bank issues/police/post office.
      - Medium: Vague promises, job offers via SMS, unknown sender claiming to know you.
      - Low: OTPs requested by user, normal shipping updates, known contacts, normal conversation.
    `;

    // 3. เรียก AI (ในที่นี้ใช้ GPT-3.5-turbo หรือ GPT-4o-mini เพื่อความประหยัดและเร็ว)
    const completion = await openai.chat.completions.create({
      model: "gpt-4o-mini", // หรือ gpt-3.5-turbo
      messages: [
        { role: "system", content: systemPrompt },
        { role: "user", content: input },
      ],
      response_format: { type: "json_object" }, // บังคับตอบเป็น JSON (ฟีเจอร์ของ OpenAI)
    });

    // 4. แปลงผลลัพธ์
    const aiResponse = JSON.parse(completion.choices[0].message.content);

    // 5. ส่งกลับ Client (โดยไม่บันทึกลง Database ตามโจทย์)
    return res.json({
      percentage: aiResponse.percentage,
      level: aiResponse.level,
      reason: aiResponse.reason ,// แถมเหตุผลให้ Frontend ไปโชว์ได้ด้วย
      is_risk: aiResponse.percentage >= 50
    });

  } catch (error) {
    console.error('AI Analysis Error:', error);
    // กรณี AI พัง หรือเน็ตหลุด ให้ Return แบบ default ไปก่อนเพื่อไม่ให้แอปค้าง
    return res.status(500).json({ 
        risk_score: 0, 
        level: "unknown", 
        reasons: "AI Service Error",
        is_risk: false
    });
  }
};