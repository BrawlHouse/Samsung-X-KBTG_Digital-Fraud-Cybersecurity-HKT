require('dotenv').config();
const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const { sequelize } = require('./models');
const userRoutes = require('./routes/userRoutes');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Test Route
app.get('/', (req, res) => {
  res.send({ message: 'Scam Guard Backend is running! 🛡️' });
});

app.use('/users', userRoutes);

sequelize.sync({ force: false }) // force: true จะลบตารางเก่าทิ้งแล้วสร้างใหม่ (ระวังข้อมูลหาย)
    .then(() => {
        console.log('✅ Database connected & tables created!');
        app.listen(PORT, () => {
            console.log(`Server is running on port ${PORT}`);
        });
    })
    .catch((err) => {
        console.error('❌ Unable to connect to the database:', err);
    });

// Start Server
app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});