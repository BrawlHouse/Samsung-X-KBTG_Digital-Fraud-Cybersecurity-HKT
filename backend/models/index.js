// models/index.js
const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

// Import Model ย่อยๆ
const Family = require('./Family');
const Device = require('./Device');

// นิยาม User ตรงนี้ (หรือจะแยกไฟล์ก็ได้ แต่รวมไว้ที่นี่ตามแผนเดิม)
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
    }
});

// --- Associations (ความสัมพันธ์) ---

// 1. User อยู่ใน Family (User มี family_id)
Family.hasMany(User, { foreignKey: 'family_id' });
User.belongsTo(Family, { foreignKey: 'family_id' });

// 2. User ผูกกับ Device (User มี device_id)
Device.hasOne(User, { foreignKey: 'device_id' }); 
User.belongsTo(Device, { foreignKey: 'device_id' });

// Export ออกไปให้ server.js ใช้
module.exports = { sequelize, User, Family, Device };