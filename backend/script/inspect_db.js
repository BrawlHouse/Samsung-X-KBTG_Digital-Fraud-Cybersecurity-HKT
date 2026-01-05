// script/inspect_db.js

// 1. โหลด Environment Variables (ถอยหลังไป 1 ชั้นเพื่อหา .env)
require('dotenv').config({ path: '../.env' });

// 2. Import Models (ถอยหลังไป 1 ชั้นเพื่อหา models)
// หมายเหตุ: ถ้าไฟล์ models/index.js ของคุณไม่ได้ export Transaction ออกมา อาจต้องแก้ตรงนี้
const { sequelize, User, Family, Device, Transaction } = require('../models');

async function inspectDatabase() {
    try {
        console.log('🔄 Connecting to Database...');
        await sequelize.authenticate();
        console.log('✅ Connection has been established successfully.\n');

        const models = [
            { name: 'Family', model: Family },
            { name: 'Device', model: Device },
            { name: 'User', model: User },
            { name: 'Transaction', model: Transaction }
        ];

        for (const item of models) {
            console.log(`\n==================================================`);
            console.log(`📊 TABLE: ${item.name}`);
            console.log(`==================================================`);

            if (!item.model) {
                console.log(`⚠️  Model ${item.name} not found or not exported in models/index.js`);
                continue;
            }

            // 1. ดูโครงสร้างตาราง
            const attributes = await item.model.describe();
            console.log('🛠 STRUCTURE (Columns):');
            Object.keys(attributes).forEach(col => {
                const attr = attributes[col];
                let info = ` - ${col}: ${attr.type}`;
                if (attr.primaryKey) info += ' (PK)';
                if (attr.allowNull) info += ' [Nullable]';
                if (attr.defaultValue !== undefined) info += ` [Default: ${attr.defaultValue}]`;
                console.log(info);
            });

            // 2. ดูข้อมูลในตาราง
            console.log('\n📝 DATA (Rows):');
            const rows = await item.model.findAll({
                include: { all: true, nested: true },
                raw: false
            });

            if (rows.length === 0) {
                console.log('   (No data found)');
            } else {
                console.log(JSON.stringify(rows, null, 2));
            }
        }

    } catch (error) {
        console.error('❌ Error:', error);
    } finally {
        await sequelize.close();
        console.log('\n🏁 Inspection finished.');
    }
}

inspectDatabase();