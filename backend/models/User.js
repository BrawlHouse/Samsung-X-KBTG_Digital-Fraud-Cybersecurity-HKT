const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');
const Family = require('./Family');
const Device = require('./Device');

const User = sequelize.define('User', {
    user_id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    nickname: {
        type: DataTypes.STRING,
        allowNull: false
    },
    role: {
        type: DataTypes.ENUM('parent', 'child'),
        allowNull: false
    },
    // ใช้เก็บ Token ยาวๆ ที่ได้จาก Firebase เอาไว้ยิง Notification
    fcm_token: {
        type: DataTypes.STRING, 
        allowNull: true,      // ต้องยอมให้เป็น Null ได้ เพราะตอนสมัครสมาชิกอาจจะยังไม่ได้ Token
        defaultValue: null
    }
    // Foreign Keys จะถูกจัดการโดย Associations ด้านล่าง
});

// --- Associations (ความสัมพันธ์) ---

// 1. User อยู่ใน Family (User มี family_id)
Family.hasMany(User, { foreignKey: 'family_id' });
User.belongsTo(Family, { foreignKey: 'family_id' });

// 2. User ผูกกับ Device (User มี device_id)
// กรณีนี้ 1 User อาจมี 1 Device หลัก หรือเรามองว่า Device เป็นแค่ ID ก็ได้
// ตาม Design คุณ User มี device_id (FK) แปลว่า device_id ต้องมีอยู่ในตาราง Devices ก่อน
Device.hasOne(User, { foreignKey: 'device_id' }); 
User.belongsTo(Device, { foreignKey: 'device_id' });

module.exports = { sequelize, User, Family, Device };