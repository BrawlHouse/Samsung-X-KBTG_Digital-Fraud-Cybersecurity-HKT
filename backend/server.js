require('dotenv').config();

const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const { sequelize } = require('./models');
const swaggerUi = require('swagger-ui-express');
const swaggerSpecs = require('./config/swagger');

const userRoutes = require('./routes/userRoutes');
const familyRoutes = require('./routes/familyRoutes');
const riskRoutes = require('./routes/riskRoutes');

const app = express();
const PORT = process.env.PORT || 3000;

// Swagger
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpecs));

// Middleware
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Test Route
app.get('/', (req, res) => {
  res.send({ message: 'Scam Guard Backend is running!' });
});

// Routes
app.use('/users', userRoutes);
app.use('/family', familyRoutes);
app.use('/risk', riskRoutes);

// ===== DB RETRY LOGIC =====
async function startServer() {
  let connected = false;

  while (!connected) {
    try {
      await sequelize.authenticate();
      console.log('✅ Database connected');

      await sequelize.sync({ force: false });
      console.log('✅ Tables synced');

      connected = true;
    } catch (err) {
      console.log('⏳ DB not ready, retrying in 5s...');
      await new Promise(res => setTimeout(res, 5000));
    }
  }

  app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 Server running on port ${PORT}`);
  });
}

startServer();
