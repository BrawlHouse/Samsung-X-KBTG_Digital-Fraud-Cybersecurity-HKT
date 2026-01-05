// models/index.js
const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

// Import Model ย่อยๆ
const Transaction = require('./Transaction'); // ต้องมั่นใจว่าไฟล์ Transaction.js มีอยู่จริง
const Family = require('./Family');
const Device = require('./Device');

// นิยาม User
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

    fcm_token: {
        type: DataTypes.STRING,
        allowNull: true,
        defaultValue: null
    }
});

// --- Associations (ความสัมพันธ์) ---

// 1. User <-> Family
Family.hasMany(User, { foreignKey: 'family_id' });
User.belongsTo(Family, { foreignKey: 'family_id' });

// 2. User <-> Device
Device.hasOne(User, { foreignKey: 'device_id' }); 
User.belongsTo(Device, { foreignKey: 'device_id' });

// 3. User <-> Transaction [เพิ่มส่วนนี้]
User.hasMany(Transaction, { foreignKey: 'user_id', as: 'transactions' });
Transaction.belongsTo(User, { foreignKey: 'user_id', as: 'user' });

module.exports = { sequelize, User, Family, Device, Transaction };