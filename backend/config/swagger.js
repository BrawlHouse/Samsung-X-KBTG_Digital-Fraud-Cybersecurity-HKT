// config/swagger.js
const swaggerJsdoc = require('swagger-jsdoc');

const options = {
  definition: {
    openapi: '3.0.0', // เวอร์ชั่นมาตรฐาน
    info: {
      title: 'Scam Guard API Documentation', // ชื่อเอกสาร
      version: '1.0.0',
      description: 'API สำหรับแอปพลิเคชันป้องกันการถูกหลอกโอนเงิน (Scam Guard)',
    },
    servers: [
      {
        url: 'http://localhost:3000',
        description: 'Local Development Server',
      },
    ],
    components: {
      securitySchemes: {
        bearerAuth: {
          type: 'http',
          scheme: 'bearer',
          bearerFormat: 'JWT',
        },
      },
    },
    security: [
      {
        bearerAuth: [], // บังคับใช้ Token กับทุก API (หรือเลือกใส่รายตัวก็ได้)
      },
    ],
  },
  // ให้มันไปหา Comment ในไฟล์ Route ทั้งหมด
  apis: ['./routes/*.js'], 
};

const specs = swaggerJsdoc(options);
module.exports = specs;