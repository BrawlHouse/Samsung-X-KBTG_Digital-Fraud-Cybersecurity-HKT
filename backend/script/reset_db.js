require('dotenv').config({ path: '../.env' }); // ถอยหลังไปหา .env
const { sequelize } = require('../models');

async function resetDatabase() {
    try {
        console.log('🔄 Starting Database Reset...');
        
        // คำสั่งนี้จะลบตารางเก่าทิ้งทั้งหมด แล้วสร้างใหม่ตาม Model ล่าสุด
        await sequelize.sync({ force: true }); 
        
        console.log('✅ Database Sync Complete! (All tables dropped and recreated)');
        console.log('✨ New columns (email, password) should be there now.');

    } catch (error) {
        console.error('❌ Error syncing database:', error);
    } finally {
        await sequelize.close();
    }
}

resetDatabase();