const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');

// Import Model ย่อยๆ
const Transaction = require('./Transaction');
const Family = require('./Family');
const Device = require('./Device');
const User = require('./User');


// --- Associations (ความสัมพันธ์) ---
// 1. User <-> Family
Family.hasMany(User, { foreignKey: 'family_id' });
User.belongsTo(Family, { foreignKey: 'family_id' });

// --- User <-> Device ---
Device.hasOne(User, { foreignKey: 'device_id' }); 
User.belongsTo(Device, { foreignKey: 'device_id' });

// --- User <-> Transaction ---
User.hasMany(Transaction, { foreignKey: 'user_id', as: 'transactions' });
Transaction.belongsTo(User, { foreignKey: 'user_id', as: 'user' });

module.exports = { sequelize, User, Family, Device, Transaction };