const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

const Family = sequelize.define('Family', {
    family_id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    }
    // created_at จะถูกสร้างให้เองโดย default ของ sequelize (updatedAt, createdAt)
});

module.exports = Family;