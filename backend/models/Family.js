const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const Family = sequelize.define('Family', {
    family_id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    invite_code: {
        type: DataTypes.STRING,
        allowNull: false,
        unique: true // ห้ามซ้ำกันทั้งระบบ
    }
    // created_at จะถูกสร้างให้เองโดย default ของ sequelize (updatedAt, createdAt)
});

module.exports = Family;