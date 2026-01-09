// models/Transaction.js
const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const Transaction = sequelize.define('Transaction', {
    transaction_id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    amount: {
        type: DataTypes.FLOAT,
        allowNull: false
    },
    destination: {
        type: DataTypes.STRING,
        allowNull: false
    },
    risk_score: {
        type: DataTypes.FLOAT,
        allowNull: true,
        defaultValue: 0
    },
    status: {
        type: DataTypes.ENUM('normal', 'waiting', 'allow', 'rejected'),
        allowNull: true,
        defaultValue: 'normal'
    },
    // FK: user_id จะถูกสร้างให้อัตโนมัติโดย Sequelize ตอนทำ Association ใน index.js
    // แต่ถ้าอยากเขียนให้ชัดเจน ก็ใส่ไว้ได้ครับ
    user_id: {
        type: DataTypes.INTEGER,
        allowNull: true
    }
});

module.exports = Transaction;