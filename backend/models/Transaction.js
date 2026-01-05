const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');
const { User } = require('./User'); // Import User เพื่อทำ Association

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
        type: DataTypes.STRING, // เลขบัญชีปลายทาง หรือชื่อร้านค้า
        allowNull: false
    },
    risk_score: {
        type: DataTypes.FLOAT, // คะแนนความเสี่ยงจาก AI (0-100)
        defaultValue: 0
    },
    status: {
        // pending_approval = รอพ่อแม่กด, approved = อนุญาต, rejected = ระงับ, normal = ปกติไม่ต้องตรวจ
        type: DataTypes.ENUM('normal', 'pending_approval', 'approved', 'rejected'),
        defaultValue: 'normal'
    }
    // user_id (FK) จะถูกสร้างอัตโนมัติตอนทำ Association
});

// เชื่อมโยงว่า Transaction นี้เป็นของ User (ลูก) คนไหน
Transaction.belongsTo(User, { foreignKey: 'user_id', as: 'child' });
User.hasMany(Transaction, { foreignKey: 'user_id' });

module.exports = Transaction;