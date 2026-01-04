const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const Device = sequelize.define('Device', {
    device_id: {
        type: DataTypes.STRING, // Device ID มักจะเป็น String ยาวๆ
        primaryKey: true,
        allowNull: false
    },
    register_at: {
        type: DataTypes.DATE,
        defaultValue: DataTypes.NOW
    }
}, {
    timestamps: false // เรามี register_at แล้ว ไม่ต้องใช้ default timestamps ก็ได้
});

module.exports = Device;