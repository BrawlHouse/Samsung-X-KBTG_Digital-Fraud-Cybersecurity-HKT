// models/User.js
const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

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
    },
    email: {
        type: DataTypes.STRING,
        allowNull: false,
        unique: true,
        validate: {
            isEmail: true
        }
    },
    password: {
        type: DataTypes.STRING,
        allowNull: false
    },
    device_id: {
        type: DataTypes.STRING,
        allowNull: true 
    },
    bank_account_number: {
        type: DataTypes.STRING,
        allowNull: true
    },
    family_id: {
        type: DataTypes.INTEGER,
        allowNull: true
    },
    status: {
        type: DataTypes.ENUM('allow', 'reject', 'waiting', 'normal'),
        allowNull: false,
        defaultValue: 'normal',
        validate: {
            // Validator: เช็กระดับ Database ว่าถ้าเป็น child ห้ามเป็นสถานะอื่น
            checkChildStatus(value) {
                if (this.role === 'child' && value !== 'normal') {
                    throw new Error("User with role 'child' must always have 'normal' status.");
                }
            }
        }
    }
});

module.exports = User;