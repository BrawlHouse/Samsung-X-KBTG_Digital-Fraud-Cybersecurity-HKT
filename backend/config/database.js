const { Sequelize } = require('sequelize');
require('dotenv').config();

const sequelize = new Sequelize(
    process.env.DB_NAME,
    process.env.DB_USER,
    process.env.DB_PASS,
    {
        host: process.env.DB_HOST,
        dialect: 'mysql',
        logging: false, // ปิด log sql รกๆ ใน console (เปิดเป็น true ถ้าอยากเห็น)
        timezone: '+07:00' // เวลาไทย
    }
);

module.exports = sequelize;